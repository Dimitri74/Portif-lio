package br.com.florinda.pagamentos.resource;

import br.com.florinda.pagamentos.domain.StatusPagamento;
import br.com.florinda.pagamentos.dto.PagamentoDTO;
import br.com.florinda.pagamentos.service.PagamentoService;
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

@Path("/v1/pagamentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Pagamentos", description = "Processamento e consulta de pagamentos")
public class PagamentoResource {

    @Inject
    PagamentoService service;

    @GET
    @Operation(summary = "Lista pagamentos — filtra por status ou clienteId")
    public List<PagamentoDTO.PagamentoResumoResponse> listar(
            @QueryParam("status")    StatusPagamento status,
            @QueryParam("clienteId") UUID clienteId) {

        if (status    != null) return service.listarPorStatus(status);
        if (clienteId != null) return service.listarPorCliente(clienteId);
        return service.listarTodos();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca pagamento por ID")
    @APIResponse(responseCode = "404", description = "Pagamento não encontrado")
    public PagamentoDTO.PagamentoResponse buscar(@PathParam("id") UUID id) {
        return service.buscarPorId(id);
    }

    @GET
    @Path("/pedido/{pedidoId}")
    @Operation(summary = "Busca pagamento pelo ID do pedido")
    public PagamentoDTO.PagamentoResponse buscarPorPedido(@PathParam("pedidoId") UUID pedidoId) {
        return service.buscarPorPedido(pedidoId);
    }

    @POST
    @Operation(summary = "Processa um pagamento manualmente (admin/testes)")
    @APIResponse(responseCode = "201", description = "Pagamento processado")
    @APIResponse(responseCode = "422", description = "Pagamento duplicado ou status inválido")
    public Response processar(@Valid PagamentoDTO.ProcessarPagamentoRequest request) {
        PagamentoDTO.PagamentoResponse response = service.processar(request);
        return Response
                .created(URI.create("/v1/pagamentos/" + response.id()))
                .entity(response)
                .build();
    }

    @POST
    @Path("/{id}/estorno")
    @Operation(summary = "Solicita estorno do pagamento")
    @APIResponse(responseCode = "200", description = "Estorno registrado")
    @APIResponse(responseCode = "422", description = "Pagamento não elegível para estorno")
    public PagamentoDTO.EstornoResponse estornar(
            @PathParam("id") UUID id,
            @Valid PagamentoDTO.EstornarRequest request) {
        return service.estornar(id, request.motivo());
    }
}
