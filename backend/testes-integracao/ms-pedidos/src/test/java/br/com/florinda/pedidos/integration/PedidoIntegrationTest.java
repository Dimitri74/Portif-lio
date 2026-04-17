package br.com.florinda.pedidos.integration;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integração do ms-pedidos com MySQL real via Testcontainers.
 * Valida: criação de pedido, regras de negócio e ciclo de vida de status.
 */
@QuarkusTest
@QuarkusTestResource(PedidosTestContainersResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PedidoIntegrationTest {

    static String pedidoId;

    static final String PAYLOAD_CRIAR = """
            {
              "clienteId":     "11111111-0000-0000-0000-000000000001",
              "restauranteId": "a1b2c3d4-0000-0000-0000-000000000001",
              "itens": [
                {
                  "itemId":        "c1000000-0000-0000-0000-000000000001",
                  "nomeItem":      "Picanha na brasa",
                  "precoUnitario": 89.90,
                  "quantidade":    1
                }
              ],
              "enderecoEntrega": "Rua dos Testes, 1, Recife PE"
            }
            """;

    @Test
    @Order(1)
    void deveCriarPedidoEPersistirNoBanco() {
        pedidoId = given()
            .contentType(ContentType.JSON)
            .body(PAYLOAD_CRIAR)
        .when()
            .post("/v1/pedidos")
        .then()
            .statusCode(201)
            .body("id",         notNullValue())
            .body("status",     equalTo("PENDENTE"))
            .body("valorTotal", equalTo(89.90f))
            .body("itens",      hasSize(1))
        .extract()
            .path("id");
    }

    @Test
    @Order(2)
    void deveBuscarPedidoPersistido() {
        Assumptions.assumeTrue(pedidoId != null);

        given()
            .when().get("/v1/pedidos/" + pedidoId)
            .then()
            .statusCode(200)
            .body("id",     equalTo(pedidoId))
            .body("status", equalTo("PENDENTE"));
    }

    @Test
    @Order(3)
    void deveRejeitarPedidoAbaixoDoMinimo() {
        // RN04: pedido mínimo R$ 15,00
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "clienteId":     "11111111-0000-0000-0000-000000000001",
                  "restauranteId": "a1b2c3d4-0000-0000-0000-000000000001",
                  "itens": [
                    {
                      "itemId":        "c1000000-0000-0000-0000-000000000002",
                      "nomeItem":      "Água",
                      "precoUnitario": 3.00,
                      "quantidade":    1
                    }
                  ]
                }
                """)
        .when()
            .post("/v1/pedidos")
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(422)));
    }

    @Test
    @Order(4)
    void deveCancelarPedidoNaFasePendente() {
        Assumptions.assumeTrue(pedidoId != null);

        given()
            .contentType(ContentType.JSON)
            .body("""
                { "motivo": "Teste de cancelamento na integração" }
                """)
        .when()
            .delete("/v1/pedidos/" + pedidoId)
        .then()
            .statusCode(200)
            .body("status", equalTo("CANCELADO"));
    }

    @Test
    @Order(5)
    void deveRejeitarCancelamentoDePedidoJaCancelado() {
        Assumptions.assumeTrue(pedidoId != null);

        // RN03: CANCELADO é estado final, não pode cancelar novamente
        given()
            .contentType(ContentType.JSON)
            .body("""
                { "motivo": "Tentativa inválida" }
                """)
        .when()
            .delete("/v1/pedidos/" + pedidoId)
        .then()
            .statusCode(422)
            .body("mensagem", containsString("não pode ser cancelado"));
    }

    @Test
    @Order(6)
    void deveListarPedidosPorCliente() {
        given()
            .queryParam("clienteId", "11111111-0000-0000-0000-000000000001")
        .when()
            .get("/v1/pedidos")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }
}
