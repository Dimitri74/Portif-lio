package br.com.florinda.pedidos.mapper;

import br.com.florinda.pedidos.domain.ItemPedido;
import br.com.florinda.pedidos.domain.Pedido;
import br.com.florinda.pedidos.dto.PedidoDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PedidoMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "status",       ignore = true)
    @Mapping(target = "valorTotal",   ignore = true)
    @Mapping(target = "criadoEm",     ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    @Mapping(target = "historico",    ignore = true)
    @Mapping(target = "itens",        ignore = true)
    Pedido toEntity(PedidoDTO.CriarPedidoRequest request);

    PedidoDTO.PedidoResponse toResponse(Pedido pedido);

    PedidoDTO.PedidoResumoResponse toResumoResponse(Pedido pedido);

    List<PedidoDTO.PedidoResumoResponse> toResumoResponseList(List<Pedido> pedidos);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "pedido",  ignore = true)
    @Mapping(target = "subtotal",ignore = true)
    ItemPedido toItemEntity(PedidoDTO.ItemPedidoRequest request);

    PedidoDTO.ItemPedidoResponse toItemResponse(ItemPedido item);
}
