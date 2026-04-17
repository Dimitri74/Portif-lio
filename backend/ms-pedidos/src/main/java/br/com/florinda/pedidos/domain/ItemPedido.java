package br.com.florinda.pedidos.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "itens_pedido")
public class ItemPedido extends PanacheEntityBase {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(columnDefinition = "CHAR(36)")
    public UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    public Pedido pedido;

    @NotNull
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "item_id", nullable = false, columnDefinition = "CHAR(36)")
    public UUID itemId;

    @NotBlank
    @Size(max = 150)
    @Column(name = "nome_item", nullable = false)
    public String nomeItem;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "preco_unitario", nullable = false, precision = 8, scale = 2)
    public BigDecimal precoUnitario;

    @Min(1)
    @Column(nullable = false)
    public int quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal subtotal;

    @PrePersist
    public void calcularSubtotal() {
        this.subtotal = this.precoUnitario.multiply(BigDecimal.valueOf(this.quantidade));
    }
}
