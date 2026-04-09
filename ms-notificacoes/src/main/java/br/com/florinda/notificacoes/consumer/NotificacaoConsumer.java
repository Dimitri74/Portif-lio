package br.com.florinda.notificacoes.consumer;

import br.com.florinda.notificacoes.dto.EventosDTO;
import br.com.florinda.notificacoes.service.NotificacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class NotificacaoConsumer {

    private static final Logger LOG = Logger.getLogger(NotificacaoConsumer.class);

    @Inject NotificacaoService service;
    @Inject ObjectMapper objectMapper;

    // -----------------------------------------------------------
    // order.created — pedido recém criado pelo cliente
    // -----------------------------------------------------------
    @Incoming("order-created-in")
    public void onOrderCreated(String payload) {
        processar(payload, EventosDTO.OrderCreatedEvent.class,
                  event -> service.notificarPedidoCriado(event),
                  "order.created");
    }

    // -----------------------------------------------------------
    // order.status.updated — cada transição de status do pedido
    // -----------------------------------------------------------
    @Incoming("order-status-in")
    public void onOrderStatusUpdated(String payload) {
        processar(payload, EventosDTO.OrderStatusUpdatedEvent.class,
                  event -> service.notificarStatusAtualizado(event),
                  "order.status.updated");
    }

    // -----------------------------------------------------------
    // payment.approved — pagamento aprovado pelo gateway
    // -----------------------------------------------------------
    @Incoming("payment-approved-in")
    public void onPaymentApproved(String payload) {
        processar(payload, EventosDTO.PaymentApprovedEvent.class,
                  event -> service.notificarPagamentoAprovado(event),
                  "payment.approved");
    }

    // -----------------------------------------------------------
    // payment.failed — pagamento rejeitado após tentativas
    // -----------------------------------------------------------
    @Incoming("payment-failed-in")
    public void onPaymentFailed(String payload) {
        processar(payload, EventosDTO.PaymentFailedEvent.class,
                  event -> service.notificarPagamentoFalhou(event),
                  "payment.failed");
    }

    // -----------------------------------------------------------
    // Processador genérico com tratamento de erro (Dead Letter)
    // -----------------------------------------------------------
    private <T> void processar(String payload, Class<T> tipo,
                                java.util.function.Consumer<T> handler,
                                String topico) {
        try {
            T event = objectMapper.readValue(payload, tipo);
            handler.accept(event);
        } catch (Exception e) {
            // Fase 2: loga e descarta.
            // Fase futura: publicar em tópico DLQ (dead letter queue)
            // ex: notificacoes.dlq com o payload original + motivo da falha
            LOG.errorf("Falha ao processar evento do tópico [%s]: %s | payload: %s",
                       topico, e.getMessage(), payload);
        }
    }
}
