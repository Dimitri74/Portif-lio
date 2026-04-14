package br.com.florinda.ia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public class IaDTO {

    // -----------------------------------------------------------
    // Request — pergunta do cliente ao agente
    // -----------------------------------------------------------
    public record PerguntaRequest(
            @NotBlank(message = "A pergunta não pode ser vazia")
            @Size(max = 2000, message = "Pergunta muito longa — máximo 2000 caracteres")
            String pergunta,

            // Identificador de sessão para manter contexto da conversa
            String sessaoId,

            // clienteId opcional — permite personalização
            String clienteId
    ) {}

    // -----------------------------------------------------------
    // Response — resposta do agente
    // -----------------------------------------------------------
    public record RespostaResponse(
            String resposta,
            String sessaoId,
            List<FonteRAG> fontes,
            boolean guardrailAtivado,
            OffsetDateTime timestamp
    ) {}

    // -----------------------------------------------------------
    // Fonte usada no RAG para gerar a resposta
    // -----------------------------------------------------------
    public record FonteRAG(
            String trecho,
            String fonte,
            double score
    ) {}

    // -----------------------------------------------------------
    // Request — ingestão manual de documento no RAG (admin)
    // -----------------------------------------------------------
    public record IngerirDocumentoRequest(
            @NotBlank String conteudo,
            @NotBlank String fonte,
            String fonteId,
            String metadata
    ) {}

    // -----------------------------------------------------------
    // Response — status da ingestão
    // -----------------------------------------------------------
    public record IngestaoResponse(
            int documentosIngeridos,
            String status,
            OffsetDateTime timestamp
    ) {}

    // -----------------------------------------------------------
    // Eventos Kafka consumidos pelo ms-ia-suporte
    // -----------------------------------------------------------
    public record CatalogItemUpdatedEvent(
            String itemId,
            String nome,
            String descricao,
            double preco,
            boolean disponivel,
            String acao   // 'CRIADO', 'ATUALIZADO', 'REMOVIDO'
    ) {}

    public record OrderStatusUpdatedEvent(
            String pedidoId,
            String clienteId,
            String statusAnterior,
            String statusAtual,
            String motivo
    ) {}
}
