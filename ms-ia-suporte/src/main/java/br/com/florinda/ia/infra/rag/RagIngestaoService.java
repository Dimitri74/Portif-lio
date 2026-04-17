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
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline de ingestão RAG:
 * Texto → Chunking → Embedding (nomic-embed-text) → PgVector
 *
 * No startup, processa automaticamente todos os registros do seed SQL
 * que têm embedding = NULL, gerando os vetores via Ollama e persistindo
 * via JDBC direto (preservando os IDs originais do seed).
 */
@Startup
@ApplicationScoped
public class RagIngestaoService {

    private static final Logger LOG = Logger.getLogger(RagIngestaoService.class);

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    DataSource dataSource;

    // Executa no boot em thread virtual para não bloquear a inicialização
    @PostConstruct
    void agendarProcessamentoInicial() {
        Thread.ofVirtual()
              .name("rag-seed-startup")
              .start(this::processarEmbeddingsPendentes);
    }

    /**
     * Lê todos os registros com embedding = NULL, gera os vetores via
     * nomic-embed-text (Ollama) e persiste com UPDATE JDBC preservando
     * os IDs do seed SQL.
     *
     * @return número de embeddings gerados com sucesso
     */
    public int processarEmbeddingsPendentes() {
        List<String[]> pendentes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id::text, conteudo FROM knowledge_embeddings " +
                     "WHERE embedding IS NULL ORDER BY criado_em")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pendentes.add(new String[]{ rs.getString("id"), rs.getString("conteudo") });
                }
            }
        } catch (Exception e) {
            LOG.errorf("Falha ao consultar embeddings pendentes: %s", e.getMessage());
            return 0;
        }

        if (pendentes.isEmpty()) {
            LOG.info("RAG seed: todos os embeddings ja foram gerados.");
            return 0;
        }

        LOG.infof("RAG seed: gerando embeddings para %d registros pendentes...", pendentes.size());
        int processados = 0;

        for (String[] row : pendentes) {
            String id      = row[0];
            String conteudo = row[1];
            try {
                Embedding embedding = embeddingModel.embed(conteudo).content();
                String vectorStr    = toVectorString(embedding.vector());

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement upd = conn.prepareStatement(
                             "UPDATE knowledge_embeddings " +
                             "SET embedding = ?::vector, embedding_id = id, text = conteudo, atualizado_em = NOW() " +
                             "WHERE id = ?::uuid")) {
                    upd.setString(1, vectorStr);
                    upd.setString(2, id);
                    upd.executeUpdate();
                }
                processados++;
                LOG.infof("Embedding gerado: %s (%d/%d)", id, processados, pendentes.size());
            } catch (Exception e) {
                LOG.warnf("Falha ao gerar embedding para [%s]: %s", id, e.getMessage());
            }
        }

        LOG.infof("RAG seed concluido: %d/%d embeddings gerados.", processados, pendentes.size());
        return processados;
    }

    // Converte float[] para o formato pgvector: [0.1,0.2,...]
    private String toVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

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
