// ============================================================
// Enums
// ============================================================
export type StatusRestaurante = 'ABERTO' | 'FECHADO' | 'SUSPENSO'
export type StatusPedido =
  | 'PENDENTE'
  | 'CONFIRMADO'
  | 'PREPARANDO'
  | 'SAIU_PARA_ENTREGA'
  | 'ENTREGUE'
  | 'CANCELADO'
export type StatusPagamento =
  | 'PENDENTE'
  | 'PROCESSANDO'
  | 'APROVADO'
  | 'REJEITADO'
  | 'ESTORNADO'
export type MetodoPagamento =
  | 'CARTAO_CREDITO'
  | 'CARTAO_DEBITO'
  | 'PIX'
  | 'VALE_REFEICAO'
export type StatusEstorno = 'SOLICITADO' | 'PROCESSADO' | 'FALHOU'

// ============================================================
// Catálogo — Restaurantes
// ============================================================
export interface EnderecoRequest {
  logradouro: string
  numero: string
  bairro: string
  cidade: string
  uf: string
  cep: string
}

export interface EnderecoResponse {
  logradouro: string
  numero: string
  bairro: string
  cidade: string
  uf: string
  cep: string
}

export interface CriarRestauranteRequest {
  nome: string
  descricao?: string
  categoria: string
  telefone?: string
  email?: string
  endereco: EnderecoRequest
  horarioAbertura?: string
  horarioFechamento?: string
}

export interface AtualizarRestauranteRequest {
  nome?: string
  descricao?: string
  categoria?: string
  telefone?: string
  email?: string
  endereco?: EnderecoRequest
  horarioAbertura?: string
  horarioFechamento?: string
}

export interface RestauranteResponse {
  id: string
  nome: string
  descricao?: string
  categoria: string
  status: StatusRestaurante
  telefone?: string
  email?: string
  endereco: EnderecoResponse
  horarioAbertura?: string
  horarioFechamento?: string
  criadoEm: string
  atualizadoEm: string
}

export interface RestauranteResumoResponse {
  id: string
  nome: string
  categoria: string
  status: StatusRestaurante
  cidade: string
}

// ============================================================
// Catálogo — Cardápios
// ============================================================
export interface CardapioResponse {
  id: string
  restauranteId: string
  nome: string
  descricao?: string
  ativo: boolean
  criadoEm: string
}

// ============================================================
// Catálogo — Itens do Cardápio
// ============================================================
export interface CriarItemRequest {
  nome: string
  descricao?: string
  preco: number
  fotoUrl?: string
  calorias?: number
  vegetariano?: boolean
  vegano?: boolean
  semGluten?: boolean
}

export interface AtualizarItemRequest {
  nome?: string
  descricao?: string
  preco?: number
  fotoUrl?: string
  calorias?: number
  vegetariano?: boolean
  vegano?: boolean
  semGluten?: boolean
  disponivel?: boolean
}

export interface ItemCardapioResponse {
  id: string
  cardapioId: string
  nome: string
  descricao?: string
  preco: number
  disponivel: boolean
  fotoUrl?: string
  calorias?: number
  vegetariano: boolean
  vegano: boolean
  semGluten: boolean
  criadoEm: string
}

// ============================================================
// Pedidos
// ============================================================
export interface ItemPedidoRequest {
  itemId: string
  nomeItem: string
  precoUnitario: number
  quantidade: number
}

export interface CriarPedidoRequest {
  clienteId: string
  restauranteId: string
  itens: ItemPedidoRequest[]
  observacao?: string
  enderecoEntrega?: string
}

export interface CancelarPedidoRequest {
  motivo: string
}

export interface ItemPedidoResponse {
  id: string
  itemId: string
  nomeItem: string
  precoUnitario: number
  quantidade: number
  subtotal: number
}

export interface PedidoResponse {
  id: string
  clienteId: string
  restauranteId: string
  status: StatusPedido
  valorTotal: number
  observacao?: string
  enderecoEntrega?: string
  itens: ItemPedidoResponse[]
  criadoEm: string
  atualizadoEm: string
}

export interface PedidoResumoResponse {
  id: string
  clienteId: string
  restauranteId: string
  status: StatusPedido
  valorTotal: number
  criadoEm: string
}

// ============================================================
// Pagamentos
// ============================================================
export interface ProcessarPagamentoRequest {
  pedidoId: string
  clienteId: string
  valor: number
  metodo: MetodoPagamento
}

export interface EstornarRequest {
  motivo: string
}

export interface PagamentoResponse {
  id: string
  pedidoId: string
  clienteId: string
  valor: number
  metodo: MetodoPagamento
  status: StatusPagamento
  gatewayId?: string
  tentativas: number
  criadoEm: string
  atualizadoEm: string
}

export interface EstornoResponse {
  id: string
  pagamentoId: string
  valor: number
  motivo: string
  status: StatusEstorno
  criadoEm: string
}

// ============================================================
// IA Suporte
// ============================================================
export interface PerguntaRequest {
  pergunta: string
  sessaoId?: string
  clienteId?: string
}

export interface FonteRAG {
  trecho: string
  fonte: string
  score: number
}

export interface RespostaResponse {
  resposta: string
  sessaoId: string
  fontes?: FonteRAG[]
  guardrailAtivado: boolean
  timestamp: string
}

// ============================================================
// Cart (client-side only)
// ============================================================
export interface CartItem {
  itemId: string
  nomeItem: string
  precoUnitario: number
  quantidade: number
  fotoUrl?: string
}

export interface Cart {
  restauranteId: string
  restauranteNome: string
  items: CartItem[]
}

// ============================================================
// Dashboard Stats
// ============================================================
export interface DashboardStats {
  totalPedidosHoje: number
  pedidosAbertos: number
  pedidosFechados: number
  receitaHoje: number
  restaurantesAtivos: number
  pedidosPorStatus: { status: StatusPedido; count: number }[]
  receitaSemanal: { dia: string; valor: number }[]
}
