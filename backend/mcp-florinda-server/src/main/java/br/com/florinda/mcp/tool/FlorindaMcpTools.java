package br.com.florinda.mcp.tool;

import br.com.florinda.mcp.infra.McpClients;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

/**
 * Tools MCP expostas ao agente IA via SSE.
 *
 * O agente (ms-ia-suporte) conecta neste servidor via:
 *   http://localhost:8085/mcp/sse
 *
 * Cada método anotado com @Tool torna-se uma ferramenta que o LLM
 * pode invocar autonomamente ao decidir que precisa de dados reais.
 *
 * Casos de uso:
 *   UC-IA01 → getOrderStatus: "qual o status do meu pedido?"
 *   UC-IA02 → cancelOrder:    "quero cancelar meu pedido"
 *   UC-IA03 → getRestaurantMenu: "o que tem no cardápio?"
 */
@Singleton
public class FlorindaMcpTools {

    private static final Logger LOG = Logger.getLogger(FlorindaMcpTools.class);

    @Inject
    @RestClient
    McpClients.PedidosClient pedidosClient;

    @Inject
    @RestClient
    McpClients.CatalogoClient catalogoClient;

    // -----------------------------------------------------------
    // UC-IA01 — Status do pedido
    // -----------------------------------------------------------
    @Tool(description = """
        Consulta o status atual de um pedido pelo ID.
        Use quando o cliente perguntar sobre onde está seu pedido,
        qual o status, se foi confirmado, etc.
        Retorna: id, status, valor total, data de criação.
        """)
    public String getOrderStatus(
            @ToolArg(description = "UUID do pedido no formato xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
            String pedidoId) {

        LOG.infof("[MCP] getOrderStatus chamado: pedidoId=%s", pedidoId);

        try {
            UUID id = UUID.fromString(pedidoId);
            McpClients.PedidosClient.PedidoResponse pedido =
                    pedidosClient.buscarPedido(id);

            return """
                Pedido encontrado:
                - ID: %s
                - Status: %s
                - Valor total: R$ %.2f
                - Criado em: %s
                """.formatted(
                    pedido.id(),
                    traduzirStatus(pedido.status()),
                    pedido.valorTotal(),
                    pedido.criadoEm()
                );

        } catch (IllegalArgumentException e) {
            return "ID de pedido inválido. Por favor, verifique o número do pedido.";
        } catch (Exception e) {
            LOG.warnf("[MCP] Falha ao buscar pedido %s: %s", pedidoId, e.getMessage());
            return "Não consegui encontrar informações sobre este pedido. " +
                   "Verifique se o ID está correto.";
        }
    }

    // -----------------------------------------------------------
    // UC-IA02 — Cancelar pedido
    // -----------------------------------------------------------
    @Tool(description = """
        Cancela um pedido pelo ID.
        Use APENAS quando o cliente confirmar explicitamente que deseja cancelar.
        Só funciona se o pedido estiver com status PENDENTE ou CONFIRMADO.
        Retorna: confirmação do cancelamento ou motivo da impossibilidade.
        """)
    public String cancelOrder(
            @ToolArg(description = "UUID do pedido a ser cancelado")
            String pedidoId,

            @ToolArg(description = "Motivo do cancelamento informado pelo cliente")
            String motivo) {

        LOG.infof("[MCP] cancelOrder chamado: pedidoId=%s motivo=%s", pedidoId, motivo);

        try {
            UUID id = UUID.fromString(pedidoId);
            var request = new McpClients.PedidosClient.CancelarRequest(
                    motivo != null ? motivo : "Cancelado pelo cliente via chat");

            McpClients.PedidosClient.PedidoResponse pedido =
                    pedidosClient.cancelarPedido(id, request);

            return "Pedido %s cancelado com sucesso. ".formatted(pedido.id()) +
                   "Se o pagamento já foi processado, o estorno será realizado em até 7 dias úteis.";

        } catch (IllegalArgumentException e) {
            return "ID de pedido inválido.";
        } catch (Exception e) {
            LOG.warnf("[MCP] Falha ao cancelar pedido %s: %s", pedidoId, e.getMessage());
            // Mensagem amigável baseada no erro
            if (e.getMessage() != null && e.getMessage().contains("422")) {
                return "Não é possível cancelar este pedido. " +
                       "O cancelamento só é permitido enquanto o pedido está Pendente ou Confirmado. " +
                       "Como o restaurante já iniciou o preparo, não é mais possível cancelar.";
            }
            return "Não consegui cancelar o pedido no momento. Por favor, tente novamente.";
        }
    }

    // -----------------------------------------------------------
    // UC-IA03 — Cardápio do restaurante
    // -----------------------------------------------------------
    @Tool(description = """
        Busca o cardápio de um restaurante pelo ID.
        Use quando o cliente perguntar sobre o que está disponível,
        opções vegetarianas, preços, descrições de pratos, etc.
        Retorna: lista de itens com nome, descrição, preço e disponibilidade.
        """)
    public String getRestaurantMenu(
            @ToolArg(description = "UUID do restaurante")
            String restauranteId,

            @ToolArg(description = "UUID do cardápio (opcional — se não souber, passe null)")
            String cardapioId) {

        LOG.infof("[MCP] getRestaurantMenu chamado: restauranteId=%s", restauranteId);

        try {
            UUID restId = UUID.fromString(restauranteId);

            // Busca informações do restaurante
            McpClients.CatalogoClient.RestauranteResponse restaurante =
                    catalogoClient.buscarRestaurante(restId);

            if (!"ABERTO".equals(restaurante.status())) {
                return "O restaurante %s está %s no momento e não aceita pedidos."
                        .formatted(restaurante.nome(), traduzirStatusRestaurante(restaurante.status()));
            }

            // Busca itens do cardápio (se cardapioId fornecido)
            if (cardapioId != null && !cardapioId.isBlank()) {
                try {
                    UUID cardId = UUID.fromString(cardapioId);
                    List<McpClients.CatalogoClient.ItemResponse> itens =
                            catalogoClient.listarItens(cardId);

                    return formatarCardapio(restaurante.nome(), itens);
                } catch (Exception e) {
                    LOG.warnf("[MCP] Falha ao listar itens do cardápio: %s", e.getMessage());
                }
            }

            return "Restaurante %s (%s) está %s. Para ver o cardápio completo, acesse o app."
                    .formatted(restaurante.nome(), restaurante.categoria(),
                               traduzirStatusRestaurante(restaurante.status()));

        } catch (IllegalArgumentException e) {
            return "ID de restaurante inválido.";
        } catch (Exception e) {
            LOG.warnf("[MCP] Falha ao buscar cardápio %s: %s", restauranteId, e.getMessage());
            return "Não consegui recuperar o cardápio no momento.";
        }
    }

    // -----------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------

    private String formatarCardapio(String nomeRestaurante,
            List<McpClients.CatalogoClient.ItemResponse> itens) {

        if (itens == null || itens.isEmpty()) {
            return "O cardápio do restaurante " + nomeRestaurante + " está vazio no momento.";
        }

        StringBuilder sb = new StringBuilder("Cardápio do ")
                .append(nomeRestaurante).append(":\n");

        itens.stream()
             .filter(McpClients.CatalogoClient.ItemResponse::disponivel)
             .forEach(item -> {
                 sb.append("• ").append(item.nome())
                   .append(" — R$ ").append(item.preco());
                 if (item.vegetariano()) sb.append(" 🌱Vegetariano");
                 if (item.vegano()) sb.append(" 🌿Vegano");
                 if (item.descricao() != null) {
                     sb.append("\n  ").append(item.descricao());
                 }
                 sb.append("\n");
             });

        return sb.toString();
    }

    private String traduzirStatus(String status) {
        return switch (status != null ? status : "") {
            case "PENDENTE"          -> "Pendente (aguardando pagamento)";
            case "CONFIRMADO"        -> "Confirmado";
            case "PREPARANDO"        -> "Em preparo no restaurante";
            case "SAIU_PARA_ENTREGA" -> "Saiu para entrega";
            case "ENTREGUE"          -> "Entregue";
            case "CANCELADO"         -> "Cancelado";
            default                  -> status;
        };
    }

    private String traduzirStatusRestaurante(String status) {
        return switch (status != null ? status : "") {
            case "ABERTO"   -> "aberto";
            case "FECHADO"  -> "fechado";
            case "SUSPENSO" -> "temporariamente suspenso";
            default         -> status;
        };
    }
}
