'use client'

import { useEffect, useState, useCallback } from 'react'
import {
  Plus,
  Edit2,
  Trash2,
  Power,
  PowerOff,
  ChevronDown,
  ChevronUp,
  Store,
  UtensilsCrossed,
  RefreshCw,
  Pause,
} from 'lucide-react'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Modal } from '@/components/ui/Modal'
import { PageLoader, EmptyState } from '@/components/ui/LoadingSpinner'
import RestaurantForm from '@/components/admin/RestaurantForm'
import ItemForm from '@/components/admin/ItemForm'
import { restaurantesApi, itensApi } from '@/lib/api'
import {
  STATUS_RESTAURANTE_LABELS,
  getStatusRestauranteClass,
  formatCurrency,
} from '@/lib/utils'
import type {
  RestauranteResponse,
  ItemCardapioResponse,
} from '@/types'

// Seed cardapio IDs para demo
const SEED_CARDAPIOS: Record<string, string> = {
  'a1b2c3d4-0000-0000-0000-000000000001': 'b1b2c3d4-0000-0000-0000-000000000001',
  'a1b2c3d4-0000-0000-0000-000000000002': 'b1b2c3d4-0000-0000-0000-000000000002',
  'a1b2c3d4-0000-0000-0000-000000000003': 'b1b2c3d4-0000-0000-0000-000000000003',
}

type AdminTab = 'restaurantes' | 'cardapio'

export default function AdminPage() {
  const [tab, setTab] = useState<AdminTab>('restaurantes')
  const [restaurantes, setRestaurantes] = useState<RestauranteResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [expanded, setExpanded] = useState<string | null>(null)
  const [items, setItems] = useState<Record<string, ItemCardapioResponse[]>>({})
  const [loadingItems, setLoadingItems] = useState<Record<string, boolean>>({})

  // Modal states
  const [modalRestaurante, setModalRestaurante] = useState(false)
  const [editingRestaurante, setEditingRestaurante] = useState<RestauranteResponse | null>(null)
  const [modalItem, setModalItem] = useState<{ restauranteId: string; cardapioId: string } | null>(null)
  const [editingItem, setEditingItem] = useState<ItemCardapioResponse | null>(null)
  const [deleteConfirm, setDeleteConfirm] = useState<{ type: 'restaurante' | 'item'; id: string; extra?: string } | null>(null)
  const [actionLoading, setActionLoading] = useState<string | null>(null)
  const [resolvingCardapio, setResolvingCardapio] = useState<string | null>(null)

  const loadRestaurantes = useCallback(async () => {
    setLoading(true)
    try {
      const list = await restaurantesApi.listar()
      // Busca dados completos de cada restaurante
      const full = await Promise.all(list.map((r) => restaurantesApi.buscar(r.id)))
      setRestaurantes(full)
    } catch {
      // silencia
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadRestaurantes()
  }, [loadRestaurantes])

  async function resolveCardapioId(restauranteId: string): Promise<string | null> {
    if (SEED_CARDAPIOS[restauranteId]) return SEED_CARDAPIOS[restauranteId]
    try {
      let cardapios = await restaurantesApi.cardapios(restauranteId)
      // Restaurante legado sem cardápio — cria automaticamente
      if (cardapios.length === 0) {
        const res = await fetch(`/api/restaurantes/${restauranteId}/cardapios`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({}),
        })
        if (res.ok) {
          const novo = await res.json()
          SEED_CARDAPIOS[restauranteId] = novo.id
          return novo.id
        }
        return null
      }
      SEED_CARDAPIOS[restauranteId] = cardapios[0].id
      return cardapios[0].id
    } catch {
      return null
    }
  }

  async function loadItems(restauranteId: string) {
    const cardapioId = await resolveCardapioId(restauranteId)
    if (!cardapioId) return

    setLoadingItems((prev) => ({ ...prev, [restauranteId]: true }))
    try {
      const its = await itensApi.listar(cardapioId)
      setItems((prev) => ({ ...prev, [restauranteId]: its }))
    } finally {
      setLoadingItems((prev) => ({ ...prev, [restauranteId]: false }))
    }
  }

  function toggleExpanded(id: string) {
    if (expanded === id) {
      setExpanded(null)
    } else {
      setExpanded(id)
      if (!items[id]) loadItems(id)
    }
  }

  async function handleAction(
    action: 'abrir' | 'fechar' | 'suspender',
    restauranteId: string,
  ) {
    setActionLoading(`${action}-${restauranteId}`)
    try {
      await restaurantesApi[action](restauranteId)
      await loadRestaurantes()
    } finally {
      setActionLoading(null)
    }
  }

  async function handleDelete() {
    if (!deleteConfirm) return
    setActionLoading('delete')
    try {
      if (deleteConfirm.type === 'restaurante') {
        await restaurantesApi.deletar(deleteConfirm.id)
        await loadRestaurantes()
      } else if (deleteConfirm.type === 'item' && deleteConfirm.extra) {
        const cardapioId = SEED_CARDAPIOS[deleteConfirm.extra] ?? ''
        if (cardapioId) {
          await itensApi.deletar(cardapioId, deleteConfirm.id)
          await loadItems(deleteConfirm.extra)
        }
      }
      setDeleteConfirm(null)
    } finally {
      setActionLoading(null)
    }
  }

  function statusVariant(status: string) {
    const m: Record<string, 'success' | 'default' | 'danger'> = {
      ABERTO: 'success',
      FECHADO: 'default',
      SUSPENSO: 'danger',
    }
    return m[status] ?? 'default'
  }

  if (loading) return <PageLoader />

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-gray-900">Administrador</h1>
          <p className="text-gray-500 text-sm mt-0.5">Gerencie restaurantes e cardápios</p>
        </div>
        <div className="flex gap-2">
          <Button variant="ghost" size="sm" onClick={loadRestaurantes}>
            <RefreshCw size={14} />
          </Button>
          <Button
            variant="primary"
            onClick={() => { setEditingRestaurante(null); setModalRestaurante(true) }}
          >
            <Plus size={16} />
            Novo Restaurante
          </Button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 bg-gray-100 rounded-xl p-1 w-fit">
        {([['restaurantes', Store, 'Restaurantes'], ['cardapio', UtensilsCrossed, 'Cardápios']] as const).map(
          ([key, Icon, label]) => (
            <button
              key={key}
              onClick={() => setTab(key)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                tab === key
                  ? 'bg-white text-red-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <Icon size={15} />
              {label}
            </button>
          ),
        )}
      </div>

      {/* Content */}
      {restaurantes.length === 0 ? (
        <EmptyState
          icon={<Store size={48} />}
          title="Nenhum restaurante cadastrado"
          description="Clique em 'Novo Restaurante' para começar"
          action={
            <Button onClick={() => setModalRestaurante(true)}>
              <Plus size={16} /> Novo Restaurante
            </Button>
          }
        />
      ) : (
        <div className="space-y-3">
          {restaurantes.map((r) => (
            <div
              key={r.id}
              className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden"
            >
              {/* Row */}
              <div className="flex items-center gap-4 p-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <h3 className="font-bold text-gray-900">{r.nome}</h3>
                    <Badge variant={statusVariant(r.status)}>
                      {STATUS_RESTAURANTE_LABELS[r.status]}
                    </Badge>
                    <Badge variant="default">{r.categoria}</Badge>
                  </div>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {r.endereco.cidade}, {r.endereco.uf} ·{' '}
                    {r.horarioAbertura} – {r.horarioFechamento}
                  </p>
                </div>

                {/* Actions */}
                <div className="flex items-center gap-1 shrink-0">
                  {r.status !== 'ABERTO' && (
                    <button
                      title="Abrir"
                      onClick={() => handleAction('abrir', r.id)}
                      disabled={actionLoading === `abrir-${r.id}`}
                      className="p-1.5 rounded-lg text-green-600 hover:bg-green-50 transition-colors"
                    >
                      <Power size={16} />
                    </button>
                  )}
                  {r.status === 'ABERTO' && (
                    <button
                      title="Fechar"
                      onClick={() => handleAction('fechar', r.id)}
                      disabled={actionLoading === `fechar-${r.id}`}
                      className="p-1.5 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors"
                    >
                      <PowerOff size={16} />
                    </button>
                  )}
                  {r.status !== 'SUSPENSO' && (
                    <button
                      title="Suspender"
                      onClick={() => handleAction('suspender', r.id)}
                      disabled={actionLoading === `suspender-${r.id}`}
                      className="p-1.5 rounded-lg text-yellow-600 hover:bg-yellow-50 transition-colors"
                    >
                      <Pause size={16} />
                    </button>
                  )}
                  <button
                    title="Editar"
                    onClick={() => { setEditingRestaurante(r); setModalRestaurante(true) }}
                    className="p-1.5 rounded-lg text-blue-600 hover:bg-blue-50 transition-colors"
                  >
                    <Edit2 size={16} />
                  </button>
                  <button
                    title="Excluir"
                    onClick={() => setDeleteConfirm({ type: 'restaurante', id: r.id })}
                    className="p-1.5 rounded-lg text-red-500 hover:bg-red-50 transition-colors"
                  >
                    <Trash2 size={16} />
                  </button>
                  {tab === 'cardapio' && (
                    <button
                      onClick={() => toggleExpanded(r.id)}
                      className="p-1.5 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors"
                    >
                      {expanded === r.id ? (
                        <ChevronUp size={16} />
                      ) : (
                        <ChevronDown size={16} />
                      )}
                    </button>
                  )}
                </div>
              </div>

              {/* Items accordion */}
              {tab === 'cardapio' && expanded === r.id && (
                <div className="border-t border-gray-100 p-4 bg-gray-50">
                  <div className="flex items-center justify-between mb-3">
                    <p className="text-sm font-bold text-gray-700">
                      Itens do Cardápio
                    </p>
                    <Button
                      size="sm"
                      variant="secondary"
                      disabled={resolvingCardapio === r.id}
                      onClick={async () => {
                        setResolvingCardapio(r.id)
                        const cardapioId = await resolveCardapioId(r.id)
                        setResolvingCardapio(null)
                        if (!cardapioId) {
                          console.error('Falha ao obter/criar cardápio para', r.id)
                          return
                        }
                        setModalItem({ restauranteId: r.id, cardapioId })
                        setEditingItem(null)
                      }}
                    >
                      <Plus size={13} />
                      {resolvingCardapio === r.id ? 'Aguarde...' : 'Novo Item'}
                    </Button>
                  </div>

                  {loadingItems[r.id] ? (
                    <div className="py-4 text-center"><RefreshCw size={18} className="animate-spin text-gray-400 mx-auto" /></div>
                  ) : !items[r.id] || items[r.id].length === 0 ? (
                    <p className="text-xs text-gray-400 text-center py-4">
                      Nenhum item cadastrado
                    </p>
                  ) : (
                    <div className="grid gap-2">
                      {items[r.id].map((item) => (
                        <div
                          key={item.id}
                          className="flex items-center gap-3 bg-white rounded-xl p-3 border border-gray-100"
                        >
                          {/* Thumbnail */}
                          {item.fotoUrl ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img
                              src={item.fotoUrl}
                              alt={item.nome}
                              className="h-12 w-12 rounded-lg object-cover bg-gray-100 shrink-0"
                              onError={(e) => {
                                (e.currentTarget as HTMLImageElement).style.display = 'none'
                              }}
                            />
                          ) : (
                            <div className="h-12 w-12 rounded-lg bg-gray-100 flex items-center justify-center shrink-0">
                              <UtensilsCrossed size={20} className="text-gray-300" />
                            </div>
                          )}
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-semibold text-gray-900 truncate">
                              {item.nome}
                            </p>
                            <p className="text-xs text-gray-500 truncate">
                              {item.descricao}
                            </p>
                            <p className="text-xs font-bold text-red-600 mt-0.5">
                              {formatCurrency(item.preco)}
                            </p>
                          </div>
                          <Badge variant={item.disponivel ? 'success' : 'default'}>
                            {item.disponivel ? 'Disponível' : 'Indisponível'}
                          </Badge>
                          <div className="flex gap-1">
                            <button
                              onClick={async () => {
                                setResolvingCardapio(r.id)
                                const cardapioId = await resolveCardapioId(r.id)
                                setResolvingCardapio(null)
                                if (!cardapioId) return
                                setModalItem({ restauranteId: r.id, cardapioId })
                                setEditingItem(item)
                              }}
                              className="p-1.5 rounded-lg text-blue-500 hover:bg-blue-50 transition-colors"
                            >
                              <Edit2 size={14} />
                            </button>
                            <button
                              onClick={() =>
                                setDeleteConfirm({
                                  type: 'item',
                                  id: item.id,
                                  extra: r.id,
                                })
                              }
                              className="p-1.5 rounded-lg text-red-500 hover:bg-red-50 transition-colors"
                            >
                              <Trash2 size={14} />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Modal: Restaurante */}
      <Modal
        open={modalRestaurante}
        onClose={() => setModalRestaurante(false)}
        title={editingRestaurante ? 'Editar Restaurante' : 'Novo Restaurante'}
        size="xl"
      >
        <RestaurantForm
          initial={editingRestaurante ?? undefined}
          onSuccess={() => {
            setModalRestaurante(false)
            loadRestaurantes()
          }}
          onCancel={() => setModalRestaurante(false)}
        />
      </Modal>

      {/* Modal: Item */}
      {modalItem && (
        <Modal
          open
          onClose={() => { setModalItem(null); setEditingItem(null) }}
          title={editingItem ? 'Editar Item' : 'Novo Item do Cardápio'}
          size="md"
        >
          <ItemForm
            cardapioId={modalItem.cardapioId}
            initial={editingItem ?? undefined}
            onSuccess={() => {
              setModalItem(null)
              setEditingItem(null)
              loadItems(modalItem.restauranteId)
            }}
            onCancel={() => { setModalItem(null); setEditingItem(null) }}
          />
        </Modal>
      )}

      {/* Delete confirm */}
      <Modal
        open={Boolean(deleteConfirm)}
        onClose={() => setDeleteConfirm(null)}
        title="Confirmar Exclusão"
        size="sm"
      >
        <div className="space-y-4">
          <p className="text-gray-700">
            Tem certeza que deseja excluir este{' '}
            {deleteConfirm?.type === 'restaurante' ? 'restaurante' : 'item'}?
          </p>
          <p className="text-sm text-red-600 bg-red-50 rounded-lg px-3 py-2">
            Esta ação não pode ser desfeita.
          </p>
          <div className="flex gap-3">
            <Button
              variant="ghost"
              onClick={() => setDeleteConfirm(null)}
              className="flex-1"
            >
              Cancelar
            </Button>
            <Button
              variant="danger"
              onClick={handleDelete}
              loading={actionLoading === 'delete'}
              className="flex-1"
            >
              Excluir
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
