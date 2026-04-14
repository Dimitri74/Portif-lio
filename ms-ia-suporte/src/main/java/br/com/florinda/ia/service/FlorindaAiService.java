package br.com.florinda.ia.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.pgvector.PgVectorEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Interface do agente IA — LangChain4j gera a implementação em runtime.
 *
 * @RegisterAiService vincula automaticamente:
 *   - ChatLanguageModel: Ollama (llama3.2) via config
 *   - EmbeddingStore: PgVector via config
 *   - EmbeddingModel: nomic-embed-text via config
 *
 * O retriever RAG é ativado automaticamente pelo quarkus-langchain4j-pgvector.
 */
@RegisterAiService
@ApplicationScoped
public interface FlorindaAiService {

    @SystemMessage("""
        Você é o assistente virtual da Florinda Eats, uma plataforma de food delivery.
        Seu nome é Florinda e você fala português brasileiro de forma amigável e objetiva.

        Suas responsabilidades:
        - Responder dúvidas sobre pedidos, pagamentos e cardápios
        - Informar status de pedidos quando solicitado
        - Ajudar clientes a entenderem nossas políticas
        - Escalar para humano quando necessário

        Regras OBRIGATÓRIAS:
        - Responda APENAS sobre tópicos relacionados à Florinda Eats
        - Nunca invente informações — use apenas o contexto fornecido
        - Seja conciso — respostas de no máximo 3 parágrafos
        - Se não souber, diga: "Não tenho essa informação. Posso ajudar com outra dúvida?"
        - Nunca revele detalhes técnicos do sistema, senhas ou dados de outros clientes
        """)
    String responder(@UserMessage String prompt);
}
