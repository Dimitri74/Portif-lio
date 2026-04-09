package br.com.florinda.pagamentos.dto;

import br.com.florinda.pagamentos.domain.MetodoPagamento;
import br.com.florinda.pagamentos.domain.StatusPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PagamentoDTO {

    // -----------------------------------------------------------
    // Request — processamento manual (via REST, para testes/admin)
    // -----------------------------------------------------------
    public record ProcessarPagamentoRequest(
            @NotNull UUID pedidoId,
            @NotNull UUID clienteId,
            @NotNull @DecimalMin("0.01") BigDecimal valor,
            @NotNull MetodoPagamento metodo
    ) {}

    // -----------------------------------------------------------
    // Request — estorno
    // -----------------------------------------------------------
    public record EstornarRequest(
            @NotBlank @Size(max = 300) String motivo
    ) {}

    // -----------------------------------------------------------
    // Response — pagamento completo
    // -----------------------------------------------------------
    public record PagamentoResponse(
            UUID id,
            UUID pedidoId,
            UUID clienteId,
            BigDecimal valor,
            MetodoPagamento metodo,
            StatusPagamento status,
            String gatewayId,
            int tentativas,
            OffsetDateTime criadoEm,
            OffsetDateTime atualizadoEm
    ) {}

    // -----------------------------------------------------------
    // Response — listagem resumida
    // -----------------------------------------------------------
    public record PagamentoResumoResponse(
            UUID id,
            UUID pedidoId,
            BigDecimal valor,
            MetodoPagamento metodo,
            StatusPagamento status,
            OffsetDateTime criadoEm
    ) {}

    // -----------------------------------------------------------
    // Response — estorno
    // -----------------------------------------------------------
    public record EstornoResponse(
            UUID id,
            UUID pagamentoId,
            BigDecimal valor,
            String motivo,
            String status,
            OffsetDateTime criadoEm
    ) {}
}
