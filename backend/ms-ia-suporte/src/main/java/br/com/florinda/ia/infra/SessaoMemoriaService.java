package br.com.florinda.ia.infra;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gerencia a memória de sessão do agente via Redis.
 * Cada sessão mantém as últimas 10 mensagens com TTL de 30 minutos.
 * Formato no Redis: "sessao:{sessaoId}" → JSON do histórico
 */
@ApplicationScoped
public class SessaoMemoriaService {

    private static final Logger LOG = Logger.getLogger(SessaoMemoriaService.class);
    private static final int MAX_HISTORICO = 10;
    private static final Duration TTL_SESSAO = Duration.ofMinutes(30);
    private static final String PREFIXO = "sessao:";

    private final ValueCommands<String, String> redis;

    @Inject
    public SessaoMemoriaService(RedisDataSource ds) {
        this.redis = ds.value(String.class);
    }

    public record Mensagem(String role, String conteudo) {}

    /**
     * Gera um novo ID de sessão se não fornecido.
     */
    public String iniciarSessao(String sessaoId) {
        if (sessaoId == null || sessaoId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessaoId;
    }

    /**
     * Recupera o histórico de mensagens da sessão.
     */
    public List<Mensagem> recuperarHistorico(String sessaoId) {
        try {
            String json = redis.get(PREFIXO + sessaoId);
            if (json == null) return new ArrayList<>();
            return parsearHistorico(json);
        } catch (Exception e) {
            LOG.warnf("Falha ao recuperar sessão %s do Redis: %s", sessaoId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Adiciona uma mensagem ao histórico e persiste no Redis.
     * Mantém no máximo MAX_HISTORICO mensagens (janela deslizante).
     */
    public void adicionarMensagem(String sessaoId, String role, String conteudo) {
        try {
            List<Mensagem> historico = recuperarHistorico(sessaoId);
            historico.add(new Mensagem(role, conteudo));

            // Janela deslizante — remove as mais antigas
            if (historico.size() > MAX_HISTORICO) {
                historico = historico.subList(historico.size() - MAX_HISTORICO, historico.size());
            }

            redis.setex(PREFIXO + sessaoId, TTL_SESSAO.getSeconds(),
                        serializarHistorico(historico));
        } catch (Exception e) {
            LOG.warnf("Falha ao salvar mensagem na sessão %s: %s", sessaoId, e.getMessage());
        }
    }

    /**
     * Formata o histórico como contexto para o prompt do LLM.
     */
    public String formatarContextoConversa(List<Mensagem> historico) {
        if (historico.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("Histórico da conversa:\n");
        historico.forEach(m ->
            sb.append(m.role().equals("user") ? "Cliente: " : "Assistente: ")
              .append(m.conteudo())
              .append("\n")
        );
        return sb.toString();
    }

    // -----------------------------------------------------------
    // Serialização simples — sem dependência extra de JSON
    // -----------------------------------------------------------

    private String serializarHistorico(List<Mensagem> historico) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < historico.size(); i++) {
            Mensagem m = historico.get(i);
            sb.append("{\"role\":\"").append(escapar(m.role()))
              .append("\",\"conteudo\":\"").append(escapar(m.conteudo()))
              .append("\"}");
            if (i < historico.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private List<Mensagem> parsearHistorico(String json) {
        List<Mensagem> resultado = new ArrayList<>();
        // Parser simples — split por objetos JSON
        String[] partes = json.replaceAll("^\\[|\\]$", "").split("\\},\\{");
        for (String parte : partes) {
            parte = parte.replaceAll("[\\[\\]{}]", "");
            String role    = extrairValor(parte, "role");
            String conteudo = extrairValor(parte, "conteudo");
            if (role != null && conteudo != null) {
                resultado.add(new Mensagem(role, conteudo));
            }
        }
        return resultado;
    }

    private String extrairValor(String json, String chave) {
        String busca = "\"" + chave + "\":\"";
        int inicio = json.indexOf(busca);
        if (inicio < 0) return null;
        inicio += busca.length();
        int fim = json.indexOf("\"", inicio);
        if (fim < 0) return null;
        return json.substring(inicio, fim).replace("\\\"", "\"");
    }

    private String escapar(String s) {
        return s == null ? "" : s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
