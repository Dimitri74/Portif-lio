'use client'

import { useEffect } from 'react'
import { X, Plus, Minus, ShoppingBag, Trash2 } from 'lucide-react'
import type { Cart } from '@/types'
import { formatCurrency } from '@/lib/utils'
import { Button } from '@/components/ui/Button'

interface CartDrawerProps {
  open: boolean
  onClose: () => void
  cart: Cart | null
  total: number
  onUpdateQty: (itemId: string, qty: number) => void
  onRemove: (itemId: string) => void
  onCheckout: () => void
}

export default function CartDrawer({
  open,
  onClose,
  cart,
  total,
  onUpdateQty,
  onRemove,
  onCheckout,
}: CartDrawerProps) {
  // Trava o scroll do body quando o drawer está aberto
  useEffect(() => {
    if (open) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
    return () => {
      document.body.style.overflow = ''
    }
  }, [open])

  if (!open) return null

  return (
    <>
      {/* Overlay — cobre toda a tela incluindo a sidebar */}
      <div
        className="fixed inset-0 bg-black/50 z-[100]"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Drawer — full-screen no mobile, largura fixa no desktop */}
      <div className="fixed right-0 top-0 h-screen w-full sm:max-w-sm bg-white shadow-2xl z-[101] flex flex-col animate-slide-in">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <div className="flex items-center gap-2">
            <ShoppingBag size={20} className="text-red-600" />
            <h2 className="font-bold text-gray-900">Seu Pedido</h2>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* Restaurant name */}
        {cart && (
          <div className="px-5 py-3 bg-orange-50 border-b border-orange-100">
            <p className="text-xs text-orange-600 font-semibold">
              🍽️ {cart.restauranteNome}
            </p>
          </div>
        )}

        {/* Items — área rolável, cresce para ocupar o espaço disponível */}
        <div className="flex-1 overflow-y-auto overscroll-contain p-5 space-y-3">
          {!cart || cart.items.length === 0 ? (
            <div className="flex flex-col items-center justify-center min-h-[200px] text-center gap-4">
              <ShoppingBag size={48} className="text-gray-200" />
              <p className="text-gray-400 text-sm">Seu carrinho está vazio</p>
            </div>
          ) : (
            cart.items.map((item) => (
              <div
                key={item.itemId}
                className="flex items-center gap-3 bg-gray-50 rounded-xl p-3"
              >
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-gray-900 truncate">
                    {item.nomeItem}
                  </p>
                  <p className="text-xs text-red-600 font-bold mt-0.5">
                    {formatCurrency(item.precoUnitario * item.quantidade)}
                  </p>
                </div>

                {/* Quantity controls */}
                <div className="flex items-center gap-2 shrink-0">
                  <button
                    onClick={() =>
                      item.quantidade > 1
                        ? onUpdateQty(item.itemId, item.quantidade - 1)
                        : onRemove(item.itemId)
                    }
                    className="w-7 h-7 rounded-lg bg-white border border-gray-200 flex items-center justify-center text-gray-600 hover:border-red-400 hover:text-red-500 transition-colors"
                  >
                    {item.quantidade === 1 ? (
                      <Trash2 size={12} />
                    ) : (
                      <Minus size={12} />
                    )}
                  </button>
                  <span className="w-5 text-center text-sm font-bold text-gray-900">
                    {item.quantidade}
                  </span>
                  <button
                    onClick={() => onUpdateQty(item.itemId, item.quantidade + 1)}
                    className="w-7 h-7 rounded-lg bg-red-600 flex items-center justify-center text-white hover:bg-red-700 transition-colors"
                  >
                    <Plus size={12} />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Footer */}
        {cart && cart.items.length > 0 && (
          <div className="p-5 border-t border-gray-100 space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-gray-600 font-semibold">Total</span>
              <span className="text-xl font-black text-red-600">
                {formatCurrency(total)}
              </span>
            </div>
            <Button
              size="lg"
              variant="primary"
              className="w-full"
              onClick={onCheckout}
            >
              Finalizar Pedido
            </Button>
          </div>
        )}
      </div>
    </>
  )
}
