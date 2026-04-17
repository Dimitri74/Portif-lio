'use client'

import { useEffect, useState, useCallback } from 'react'
import { Search, ShoppingCart, ArrowLeft, X } from 'lucide-react'
import RestaurantCard from '@/components/pedidos/RestaurantCard'
import MenuItemCard from '@/components/pedidos/MenuItemCard'
import CartDrawer from '@/components/pedidos/CartDrawer'
import CheckoutForm from '@/components/pedidos/CheckoutForm'
import OrderTracking from '@/components/pedidos/OrderTracking'
import { Modal } from '@/components/ui/Modal'
import { PageLoader } from '@/components/ui/LoadingSpinner'
import { Button } from '@/components/ui/Button'
import { restaurantesApi, itensApi } from '@/lib/api'
import { useCart } from '@/hooks/useCart'
import type {
  RestauranteResumoResponse,
  ItemCardapioResponse,
  PedidoResponse,
} from '@/types'

type Step = 'restaurants' | 'menu' | 'checkout' | 'tracking'

// IDs fixos do seed para demo
const SEED_CARDAPIOS: Record<string, string> = {
  'a1b2c3d4-0000-0000-0000-000000000001': 'b1b2c3d4-0000-0000-0000-000000000001',
  'a1b2c3d4-0000-0000-0000-000000000002': 'b1b2c3d4-0000-0000-0000-000000000002',
  'a1b2c3d4-0000-0000-0000-000000000003': 'b1b2c3d4-0000-0000-0000-000000000003',
}

export default function PedidosPage() {
  const [step, setStep] = useState<Step>('restaurants')
  const [search, setSearch] = useState('')
  const [restaurantes, setRestaurantes] = useState<RestauranteResumoResponse[]>([])
  const [selectedRestaurante, setSelectedRestaurante] = useState<RestauranteResumoResponse | null>(null)
  const [menuItems, setMenuItems] = useState<ItemCardapioResponse[]>([])
  const [loadingRestaurantes, setLoadingRestaurantes] = useState(true)
  const [loadingMenu, setLoadingMenu] = useState(false)
  const [cartOpen, setCartOpen] = useState(false)
  const [activePedido, setActivePedido] = useState<PedidoResponse | null>(null)

  const { cart, addItem, removeItem, updateQuantity, clearCart, total, itemCount } = useCart()

  useEffect(() => {
    restaurantesApi.listar().then(setRestaurantes).finally(() => setLoadingRestaurantes(false))
  }, [])

  const handleSelectRestaurante = useCallback(async (id: string) => {
    const r = restaurantes.find((x) => x.id === id)
    if (!r) return
    setSelectedRestaurante(r)
    setStep('menu')
    setLoadingMenu(true)

    try {
      // Tenta buscar cardapios do restaurante
      let cardapioId: string | null = SEED_CARDAPIOS[id] ?? null

      if (!cardapioId) {
        // Tenta a API de cardapios
        try {
          const cardapios = await restaurantesApi.cardapios(id)
          if (cardapios.length > 0) cardapioId = cardapios[0].id
        } catch {
          // ignora
        }
      }

      if (cardapioId) {
        const items = await itensApi.listar(cardapioId)
        setMenuItems(items)
      } else {
        setMenuItems([])
      }
    } catch {
      setMenuItems([])
    } finally {
      setLoadingMenu(false)
    }
  }, [restaurantes])

  function handleAddToCart(item: ItemCardapioResponse) {
    if (!selectedRestaurante) return
    addItem(selectedRestaurante.id, selectedRestaurante.nome, {
      itemId: item.id,
      nomeItem: item.nome,
      precoUnitario: item.preco,
      fotoUrl: item.fotoUrl,
    })
    setCartOpen(true)
  }

  function handleOrderSuccess(pedido: PedidoResponse) {
    clearCart()
    setActivePedido(pedido)
    setCartOpen(false)
    setStep('tracking')
  }

  const filtered = restaurantes.filter(
    (r) =>
      r.nome.toLowerCase().includes(search.toLowerCase()) ||
      r.categoria.toLowerCase().includes(search.toLowerCase()),
  )

  // ======================================================
  // STEP: tracking
  // ======================================================
  if (step === 'tracking' && activePedido) {
    return (
      <div className="max-w-lg mx-auto animate-fade-in">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h2 className="text-xl font-black text-gray-900 mb-6">
            🎉 Pedido Realizado!
          </h2>
          <OrderTracking
            pedidoId={activePedido.id}
            onClose={() => {
              setStep('restaurants')
              setActivePedido(null)
            }}
          />
        </div>
      </div>
    )
  }

  // ======================================================
  // STEP: checkout
  // ======================================================
  if (step === 'checkout' && cart) {
    return (
      <div className="max-w-lg mx-auto animate-fade-in">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="flex items-center gap-3 mb-6">
            <button
              onClick={() => setStep('menu')}
              className="p-1.5 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <ArrowLeft size={20} className="text-gray-600" />
            </button>
            <h2 className="text-xl font-black text-gray-900">Finalizar Pedido</h2>
          </div>
          <CheckoutForm
            cart={cart}
            total={total}
            onSuccess={handleOrderSuccess}
            onCancel={() => setStep('menu')}
          />
        </div>
      </div>
    )
  }

  // ======================================================
  // STEP: menu
  // ======================================================
  if (step === 'menu' && selectedRestaurante) {
    return (
      <div className="animate-fade-in">
        {/* Restaurant header */}
        <div className="flex items-center gap-4 mb-6">
          <button
            onClick={() => { setStep('restaurants'); setSelectedRestaurante(null); setMenuItems([]) }}
            className="p-2 rounded-xl hover:bg-gray-100 transition-colors"
          >
            <ArrowLeft size={20} className="text-gray-700" />
          </button>
          <div>
            <h2 className="text-xl font-black text-gray-900">{selectedRestaurante.nome}</h2>
            <p className="text-sm text-gray-500">{selectedRestaurante.categoria} · {selectedRestaurante.cidade}</p>
          </div>

          {/* Cart button */}
          {itemCount > 0 && (
            <button
              onClick={() => setCartOpen(true)}
              className="ml-auto flex items-center gap-2 bg-red-600 text-white px-4 py-2 rounded-xl font-semibold text-sm hover:bg-red-700 transition-colors"
            >
              <ShoppingCart size={16} />
              <span>{itemCount}</span>
            </button>
          )}
        </div>

        {loadingMenu ? (
          <PageLoader />
        ) : menuItems.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-4xl mb-4">🍽️</p>
            <p className="font-semibold">Cardápio não disponível</p>
            <p className="text-sm mt-1">Nenhum item cadastrado ainda</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {menuItems.map((item) => (
              <MenuItemCard
                key={item.id}
                item={item}
                onAdd={handleAddToCart}
              />
            ))}
          </div>
        )}

        {/* Cart drawer */}
        <CartDrawer
          open={cartOpen}
          onClose={() => setCartOpen(false)}
          cart={cart}
          total={total}
          onUpdateQty={updateQuantity}
          onRemove={removeItem}
          onCheckout={() => { setCartOpen(false); setStep('checkout') }}
        />
      </div>
    )
  }

  // ======================================================
  // STEP: restaurants
  // ======================================================
  return (
    <div className="animate-fade-in space-y-6">
      {/* Hero */}
      <div className="relative overflow-hidden bg-gradient-to-r from-red-700 to-orange-500 rounded-2xl p-6 text-white">
        <div className="relative z-10">
          <h2 className="text-2xl font-black mb-1">O que você quer comer hoje? 🍽️</h2>
          <p className="text-red-100 text-sm">Escolha um restaurante e monte seu pedido</p>
        </div>
        <div className="absolute right-4 top-1/2 -translate-y-1/2 text-7xl opacity-20">
          🍴
        </div>
      </div>

      {/* Search */}
      <div className="relative">
        <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="Buscar restaurante ou categoria..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-11 pr-4 py-3 bg-white border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-red-500 shadow-sm"
        />
        {search && (
          <button
            onClick={() => setSearch('')}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-700"
          >
            <X size={16} />
          </button>
        )}
      </div>

      {/* Restaurants grid */}
      {loadingRestaurantes ? (
        <PageLoader />
      ) : filtered.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <p className="text-4xl mb-4">🔍</p>
          <p className="font-semibold">Nenhum restaurante encontrado</p>
        </div>
      ) : (
        <div>
          <p className="text-sm text-gray-500 mb-3">
            {filtered.length} restaurante{filtered.length !== 1 ? 's' : ''} encontrado{filtered.length !== 1 ? 's' : ''}
          </p>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {filtered.map((r) => (
              <RestaurantCard
                key={r.id}
                restaurante={r}
                onClick={handleSelectRestaurante}
              />
            ))}
          </div>
        </div>
      )}

      {/* Floating cart indicator */}
      {itemCount > 0 && (
        <div className="fixed bottom-24 right-6 z-30">
          <button
            onClick={() => setCartOpen(true)}
            className="bg-red-600 text-white px-5 py-3 rounded-2xl shadow-xl flex items-center gap-3 hover:bg-red-700 transition-colors"
          >
            <ShoppingCart size={20} />
            <span className="font-bold">{itemCount} item(s)</span>
            <span className="bg-white/20 px-2 py-0.5 rounded-full text-xs font-bold">
              Ver carrinho
            </span>
          </button>
        </div>
      )}

      {/* Cart drawer */}
      <CartDrawer
        open={cartOpen}
        onClose={() => setCartOpen(false)}
        cart={cart}
        total={total}
        onUpdateQty={updateQuantity}
        onRemove={removeItem}
        onCheckout={() => { setCartOpen(false); setStep('checkout') }}
      />
    </div>
  )
}
