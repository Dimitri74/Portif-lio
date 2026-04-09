package br.com.florinda.notificacoes;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class NotificacaoResourceTest {

    @Test
    void deveRetornarStatusUp() {
        given()
            .when().get("/v1/notificacoes/status")
            .then()
            .statusCode(200)
            .body("status",  equalTo("UP"))
            .body("servico", equalTo("ms-notificacoes"));
    }

    @Test
    void healthCheckDeveEstarOk() {
        given()
            .when().get("/q/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }
}
