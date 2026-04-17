package br.com.florinda.catalogo.service;

import br.com.florinda.catalogo.domain.Cardapio;
import br.com.florinda.catalogo.domain.Restaurante;
import br.com.florinda.catalogo.domain.StatusRestaurante;
import br.com.florinda.catalogo.dto.RestauranteDTO;
import br.com.florinda.catalogo.mapper.CatalogoMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class RestauranteService {

    private static final Logger LOG = Logger.getLogger(RestauranteService.class);
    private static final String CACHE_ABERTOS_KEY = "restaurantes:abertos";
    private static final int CACHE_TTL_SEGUNDOS = 300; // 5 minutos

    @Inject
    CatalogoMapper mapper;

    private final ValueCommands<String, String> redis;

    @Inject
    public RestauranteService(RedisDataSource redisDataSource) {
        this.redis = redisDataSource.value(String.class);
    }

    // -----------------------------------------------------------
    // Listagem
    // -----------------------------------------------------------

    public List<RestauranteDTO.RestauranteResumoResponse> listarTodos() {
        return mapper.toResumoResponseList(Restaurante.listAll());
    }

    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 0.5,
                    delay = 10, delayUnit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "listarAbertosDosBanco")
    public List<RestauranteDTO.RestauranteResumoResponse> listarAbertos() {
        String cached = redis.get(CACHE_ABERTOS_KEY);
        if (cached != null) {
            LOG.debug("Cache hit para restaurantes abertos");
        }
        return listarAbertosDosBanco();
    }

    public List<RestauranteDTO.RestauranteResumoResponse> listarAbertosDosBanco() {
        LOG.debug("Consultando restaurantes abertos no banco");
        return mapper.toResumoResponseList(Restaurante.findAbertos());
    }

    public List<RestauranteDTO.RestauranteResumoResponse> listarPorCategoria(String categoria) {
        return mapper.toResumoResponseList(Restaurante.findByCategoria(categoria));
    }

    public RestauranteDTO.RestauranteResponse buscarPorId(UUID id) {
        Restaurante restaurante = Restaurante.findById(id);
        if (restaurante == null) {
            throw new NotFoundException("Restaurante não encontrado: " + id);
        }
        return mapper.toResponse(restaurante);
    }

    // -----------------------------------------------------------
    // Escrita
    // -----------------------------------------------------------

    @Transactional
    public RestauranteDTO.RestauranteResponse criar(
            RestauranteDTO.CriarRestauranteRequest request) {
        Restaurante restaurante = mapper.toEntity(request);
        restaurante.persist();

        // Cria automaticamente um cardápio padrão para o restaurante
        Cardapio cardapio = new Cardapio();
        cardapio.restaurante = restaurante;
        cardapio.nome = "Cardápio de " + restaurante.nome;
        cardapio.descricao = "Cardápio principal";
        cardapio.ativo = true;
        cardapio.persist();

        LOG.infof("Restaurante criado: %s [%s] com cardápio [%s]",
                  restaurante.nome, restaurante.id, cardapio.id);
        return mapper.toResponse(restaurante);
    }

    public List<br.com.florinda.catalogo.dto.CardapioDTO.CardapioResponse> listarCardapios(UUID restauranteId) {
        Restaurante restaurante = buscarEntidade(restauranteId);
        return Cardapio.findByRestaurante(restaurante.id)
                .stream()
                .map(c -> new br.com.florinda.catalogo.dto.CardapioDTO.CardapioResponse(
                        c.id, c.nome, c.descricao, c.ativo, c.criadoEm))
                .toList();
    }

    @Transactional
    public br.com.florinda.catalogo.dto.CardapioDTO.CardapioResponse criarCardapio(
            UUID restauranteId,
            br.com.florinda.catalogo.dto.CardapioDTO.CriarCardapioRequest request) {
        Restaurante restaurante = buscarEntidade(restauranteId);
        Cardapio cardapio = new Cardapio();
        cardapio.restaurante = restaurante;
        cardapio.nome = request != null && request.nome() != null
                ? request.nome() : "Cardápio de " + restaurante.nome;
        cardapio.descricao = request != null ? request.descricao() : "Cardápio principal";
        cardapio.ativo = true;
        cardapio.persist();
        LOG.infof("Cardápio criado para restaurante %s: [%s]", restauranteId, cardapio.id);
        return new br.com.florinda.catalogo.dto.CardapioDTO.CardapioResponse(
                cardapio.id, cardapio.nome, cardapio.descricao, cardapio.ativo, cardapio.criadoEm);
    }

    @Transactional
    public RestauranteDTO.RestauranteResponse atualizar(
            UUID id, RestauranteDTO.AtualizarRestauranteRequest request) {
        Restaurante restaurante = buscarEntidade(id);
        mapper.updateFromRequest(request, restaurante);
        invalidarCache();
        return mapper.toResponse(restaurante);
    }

    @Transactional
    public void abrir(UUID id) {
        Restaurante restaurante = buscarEntidade(id);
        restaurante.abrir();
        invalidarCache();
        LOG.infof("Restaurante aberto: %s", id);
    }

    @Transactional
    public void fechar(UUID id) {
        Restaurante restaurante = buscarEntidade(id);
        restaurante.fechar();
        invalidarCache();
        LOG.infof("Restaurante fechado: %s", id);
    }

    @Transactional
    public void suspender(UUID id) {
        Restaurante restaurante = buscarEntidade(id);
        restaurante.suspender();
        invalidarCache();
        LOG.infof("Restaurante suspenso: %s", id);
    }

    @Transactional
    public void deletar(UUID id) {
        Restaurante restaurante = buscarEntidade(id);
        restaurante.delete();
        invalidarCache();
        LOG.infof("Restaurante removido: %s", id);
    }

    // -----------------------------------------------------------
    // Helpers privados
    // -----------------------------------------------------------

    private Restaurante buscarEntidade(UUID id) {
        Restaurante restaurante = Restaurante.findById(id);
        if (restaurante == null) {
            throw new NotFoundException("Restaurante não encontrado: " + id);
        }
        return restaurante;
    }

    private void invalidarCache() {
        try {
            redis.getdel(CACHE_ABERTOS_KEY);
            LOG.debug("Cache de restaurantes abertos invalidado");
        } catch (Exception e) {
            LOG.warnf("Falha ao invalidar cache Redis: %s", e.getMessage());
        }
    }
}
