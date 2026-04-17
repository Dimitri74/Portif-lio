package br.com.florinda.catalogo.integration;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Teste de integração com PostgreSQL real via Testcontainers.
 * Diferente do @QuarkusTest padrão (Dev Services),
 * este sobe um container PostgreSQL real e valida o Flyway na íntegra.
 */
@QuarkusTest
@QuarkusTestResource(TestContainersResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestauranteIntegrationTest {

    static String restauranteId;

    @Test
    @Order(1)
    void deveCriarRestauranteEPersistirNoBanco() {
        restauranteId = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "nome": "Restaurante IT Test",
                  "categoria": "NORDESTINO",
                  "endereco": {
                    "logradouro": "Rua dos Testes",
                    "numero": "1",
                    "bairro": "Tech",
                    "cidade": "Recife",
                    "uf": "PE",
                    "cep": "50000-000"
                  }
                }
                """)
        .when()
            .post("/v1/restaurantes")
        .then()
            .statusCode(201)
            .body("id",     notNullValue())
            .body("status", equalTo("FECHADO"))
        .extract()
            .path("id");
    }

    @Test
    @Order(2)
    void deveAbrirRestauranteERefletirNoBanco() {
        Assumptions.assumeTrue(restauranteId != null);

        given()
            .when().put("/v1/restaurantes/" + restauranteId + "/abrir")
            .then().statusCode(204);

        given()
            .when().get("/v1/restaurantes/" + restauranteId)
            .then()
            .statusCode(200)
            .body("status", equalTo("ABERTO"));
    }

    @Test
    @Order(3)
    void deveListarApenaRestaurantesAbertos() {
        given()
            .queryParam("abertos", true)
        .when()
            .get("/v1/restaurantes")
        .then()
            .statusCode(200)
            .body("status", everyItem(equalTo("ABERTO")));
    }

    @Test
    @Order(4)
    void deveSeedDadosEstaremPresentes() {
        // Valida que as migrations V1 + V2 rodaram corretamente
        given()
            .when().get("/v1/restaurantes")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(5)
    void deveRejeitarRestauranteSuspensoAoAbrir() {
        Assumptions.assumeTrue(restauranteId != null);

        given()
            .when().put("/v1/restaurantes/" + restauranteId + "/suspender")
            .then().statusCode(204);

        given()
            .when().put("/v1/restaurantes/" + restauranteId + "/abrir")
            .then()
            .statusCode(422)
            .body("mensagem", containsString("suspenso"));
    }
}
