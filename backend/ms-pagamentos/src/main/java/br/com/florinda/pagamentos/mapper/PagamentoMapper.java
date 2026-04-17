package br.com.florinda.pagamentos.mapper;

import br.com.florinda.pagamentos.domain.Estorno;
import br.com.florinda.pagamentos.domain.Pagamento;
import br.com.florinda.pagamentos.dto.PagamentoDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PagamentoMapper {

    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "status",         ignore = true)
    @Mapping(target = "gatewayId",      ignore = true)
    @Mapping(target = "gatewayPayload", ignore = true)
    @Mapping(target = "tentativas",     ignore = true)
    @Mapping(target = "estornos",       ignore = true)
    @Mapping(target = "criadoEm",       ignore = true)
    @Mapping(target = "atualizadoEm",   ignore = true)
    Pagamento toEntity(PagamentoDTO.ProcessarPagamentoRequest request);

    PagamentoDTO.PagamentoResponse toResponse(Pagamento pagamento);

    PagamentoDTO.PagamentoResumoResponse toResumoResponse(Pagamento pagamento);

    List<PagamentoDTO.PagamentoResumoResponse> toResumoResponseList(List<Pagamento> pagamentos);

    @Mapping(source = "pagamento.id",   target = "pagamentoId")
    @Mapping(source = "status",         target = "status",
             qualifiedByName = "statusToString")
    PagamentoDTO.EstornoResponse toEstornoResponse(Estorno estorno);

    @Named("statusToString")
    default String statusToString(Estorno.StatusEstorno status) {
        return status != null ? status.name() : null;
    }
}
