package br.com.florinda.pagamentos.event;

import br.com.florinda.pagamentos.domain.MetodoPagamento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PagamentoEvents {

    // -----------------------------------------------------------
    // Consumido de: order.created (publicado pelo ms-pedidos)
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
    // Publicado em: payment.approved
    // Consumidores: ms-pedidos, ms-notificacoes
    // -----------------------------------------------------------
    public record PaymentApprovedEvent(
            UUID pedidoId,
            UUID pagamentoId,
            BigDecimal valor,
            OffsetDateTime aprovadoEm
    ) {}

    // -----------------------------------------------------------
    // Publicado em: payment.failed
    // Consumidores: ms-pedidos, ms-notificacoes
    // -----------------------------------------------------------
    public record PaymentFailedEvent(
            UUID pedidoId,
            UUID pagamentoId,
            String motivo,
            int tentativas,
            OffsetDateTime falhadoEm
    ) {}
}
