package br.com.florinda.pagamentos.integration;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de integração do ms-pagamentos com MySQL real via Testcontainers.
 * Valida: processamento, idempotência (RN06), estorno (RN08).
 */
@QuarkusTest
@QuarkusTestResource(PagamentosTestContainersResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PagamentoIntegrationTest {

    static final String PEDIDO_ID  = UUID.randomUUID().toString();
    static final String CLIENTE_ID = "bbbbbbbb-0000-0000-0000-000000000001";
    static String pagamentoId;

    @Test
    @Order(1)
    void deveProcessarPagamentoPIXComSucesso() {
        pagamentoId = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "pedidoId":  "%s",
                  "clienteId": "%s",
                  "valor":     89.90,
                  "metodo":    "PIX"
                }
                """.formatted(PEDIDO_ID, CLIENTE_ID))
        .when()
            .post("/v1/pagamentos")
        .then()
            .statusCode(201)
            .body("pedidoId", equalTo(PEDIDO_ID))
            .body("metodo",   equalTo("PIX"))
            // PIX sempre é aprovado no simulador
            .body("status",   equalTo("APROVADO"))
        .extract()
            .path("id");
    }

    @Test
    @Order(2)
    void deveBloquearPagamentoDuplicado_RN06() {
        // RN06: segundo pagamento para o mesmo pedidoId deve retornar 422
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "pedidoId":  "%s",
                  "clienteId": "%s",
                  "valor":     89.90,
                  "metodo":    "PIX"
                }
                """.formatted(PEDIDO_ID, CLIENTE_ID))
        .when()
            .post("/v1/pagamentos")
        .then()
            .statusCode(422)
            .body("mensagem", containsString("já existe um pagamento"));
    }

    @Test
    @Order(3)
    void deveBuscarPagamentoPorPedido() {
        given()
            .when().get("/v1/pagamentos/pedido/" + PEDIDO_ID)
            .then()
            .statusCode(200)
            .body("pedidoId", equalTo(PEDIDO_ID))
            .body("status",   equalTo("APROVADO"));
    }

    @Test
    @Order(4)
    void deveEstornarPagamentoAprovado_RN08() {
        Assumptions.assumeTrue(pagamentoId != null);

        given()
            .contentType(ContentType.JSON)
            .body("""
                { "motivo": "Cliente cancelou o pedido" }
                """)
        .when()
            .post("/v1/pagamentos/" + pagamentoId + "/estorno")
        .then()
            .statusCode(200)
            .body("motivo",  equalTo("Cliente cancelou o pedido"))
            .body("status",  equalTo("SOLICITADO"));
    }

    @Test
    @Order(5)
    void deveRejeitarEstornoEmPagamentoJaEstornado() {
        Assumptions.assumeTrue(pagamentoId != null);

        // Após estorno, status vira ESTORNADO — não pode estornar novamente
        given()
            .contentType(ContentType.JSON)
            .body("""
                { "motivo": "Segunda tentativa inválida" }
                """)
        .when()
            .post("/v1/pagamentos/" + pagamentoId + "/estorno")
        .then()
            .statusCode(422)
            .body("mensagem", containsString("APROVADOS"));
    }

    @Test
    @Order(6)
    void deveListarTodosPagamentos() {
        given()
            .when().get("/v1/pagamentos")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }
}
