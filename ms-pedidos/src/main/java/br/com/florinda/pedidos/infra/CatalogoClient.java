package br.com.florinda.pedidos.infra;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RegisterRestClient(configKey = "catalogo-api")
@Path("/v1/restaurantes")
@Produces(MediaType.APPLICATION_JSON)
public interface CatalogoClient {

    @GET
    @Path("/{id}")
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 500)
    @CircuitBreaker(requestVolumeThreshold = 5,
                    failureRatio = 0.5,
                    delay = 10,
                    delayUnit = ChronoUnit.SECONDS)
    RestauranteStatusResponse buscarRestaurante(@PathParam("id") UUID id);

    // -----------------------------------------------------------
    // Resposta mínima — só precisamos saber se está ABERTO
    // -----------------------------------------------------------
    record RestauranteStatusResponse(
            UUID id,
            String nome,
            String status
    ) {
        public boolean estaAberto() {
            return "ABERTO".equals(status);
        }
    }
}
