package br.com.florinda.ia.infra.rag;

import br.com.florinda.ia.dto.IaDTO;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Pipeline de ingestão RAG:
 * Texto → Chunking → Embedding (nomic-embed-text) → PgVector
 *
 * Usado em:
 * - Seed inicial de FAQ (startup)
 * - Atualização automática via evento Kafka catalog.item.updated
 * - Ingestão manual via endpoint admin
 */
@ApplicationScoped
public class RagIngestaoService {

    private static final Logger LOG = Logger.getLogger(RagIngestaoService.class);

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    /**
     * Ingere um único documento — chunking + embedding + store.
     */
    public void ingerirDocumento(String conteudo, String fonte, String fonteId) {
        try {
            Document doc = Document.from(conteudo,
                    Metadata.from("fonte", fonte)
                            .add("fonte_id", fonteId != null ? fonteId : ""));

            // Chunking: máximo 500 chars por chunk, 50 de overlap
            DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
            List<TextSegment> segmentos = splitter.split(doc);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .documentSplitter(splitter)
                    .build();

            ingestor.ingest(doc);

            LOG.infof("Ingerido: fonte=%s fonteId=%s segmentos=%d",
                      fonte, fonteId, segmentos.size());

        } catch (Exception e) {
            LOG.errorf("Falha ao ingerir documento [%s/%s]: %s",
                       fonte, fonteId, e.getMessage());
            throw new RuntimeException("Falha na ingestão: " + e.getMessage(), e);
        }
    }

    /**
     * Ingere lista de documentos — usado no seed de FAQ.
     */
    public int ingerirLote(List<IaDTO.IngerirDocumentoRequest> documentos) {
        int sucesso = 0;
        for (IaDTO.IngerirDocumentoRequest doc : documentos) {
            try {
                ingerirDocumento(doc.conteudo(), doc.fonte(), doc.fonteId());
                sucesso++;
            } catch (Exception e) {
                LOG.warnf("Falha ao ingerir item do lote: %s", e.getMessage());
            }
        }
        LOG.infof("Lote ingerido: %d/%d documentos com sucesso", sucesso, documentos.size());
        return sucesso;
    }

    /**
     * Atualiza o embedding de um item do catálogo.
     * Chamado pelo consumer Kafka ao receber catalog.item.updated.
     */
    public void atualizarItemCatalogo(IaDTO.CatalogItemUpdatedEvent event) {
        if ("REMOVIDO".equals(event.acao())) {
            LOG.infof("Item removido do catálogo — sem reindexação: %s", event.itemId());
            return;
        }

        String conteudo = "Item do cardápio: %s. Descrição: %s. Preço: R$ %.2f. %s"
                .formatted(
                    event.nome(),
                    event.descricao() != null ? event.descricao() : "sem descrição",
                    event.preco(),
                    event.disponivel() ? "Disponível." : "Indisponível no momento."
                );

        ingerirDocumento(conteudo, "cardapio", event.itemId());
        LOG.infof("Item do catálogo reindexado no RAG: %s", event.itemId());
    }
}
