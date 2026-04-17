'use client'

import { useEffect, useState } from 'react'
import {
  ShoppingBag,
  DollarSign,
  Store,
  Clock,
} from 'lucide-react'
import StatsCard from '@/components/dashboard/StatsCard'
import OrdersPieChart from '@/components/dashboard/OrdersPieChart'
import RevenueChart from '@/components/dashboard/RevenueChart'
import RecentOrdersTable from '@/components/dashboard/RecentOrdersTable'
import { PageLoader } from '@/components/ui/LoadingSpinner'
import { restaurantesApi, pedidosApi, pagamentosApi } from '@/lib/api'
import { formatCurrency } from '@/lib/utils'
import type {
  PedidoResumoResponse,
  RestauranteResumoResponse,
  StatusPedido,
} from '@/types'

interface DashData {
  pedidos: PedidoResumoResponse[]
  restaurantes: RestauranteResumoResponse[]
  totalReceita: number
}

function buildPedidosPorStatus(pedidos: PedidoResumoResponse[]) {
  const count: Partial<Record<StatusPedido, number>> = {}
  for (const p of pedidos) {
    count[p.status] = (count[p.status] ?? 0) + 1
  }
  return (Object.entries(count) as [StatusPedido, number][]).map(
    ([status, c]) => ({ status, count: c }),
  )
}

function buildReceitaSemanal(pedidos: PedidoResumoResponse[]) {
  const days = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']
  const map: Record<string, number> = {}

  // Últimos 7 dias
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const key = days[d.getDay()]
    map[key] = 0
  }

  for (const p of pedidos) {
    if (p.status === 'CANCELADO') continue
    const d = new Date(p.criadoEm)
    const key = days[d.getDay()]
    if (key in map) {
      map[key] = (map[key] ?? 0) + p.valorTotal
    }
  }

  return Object.entries(map).map(([dia, valor]) => ({ dia, valor }))
}

export default function DashboardPage() {
  const [data, setData] = useState<DashData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function load() {
      try {
        const [pedidos, restaurantes] = await Promise.all([
          pedidosApi.listar(),
          restaurantesApi.listar(),
        ])

        // Tenta buscar receita de pagamentos aprovados
        let totalReceita = 0
        try {
          const pagamentos = await pagamentosApi.listar()
          totalReceita = pagamentos
            .filter((p) => p.status === 'APROVADO')
            .reduce((sum, p) => sum + p.valor, 0)
        } catch {
          // Usa soma dos pedidos como fallback
          totalReceita = pedidos
            .filter((p) => p.status !== 'CANCELADO')
            .reduce((sum, p) => sum + p.valorTotal, 0)
        }

        setData({ pedidos, restaurantes, totalReceita })
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erro ao carregar dados')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  if (loading) return <PageLoader />

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-64">
        <div className="bg-red-50 border border-red-200 rounded-2xl p-6 text-center max-w-md">
          <p className="text-red-700 font-semibold mb-2">Erro ao carregar dashboard</p>
          <p className="text-red-500 text-sm">{error}</p>
          <p className="text-gray-500 text-xs mt-2">
            Verifique se os microserviços estão rodando
          </p>
        </div>
      </div>
    )
  }

  const pedidos = data?.pedidos ?? []
  const restaurantes = data?.restaurantes ?? []

  const pedidosHoje = pedidos.filter((p) => {
    const d = new Date(p.criadoEm)
    const today = new Date()
    return (
      d.getDate() === today.getDate() &&
      d.getMonth() === today.getMonth() &&
      d.getFullYear() === today.getFullYear()
    )
  })

  const pedidosAbertos = pedidos.filter((p) =>
    ['PENDENTE', 'CONFIRMADO', 'PREPARANDO', 'SAIU_PARA_ENTREGA'].includes(p.status),
  )

  const restaurantesAtivos = restaurantes.filter((r) => r.status === 'ABERTO')
  const pedidosPorStatus = buildPedidosPorStatus(pedidos)
  const receitaSemanal = buildReceitaSemanal(pedidos)

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Greeting */}
      <div>
        <h1 className="text-2xl font-black text-gray-900">
          Bem-vinda, Florinda! 👩‍🍳
        </h1>
        <p className="text-gray-500 text-sm mt-1">
          Confira o resumo do dia
        </p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard
          title="Pedidos Hoje"
          value={pedidosHoje.length}
          icon={<ShoppingBag size={20} />}
          color="red"
          trend={{ value: 12, label: 'vs. ontem' }}
        />
        <StatsCard
          title="Em Andamento"
          value={pedidosAbertos.length}
          subtitle="Precisam de atenção"
          icon={<Clock size={20} />}
          color="orange"
        />
        <StatsCard
          title="Receita Total"
          value={formatCurrency(data?.totalReceita ?? 0)}
          subtitle="Pedidos aprovados"
          icon={<DollarSign size={20} />}
          color="green"
          trend={{ value: 8, label: 'vs. semana passada' }}
        />
        <StatsCard
          title="Restaurantes Ativos"
          value={restaurantesAtivos.length}
          subtitle={`de ${restaurantes.length} cadastrados`}
          icon={<Store size={20} />}
          color="blue"
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <OrdersPieChart data={pedidosPorStatus} />
        <RevenueChart data={receitaSemanal} />
      </div>

      {/* Recent Orders */}
      <RecentOrdersTable orders={pedidos} />
    </div>
  )
}
