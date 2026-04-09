package br.com.florinda.pagamentos.service;

import br.com.florinda.pagamentos.event.PagamentoEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class KafkaConsumerService {

    private static final Logger LOG = Logger.getLogger(KafkaConsumerService.class);

    @Inject PagamentoService pagamentoService;
    @Inject ObjectMapper objectMapper;

    @Incoming("order-in")
    public void onOrderCreated(String payload) {
        try {
            PagamentoEvents.OrderCreatedEvent event =
                    objectMapper.readValue(payload, PagamentoEvents.OrderCreatedEvent.class);
            LOG.infof("Evento order.created recebido: pedido=%s valor=R$%.2f",
                      event.pedidoId(), event.valorTotal());
            pagamentoService.processarViaEvento(event);
        } catch (Exception e) {
            LOG.errorf("Erro ao processar order.created: %s | payload: %s",
                       e.getMessage(), payload);
        }
    }
}
