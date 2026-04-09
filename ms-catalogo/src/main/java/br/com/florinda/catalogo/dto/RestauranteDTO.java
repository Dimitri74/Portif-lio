package br.com.florinda.catalogo.dto;

import br.com.florinda.catalogo.domain.StatusRestaurante;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public class RestauranteDTO {

    // -----------------------------------------------------------
    // Request — criação
    // -----------------------------------------------------------
    public record CriarRestauranteRequest(
            @NotBlank @Size(max = 150)
            String nome,

            @Size(max = 500)
            String descricao,

            @NotBlank @Size(max = 50)
            String categoria,

            @Size(max = 20)
            String telefone,

            @Email @Size(max = 100)
            String email,

            @Valid @NotNull
            EnderecoRequest endereco,

            LocalTime horarioAbertura,
            LocalTime horarioFechamento
    ) {}

    // -----------------------------------------------------------
    // Request — atualização parcial
    // -----------------------------------------------------------
    public record AtualizarRestauranteRequest(
            @Size(max = 150)
            String nome,

            @Size(max = 500)
            String descricao,

            @Size(max = 50)
            String categoria,

            @Size(max = 20)
            String telefone,

            @Email @Size(max = 100)
            String email,

            @Valid
            EnderecoRequest endereco,

            LocalTime horarioAbertura,
            LocalTime horarioFechamento
    ) {}

    // -----------------------------------------------------------
    // Request — endereço embutido
    // -----------------------------------------------------------
    public record EnderecoRequest(
            @NotBlank @Size(max = 200) String logradouro,
            @NotBlank @Size(max = 10)  String numero,
            @NotBlank @Size(max = 100) String bairro,
            @NotBlank @Size(max = 100) String cidade,
            @NotBlank @Size(min = 2, max = 2) String uf,
            @NotBlank @Pattern(regexp = "\\d{5}-\\d{3}") String cep
    ) {}

    // -----------------------------------------------------------
    // Response — restaurante completo
    // -----------------------------------------------------------
    public record RestauranteResponse(
            UUID id,
            String nome,
            String descricao,
            String categoria,
            StatusRestaurante status,
            String telefone,
            String email,
            EnderecoResponse endereco,
            LocalTime horarioAbertura,
            LocalTime horarioFechamento,
            OffsetDateTime criadoEm,
            OffsetDateTime atualizadoEm
    ) {}

    // -----------------------------------------------------------
    // Response — endereço
    // -----------------------------------------------------------
    public record EnderecoResponse(
            String logradouro,
            String numero,
            String bairro,
            String cidade,
            String uf,
            String cep
    ) {}

    // -----------------------------------------------------------
    // Response — listagem resumida
    // -----------------------------------------------------------
    public record RestauranteResumoResponse(
            UUID id,
            String nome,
            String categoria,
            StatusRestaurante status,
            String cidade
    ) {}
}
