package br.com.florinda.ia.service;

import br.com.florinda.ia.dto.IaDTO;
import br.com.florinda.ia.infra.SessaoMemoriaService;
import br.com.florinda.ia.infra.guardrail.InputGuardrail;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquestrador do agente IA.
 *
 * Fluxo por pergunta:
 * 1. Guardrail valida entrada
 * 2. Recupera histórico de sessão do Redis
 * 3. Busca semântica no PgVector (RAG)
 * 4. Monta prompt enriquecido com contexto + histórico
 * 5. Envia ao LLM (Ollama llama3.2)
 * 6. Sanitiza saída
 * 7. Salva troca no Redis
 * 8. Retorna resposta com fontes
 */
@ApplicationScoped
public class AgenteService {

    private static final Logger LOG = Logger.getLogger(AgenteService.class);

    @Inject InputGuardrail guardrail;
    @Inject SessaoMemoriaService sessaoService;
    @Inject FlorindaAiService aiService;
    @Inject EmbeddingModel embeddingModel;
    @Inject EmbeddingStore<TextSegment> embeddingStore;

    @Timeout(value = 60, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "fallbackResposta")
    public IaDTO.RespostaResponse responder(IaDTO.PerguntaRequest request) {

        // 1. Guardrail — valida entrada
        InputGuardrail.ValidacaoResult validacao =
                guardrail.validarEntrada(request.pergunta());

        if (!validacao.valido()) {
            LOG.warnf("Entrada bloqueada pelo guardrail: %s", validacao.motivo());
            return new IaDTO.RespostaResponse(
                    validacao.motivo(),
                    request.sessaoId(),
                    List.of(),
                    true,
                    OffsetDateTime.now()
            );
        }

        // 2. Sessão — recupera ou cria
        String sessaoId = sessaoService.iniciarSessao(request.sessaoId());
        List<SessaoMemoriaService.Mensagem> historico =
                sessaoService.recuperarHistorico(sessaoId);

        // 3. RAG — busca semântica no PgVector
        List<IaDTO.FonteRAG> fontes = new ArrayList<>();
        String contextoRAG = recuperarContextoRAG(request.pergunta(), fontes);

        // 4. Monta prompt enriquecido
        String prompt = montarPrompt(request.pergunta(), contextoRAG,
                                     sessaoService.formatarContextoConversa(historico),
                                     request.clienteId());

        LOG.debugf("Prompt enviado ao LLM: %d chars | sessao: %s", prompt.length(), sessaoId);

        // 5. Chama o LLM
        String respostaBruta = aiService.responder(prompt);

        // 6. Sanitiza saída
        String resposta = guardrail.sanitizarSaida(respostaBruta);

        // 7. Persiste conversa no Redis
        sessaoService.adicionarMensagem(sessaoId, "user",      request.pergunta());
        sessaoService.adicionarMensagem(sessaoId, "assistant", resposta);

        LOG.infof("Resposta gerada: sessao=%s fontes=%d guardrail=false",
                  sessaoId, fontes.size());

        return new IaDTO.RespostaResponse(
                resposta, sessaoId, fontes, false, OffsetDateTime.now());
    }

    // -----------------------------------------------------------
    // Fallback — quando o LLM não responde a tempo
    // -----------------------------------------------------------
    public IaDTO.RespostaResponse fallbackResposta(IaDTO.PerguntaRequest request) {
        LOG.warnf("Fallback ativado — LLM indisponível para sessão: %s",
                  request.sessaoId());
        return new IaDTO.RespostaResponse(
                "Estou com dificuldades técnicas no momento. " +
                "Por favor, tente novamente em alguns instantes. " +
                "Se precisar de ajuda urgente, ligue para nosso suporte.",
                request.sessaoId(),
                List.of(),
                false,
                OffsetDateTime.now()
        );
    }

    // -----------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------

    private String recuperarContextoRAG(String pergunta, List<IaDTO.FonteRAG> fontes) {
        try {
            // Gera embedding da pergunta
            var embedding = embeddingModel.embed(pergunta).content();

            // Busca os 5 chunks mais similares no PgVector (min-score 0.75 via config)
            List<EmbeddingMatch<TextSegment>> matches =
                    embeddingStore.findRelevant(embedding, 5, 0.75);

            if (matches.isEmpty()) return "";

            StringBuilder ctx = new StringBuilder("Informações relevantes:\n");
            for (EmbeddingMatch<TextSegment> match : matches) {
                String texto = match.embedded().text();
                String fonte = match.embedded().metadata().getString("fonte");
                double score = match.score();

                ctx.append("- ").append(texto).append("\n");
                fontes.add(new IaDTO.FonteRAG(
                        texto.length() > 100 ? texto.substring(0, 100) + "..." : texto,
                        fonte != null ? fonte : "base",
                        Math.round(score * 100.0) / 100.0
                ));
            }
            return ctx.toString();

        } catch (Exception e) {
            LOG.warnf("Falha na busca RAG: %s — continuando sem contexto", e.getMessage());
            return "";
        }
    }

    private String montarPrompt(String pergunta, String contextoRAG,
                                String historico, String clienteId) {
        StringBuilder sb = new StringBuilder();

        if (!contextoRAG.isBlank()) {
            sb.append(contextoRAG).append("\n\n");
        }

        if (!historico.isBlank()) {
            sb.append(historico).append("\n");
        }

        if (clienteId != null && !clienteId.isBlank()) {
            sb.append("Cliente ID: ").append(clienteId).append("\n\n");
        }

        sb.append("Pergunta do cliente: ").append(pergunta);
        return sb.toString();
    }
}
