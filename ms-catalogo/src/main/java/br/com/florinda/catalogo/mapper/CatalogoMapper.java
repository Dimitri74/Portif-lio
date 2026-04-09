package br.com.florinda.catalogo.mapper;

import br.com.florinda.catalogo.domain.Endereco;
import br.com.florinda.catalogo.domain.ItemCardapio;
import br.com.florinda.catalogo.domain.Restaurante;
import br.com.florinda.catalogo.dto.ItemCardapioDTO;
import br.com.florinda.catalogo.dto.RestauranteDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CatalogoMapper {

    // -----------------------------------------------------------
    // Restaurante
    // -----------------------------------------------------------

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "cardapios",   ignore = true)
    @Mapping(target = "criadoEm",    ignore = true)
    @Mapping(target = "atualizadoEm",ignore = true)
    Restaurante toEntity(RestauranteDTO.CriarRestauranteRequest request);

    @Mapping(source = "endereco.logradouro", target = "endereco.logradouro")
    @Mapping(source = "endereco.cidade",     target = "endereco.cidade")
    RestauranteDTO.RestauranteResponse toResponse(Restaurante restaurante);

    RestauranteDTO.RestauranteResumoResponse toResumoResponse(Restaurante restaurante);

    List<RestauranteDTO.RestauranteResumoResponse> toResumoResponseList(List<Restaurante> restaurantes);

    @Mapping(target = "cidade", source = "endereco.cidade")
    RestauranteDTO.EnderecoResponse toEnderecoResponse(Endereco endereco);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "cardapios",   ignore = true)
    @Mapping(target = "criadoEm",    ignore = true)
    @Mapping(target = "atualizadoEm",ignore = true)
    void updateFromRequest(RestauranteDTO.AtualizarRestauranteRequest request,
                           @MappingTarget Restaurante restaurante);

    // -----------------------------------------------------------
    // ItemCardapio
    // -----------------------------------------------------------

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "cardapio",    ignore = true)
    @Mapping(target = "criadoEm",    ignore = true)
    @Mapping(target = "atualizadoEm",ignore = true)
    @Mapping(target = "disponivel",  constant = "true")
    ItemCardapio toEntity(ItemCardapioDTO.CriarItemRequest request);

    @Mapping(source = "cardapio.id", target = "cardapioId")
    ItemCardapioDTO.ItemCardapioResponse toResponse(ItemCardapio item);

    List<ItemCardapioDTO.ItemCardapioResponse> toResponseList(List<ItemCardapio> itens);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "cardapio",    ignore = true)
    @Mapping(target = "criadoEm",    ignore = true)
    @Mapping(target = "atualizadoEm",ignore = true)
    void updateFromRequest(ItemCardapioDTO.AtualizarItemRequest request,
                           @MappingTarget ItemCardapio item);
}
