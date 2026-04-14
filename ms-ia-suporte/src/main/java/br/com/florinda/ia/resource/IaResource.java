package br.com.florinda.ia.resource;

import br.com.florinda.ia.dto.IaDTO;
import br.com.florinda.ia.infra.rag.RagIngestaoService;
import br.com.florinda.ia.service.AgenteService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.OffsetDateTime;
import java.util.List;

@Path("/v1/ia")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Agente IA", description = "Suporte conversacional com RAG")
public class IaResource {

    @Inject AgenteService agenteService;
    @Inject RagIngestaoService ingestaoService;

    // -----------------------------------------------------------
    // UC-IA01/02/03/04 — Chat com o agente
    // -----------------------------------------------------------
    @POST
    @Path("/chat")
    @Operation(
        summary = "Envia pergunta ao agente IA",
        description = "Responde com base no RAG (PgVector) + histórico de sessão (Redis) + LLM (Ollama)"
    )
    @APIResponse(responseCode = "200", description = "Resposta gerada com sucesso")
    @APIResponse(responseCode = "400", description = "Pergunta inválida ou vazia")
    public IaDTO.RespostaResponse chat(@Valid IaDTO.PerguntaRequest request) {
        return agenteService.responder(request);
    }

    // -----------------------------------------------------------
    // Admin — ingestão manual de documentos no RAG
    // -----------------------------------------------------------
    @POST
    @Path("/admin/ingerir")
    @Operation(
        summary = "Ingere documentos no base vetorial (admin)",
        description = "Divide em chunks, gera embeddings via nomic-embed-text e armazena no PgVector"
    )
    public IaDTO.IngestaoResponse ingerirDocumento(
            @Valid IaDTO.IngerirDocumentoRequest request) {

        ingestaoService.ingerirDocumento(
                request.conteudo(), request.fonte(), request.fonteId());

        return new IaDTO.IngestaoResponse(1, "OK", OffsetDateTime.now());
    }

    @POST
    @Path("/admin/ingerir/lote")
    @Operation(summary = "Ingere lote de documentos no RAG (admin)")
    public IaDTO.IngestaoResponse ingerirLote(
            List<IaDTO.IngerirDocumentoRequest> documentos) {

        int total = ingestaoService.ingerirLote(documentos);
        return new IaDTO.IngestaoResponse(total, "OK", OffsetDateTime.now());
    }

    // -----------------------------------------------------------
    // Health estendido — verifica Ollama e PgVector
    // -----------------------------------------------------------
    @GET
    @Path("/health")
    @Operation(summary = "Health check estendido do agente IA")
    public Response healthExtendido() {
        return Response.ok(java.util.Map.of(
            "servico",       "ms-ia-suporte",
            "llm",           "ollama/llama3.2",
            "embeddings",    "ollama/nomic-embed-text",
            "vectorStore",   "pgvector",
            "memoria",       "redis",
            "timestamp",     OffsetDateTime.now().toString()
        )).build();
    }
}
