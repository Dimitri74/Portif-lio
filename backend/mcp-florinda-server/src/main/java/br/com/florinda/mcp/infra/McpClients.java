package br.com.florinda.mcp.infra;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class McpClients {

    // -----------------------------------------------------------
    // Cliente do ms-pedidos
    // -----------------------------------------------------------
    @RegisterRestClient(configKey = "pedidos-api")
    @Path("/v1/pedidos")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public interface PedidosClient {

        @GET
        @Path("/{id}")
        @Timeout(value = 5, unit = ChronoUnit.SECONDS)
        @Retry(maxRetries = 2, delay = 300, delayUnit = ChronoUnit.MILLIS)
        @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 0.5,
                        delay = 10, delayUnit = ChronoUnit.SECONDS)
        PedidoResponse buscarPedido(@PathParam("id") UUID id);

        @DELETE
        @Path("/{id}")
        @Timeout(value = 5, unit = ChronoUnit.SECONDS)
        @Retry(maxRetries = 1)
        PedidoResponse cancelarPedido(@PathParam("id") UUID id,
                                      CancelarRequest request);

        record PedidoResponse(
                UUID id,
                UUID clienteId,
                UUID restauranteId,
                String status,
                BigDecimal valorTotal,
                String observacao,
                String criadoEm,
                String atualizadoEm
        ) {}

        record CancelarRequest(String motivo) {}
    }

    // -----------------------------------------------------------
    // Cliente do ms-catalogo
    // -----------------------------------------------------------
    @RegisterRestClient(configKey = "catalogo-api")
    @Path("/v1")
    @Produces(MediaType.APPLICATION_JSON)
    public interface CatalogoClient {

        @GET
        @Path("/restaurantes/{id}")
        @Timeout(value = 5, unit = ChronoUnit.SECONDS)
        @Retry(maxRetries = 2, delay = 300, delayUnit = ChronoUnit.MILLIS)
        RestauranteResponse buscarRestaurante(@PathParam("id") UUID id);

        @GET
        @Path("/cardapios/{cardapioId}/itens")
        @Timeout(value = 5, unit = ChronoUnit.SECONDS)
        @Retry(maxRetries = 2, delay = 300, delayUnit = ChronoUnit.MILLIS)
        List<ItemResponse> listarItens(@PathParam("cardapioId") UUID cardapioId);

        record RestauranteResponse(
                UUID id,
                String nome,
                String categoria,
                String status,
                EnderecoResponse endereco
        ) {}

        record EnderecoResponse(
                String logradouro, String cidade, String uf
        ) {}

        record ItemResponse(
                UUID id,
                String nome,
                String descricao,
                BigDecimal preco,
                boolean disponivel,
                boolean vegetariano,
                boolean vegano
        ) {}
    }
}
