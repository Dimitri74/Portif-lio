package br.com.florinda.catalogo.resource;

import br.com.florinda.catalogo.dto.RestauranteDTO;
import br.com.florinda.catalogo.service.RestauranteService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/v1/restaurantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Restaurantes", description = "Gerenciamento de restaurantes")
public class RestauranteResource {

    @Inject
    RestauranteService service;

    @GET
    @Operation(summary = "Lista todos os restaurantes")
    public List<RestauranteDTO.RestauranteResumoResponse> listarTodos(
            @QueryParam("categoria") String categoria,
            @QueryParam("abertos") boolean apenasAbertos) {

        if (apenasAbertos) return service.listarAbertos();
        if (categoria != null) return service.listarPorCategoria(categoria);
        return service.listarTodos();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca restaurante por ID")
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "404", description = "Restaurante não encontrado")
    public RestauranteDTO.RestauranteResponse buscarPorId(
            @Parameter(description = "UUID do restaurante") @PathParam("id") UUID id) {
        return service.buscarPorId(id);
    }

    @POST
    @Operation(summary = "Cria um novo restaurante")
    @APIResponse(responseCode = "201", description = "Restaurante criado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    public Response criar(@Valid RestauranteDTO.CriarRestauranteRequest request) {
        RestauranteDTO.RestauranteResponse response = service.criar(request);
        return Response.created(URI.create("/v1/restaurantes/" + response.id()))
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza dados do restaurante")
    public RestauranteDTO.RestauranteResponse atualizar(
            @PathParam("id") UUID id,
            @Valid RestauranteDTO.AtualizarRestauranteRequest request) {
        return service.atualizar(id, request);
    }

    @PUT
    @Path("/{id}/abrir")
    @Operation(summary = "Abre o restaurante (muda status para ABERTO)")
    @APIResponse(responseCode = "204")
    @APIResponse(responseCode = "422", description = "Restaurante suspenso não pode ser aberto")
    public Response abrir(@PathParam("id") UUID id) {
        service.abrir(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/fechar")
    @Operation(summary = "Fecha o restaurante")
    public Response fechar(@PathParam("id") UUID id) {
        service.fechar(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/suspender")
    @Operation(summary = "Suspende o restaurante")
    public Response suspender(@PathParam("id") UUID id) {
        service.suspender(id);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove o restaurante")
    public Response deletar(@PathParam("id") UUID id) {
        service.deletar(id);
        return Response.noContent().build();
    }
}
