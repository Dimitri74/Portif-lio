package br.com.florinda.catalogo.service;

import br.com.florinda.catalogo.domain.Cardapio;
import br.com.florinda.catalogo.domain.ItemCardapio;
import br.com.florinda.catalogo.dto.ItemCardapioDTO;
import br.com.florinda.catalogo.mapper.CatalogoMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ItemCardapioService {

    private static final Logger LOG = Logger.getLogger(ItemCardapioService.class);

    @Inject
    CatalogoMapper mapper;

    public List<ItemCardapioDTO.ItemCardapioResponse> listarPorCardapio(UUID cardapioId) {
        return mapper.toResponseList(ItemCardapio.findByCardapio(cardapioId));
    }

    public ItemCardapioDTO.ItemCardapioResponse buscarPorId(UUID id) {
        ItemCardapio item = ItemCardapio.findById(id);
        if (item == null) {
            throw new NotFoundException("Item não encontrado: " + id);
        }
        return mapper.toResponse(item);
    }

    @Transactional
    public ItemCardapioDTO.ItemCardapioResponse criar(UUID cardapioId,
            ItemCardapioDTO.CriarItemRequest request) {
        Cardapio cardapio = Cardapio.findById(cardapioId);
        if (cardapio == null) {
            throw new NotFoundException("Cardápio não encontrado: " + cardapioId);
        }
        ItemCardapio item = mapper.toEntity(request);
        item.cardapio = cardapio;
        item.persist();
        LOG.infof("Item criado: %s no cardápio %s", item.nome, cardapioId);
        return mapper.toResponse(item);
    }

    @Transactional
    public ItemCardapioDTO.ItemCardapioResponse atualizar(UUID id,
            ItemCardapioDTO.AtualizarItemRequest request) {
        ItemCardapio item = buscarEntidade(id);
        mapper.updateFromRequest(request, item);
        return mapper.toResponse(item);
    }

    @Transactional
    public void deletar(UUID id) {
        ItemCardapio item = buscarEntidade(id);
        item.delete();
        LOG.infof("Item removido: %s", id);
    }

    private ItemCardapio buscarEntidade(UUID id) {
        ItemCardapio item = ItemCardapio.findById(id);
        if (item == null) {
            throw new NotFoundException("Item não encontrado: " + id);
        }
        return item;
    }
}
