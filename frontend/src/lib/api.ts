import type {
  RestauranteResponse,
  RestauranteResumoResponse,
  CriarRestauranteRequest,
  AtualizarRestauranteRequest,
  CardapioResponse,
  ItemCardapioResponse,
  CriarItemRequest,
  AtualizarItemRequest,
  PedidoResponse,
  PedidoResumoResponse,
  CriarPedidoRequest,
  CancelarPedidoRequest,
  PagamentoResponse,
  ProcessarPagamentoRequest,
  EstornoResponse,
  EstornarRequest,
  RespostaResponse,
  PerguntaRequest,
} from '@/types'

// ============================================================
// Base fetch com tratamento de erro padronizado
// ============================================================
async function apiFetch<T>(
  url: string,
  options?: RequestInit,
): Promise<T> {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  })

  if (!res.ok) {
    let message = `Erro ${res.status}: ${res.statusText}`
    try {
      const body = await res.json()
      message = body.message ?? body.error ?? message
    } catch {
      // mantém mensagem padrão
    }
    throw new Error(message)
  }

  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

// ============================================================
// Helpers de URL — proxy via Next.js API routes
// ============================================================
const BASE = '/api'

const url = {
  restaurantes: (id?: string) =>
    id ? `${BASE}/restaurantes/${id}` : `${BASE}/restaurantes`,
  restauranteAcao: (id: string, acao: string) =>
    `${BASE}/restaurantes/${id}/${acao}`,
  cardapios: (restauranteId: string) =>
    `${BASE}/restaurantes/${restauranteId}/cardapios`,
  itens: (cardapioId: string, itemId?: string) =>
    itemId
      ? `${BASE}/cardapios/${cardapioId}/itens/${itemId}`
      : `${BASE}/cardapios/${cardapioId}/itens`,
  pedidos: (id?: string) =>
    id ? `${BASE}/pedidos/${id}` : `${BASE}/pedidos`,
  pagamentos: (id?: string) =>
    id ? `${BASE}/pagamentos/${id}` : `${BASE}/pagamentos`,
  pagamentoPorPedido: (pedidoId: string) =>
    `${BASE}/pagamentos/pedido/${pedidoId}`,
  iaChat: () => `${BASE}/ia/chat`,
  iaHealth: () => `${BASE}/ia/health`,
}

// ============================================================
// Catálogo — Restaurantes
// ============================================================
export const restaurantesApi = {
  listar: (params?: { categoria?: string; abertos?: boolean }) => {
    const qs = new URLSearchParams()
    if (params?.categoria) qs.set('categoria', params.categoria)
    if (params?.abertos !== undefined) qs.set('abertos', String(params.abertos))
    const query = qs.toString() ? `?${qs.toString()}` : ''
    return apiFetch<RestauranteResumoResponse[]>(`${url.restaurantes()}${query}`)
  },

  buscar: (id: string) =>
    apiFetch<RestauranteResponse>(url.restaurantes(id)),

  criar: (data: CriarRestauranteRequest) =>
    apiFetch<RestauranteResponse>(url.restaurantes(), {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  atualizar: (id: string, data: AtualizarRestauranteRequest) =>
    apiFetch<RestauranteResponse>(url.restaurantes(id), {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  abrir: (id: string) =>
    apiFetch<void>(url.restauranteAcao(id, 'abrir'), { method: 'PUT' }),

  fechar: (id: string) =>
    apiFetch<void>(url.restauranteAcao(id, 'fechar'), { method: 'PUT' }),

  suspender: (id: string) =>
    apiFetch<void>(url.restauranteAcao(id, 'suspender'), { method: 'PUT' }),

  deletar: (id: string) =>
    apiFetch<void>(url.restaurantes(id), { method: 'DELETE' }),

  cardapios: (restauranteId: string) =>
    apiFetch<CardapioResponse[]>(url.cardapios(restauranteId)),
}

// ============================================================
// Catálogo — Itens do Cardápio
// ============================================================
export const itensApi = {
  listar: (cardapioId: string) =>
    apiFetch<ItemCardapioResponse[]>(url.itens(cardapioId)),

  buscar: (cardapioId: string, itemId: string) =>
    apiFetch<ItemCardapioResponse>(url.itens(cardapioId, itemId)),

  criar: (cardapioId: string, data: CriarItemRequest) =>
    apiFetch<ItemCardapioResponse>(url.itens(cardapioId), {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  atualizar: (cardapioId: string, itemId: string, data: AtualizarItemRequest) =>
    apiFetch<ItemCardapioResponse>(url.itens(cardapioId, itemId), {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  deletar: (cardapioId: string, itemId: string) =>
    apiFetch<void>(url.itens(cardapioId, itemId), { method: 'DELETE' }),
}

// ============================================================
// Pedidos
// ============================================================
export const pedidosApi = {
  listar: (params?: { clienteId?: string; status?: string }) => {
    const qs = new URLSearchParams()
    if (params?.clienteId) qs.set('clienteId', params.clienteId)
    if (params?.status) qs.set('status', params.status)
    const query = qs.toString() ? `?${qs.toString()}` : ''
    return apiFetch<PedidoResumoResponse[]>(`${url.pedidos()}${query}`)
  },

  buscar: (id: string) =>
    apiFetch<PedidoResponse>(url.pedidos(id)),

  criar: (data: CriarPedidoRequest) =>
    apiFetch<PedidoResponse>(url.pedidos(), {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  cancelar: (id: string, data: CancelarPedidoRequest) =>
    apiFetch<PedidoResponse>(url.pedidos(id), {
      method: 'DELETE',
      body: JSON.stringify(data),
    }),

  avancarStatus: (id: string) =>
    apiFetch<PedidoResponse>(url.pedidos(id), { method: 'PUT' }),
}

// ============================================================
// Pagamentos
// ============================================================
export const pagamentosApi = {
  listar: () => apiFetch<PagamentoResponse[]>(url.pagamentos()),

  buscar: (id: string) =>
    apiFetch<PagamentoResponse>(url.pagamentos(id)),

  buscarPorPedido: (pedidoId: string) =>
    apiFetch<PagamentoResponse>(url.pagamentoPorPedido(pedidoId)),

  processar: (data: ProcessarPagamentoRequest) =>
    apiFetch<PagamentoResponse>(url.pagamentos(), {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  estornar: (id: string, data: EstornarRequest) =>
    apiFetch<EstornoResponse>(`${url.pagamentos(id)}/estorno`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),
}

// ============================================================
// IA Suporte
// ============================================================
export const iaApi = {
  chat: (data: PerguntaRequest) =>
    apiFetch<RespostaResponse>(url.iaChat(), {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  health: () => apiFetch<{ status: string }>(url.iaHealth()),
}
