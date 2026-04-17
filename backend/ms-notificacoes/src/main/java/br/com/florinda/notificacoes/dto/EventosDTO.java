package br.com.florinda.notificacoes.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTOs que espelham os eventos publicados pelos outros microsserviços.
 * Mantidos aqui para desacoplamento — ms-notificacoes não depende
 * dos módulos de pedidos ou pagamentos.
 */
public class EventosDTO {

    // -----------------------------------------------------------
    // Consumido de: order.created
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
    // Consumido de: order.status.updated
    // -----------------------------------------------------------
    public record OrderStatusUpdatedEvent(
            UUID pedidoId,
            UUID clienteId,
            String statusAnterior,
            String statusAtual,
            String motivo,
            OffsetDateTime atualizadoEm
    ) {}

    // -----------------------------------------------------------
    // Consumido de: payment.approved
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
            UUID pagamentoId,
            String motivo,
            int tentativas,
            OffsetDateTime falhadoEm
    ) {}
}
