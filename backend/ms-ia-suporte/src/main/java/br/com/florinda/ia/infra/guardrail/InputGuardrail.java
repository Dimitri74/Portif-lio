package br.com.florinda.ia.infra.guardrail;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Guardrail de segurança — primeira linha de defesa contra:
 * - Prompt injection ("ignore previous instructions")
 * - Jailbreak ("act as DAN", "you are now")
 * - Entradas maliciosas ou excessivamente longas
 *
 * Princípio: validar ENTRADA antes de enviar ao LLM
 *            e validar SAÍDA antes de retornar ao cliente.
 */
@ApplicationScoped
public class InputGuardrail {

    private static final Logger LOG = Logger.getLogger(InputGuardrail.class);

    @ConfigProperty(name = "florinda.ia.guardrail.max-input-length", defaultValue = "2000")
    int maxInputLength;

    @ConfigProperty(name = "florinda.ia.guardrail.blocked-patterns",
                    defaultValue = "ignore previous,forget instructions,jailbreak,act as,you are now")
    String blockedPatternsConfig;

    public record ValidacaoResult(boolean valido, String motivo) {
        public static ValidacaoResult ok() {
            return new ValidacaoResult(true, null);
        }
        public static ValidacaoResult bloqueado(String motivo) {
            return new ValidacaoResult(false, motivo);
        }
    }

    /**
     * Valida a entrada do usuário antes de enviar ao LLM.
     * Retorna ValidacaoResult com motivo em caso de bloqueio.
     */
    public ValidacaoResult validarEntrada(String input) {

        if (input == null || input.isBlank()) {
            return ValidacaoResult.bloqueado("Entrada vazia.");
        }

        // Verifica tamanho máximo
        if (input.length() > maxInputLength) {
            LOG.warnf("Input bloqueado por tamanho: %d chars", input.length());
            return ValidacaoResult.bloqueado(
                "Sua mensagem é muito longa. Por favor, seja mais conciso.");
        }

        // Verifica padrões de prompt injection
        String inputLower = input.toLowerCase();
        List<String> patterns = Arrays.asList(blockedPatternsConfig.split(","));

        for (String pattern : patterns) {
            if (inputLower.contains(pattern.trim().toLowerCase())) {
                LOG.warnf("Input bloqueado por padrão suspeito: '%s'", pattern.trim());
                return ValidacaoResult.bloqueado(
                    "Sua mensagem contém conteúdo não permitido. " +
                    "Por favor, faça uma pergunta sobre seu pedido ou cardápio.");
            }
        }

        return ValidacaoResult.ok();
    }

    /**
     * Valida a saída do LLM antes de retornar ao cliente.
     * Garante que o modelo não vazou informações sensíveis.
     */
    public String sanitizarSaida(String output) {
        if (output == null) return "Não consegui processar sua solicitação. Tente novamente.";

        // Remove possíveis tokens de sistema que o modelo possa ter incluído
        String sanitized = output
                .replaceAll("(?i)<system>.*?</system>", "")
                .replaceAll("(?i)\\[INST\\].*?\\[/INST\\]", "")
                .trim();

        if (sanitized.isBlank()) {
            return "Não tenho uma resposta para isso. Posso ajudar com informações sobre seu pedido ou nosso cardápio.";
        }

        return sanitized;
    }
}
