package br.com.florinda.pagamentos.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PagamentoResourceTest {

    static final String BASE = "/v1/pagamentos";

    static final String PEDIDO_ID  = "aaaaaaaa-0000-0000-0000-000000000001";
    static final String CLIENTE_ID = "bbbbbbbb-0000-0000-0000-000000000001";

    static String pagamentoIdCriado;

    static final String PAYLOAD_PROCESSAR = """
            {
              "pedidoId":  "%s",
              "clienteId": "%s",
              "valor":     89.90,
              "metodo":    "PIX"
            }
            """.formatted(PEDIDO_ID, CLIENTE_ID);

    @Test
    @Order(1)
    void deveProcessarPagamentoComSucesso() {
        pagamentoIdCriado = given()
            .contentType(ContentType.JSON)
            .body(PAYLOAD_PROCESSAR)
        .when()
            .post(BASE)
        .then()
            .statusCode(201)
            .body("id",       notNullValue())
            .body("pedidoId", equalTo(PEDIDO_ID))
            .body("valor",    equalTo(89.90f))
            .body("metodo",   equalTo("PIX"))
            .body("status",   anyOf(equalTo("APROVADO"), equalTo("REJEITADO")))
        .extract()
            .path("id");
    }

    @Test
    @Order(2)
    void deveBloquearPagamentoDuplicado() {
        // RN06: segundo pagamento para o mesmo pedidoId deve retornar 422
        given()
            .contentType(ContentType.JSON)
            .body(PAYLOAD_PROCESSAR)
        .when()
            .post(BASE)
        .then()
            .statusCode(422)
            .body("mensagem", containsString("já existe um pagamento"));
    }

    @Test
    @Order(3)
    void deveBuscarPagamentoPorId() {
        Assumptions.assumeTrue(pagamentoIdCriado != null);
        given()
            .when().get(BASE + "/" + pagamentoIdCriado)
            .then()
            .statusCode(200)
            .body("id", equalTo(pagamentoIdCriado));
    }

    @Test
    @Order(4)
    void deveBuscarPagamentoPorPedido() {
        given()
            .when().get(BASE + "/pedido/" + PEDIDO_ID)
            .then()
            .statusCode(200)
            .body("pedidoId", equalTo(PEDIDO_ID));
    }

    @Test
    @Order(5)
    void deveListarTodosPagamentos() {
        given()
            .when().get(BASE)
            .then()
            .statusCode(200)
            .body("$", not(empty()));
    }

    @Test
    @Order(6)
    void deveRetornar422EstornoEmPagamentoNaoAprovado() {
        // Cria pagamento separado para testar estorno
        String outrosPedidoId = "cccccccc-0000-0000-0000-000000000001";
        String novoPayload = """
                {
                  "pedidoId":  "%s",
                  "clienteId": "%s",
                  "valor":     50.00,
                  "metodo":    "CARTAO_CREDITO"
                }
                """.formatted(outrosPedidoId, CLIENTE_ID);

        String novoPagamentoId = given()
            .contentType(ContentType.JSON)
            .body(novoPayload)
        .when()
            .post(BASE)
        .then()
            .statusCode(201)
        .extract()
            .path("id");

        // Só testa estorno se o status foi REJEITADO (não pode estornar)
        String status = given()
            .when().get(BASE + "/" + novoPagamentoId)
            .then().extract().path("status");

        if ("REJEITADO".equals(status)) {
            given()
                .contentType(ContentType.JSON)
                .body("{\"motivo\": \"teste\"}")
            .when()
                .post(BASE + "/" + novoPagamentoId + "/estorno")
            .then()
                .statusCode(422);
        }
    }

    @Test
    @Order(7)
    void deveRetornar404ParaIdInexistente() {
        given()
            .when().get(BASE + "/00000000-0000-0000-0000-000000000099")
            .then()
            .statusCode(404);
    }

    @Test
    @Order(8)
    void healthCheckDeveEstarOk() {
        given()
            .when().get("/q/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }
}
