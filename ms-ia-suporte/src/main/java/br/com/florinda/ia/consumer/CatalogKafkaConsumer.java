package br.com.florinda.ia.consumer;

import br.com.florinda.ia.dto.IaDTO;
import br.com.florinda.ia.infra.rag.RagIngestaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CatalogKafkaConsumer {

    private static final Logger LOG = Logger.getLogger(CatalogKafkaConsumer.class);

    @Inject RagIngestaoService ingestaoService;
    @Inject ObjectMapper objectMapper;

    /**
     * Reindexação automática do RAG quando o catálogo é atualizado.
     * Garante que o agente sempre conhece o cardápio mais recente.
     */
    @Incoming("catalog-updated-in")
    public void onCatalogItemUpdated(String payload) {
        try {
            IaDTO.CatalogItemUpdatedEvent event =
                    objectMapper.readValue(payload, IaDTO.CatalogItemUpdatedEvent.class);

            LOG.infof("Evento catalog.item.updated recebido: item=%s acao=%s",
                      event.itemId(), event.acao());

            ingestaoService.atualizarItemCatalogo(event);

        } catch (Exception e) {
            LOG.errorf("Falha ao processar catalog.item.updated: %s | payload: %s",
                       e.getMessage(), payload);
        }
    }

    /**
     * Monitora atualizações de status para enriquecer o contexto do agente.
     * Fase futura: atualizar base de conhecimento com histórico de pedidos.
     */
    @Incoming("order-status-ia-in")
    public void onOrderStatusUpdated(String payload) {
        try {
            IaDTO.OrderStatusUpdatedEvent event =
                    objectMapper.readValue(payload, IaDTO.OrderStatusUpdatedEvent.class);

            LOG.debugf("Status de pedido recebido pelo agente: pedido=%s status=%s",
                       event.pedidoId(), event.statusAtual());

            // Fase futura: enriquecer RAG com histórico de pedidos do cliente

        } catch (Exception e) {
            LOG.warnf("Falha ao processar order.status.updated no agente: %s", e.getMessage());
        }
    }
}
