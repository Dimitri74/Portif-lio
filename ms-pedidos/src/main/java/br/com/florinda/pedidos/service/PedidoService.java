package br.com.florinda.pedidos.service;

import br.com.florinda.pedidos.domain.ItemPedido;
import br.com.florinda.pedidos.domain.Pedido;
import br.com.florinda.pedidos.domain.StatusPedido;
import br.com.florinda.pedidos.dto.PedidoDTO;
import br.com.florinda.pedidos.event.PedidoEvents;
import br.com.florinda.pedidos.infra.CatalogoClient;
import br.com.florinda.pedidos.mapper.PedidoMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PedidoService {

    private static final Logger LOG = Logger.getLogger(PedidoService.class);

    @Inject PedidoMapper mapper;
    @Inject ObjectMapper objectMapper;

    @Inject
    @RestClient
    CatalogoClient catalogoClient;

    @Inject
    @Channel("order-out")
    Emitter<String> orderCreatedEmitter;

    @Inject
    @Channel("order-status-out")
    Emitter<String> orderStatusEmitter;

    // -----------------------------------------------------------
    // Leitura
    // -----------------------------------------------------------

    public List<PedidoDTO.PedidoResumoResponse> listarPorCliente(UUID clienteId) {
        return mapper.toResumoResponseList(Pedido.findByCliente(clienteId));
    }

    public List<PedidoDTO.PedidoResumoResponse> listarPorStatus(StatusPedido status) {
        return mapper.toResumoResponseList(Pedido.findByStatus(status));
    }

    public List<PedidoDTO.PedidoResumoResponse> listarTodos() {
        return mapper.toResumoResponseList(Pedido.listAll());
    }

    public PedidoDTO.PedidoResponse buscarPorId(UUID id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    // -----------------------------------------------------------
    // RN01 — Restaurante deve estar ABERTO
    // RN04 — Pedido mínimo R$ 15,00 (validado na entidade)
    // RN05 — Emite evento Kafka ao criar
    // -----------------------------------------------------------
    @Transactional
    public PedidoDTO.PedidoResponse criar(PedidoDTO.CriarPedidoRequest request) {

        // RN01 — valida restaurante aberto via REST Client
        try {
            CatalogoClient.RestauranteStatusResponse restaurante =
                    catalogoClient.buscarRestaurante(request.restauranteId());
            if (!restaurante.estaAberto()) {
                throw new IllegalStateException(
                    "Restaurante " + request.restauranteId() + " está fechado ou suspenso.");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            LOG.warnf("Falha ao validar restaurante no catálogo: %s. Prosseguindo.", e.getMessage());
        }

        Pedido pedido = mapper.toEntity(request);

        // Mapeia itens e vincula ao pedido
        request.itens().forEach(itemReq -> {
            ItemPedido item = mapper.toItemEntity(itemReq);
            item.pedido = pedido;
            item.calcularSubtotal();
            pedido.itens.add(item);
        });

        pedido.calcularTotal();
        pedido.persist();

        LOG.infof("Pedido criado: %s | cliente: %s | total: R$ %.2f",
                  pedido.id, pedido.clienteId, pedido.valorTotal);

        // RN05 — publica evento order.created
        publicarOrderCreated(pedido);

        return mapper.toResponse(pedido);
    }

    // -----------------------------------------------------------
    // RN03 — Cancelamento apenas em PENDENTE ou CONFIRMADO
    // -----------------------------------------------------------
    @Transactional
    public PedidoDTO.PedidoResponse cancelar(UUID id, String motivo) {
        Pedido pedido = buscarEntidade(id);
        StatusPedido statusAnterior = pedido.status;
        pedido.cancelar(motivo);

        publicarStatusAtualizado(pedido, statusAnterior);
        LOG.infof("Pedido cancelado: %s | motivo: %s", id, motivo);
        return mapper.toResponse(pedido);
    }

    // -----------------------------------------------------------
    // Chamado pelo consumer Kafka (payment.approved)
    // -----------------------------------------------------------
    @Transactional
    public void confirmarPorPagamento(PedidoEvents.PaymentApprovedEvent event) {
        Pedido pedido = buscarEntidade(event.pedidoId());
        StatusPedido anterior = pedido.status;
        pedido.confirmar();
        publicarStatusAtualizado(pedido, anterior);
        LOG.infof("Pedido %s confirmado via pagamento %s", event.pedidoId(), event.pagamentoId());
    }

    // -----------------------------------------------------------
    // Chamado pelo consumer Kafka (payment.failed)
    // -----------------------------------------------------------
    @Transactional
    public void falharPorPagamento(PedidoEvents.PaymentFailedEvent event) {
        Pedido pedido = buscarEntidade(event.pedidoId());
        StatusPedido anterior = pedido.status;
        pedido.falharPagamento();
        publicarStatusAtualizado(pedido, anterior);
        LOG.warnf("Pedido %s cancelado por falha no pagamento: %s", event.pedidoId(), event.motivo());
    }

    // -----------------------------------------------------------
    // Helpers de publicação de eventos
    // -----------------------------------------------------------

    private void publicarOrderCreated(Pedido pedido) {
        try {
            var itensEvent = pedido.itens.stream()
                    .map(i -> new PedidoEvents.OrderCreatedEvent.ItemEvent(
                            i.itemId, i.nomeItem, i.precoUnitario, i.quantidade))
                    .toList();

            var event = new PedidoEvents.OrderCreatedEvent(
                    pedido.id, pedido.clienteId, pedido.restauranteId,
                    pedido.valorTotal, itensEvent, pedido.criadoEm);

            orderCreatedEmitter.send(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            LOG.errorf("Falha ao serializar OrderCreatedEvent: %s", e.getMessage());
        }
    }

    private void publicarStatusAtualizado(Pedido pedido, StatusPedido statusAnterior) {
        try {
            var event = new PedidoEvents.OrderStatusUpdatedEvent(
                    pedido.id, pedido.clienteId,
                    statusAnterior, pedido.status,
                    null, OffsetDateTime.now());

            orderStatusEmitter.send(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            LOG.errorf("Falha ao serializar OrderStatusUpdatedEvent: %s", e.getMessage());
        }
    }

    private Pedido buscarEntidade(UUID id) {
        Pedido pedido = Pedido.findById(id);
        if (pedido == null) throw new NotFoundException("Pedido não encontrado: " + id);
        return pedido;
    }
}
