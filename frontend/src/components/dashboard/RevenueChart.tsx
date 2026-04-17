'use client'

import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'
import { formatCurrency } from '@/lib/utils'

interface RevenueData {
  dia: string
  valor: number
}

interface RevenueChartProps {
  data: RevenueData[]
}

function CustomTooltip({
  active,
  payload,
  label,
}: {
  active?: boolean
  payload?: { value: number }[]
  label?: string
}) {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-white border border-gray-200 rounded-xl shadow-lg px-4 py-3">
      <p className="text-sm font-semibold text-gray-700">{label}</p>
      <p className="text-base font-black text-red-600">
        {formatCurrency(payload[0].value)}
      </p>
    </div>
  )
}

export default function RevenueChart({ data }: RevenueChartProps) {
  return (
    <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
      <h3 className="font-bold text-gray-900 mb-4">Receita Semanal</h3>
      {data.length === 0 ? (
        <div className="flex items-center justify-center h-48 text-gray-400">
          <p className="text-sm">Sem dados de receita</p>
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={data} margin={{ top: 0, right: 0, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#F3F4F6" />
            <XAxis
              dataKey="dia"
              tick={{ fontSize: 11, fill: '#9CA3AF' }}
              axisLine={false}
              tickLine={false}
            />
            <YAxis
              tick={{ fontSize: 11, fill: '#9CA3AF' }}
              axisLine={false}
              tickLine={false}
              tickFormatter={(v: number) => `R$${v}`}
            />
            <Tooltip content={<CustomTooltip />} />
            <Bar
              dataKey="valor"
              fill="#DC2626"
              radius={[6, 6, 0, 0]}
              maxBarSize={40}
            />
          </BarChart>
        </ResponsiveContainer>
      )}
    </div>
  )
}
