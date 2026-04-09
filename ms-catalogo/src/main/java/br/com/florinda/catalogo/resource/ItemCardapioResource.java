package br.com.florinda.catalogo.resource;

import br.com.florinda.catalogo.dto.ItemCardapioDTO;
import br.com.florinda.catalogo.service.ItemCardapioService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/v1/cardapios/{cardapioId}/itens")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Itens do Cardápio", description = "Gerenciamento de itens por cardápio")
public class ItemCardapioResource {

    @Inject
    ItemCardapioService service;

    @GET
    @Operation(summary = "Lista itens disponíveis do cardápio")
    public List<ItemCardapioDTO.ItemCardapioResponse> listar(@PathParam("cardapioId") UUID cardapioId) {
        return service.listarPorCardapio(cardapioId);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca item por ID")
    public ItemCardapioDTO.ItemCardapioResponse buscar(
            @PathParam("cardapioId") UUID cardapioId,
            @PathParam("id") UUID id) {
        return service.buscarPorId(id);
    }

    @POST
    @Operation(summary = "Adiciona item ao cardápio")
    public Response criar(
            @PathParam("cardapioId") UUID cardapioId,
            @Valid ItemCardapioDTO.CriarItemRequest request) {
        ItemCardapioDTO.ItemCardapioResponse response = service.criar(cardapioId, request);
        return Response.created(
                URI.create("/v1/cardapios/" + cardapioId + "/itens/" + response.id()))
                .entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza item do cardápio")
    public ItemCardapioDTO.ItemCardapioResponse atualizar(
            @PathParam("cardapioId") UUID cardapioId,
            @PathParam("id") UUID id,
            @Valid ItemCardapioDTO.AtualizarItemRequest request) {
        return service.atualizar(id, request);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove item do cardápio")
    public Response deletar(
            @PathParam("cardapioId") UUID cardapioId,
            @PathParam("id") UUID id) {
        service.deletar(id);
        return Response.noContent().build();
    }
}
