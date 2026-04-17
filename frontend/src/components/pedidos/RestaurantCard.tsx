'use client'

import { MapPin, Clock, Star } from 'lucide-react'
import type { RestauranteResumoResponse } from '@/types'
import { Badge } from '@/components/ui/Badge'

// Ícones por categoria
const categoryEmoji: Record<string, string> = {
  CHURRASCARIA: '🥩',
  PIZZARIA: '🍕',
  JAPONES: '🍱',
  HAMBURGUER: '🍔',
  SAUDAVEL: '🥗',
  LANCHES: '🥪',
  SORVETES: '🍦',
  FRUTOS_DO_MAR: '🦐',
  NORDESTINO: '🫙',
  ITALIANA: '🍝',
  MEXICANA: '🌮',
  CHINESE: '🥟',
  DEFAULT: '🍽️',
}

interface RestaurantCardProps {
  restaurante: RestauranteResumoResponse
  onClick: (id: string) => void
}

export default function RestaurantCard({
  restaurante,
  onClick,
}: RestaurantCardProps) {
  const isOpen = restaurante.status === 'ABERTO'
  const emoji =
    categoryEmoji[restaurante.categoria.toUpperCase()] ?? categoryEmoji.DEFAULT

  // Gera cor de fundo aleatória e consistente por ID
  const hue = parseInt(restaurante.id.slice(0, 2), 16) % 360

  return (
    <button
      className={`
        w-full text-left bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md
        transition-all duration-200 border border-gray-100 group
        ${!isOpen ? 'opacity-70' : ''}
      `}
      onClick={() => onClick(restaurante.id)}
      disabled={!isOpen}
    >
      {/* Cover */}
      <div
        className="h-28 flex items-center justify-center relative"
        style={{
          background: `linear-gradient(135deg, hsl(${hue}, 70%, 85%), hsl(${
            (hue + 30) % 360
          }, 70%, 75%))`,
        }}
      >
        <span className="text-5xl">{emoji}</span>
        {!isOpen && (
          <div className="absolute inset-0 bg-black/30 flex items-center justify-center">
            <span className="bg-black/60 text-white text-xs font-bold px-3 py-1 rounded-full">
              FECHADO
            </span>
          </div>
        )}
      </div>

      {/* Info */}
      <div className="p-4">
        <div className="flex items-start justify-between mb-1">
          <h3 className="font-bold text-gray-900 text-sm leading-tight group-hover:text-red-600 transition-colors line-clamp-1">
            {restaurante.nome}
          </h3>
          <div className="flex items-center gap-0.5 text-yellow-500 ml-2 shrink-0">
            <Star size={12} fill="currentColor" />
            <span className="text-xs text-gray-600 font-semibold">4.8</span>
          </div>
        </div>

        <Badge variant={isOpen ? 'orange' : 'default'} className="mb-2">
          {restaurante.categoria}
        </Badge>

        <div className="flex items-center gap-3 mt-2 text-xs text-gray-500">
          <span className="flex items-center gap-1">
            <MapPin size={11} />
            {restaurante.cidade}
          </span>
          {isOpen && (
            <span className="flex items-center gap-1 text-green-600">
              <Clock size={11} />
              30-45 min
            </span>
          )}
        </div>
      </div>
    </button>
  )
}
