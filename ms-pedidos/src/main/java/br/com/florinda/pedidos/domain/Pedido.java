package br.com.florinda.pedidos.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
public class Pedido extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    public UUID id = UUID.randomUUID();

    @NotNull
    @Column(name = "cliente_id", nullable = false, columnDefinition = "CHAR(36)")
    public UUID clienteId;

    @NotNull
    @Column(name = "restaurante_id", nullable = false, columnDefinition = "CHAR(36)")
    public UUID restauranteId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public StatusPedido status = StatusPedido.PENDENTE;

    @NotNull
    @DecimalMin(value = "15.00", message = "Pedido mínimo de R$ 15,00")
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    public BigDecimal valorTotal;

    @Size(max = 500)
    public String observacao;

    @Size(max = 500)
    @Column(name = "endereco_entrega")
    public String enderecoEntrega;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em", nullable = false)
    public OffsetDateTime atualizadoEm = OffsetDateTime.now();

    @Valid
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    public List<ItemPedido> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    public List<HistoricoStatusPedido> historico = new ArrayList<>();

    // -----------------------------------------------------------
    // Comportamentos de domínio (RN02, RN03, RN04, RN05)
    // -----------------------------------------------------------

    public void confirmar() {
        transicionarStatus(StatusPedido.CONFIRMADO, "Pagamento aprovado");
    }

    public void iniciarPreparo() {
        transicionarStatus(StatusPedido.PREPARANDO, "Restaurante iniciou o preparo");
    }

    public void sairParaEntrega() {
        transicionarStatus(StatusPedido.SAIU_PARA_ENTREGA, "Pedido saiu para entrega");
    }

    public void entregar() {
        transicionarStatus(StatusPedido.ENTREGUE, "Pedido entregue ao cliente");
    }

    public void cancelar(String motivo) {
        if (!this.status.podeSerCancelado()) {
            throw new IllegalStateException(
                "Pedido no status " + this.status + " não pode ser cancelado. " +
                "Cancelamento permitido apenas em PENDENTE e CONFIRMADO.");
        }
        transicionarStatus(StatusPedido.CANCELADO, motivo);
    }

    public void falharPagamento() {
        transicionarStatus(StatusPedido.CANCELADO, "Pagamento recusado");
    }

    private void transicionarStatus(StatusPedido destino, String motivo) {
        if (!this.status.podeTransicionarPara(destino)) {
            throw new IllegalStateException(
                "Transição inválida: " + this.status + " → " + destino);
        }
        HistoricoStatusPedido registro = HistoricoStatusPedido.registrar(
                this, this.status, destino, motivo);
        this.historico.add(registro);
        this.status       = destino;
        this.atualizadoEm = OffsetDateTime.now();
    }

    public void calcularTotal() {
        this.valorTotal = itens.stream()
                .map(i -> i.precoUnitario.multiply(BigDecimal.valueOf(i.quantidade)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // -----------------------------------------------------------
    // Queries estáticas
    // -----------------------------------------------------------

    public static List<Pedido> findByCliente(UUID clienteId) {
        return list("clienteId", clienteId);
    }

    public static List<Pedido> findByStatus(StatusPedido status) {
        return list("status", status);
    }

    public static List<Pedido> findByRestaurante(UUID restauranteId) {
        return list("restauranteId", restauranteId);
    }
}
