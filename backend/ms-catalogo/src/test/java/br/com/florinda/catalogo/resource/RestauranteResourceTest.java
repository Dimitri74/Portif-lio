package br.com.florinda.catalogo.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestauranteResourceTest {

    static final String BASE = "/v1/restaurantes";

    static final String PAYLOAD_CRIAR = """
            {
              "nome": "Tapiocaria da Maria",
              "descricao": "Tapiocas artesanais nordestinas",
              "categoria": "NORDESTINO",
              "telefone": "88999990000",
              "email": "maria@tapiocaria.com",
              "endereco": {
                "logradouro": "Rua do Horto",
                "numero": "10",
                "bairro": "Horto",
                "cidade": "Juazeiro do Norte",
                "uf": "CE",
                "cep": "63050-000"
              },
              "horarioAbertura": "07:00",
              "horarioFechamento": "18:00"
            }
            """;

    @Test
    @Order(1)
    void deveListarRestaurantes() {
        given()
            .when().get(BASE)
            .then()
            .statusCode(200)
            .body("$", not(empty()));
    }

    @Test
    @Order(2)
    void deveCriarRestauranteComSucesso() {
        given()
            .contentType(ContentType.JSON)
            .body(PAYLOAD_CRIAR)
        .when()
            .post(BASE)
        .then()
            .statusCode(201)
            .body("id",       notNullValue())
            .body("nome",     equalTo("Tapiocaria da Maria"))
            .body("status",   equalTo("FECHADO"))
            .body("endereco.cidade", equalTo("Juazeiro do Norte"));
    }

    @Test
    @Order(3)
    void deveRetornar400QuandoDadosInvalidos() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post(BASE)
        .then()
            .statusCode(400);
    }

    @Test
    @Order(4)
    void deveListarApenasAbertos() {
        given()
            .queryParam("abertos", true)
        .when()
            .get(BASE)
        .then()
            .statusCode(200)
            .body("status", everyItem(equalTo("ABERTO")));
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
