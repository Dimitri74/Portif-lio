package br.com.florinda.catalogo.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurantes")
public class Restaurante extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    public UUID id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    public String nome;

    @Size(max = 500)
    @Column(length = 500)
    public String descricao;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    public String categoria;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public StatusRestaurante status = StatusRestaurante.FECHADO;

    @Size(max = 20)
    @Column(length = 20)
    public String telefone;

    @Email
    @Size(max = 100)
    @Column(length = 100)
    public String email;

    @Valid
    @NotNull
    @Embedded
    public Endereco endereco;

    @Column(name = "horario_abertura")
    public LocalTime horarioAbertura;

    @Column(name = "horario_fechamento")
    public LocalTime horarioFechamento;

    @Column(name = "criado_em", nullable = false, updatable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em", nullable = false)
    public OffsetDateTime atualizadoEm = OffsetDateTime.now();

    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Cardapio> cardapios = new ArrayList<>();

    // -----------------------------------------------------------
    // Comportamentos de domínio (DDD — lógica na entidade)
    // -----------------------------------------------------------

    public void abrir() {
        if (this.status == StatusRestaurante.SUSPENSO) {
            throw new IllegalStateException(
                "Restaurante suspenso não pode ser aberto diretamente.");
        }
        this.status = StatusRestaurante.ABERTO;
    }

    public void fechar() {
        this.status = StatusRestaurante.FECHADO;
    }

    public void suspender() {
        this.status = StatusRestaurante.SUSPENSO;
    }

    public boolean estaAberto() {
        return StatusRestaurante.ABERTO.equals(this.status);
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
    }

    // -----------------------------------------------------------
    // Queries estáticas (Panache active record pattern)
    // -----------------------------------------------------------

    public static List<Restaurante> findByStatus(StatusRestaurante status) {
        return list("status", status);
    }

    public static List<Restaurante> findByCategoria(String categoria) {
        return list("categoria", categoria);
    }

    public static List<Restaurante> findAbertos() {
        return findByStatus(StatusRestaurante.ABERTO);
    }
}
