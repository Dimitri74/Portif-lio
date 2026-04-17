'use client'

import { Plus, Leaf, WheatOff, Flame } from 'lucide-react'
import type { ItemCardapioResponse } from '@/types'
import { formatCurrency } from '@/lib/utils'
import { Button } from '@/components/ui/Button'

interface MenuItemCardProps {
  item: ItemCardapioResponse
  onAdd: (item: ItemCardapioResponse) => void
}

export default function MenuItemCard({ item, onAdd }: MenuItemCardProps) {
  return (
    <div
      className={`bg-white rounded-2xl p-4 shadow-sm border border-gray-100 flex gap-3 ${
        !item.disponivel ? 'opacity-60' : ''
      }`}
    >
      {/* Emoji placeholder para imagem */}
      {item.fotoUrl ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={item.fotoUrl}
          alt={item.nome}
          className="w-20 h-20 object-cover rounded-xl shrink-0"
        />
      ) : (
        <div className="w-20 h-20 bg-orange-50 rounded-xl flex items-center justify-center shrink-0">
          <span className="text-3xl">🍽️</span>
        </div>
      )}

      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0">
            <h4 className="font-bold text-gray-900 text-sm leading-tight">{item.nome}</h4>
            {item.descricao && (
              <p className="text-xs text-gray-500 mt-0.5 line-clamp-2">
                {item.descricao}
              </p>
            )}
          </div>
        </div>

        {/* Tags */}
        <div className="flex items-center gap-1.5 mt-2">
          {item.vegetariano && (
            <span className="flex items-center gap-0.5 text-green-600 bg-green-50 px-2 py-0.5 rounded-full text-xs font-semibold">
              <Leaf size={10} />
              Veg
            </span>
          )}
          {item.semGluten && (
            <span className="flex items-center gap-0.5 text-orange-600 bg-orange-50 px-2 py-0.5 rounded-full text-xs font-semibold">
              <WheatOff size={10} />
              S/Glúten
            </span>
          )}
          {item.calorias && (
            <span className="flex items-center gap-0.5 text-gray-500 bg-gray-50 px-2 py-0.5 rounded-full text-xs">
              <Flame size={10} />
              {item.calorias}kcal
            </span>
          )}
        </div>

        <div className="flex items-center justify-between mt-3">
          <span className="font-black text-red-600 text-base">
            {formatCurrency(item.preco)}
          </span>
          {item.disponivel ? (
            <Button
              size="sm"
              variant="primary"
              onClick={() => onAdd(item)}
              className="gap-1"
            >
              <Plus size={14} />
              Adicionar
            </Button>
          ) : (
            <span className="text-xs text-gray-400 font-semibold">
              Indisponível
            </span>
          )}
        </div>
      </div>
    </div>
  )
}
