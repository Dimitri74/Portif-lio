package br.com.florinda.pagamentos.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "estornos")
public class Estorno extends PanacheEntityBase {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(columnDefinition = "CHAR(36)")
    public UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagamento_id", nullable = false)
    public Pagamento pagamento;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal valor;

    @NotBlank
    @Column(nullable = false, length = 300)
    public String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public StatusEstorno status = StatusEstorno.SOLICITADO;

    @Column(name = "gateway_id", length = 100)
    public String gatewayId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em", nullable = false)
    public OffsetDateTime atualizadoEm = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
    }

    public enum StatusEstorno {
        SOLICITADO, PROCESSADO, FALHOU
    }
}
