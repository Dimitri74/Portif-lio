package br.com.florinda.catalogo.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ItemCardapioDTO {

    public record CriarItemRequest(
            @NotBlank @Size(max = 150)
            String nome,

            @Size(max = 500)
            String descricao,

            @NotNull
            @DecimalMin(value = "0.01")
            @Digits(integer = 6, fraction = 2)
            BigDecimal preco,

            String fotoUrl,
            Integer calorias,
            boolean vegetariano,
            boolean vegano,
            boolean semGluten
    ) {}

    public record AtualizarItemRequest(
            @Size(max = 150)  String nome,
            @Size(max = 500)  String descricao,
            @DecimalMin("0.01") @Digits(integer = 6, fraction = 2) BigDecimal preco,
            Boolean disponivel,
            String fotoUrl,
            Integer calorias,
            Boolean vegetariano,
            Boolean vegano,
            Boolean semGluten
    ) {}

    public record ItemCardapioResponse(
            UUID id,
            UUID cardapioId,
            String nome,
            String descricao,
            BigDecimal preco,
            boolean disponivel,
            String fotoUrl,
            Integer calorias,
            boolean vegetariano,
            boolean vegano,
            boolean semGluten,
            OffsetDateTime criadoEm
    ) {}
}
