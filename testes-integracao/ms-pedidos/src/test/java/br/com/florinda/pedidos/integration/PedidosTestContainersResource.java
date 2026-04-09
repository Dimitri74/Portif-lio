package br.com.florinda.pedidos.integration;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class PedidosTestContainersResource
        implements QuarkusTestResourceLifecycleManager {

    static MySQLContainer<?> mysql =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
                    .withDatabaseName("pedidos_db")
                    .withUsername("florinda")
                    .withPassword("florinda123");

    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @Override
    public Map<String, String> start() {
        mysql.start();
        kafka.start();

        return Map.of(
            "quarkus.datasource.jdbc.url", mysql.getJdbcUrl(),
            "quarkus.datasource.username", mysql.getUsername(),
            "quarkus.datasource.password", mysql.getPassword(),
            "kafka.bootstrap.servers",     kafka.getBootstrapServers(),
            // Mantém Kafka em memória para os channels de saída nos testes
            "mp.messaging.outgoing.order-out.connector",        "smallrye-in-memory",
            "mp.messaging.outgoing.order-status-out.connector", "smallrye-in-memory",
            "mp.messaging.incoming.payment-in.connector",       "smallrye-in-memory",
            "mp.messaging.incoming.payment-failed-in.connector","smallrye-in-memory"
        );
    }

    @Override
    public void stop() {
        if (mysql.isRunning()) mysql.stop();
        if (kafka.isRunning()) kafka.stop();
    }
}
