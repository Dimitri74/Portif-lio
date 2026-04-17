import type { StatusPedido, StatusPagamento, StatusRestaurante, MetodoPagamento } from '@/types'

// ============================================================
// Formatação monetária
// ============================================================
export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value)
}

// ============================================================
// Formatação de datas
// ============================================================
export function formatDate(iso: string): string {
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(iso))
}

export function formatDateShort(iso: string): string {
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(new Date(iso))
}

export function formatTime(iso: string): string {
  return new Intl.DateTimeFormat('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(iso))
}

// ============================================================
// Labels de enums
// ============================================================
export const STATUS_PEDIDO_LABELS: Record<StatusPedido, string> = {
  PENDENTE: 'Pendente',
  CONFIRMADO: 'Confirmado',
  PREPARANDO: 'Preparando',
  SAIU_PARA_ENTREGA: 'Saiu para Entrega',
  ENTREGUE: 'Entregue',
  CANCELADO: 'Cancelado',
}

export const STATUS_PEDIDO_COLORS: Record<StatusPedido, string> = {
  PENDENTE: '#F59E0B',
  CONFIRMADO: '#3B82F6',
  PREPARANDO: '#F97316',
  SAIU_PARA_ENTREGA: '#8B5CF6',
  ENTREGUE: '#10B981',
  CANCELADO: '#EF4444',
}

export const STATUS_PAGAMENTO_LABELS: Record<StatusPagamento, string> = {
  PENDENTE: 'Pendente',
  PROCESSANDO: 'Processando',
  APROVADO: 'Aprovado',
  REJEITADO: 'Rejeitado',
  ESTORNADO: 'Estornado',
}

export const STATUS_RESTAURANTE_LABELS: Record<StatusRestaurante, string> = {
  ABERTO: 'Aberto',
  FECHADO: 'Fechado',
  SUSPENSO: 'Suspenso',
}

export const METODO_PAGAMENTO_LABELS: Record<MetodoPagamento, string> = {
  CARTAO_CREDITO: 'Cartão de Crédito',
  CARTAO_DEBITO: 'Cartão de Débito',
  PIX: 'PIX',
  VALE_REFEICAO: 'Vale Refeição',
}

// ============================================================
// CSS classes por status
// ============================================================
export function getStatusPedidoClass(status: StatusPedido): string {
  const map: Record<StatusPedido, string> = {
    PENDENTE: 'bg-yellow-100 text-yellow-800',
    CONFIRMADO: 'bg-blue-100 text-blue-800',
    PREPARANDO: 'bg-orange-100 text-orange-800',
    SAIU_PARA_ENTREGA: 'bg-purple-100 text-purple-800',
    ENTREGUE: 'bg-green-100 text-green-800',
    CANCELADO: 'bg-red-100 text-red-800',
  }
  return map[status] ?? 'bg-gray-100 text-gray-800'
}

export function getStatusRestauranteClass(status: StatusRestaurante): string {
  const map: Record<StatusRestaurante, string> = {
    ABERTO: 'bg-green-100 text-green-800',
    FECHADO: 'bg-gray-100 text-gray-700',
    SUSPENSO: 'bg-red-100 text-red-800',
  }
  return map[status] ?? 'bg-gray-100 text-gray-800'
}

export function getStatusPagamentoClass(status: StatusPagamento): string {
  const map: Record<StatusPagamento, string> = {
    PENDENTE: 'bg-yellow-100 text-yellow-800',
    PROCESSANDO: 'bg-blue-100 text-blue-800',
    APROVADO: 'bg-green-100 text-green-800',
    REJEITADO: 'bg-red-100 text-red-800',
    ESTORNADO: 'bg-gray-100 text-gray-700',
  }
  return map[status] ?? 'bg-gray-100 text-gray-800'
}

// ============================================================
// Misc
// ============================================================
export function cn(...classes: (string | undefined | null | false)[]): string {
  return classes.filter(Boolean).join(' ')
}

export function generateClienteId(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export function getOrCreateClienteId(): string {
  if (typeof window === 'undefined') return '11111111-0000-0000-0000-000000000001'
  const stored = localStorage.getItem('florinda_cliente_id')
  if (stored) return stored
  const id = generateClienteId()
  localStorage.setItem('florinda_cliente_id', id)
  return id
}
