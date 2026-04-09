package br.com.florinda.catalogo.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cardapios")
public class Cardapio extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    public Restaurante restaurante;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    public String nome;

    @Size(max = 300)
    public String descricao;

    @Column(nullable = false)
    public boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em", nullable = false)
    public OffsetDateTime atualizadoEm = OffsetDateTime.now();

    @OneToMany(mappedBy = "cardapio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<ItemCardapio> itens = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
    }

    public static List<Cardapio> findByRestaurante(UUID restauranteId) {
        return list("restaurante.id = ?1 and ativo = true", restauranteId);
    }
}
