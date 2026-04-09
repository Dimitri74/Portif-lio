package br.com.florinda.catalogo.integration;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/**
 * Gerenciador de recursos Testcontainers para testes de integração.
 * Sobe PostgreSQL real + Kafka real antes dos testes e derruba ao final.
 *
 * Uso: anotar a classe de teste com
 *   @QuarkusTest
 *   @QuarkusTestResource(TestContainersResource.class)
 */
public class TestContainersResource
        implements QuarkusTestResourceLifecycleManager {

    // PostgreSQL — ms-catalogo
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("catalogo_db")
                    .withUsername("florinda")
                    .withPassword("florinda123");

    // Kafka — todos os testes que precisam de eventos
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    // Redis — ms-catalogo cache
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
                    .withExposedPorts(6379);

    @Override
    public Map<String, String> start() {
        postgres.start();
        kafka.start();
        redis.start();

        return Map.of(
            // DataSource
            "quarkus.datasource.jdbc.url",  postgres.getJdbcUrl(),
            "quarkus.datasource.username",  postgres.getUsername(),
            "quarkus.datasource.password",  postgres.getPassword(),
            // Kafka
            "kafka.bootstrap.servers",      kafka.getBootstrapServers(),
            // Redis
            "quarkus.redis.hosts",          "redis://" + redis.getHost()
                                            + ":" + redis.getMappedPort(6379)
        );
    }

    @Override
    public void stop() {
        if (postgres.isRunning()) postgres.stop();
        if (kafka.isRunning())    kafka.stop();
        if (redis.isRunning())    redis.stop();
    }
}
