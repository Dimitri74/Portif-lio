'use client'

import { useEffect, useState } from 'react'
import {
  CheckCircle,
  Clock,
  ChefHat,
  Bike,
  XCircle,
  RefreshCw,
} from 'lucide-react'
import type { PedidoResponse, StatusPedido } from '@/types'
import { pedidosApi } from '@/lib/api'
import {
  formatDate,
  formatCurrency,
  STATUS_PEDIDO_LABELS,
} from '@/lib/utils'
import { Button } from '@/components/ui/Button'
import { Modal } from '@/components/ui/Modal'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'

const STEPS: { status: StatusPedido; icon: React.ComponentType<{ size?: number; className?: string }>; label: string }[] = [
  { status: 'PENDENTE', icon: Clock, label: 'Recebido' },
  { status: 'CONFIRMADO', icon: CheckCircle, label: 'Confirmado' },
  { status: 'PREPARANDO', icon: ChefHat, label: 'Preparando' },
  { status: 'SAIU_PARA_ENTREGA', icon: Bike, label: 'A caminho' },
  { status: 'ENTREGUE', icon: CheckCircle, label: 'Entregue' },
]

const STATUS_ORDER: StatusPedido[] = [
  'PENDENTE',
  'CONFIRMADO',
  'PREPARANDO',
  'SAIU_PARA_ENTREGA',
  'ENTREGUE',
]

interface OrderTrackingProps {
  pedidoId: string
  onClose: () => void
}

export default function OrderTracking({ pedidoId, onClose }: OrderTrackingProps) {
  const [pedido, setPedido] = useState<PedidoResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [cancelModal, setCancelModal] = useState(false)
  const [cancelMotivo, setCancelMotivo] = useState('')
  const [cancelling, setCancelling] = useState(false)
  const [advancing, setAdvancing] = useState(false)

  async function load() {
    try {
      const p = await pedidosApi.buscar(pedidoId)
      setPedido(p)
    } catch {
      // silencia
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    const interval = setInterval(load, 10000) // polling a cada 10s
    return () => clearInterval(interval)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pedidoId])

  async function handleCancel() {
    if (!cancelMotivo.trim()) return
    setCancelling(true)
    try {
      await pedidosApi.cancelar(pedidoId, { motivo: cancelMotivo })
      await load()
      setCancelModal(false)
    } catch {
      // mostra erro inline
    } finally {
      setCancelling(false)
    }
  }

  async function handleAdvance() {
    setAdvancing(true)
    try {
      await pedidosApi.avancarStatus(pedidoId)
      await load()
    } catch {
      // silencia
    } finally {
      setAdvancing(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-8">
        <LoadingSpinner size={32} />
      </div>
    )
  }

  if (!pedido) {
    return (
      <div className="text-center py-8 text-gray-500">
        Pedido não encontrado
      </div>
    )
  }

  const currentIndex =
    pedido.status === 'CANCELADO'
      ? -1
      : STATUS_ORDER.indexOf(pedido.status)

  const isCancelled = pedido.status === 'CANCELADO'
  const canCancel = ['PENDENTE', 'CONFIRMADO'].includes(pedido.status)
  const canAdvance = ['CONFIRMADO', 'PREPARANDO', 'SAIU_PARA_ENTREGA'].includes(pedido.status)

  return (
    <div className="space-y-5">
      {/* Status header */}
      <div
        className={`rounded-xl p-4 text-center ${
          isCancelled
            ? 'bg-red-50 border border-red-100'
            : 'bg-green-50 border border-green-100'
        }`}
      >
        {isCancelled ? (
          <>
            <XCircle size={32} className="text-red-500 mx-auto mb-2" />
            <p className="font-bold text-red-700">Pedido Cancelado</p>
          </>
        ) : pedido.status === 'ENTREGUE' ? (
          <>
            <CheckCircle size={32} className="text-green-500 mx-auto mb-2" />
            <p className="font-bold text-green-700">Pedido Entregue! 🎉</p>
          </>
        ) : (
          <>
            <div className="w-8 h-8 mx-auto mb-2"><LoadingSpinner size={32} /></div>
            <p className="font-bold text-green-700">
              {STATUS_PEDIDO_LABELS[pedido.status]}
            </p>
          </>
        )}
        <p className="text-xs text-gray-500 mt-1">
          #{pedido.id.slice(0, 8).toUpperCase()}
        </p>
      </div>

      {/* Progress steps */}
      {!isCancelled && (
        <div className="flex items-center justify-between relative">
          {/* Line */}
          <div className="absolute left-4 right-4 top-4 h-0.5 bg-gray-200 z-0" />
          <div
            className="absolute left-4 top-4 h-0.5 bg-red-500 z-0 transition-all duration-500"
            style={{
              width: `${(currentIndex / (STEPS.length - 1)) * (100 - 8)}%`,
            }}
          />

          {STEPS.map((step, index) => {
            const Icon = step.icon
            const done = index <= currentIndex
            return (
              <div key={step.status} className="flex flex-col items-center z-10">
                <div
                  className={`w-8 h-8 rounded-full flex items-center justify-center border-2 transition-all ${
                    done
                      ? 'bg-red-600 border-red-600'
                      : 'bg-white border-gray-300'
                  }`}
                >
                  <Icon
                    size={14}
                    className={done ? 'text-white' : 'text-gray-400'}
                  />
                </div>
                <p
                  className={`text-xs mt-1 font-semibold ${
                    done ? 'text-red-600' : 'text-gray-400'
                  }`}
                >
                  {step.label}
                </p>
              </div>
            )
          })}
        </div>
      )}

      {/* Details */}
      <div className="bg-gray-50 rounded-xl p-4 space-y-2">
        <div className="flex justify-between text-sm">
          <span className="text-gray-600">Criado em</span>
          <span className="font-semibold">{formatDate(pedido.criadoEm)}</span>
        </div>
        {pedido.enderecoEntrega && (
          <div className="flex justify-between text-sm">
            <span className="text-gray-600">Entrega</span>
            <span className="font-semibold text-right max-w-40 truncate">
              {pedido.enderecoEntrega}
            </span>
          </div>
        )}
        <div className="flex justify-between text-sm">
          <span className="text-gray-600">Total</span>
          <span className="font-black text-red-600">
            {formatCurrency(pedido.valorTotal)}
          </span>
        </div>
      </div>

      {/* Items */}
      <div>
        <p className="text-sm font-bold text-gray-700 mb-2">Itens</p>
        <div className="space-y-1">
          {pedido.itens.map((item) => (
            <div key={item.id} className="flex justify-between text-sm">
              <span className="text-gray-700">
                {item.quantidade}x {item.nomeItem}
              </span>
              <span className="font-semibold text-gray-900">
                {formatCurrency(item.subtotal)}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Actions */}
      <div className="flex gap-3 pt-2">
        <Button
          variant="ghost"
          size="sm"
          onClick={load}
          className="gap-1.5"
        >
          <RefreshCw size={14} />
          Atualizar
        </Button>
        {canAdvance && (
          <Button
            variant="outline"
            size="sm"
            onClick={handleAdvance}
            loading={advancing}
            className="gap-1.5 border-orange-300 text-orange-600 hover:bg-orange-50"
          >
            ▶ Simular próximo status
          </Button>
        )}
        {canCancel && (
          <Button
            variant="danger"
            size="sm"
            onClick={() => setCancelModal(true)}
          >
            Cancelar Pedido
          </Button>
        )}
        <Button variant="outline" size="sm" onClick={onClose} className="ml-auto">
          Fechar
        </Button>
      </div>

      {/* Cancel Modal */}
      <Modal
        open={cancelModal}
        onClose={() => setCancelModal(false)}
        title="Cancelar Pedido"
        size="sm"
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            Por favor, informe o motivo do cancelamento:
          </p>
          <textarea
            className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 resize-none"
            rows={3}
            placeholder="Motivo do cancelamento..."
            value={cancelMotivo}
            onChange={(e) => setCancelMotivo(e.target.value)}
          />
          <div className="flex gap-3">
            <Button
              variant="ghost"
              onClick={() => setCancelModal(false)}
              className="flex-1"
            >
              Voltar
            </Button>
            <Button
              variant="danger"
              onClick={handleCancel}
              loading={cancelling}
              disabled={!cancelMotivo.trim()}
              className="flex-1"
            >
              Confirmar
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
