package br.com.florinda.pedidos.event;

import br.com.florinda.pedidos.domain.StatusPedido;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PedidoEvents {

    // -----------------------------------------------------------
    // Publicado em: order.created
    // Consumidores: ms-pagamentos, ms-notificacoes
    // -----------------------------------------------------------
    public record OrderCreatedEvent(
            UUID pedidoId,
            UUID clienteId,
            UUID restauranteId,
            BigDecimal valorTotal,
            List<ItemEvent> itens,
            OffsetDateTime criadoEm
    ) {
        public record ItemEvent(
                UUID itemId,
                String nomeItem,
                BigDecimal precoUnitario,
                int quantidade
        ) {}
    }

    // -----------------------------------------------------------
    // Publicado em: order.status.updated
    // Consumidores: ms-ia-suporte, ms-notificacoes
    // -----------------------------------------------------------
    public record OrderStatusUpdatedEvent(
            UUID pedidoId,
            UUID clienteId,
            StatusPedido statusAnterior,
            StatusPedido statusAtual,
            String motivo,
            OffsetDateTime atualizadoEm
    ) {}

    // -----------------------------------------------------------
    // Consumido de: payment.approved (publicado pelo ms-pagamentos)
    // -----------------------------------------------------------
    public record PaymentApprovedEvent(
            UUID pedidoId,
            UUID pagamentoId,
            BigDecimal valor,
            OffsetDateTime aprovadoEm
    ) {}

    // -----------------------------------------------------------
    // Consumido de: payment.failed
    // -----------------------------------------------------------
    public record PaymentFailedEvent(
            UUID pedidoId,
            String motivo,
            OffsetDateTime falhadoEm
    ) {}
}
