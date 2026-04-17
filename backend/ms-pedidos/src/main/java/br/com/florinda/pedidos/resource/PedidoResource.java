package br.com.florinda.pedidos.resource;

import br.com.florinda.pedidos.domain.StatusPedido;
import br.com.florinda.pedidos.dto.PedidoDTO;
import br.com.florinda.pedidos.service.PedidoService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/v1/pedidos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Pedidos", description = "Ciclo de vida de pedidos")
public class PedidoResource {

    @Inject
    PedidoService service;

    @GET
    @Operation(summary = "Lista pedidos por cliente ou status")
    public List<PedidoDTO.PedidoResumoResponse> listar(
            @QueryParam("clienteId") UUID clienteId,
            @QueryParam("status") StatusPedido status) {

        if (clienteId != null) return service.listarPorCliente(clienteId);
        if (status != null)    return service.listarPorStatus(status);
        return service.listarTodos();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca pedido por ID")
    @APIResponse(responseCode = "404", description = "Pedido não encontrado")
    public PedidoDTO.PedidoResponse buscar(@PathParam("id") UUID id) {
        return service.buscarPorId(id);
    }

    @POST
    @Operation(summary = "Cria um novo pedido")
    @APIResponse(responseCode = "201", description = "Pedido criado e evento Kafka publicado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "422", description = "Restaurante fechado ou valor mínimo não atingido")
    public Response criar(@Valid PedidoDTO.CriarPedidoRequest request) {
        PedidoDTO.PedidoResponse response = service.criar(request);
        return Response
                .created(URI.create("/v1/pedidos/" + response.id()))
                .entity(response)
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Cancela o pedido")
    @APIResponse(responseCode = "200", description = "Pedido cancelado")
    @APIResponse(responseCode = "422", description = "Pedido não pode ser cancelado no status atual")
    public PedidoDTO.PedidoResponse cancelar(
            @PathParam("id") UUID id,
            @Valid PedidoDTO.CancelarPedidoRequest request) {
        return service.cancelar(id, request.motivo());
    }

    @PUT
    @Path("/{id}/avancar")
    @Operation(summary = "Avança o status do pedido para o próximo (simulação)")
    @APIResponse(responseCode = "200", description = "Status avançado com sucesso")
    @APIResponse(responseCode = "422", description = "Status atual não permite avanço manual")
    public PedidoDTO.PedidoResponse avancarStatus(@PathParam("id") UUID id) {
        return service.avancarStatus(id);
    }
}
