package br.com.florinda.pedidos.dto;

import br.com.florinda.pedidos.domain.StatusPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PedidoDTO {

    // -----------------------------------------------------------
    // Request — criação
    // -----------------------------------------------------------
    public record CriarPedidoRequest(
            @NotNull UUID clienteId,
            @NotNull UUID restauranteId,

            @NotNull @Size(min = 1, message = "O pedido deve ter ao menos 1 item")
            @Valid List<ItemPedidoRequest> itens,

            @Size(max = 500) String observacao,
            @Size(max = 500) String enderecoEntrega
    ) {}

    public record ItemPedidoRequest(
            @NotNull UUID itemId,
            @NotBlank @Size(max = 150) String nomeItem,
            @NotNull @DecimalMin("0.01") BigDecimal precoUnitario,
            @Min(1) int quantidade
    ) {}

    // -----------------------------------------------------------
    // Request — cancelamento
    // -----------------------------------------------------------
    public record CancelarPedidoRequest(
            @NotBlank @Size(max = 300) String motivo
    ) {}

    // -----------------------------------------------------------
    // Response — pedido completo
    // -----------------------------------------------------------
    public record PedidoResponse(
            UUID id,
            UUID clienteId,
            UUID restauranteId,
            StatusPedido status,
            BigDecimal valorTotal,
            String observacao,
            String enderecoEntrega,
            List<ItemPedidoResponse> itens,
            OffsetDateTime criadoEm,
            OffsetDateTime atualizadoEm
    ) {}

    public record ItemPedidoResponse(
            UUID id,
            UUID itemId,
            String nomeItem,
            BigDecimal precoUnitario,
            int quantidade,
            BigDecimal subtotal
    ) {}

    // -----------------------------------------------------------
    // Response — listagem resumida
    // -----------------------------------------------------------
    public record PedidoResumoResponse(
            UUID id,
            UUID clienteId,
            UUID restauranteId,
            StatusPedido status,
            BigDecimal valorTotal,
            OffsetDateTime criadoEm
    ) {}
}
