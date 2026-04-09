package br.com.florinda.pagamentos.infra;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Simulação do gateway externo de pagamento.
 * Em produção, substituir pela integração real (Stripe, PagSeguro, etc).
 *
 * Demonstra:
 *   - @Timeout com ChronoUnit (correção aplicada — não usar TimeUnit aqui)
 *   - @Retry com 3 tentativas e delay de 500ms
 *   - @CircuitBreaker: abre após 50% de falhas em 5 requisições
 *   - @Fallback: retorna REJEITADO quando o CB está aberto
 */
@ApplicationScoped
public class GatewayPagamentoSimulado {

    private static final Logger LOG = Logger.getLogger(GatewayPagamentoSimulado.class);

    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 3, delay = 500, delayUnit = ChronoUnit.MILLIS)
    @CircuitBreaker(
        requestVolumeThreshold = 5,
        failureRatio = 0.5,
        delay = 10,
        delayUnit = ChronoUnit.SECONDS
    )
    @Fallback(fallbackMethod = "fallbackProcessar")
    public GatewayResponse processar(UUID pedidoId, BigDecimal valor, String metodo) {
        LOG.infof("Chamando gateway externo: pedido=%s valor=%.2f metodo=%s",
                  pedidoId, valor, metodo);

        // Simulação: PIX sempre aprova, cartão tem 10% de falha
        boolean aprovado = !"CARTAO_CREDITO".equals(metodo) || Math.random() > 0.1;

        if (!aprovado) {
            throw new RuntimeException("Gateway rejeitou o pagamento: limite excedido");
        }

        return new GatewayResponse(
                true,
                "GW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "{\"status\":\"approved\",\"code\":\"00\"}"
        );
    }

    public GatewayResponse fallbackProcessar(UUID pedidoId, BigDecimal valor, String metodo) {
        LOG.warnf("Fallback ativado para gateway: pedido=%s — circuit breaker aberto", pedidoId);
        return new GatewayResponse(false, null,
                "{\"status\":\"rejected\",\"code\":\"CB\",\"message\":\"gateway unavailable\"}");
    }

    public record GatewayResponse(
            boolean aprovado,
            String gatewayId,
            String payload
    ) {}
}
