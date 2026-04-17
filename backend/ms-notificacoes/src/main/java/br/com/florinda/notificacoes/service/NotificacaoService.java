package br.com.florinda.notificacoes.service;

import br.com.florinda.notificacoes.domain.Notificacao;
import br.com.florinda.notificacoes.dto.EventosDTO;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.UUID;

@ApplicationScoped
public class NotificacaoService {

    private static final Logger LOG = Logger.getLogger(NotificacaoService.class);

    // -----------------------------------------------------------
    // Fase 2: notificações via LOG estruturado.
    // Fase futura: injetar canal real (push, e-mail, SMS).
    // -----------------------------------------------------------

    public void notificarPedidoCriado(EventosDTO.OrderCreatedEvent event) {
        var n = Notificacao.Registro.criar(
                event.clienteId(),
                event.pedidoId(),
                Notificacao.Tipo.PEDIDO_CRIADO,
                "Seu pedido foi recebido! Total: R$ %.2f".formatted(event.valorTotal())
        );
        despachar(n);
    }

    public void notificarStatusAtualizado(EventosDTO.OrderStatusUpdatedEvent event) {
        Notificacao.Tipo tipo = resolverTipo(event.statusAtual());
        String mensagem = gerarMensagemStatus(event.statusAtual(), event.motivo());

        var n = Notificacao.Registro.criar(
                event.clienteId(),
                event.pedidoId(),
                tipo,
                mensagem
        );
        despachar(n);
    }

    public void notificarPagamentoAprovado(EventosDTO.PaymentApprovedEvent event) {
        // clienteId não vem no evento de pagamento — usa pedidoId como referência
        var n = Notificacao.Registro.criar(
                null,
                event.pedidoId(),
                Notificacao.Tipo.PAGAMENTO_APROVADO,
                "Pagamento de R$ %.2f aprovado para o pedido %s"
                    .formatted(event.valor(), event.pedidoId())
        );
        despachar(n);
    }

    public void notificarPagamentoFalhou(EventosDTO.PaymentFailedEvent event) {
        var n = Notificacao.Registro.criar(
                null,
                event.pedidoId(),
                Notificacao.Tipo.PAGAMENTO_FALHOU,
                "Falha no pagamento do pedido %s após %d tentativa(s). Motivo: %s"
                    .formatted(event.pedidoId(), event.tentativas(), event.motivo())
        );
        despachar(n);
    }

    // -----------------------------------------------------------
    // Despachador central — ponto único para adicionar canais
    // -----------------------------------------------------------
    private void despachar(Notificacao.Registro notificacao) {
        // Fase 2: apenas log estruturado
        LOG.infof("[NOTIFICACAO] tipo=%s pedido=%s canal=%s mensagem='%s'",
                  notificacao.tipo(),
                  notificacao.pedidoId(),
                  notificacao.canal(),
                  notificacao.mensagem());

        // Fase futura: switch por canal
        // switch (notificacao.canal()) {
        //     case PUSH  -> pushService.enviar(notificacao);
        //     case EMAIL -> emailService.enviar(notificacao);
        //     case SMS   -> smsService.enviar(notificacao);
        // }
    }

    private Notificacao.Tipo resolverTipo(String status) {
        return switch (status) {
            case "CONFIRMADO"        -> Notificacao.Tipo.PEDIDO_CONFIRMADO;
            case "PREPARANDO"        -> Notificacao.Tipo.PEDIDO_PREPARANDO;
            case "SAIU_PARA_ENTREGA" -> Notificacao.Tipo.PEDIDO_SAIU_PARA_ENTREGA;
            case "ENTREGUE"          -> Notificacao.Tipo.PEDIDO_ENTREGUE;
            case "CANCELADO"         -> Notificacao.Tipo.PEDIDO_CANCELADO;
            default                  -> Notificacao.Tipo.PEDIDO_CRIADO;
        };
    }

    private String gerarMensagemStatus(String status, String motivo) {
        return switch (status) {
            case "CONFIRMADO"        -> "Seu pedido foi confirmado e vai para o restaurante!";
            case "PREPARANDO"        -> "O restaurante está preparando seu pedido.";
            case "SAIU_PARA_ENTREGA" -> "Seu pedido saiu para entrega!";
            case "ENTREGUE"          -> "Pedido entregue. Bom apetite!";
            case "CANCELADO"         -> "Pedido cancelado. Motivo: " +
                                        (motivo != null ? motivo : "não informado");
            default                  -> "Status atualizado para " + status;
        };
    }
}
