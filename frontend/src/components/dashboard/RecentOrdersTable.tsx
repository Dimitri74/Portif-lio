'use client'

import Link from 'next/link'
import type { PedidoResumoResponse } from '@/types'
import { formatDate, formatCurrency, STATUS_PEDIDO_LABELS, getStatusPedidoClass } from '@/lib/utils'
import { Badge } from '@/components/ui/Badge'
import { ChevronRight } from 'lucide-react'

interface RecentOrdersTableProps {
  orders: PedidoResumoResponse[]
}

function statusVariant(status: string) {
  const map: Record<string, 'warning' | 'info' | 'orange' | 'success' | 'danger' | 'default'> = {
    PENDENTE: 'warning',
    CONFIRMADO: 'info',
    PREPARANDO: 'orange',
    SAIU_PARA_ENTREGA: 'info',
    ENTREGUE: 'success',
    CANCELADO: 'danger',
  }
  return map[status] ?? 'default'
}

export default function RecentOrdersTable({ orders }: RecentOrdersTableProps) {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
      <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
        <h3 className="font-bold text-gray-900">Pedidos Recentes</h3>
        <Link
          href="/pedidos"
          className="text-xs font-semibold text-red-600 hover:text-red-700 flex items-center gap-1"
        >
          Ver todos <ChevronRight size={14} />
        </Link>
      </div>

      {orders.length === 0 ? (
        <div className="py-12 text-center text-gray-400">
          <p className="text-sm">Nenhum pedido recente</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-50">
                <th className="text-left text-xs font-semibold text-gray-500 px-5 py-3">
                  PEDIDO
                </th>
                <th className="text-left text-xs font-semibold text-gray-500 px-3 py-3 hidden md:table-cell">
                  DATA
                </th>
                <th className="text-left text-xs font-semibold text-gray-500 px-3 py-3">
                  STATUS
                </th>
                <th className="text-right text-xs font-semibold text-gray-500 px-5 py-3">
                  VALOR
                </th>
              </tr>
            </thead>
            <tbody>
              {orders.slice(0, 8).map((order) => (
                <tr
                  key={order.id}
                  className="border-b border-gray-50 hover:bg-gray-50 transition-colors"
                >
                  <td className="px-5 py-3">
                    <span className="font-mono text-xs bg-gray-100 px-2 py-1 rounded-lg text-gray-600">
                      #{order.id.slice(0, 8).toUpperCase()}
                    </span>
                  </td>
                  <td className="px-3 py-3 text-xs text-gray-500 hidden md:table-cell">
                    {formatDate(order.criadoEm)}
                  </td>
                  <td className="px-3 py-3">
                    <Badge variant={statusVariant(order.status)}>
                      {STATUS_PEDIDO_LABELS[order.status] ?? order.status}
                    </Badge>
                  </td>
                  <td className="px-5 py-3 text-right font-bold text-gray-900 text-sm">
                    {formatCurrency(order.valorTotal)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
