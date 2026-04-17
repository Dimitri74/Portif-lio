package br.com.florinda.mcp;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class McpServerTest {

    @Test
    void healthCheckDeveEstarOk() {
        given()
            .when().get("/q/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    void endpointSseDeveEstarAcessivel() {
        // Verifica que o endpoint SSE do MCP está disponível
        given()
            .when().get("/mcp/sse")
            .then()
            // SSE retorna 200 com content-type text/event-stream
            .statusCode(200);
    }
}
