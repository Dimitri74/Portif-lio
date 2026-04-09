package br.com.florinda.catalogo.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "itens_cardapio")
public class ItemCardapio extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cardapio_id", nullable = false)
    public Cardapio cardapio;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false)
    public String nome;

    @Size(max = 500)
    public String descricao;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    @Column(nullable = false, precision = 8, scale = 2)
    public BigDecimal preco;

    @Column(nullable = false)
    public boolean disponivel = true;

    @Size(max = 500)
    @Column(name = "foto_url")
    public String fotoUrl;

    public Integer calorias;

    @Column(nullable = false)
    public boolean vegetariano = false;

    @Column(nullable = false)
    public boolean vegano = false;

    @Column(name = "sem_gluten", nullable = false)
    public boolean semGluten = false;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em", nullable = false)
    public OffsetDateTime atualizadoEm = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
    }

    // -----------------------------------------------------------
    // Queries
    // -----------------------------------------------------------

    public static List<ItemCardapio> findByCardapio(UUID cardapioId) {
        return list("cardapio.id = ?1 and disponivel = true", cardapioId);
    }

    public static List<ItemCardapio> findVegetarianosByCardapio(UUID cardapioId) {
        return list("cardapio.id = ?1 and vegetariano = true and disponivel = true", cardapioId);
    }
}
