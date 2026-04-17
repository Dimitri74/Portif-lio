'use client'

import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'
import type { StatusPedido } from '@/types'
import { STATUS_PEDIDO_LABELS, STATUS_PEDIDO_COLORS } from '@/lib/utils'

interface PieData {
  status: StatusPedido
  count: number
}

interface OrdersPieChartProps {
  data: PieData[]
  title?: string
}

interface TooltipPayload {
  name: string
  value: number
  payload: { status: StatusPedido }
}

function CustomTooltip({
  active,
  payload,
}: {
  active?: boolean
  payload?: TooltipPayload[]
}) {
  if (!active || !payload?.length) return null
  const item = payload[0]
  return (
    <div className="bg-white border border-gray-200 rounded-xl shadow-lg px-4 py-3">
      <p className="font-semibold text-gray-900">
        {STATUS_PEDIDO_LABELS[item.payload.status] ?? item.name}
      </p>
      <p className="text-sm text-gray-600">
        <span className="font-bold text-gray-900">{item.value}</span> pedidos
      </p>
    </div>
  )
}

export default function OrdersPieChart({ data, title = 'Pedidos por Status' }: OrdersPieChartProps) {
  const total = data.reduce((s, d) => s + d.count, 0)

  const chartData = data.map((d) => ({
    ...d,
    name: STATUS_PEDIDO_LABELS[d.status] ?? d.status,
    fill: STATUS_PEDIDO_COLORS[d.status] ?? '#9CA3AF',
  }))

  // Dados resumidos: abertos vs fechados
  const abertos = data
    .filter((d) => !['ENTREGUE', 'CANCELADO'].includes(d.status))
    .reduce((s, d) => s + d.count, 0)
  const fechados = data
    .filter((d) => ['ENTREGUE', 'CANCELADO'].includes(d.status))
    .reduce((s, d) => s + d.count, 0)

  return (
    <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
      <h3 className="font-bold text-gray-900 mb-4">{title}</h3>

      {/* Resumo abertos/fechados */}
      <div className="flex gap-4 mb-4">
        <div className="flex-1 bg-orange-50 rounded-xl p-3 text-center">
          <p className="text-2xl font-black text-orange-600">{abertos}</p>
          <p className="text-xs text-orange-500 font-semibold">Em andamento</p>
        </div>
        <div className="flex-1 bg-gray-50 rounded-xl p-3 text-center">
          <p className="text-2xl font-black text-gray-600">{fechados}</p>
          <p className="text-xs text-gray-500 font-semibold">Finalizados</p>
        </div>
        <div className="flex-1 bg-red-50 rounded-xl p-3 text-center">
          <p className="text-2xl font-black text-red-600">{total}</p>
          <p className="text-xs text-red-500 font-semibold">Total</p>
        </div>
      </div>

      {total === 0 ? (
        <div className="flex items-center justify-center h-48 text-gray-400">
          <p className="text-sm">Nenhum pedido registrado</p>
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={240}>
          <PieChart>
            <Pie
              data={chartData}
              dataKey="count"
              nameKey="name"
              cx="50%"
              cy="50%"
              outerRadius={80}
              innerRadius={40}
              paddingAngle={3}
            >
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.fill} />
              ))}
            </Pie>
            <Tooltip content={<CustomTooltip />} />
            <Legend
              iconType="circle"
              iconSize={8}
              formatter={(value) => (
                <span className="text-xs text-gray-600">{value}</span>
              )}
            />
          </PieChart>
        </ResponsiveContainer>
      )}
    </div>
  )
}
