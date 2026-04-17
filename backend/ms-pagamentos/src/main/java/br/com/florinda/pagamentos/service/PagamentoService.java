package br.com.florinda.pagamentos.service;

import br.com.florinda.pagamentos.domain.MetodoPagamento;
import br.com.florinda.pagamentos.domain.Pagamento;
import br.com.florinda.pagamentos.domain.StatusPagamento;
import br.com.florinda.pagamentos.dto.PagamentoDTO;
import br.com.florinda.pagamentos.event.PagamentoEvents;
import br.com.florinda.pagamentos.infra.GatewayPagamentoSimulado;
import br.com.florinda.pagamentos.mapper.PagamentoMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PagamentoService {

    private static final Logger LOG = Logger.getLogger(PagamentoService.class);

    @Inject PagamentoMapper mapper;
    @Inject ObjectMapper objectMapper;
    @Inject GatewayPagamentoSimulado gateway;

    @Inject
    @Channel("payment-approved-out")
    Emitter<String> approvedEmitter;

    @Inject
    @Channel("payment-failed-out")
    Emitter<String> failedEmitter;

    // -----------------------------------------------------------
    // Leitura
    // -----------------------------------------------------------

    public List<PagamentoDTO.PagamentoResumoResponse> listarTodos() {
        return mapper.toResumoResponseList(Pagamento.listarTodos());
    }

    public List<PagamentoDTO.PagamentoResumoResponse> listarPorStatus(StatusPagamento status) {
        return mapper.toResumoResponseList(Pagamento.findByStatus(status));
    }

    public List<PagamentoDTO.PagamentoResumoResponse> listarPorCliente(UUID clienteId) {
        return mapper.toResumoResponseList(Pagamento.findByClienteId(clienteId));
    }

    public PagamentoDTO.PagamentoResponse buscarPorId(UUID id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    public PagamentoDTO.PagamentoResponse buscarPorPedido(UUID pedidoId) {
        Pagamento p = Pagamento.findByPedidoId(pedidoId)
                .orElseThrow(() -> new NotFoundException(
                        "Pagamento não encontrado para o pedido: " + pedidoId));
        return mapper.toResponse(p);
    }

    // -----------------------------------------------------------
    // RN06 — Idempotência: rejeita pedidoId duplicado
    // RN07 — 3 tentativas via gateway, timeout 5s
    // -----------------------------------------------------------
    @Transactional
    public PagamentoDTO.PagamentoResponse processar(PagamentoDTO.ProcessarPagamentoRequest request) {

        // RN06 — idempotência
        if (Pagamento.existePagamentoPara(request.pedidoId())) {
            LOG.warnf("Pagamento duplicado bloqueado para pedido: %s", request.pedidoId());
            throw new IllegalStateException(
                "Já existe um pagamento registrado para o pedido: " + request.pedidoId());
        }

        Pagamento pagamento = mapper.toEntity(request);
        pagamento.persist();
        pagamento.iniciarProcessamento();

        LOG.infof("Processando pagamento %s para pedido %s via %s",
                  pagamento.id, pagamento.pedidoId, pagamento.metodo);

        // RN07 — chama gateway com Circuit Breaker e Retry
        GatewayPagamentoSimulado.GatewayResponse gatewayResp =
                gateway.processar(pagamento.pedidoId, pagamento.valor, pagamento.metodo.name());

        if (gatewayResp.aprovado()) {
            pagamento.aprovar(gatewayResp.gatewayId(), gatewayResp.payload());
            publicarAprovado(pagamento);
            LOG.infof("Pagamento APROVADO: %s | gateway: %s", pagamento.id, gatewayResp.gatewayId());
        } else {
            pagamento.rejeitar(gatewayResp.payload());
            publicarFalhou(pagamento, "Gateway rejeitou o pagamento");
            LOG.warnf("Pagamento REJEITADO: %s", pagamento.id);
        }

        return mapper.toResponse(pagamento);
    }

    // -----------------------------------------------------------
    // RN08 — Estorno: só para status APROVADO
    // -----------------------------------------------------------
    @Transactional
    public PagamentoDTO.EstornoResponse estornar(UUID id, String motivo) {
        Pagamento pagamento = buscarEntidade(id);
        var estorno = pagamento.solicitarEstorno(motivo);
        estorno.persist();

        LOG.infof("Estorno solicitado para pagamento %s | motivo: %s", id, motivo);
        return mapper.toEstornoResponse(estorno);
    }

    // -----------------------------------------------------------
    // Chamado pelo KafkaConsumerService (order.created)
    // -----------------------------------------------------------
    @Transactional
    public void processarViaEvento(PagamentoEvents.OrderCreatedEvent event) {
        if (Pagamento.existePagamentoPara(event.pedidoId())) {
            LOG.warnf("Evento order.created ignorado — pagamento já existe: %s", event.pedidoId());
            return;
        }

        var request = new PagamentoDTO.ProcessarPagamentoRequest(
                event.pedidoId(),
                event.clienteId(),
                event.valorTotal(),
                MetodoPagamento.PIX   // padrão; em produção vem do payload do pedido
        );
        processar(request);
    }

    // -----------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------

    private void publicarAprovado(Pagamento pagamento) {
        try {
            var event = new PagamentoEvents.PaymentApprovedEvent(
                    pagamento.pedidoId, pagamento.id,
                    pagamento.valor, OffsetDateTime.now());
            approvedEmitter.send(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            LOG.errorf("Falha ao publicar PaymentApprovedEvent: %s", e.getMessage());
        }
    }

    private void publicarFalhou(Pagamento pagamento, String motivo) {
        try {
            var event = new PagamentoEvents.PaymentFailedEvent(
                    pagamento.pedidoId, pagamento.id,
                    motivo, pagamento.tentativas, OffsetDateTime.now());
            failedEmitter.send(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            LOG.errorf("Falha ao publicar PaymentFailedEvent: %s", e.getMessage());
        }
    }

    private Pagamento buscarEntidade(UUID id) {
        Pagamento p = Pagamento.findById(id);
        if (p == null) throw new NotFoundException("Pagamento não encontrado: " + id);
        return p;
    }
}
