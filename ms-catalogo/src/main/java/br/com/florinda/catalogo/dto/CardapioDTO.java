package br.com.florinda.catalogo.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CardapioDTO {

    public record CriarCardapioRequest(
            String nome,
            String descricao
    ) {}

    public record CardapioResponse(
            UUID id,
            String nome,
            String descricao,
            boolean ativo,
            OffsetDateTime criadoEm
    ) {}
}
