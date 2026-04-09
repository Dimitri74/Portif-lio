package br.com.florinda.pagamentos.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "pagamentos")
public class Pagamento extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    public UUID id = UUID.randomUUID();

    // RN06: UNIQUE garante idempotência — mesmo pedidoId não gera dois pagamentos
    @NotNull
    @Column(name = "pedido_id", nullable = false, unique = true,
            columnDefinition = "CHAR(36)")
    public UUID pedidoId;

    @NotNull
    @Column(name = "cliente_id", nullable = false, columnDefinition = "CHAR(36)")
    public UUID clienteId;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal valor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public MetodoPagamento metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "gateway_id", length = 100)
    public String gatewayId;

    @Column(name = "gateway_payload", columnDefinition = "TEXT")
    public String gatewayPayload;

    @Column(nullable = false)
    public int tentativas = 0;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em", nullable = false)
    public OffsetDateTime atualizadoEm = OffsetDateTime.now();

    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Estorno> estornos = new ArrayList<>();

    // -----------------------------------------------------------
    // Comportamentos de domínio
    // -----------------------------------------------------------

    public void iniciarProcessamento() {
        if (this.status != StatusPagamento.PENDENTE) {
            throw new IllegalStateException(
                "Pagamento só pode ser processado a partir do status PENDENTE. " +
                "Status atual: " + this.status);
        }
        this.status = StatusPagamento.PROCESSANDO;
        this.tentativas++;
        this.atualizadoEm = OffsetDateTime.now();
    }

    public void aprovar(String gatewayId, String gatewayPayload) {
        this.status         = StatusPagamento.APROVADO;
        this.gatewayId      = gatewayId;
        this.gatewayPayload = gatewayPayload;
        this.atualizadoEm   = OffsetDateTime.now();
    }

    public void rejeitar(String gatewayPayload) {
        this.status         = StatusPagamento.REJEITADO;
        this.gatewayPayload = gatewayPayload;
        this.atualizadoEm   = OffsetDateTime.now();
    }

    // RN08: estorno só permitido em APROVADO
    public Estorno solicitarEstorno(String motivo) {
        if (!this.status.podeSerEstornado()) {
            throw new IllegalStateException(
                "Estorno só é possível para pagamentos APROVADOS. " +
                "Status atual: " + this.status);
        }
        Estorno estorno = new Estorno();
        estorno.pagamento = this;
        estorno.valor     = this.valor;
        estorno.motivo    = motivo;
        this.estornos.add(estorno);
        this.status       = StatusPagamento.ESTORNADO;
        this.atualizadoEm = OffsetDateTime.now();
        return estorno;
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
    }

    // -----------------------------------------------------------
    // Queries estáticas
    // -----------------------------------------------------------

    public static List<Pagamento> listarTodos() {
        return listAll();
    }

    public static Optional<Pagamento> findByPedidoId(UUID pedidoId) {
        return find("pedidoId", pedidoId).firstResultOptional();
    }

    public static List<Pagamento> findByStatus(StatusPagamento status) {
        return list("status", status);
    }

    public static List<Pagamento> findByClienteId(UUID clienteId) {
        return list("clienteId", clienteId);
    }

    // RN06: verifica se já existe pagamento para o pedido (idempotência)
    public static boolean existePagamentoPara(UUID pedidoId) {
        return count("pedidoId", pedidoId) > 0;
    }
}
