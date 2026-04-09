package br.com.florinda.notificacoes.consumer;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.OffsetDateTime;
import java.util.Map;

@Path("/v1/notificacoes")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Notificações", description = "Status do serviço de notificações")
public class NotificacaoResource {

    @GET
    @Path("/status")
    @Operation(summary = "Retorna status do serviço e tópicos monitorados")
    public Map<String, Object> status() {
        return Map.of(
            "servico",   "ms-notificacoes",
            "status",    "UP",
            "topicos",   new String[]{
                "order.created",
                "order.status.updated",
                "payment.approved",
                "payment.failed"
            },
            "timestamp", OffsetDateTime.now().toString()
        );
    }
}
