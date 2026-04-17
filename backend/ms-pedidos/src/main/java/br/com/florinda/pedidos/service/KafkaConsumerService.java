package br.com.florinda.pedidos.service;

import br.com.florinda.pedidos.event.PedidoEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class KafkaConsumerService {

    private static final Logger LOG = Logger.getLogger(KafkaConsumerService.class);

    @Inject PedidoService pedidoService;
    @Inject ObjectMapper objectMapper;

    @Incoming("payment-in")
    public void onPaymentApproved(String payload) {
        try {
            PedidoEvents.PaymentApprovedEvent event =
                    objectMapper.readValue(payload, PedidoEvents.PaymentApprovedEvent.class);
            LOG.infof("Evento payment.approved recebido: pedido %s", event.pedidoId());
            pedidoService.confirmarPorPagamento(event);
        } catch (Exception e) {
            LOG.errorf("Erro ao processar payment.approved: %s | payload: %s",
                       e.getMessage(), payload);
        }
    }

    @Incoming("payment-failed-in")
    public void onPaymentFailed(String payload) {
        try {
            PedidoEvents.PaymentFailedEvent event =
                    objectMapper.readValue(payload, PedidoEvents.PaymentFailedEvent.class);
            LOG.warnf("Evento payment.failed recebido: pedido %s | motivo: %s",
                      event.pedidoId(), event.motivo());
            pedidoService.falharPorPagamento(event);
        } catch (Exception e) {
            LOG.errorf("Erro ao processar payment.failed: %s | payload: %s",
                       e.getMessage(), payload);
        }
    }
}
