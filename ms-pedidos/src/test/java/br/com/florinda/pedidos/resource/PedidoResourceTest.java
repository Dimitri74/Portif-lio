package br.com.florinda.pedidos.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PedidoResourceTest {

    static final String BASE = "/v1/pedidos";

    static final String PAYLOAD_CRIAR = """
            {
              "clienteId":     "11111111-0000-0000-0000-000000000001",
              "restauranteId": "a1b2c3d4-0000-0000-0000-000000000001",
              "itens": [
                {
                  "itemId":         "c1000000-0000-0000-0000-000000000001",
                  "nomeItem":       "Picanha na brasa",
                  "precoUnitario":  89.90,
                  "quantidade":     1
                }
              ],
              "observacao":       "Sem cebola",
              "enderecoEntrega":  "Rua das Flores, 10, Juazeiro do Norte CE"
            }
            """;

    static String pedidoIdCriado;

    @Test
    @Order(1)
    void deveCriarPedidoComSucesso() {
        pedidoIdCriado = given()
            .contentType(ContentType.JSON)
            .body(PAYLOAD_CRIAR)
        .when()
            .post(BASE)
        .then()
            .statusCode(201)
            .body("id",          notNullValue())
            .body("status",      equalTo("PENDENTE"))
            .body("valorTotal",  equalTo(89.90f))
            .body("itens",       hasSize(1))
            .body("itens[0].nomeItem", equalTo("Picanha na brasa"))
        .extract()
            .path("id");
    }

    @Test
    @Order(2)
    void deveBuscarPedidoCriado() {
        Assumptions.assumeTrue(pedidoIdCriado != null);
        given()
            .when().get(BASE + "/" + pedidoIdCriado)
            .then()
            .statusCode(200)
            .body("id", equalTo(pedidoIdCriado));
    }

    @Test
    @Order(3)
    void deveRetornar400SemItens() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "clienteId": "11111111-0000-0000-0000-000000000001",
                  "restauranteId": "a1b2c3d4-0000-0000-0000-000000000001",
                  "itens": []
                }
                """)
        .when()
            .post(BASE)
        .then()
            .statusCode(400);
    }

    @Test
    @Order(4)
    void deveCancelarPedido() {
        Assumptions.assumeTrue(pedidoIdCriado != null);
        given()
            .contentType(ContentType.JSON)
            .body("""
                { "motivo": "Cliente desistiu" }
                """)
        .when()
            .delete(BASE + "/" + pedidoIdCriado)
        .then()
            .statusCode(200)
            .body("status", equalTo("CANCELADO"));
    }

    @Test
    @Order(5)
    void deveRetornar404ParaIdInexistente() {
        given()
            .when().get(BASE + "/00000000-0000-0000-0000-000000000099")
            .then()
            .statusCode(404);
    }

    @Test
    @Order(6)
    void healthCheckDeveEstarOk() {
        given()
            .when().get("/q/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }
}
