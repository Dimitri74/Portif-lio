'use client'

import { TrendingUp, TrendingDown } from 'lucide-react'

interface StatsCardProps {
  title: string
  value: string | number
  subtitle?: string
  icon: React.ReactNode
  trend?: { value: number; label: string }
  color?: 'red' | 'orange' | 'green' | 'blue' | 'purple'
}

const colorMap = {
  red: {
    bg: 'bg-red-50',
    icon: 'bg-red-100 text-red-600',
    text: 'text-red-600',
  },
  orange: {
    bg: 'bg-orange-50',
    icon: 'bg-orange-100 text-orange-600',
    text: 'text-orange-600',
  },
  green: {
    bg: 'bg-green-50',
    icon: 'bg-green-100 text-green-600',
    text: 'text-green-600',
  },
  blue: {
    bg: 'bg-blue-50',
    icon: 'bg-blue-100 text-blue-600',
    text: 'text-blue-600',
  },
  purple: {
    bg: 'bg-purple-50',
    icon: 'bg-purple-100 text-purple-600',
    text: 'text-purple-600',
  },
}

export default function StatsCard({
  title,
  value,
  subtitle,
  icon,
  trend,
  color = 'red',
}: StatsCardProps) {
  const c = colorMap[color]
  const isPositive = (trend?.value ?? 0) >= 0

  return (
    <div className={`rounded-2xl p-5 ${c.bg} border border-white/80 shadow-sm hover:shadow-md transition-shadow`}>
      <div className="flex items-start justify-between mb-3">
        <div className={`p-2.5 rounded-xl ${c.icon}`}>{icon}</div>
        {trend && (
          <div
            className={`flex items-center gap-1 text-xs font-semibold px-2 py-1 rounded-full ${
              isPositive
                ? 'bg-green-100 text-green-700'
                : 'bg-red-100 text-red-700'
            }`}
          >
            {isPositive ? <TrendingUp size={12} /> : <TrendingDown size={12} />}
            {Math.abs(trend.value)}%
          </div>
        )}
      </div>

      <div>
        <p className="text-2xl font-black text-gray-900">{value}</p>
        <p className="text-sm font-semibold text-gray-600 mt-0.5">{title}</p>
        {subtitle && <p className="text-xs text-gray-400 mt-1">{subtitle}</p>}
        {trend && (
          <p className="text-xs text-gray-400 mt-1">{trend.label}</p>
        )}
      </div>
    </div>
  )
}
