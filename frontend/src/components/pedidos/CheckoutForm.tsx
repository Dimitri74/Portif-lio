'use client'

import { useState } from 'react'
import { Input, Textarea, Select } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import type { Cart, PedidoResponse } from '@/types'
import { pedidosApi } from '@/lib/api'
import { formatCurrency, getOrCreateClienteId } from '@/lib/utils'

interface CheckoutFormProps {
  cart: Cart
  total: number
  onSuccess: (pedido: PedidoResponse) => void
  onCancel: () => void
}

export default function CheckoutForm({
  cart,
  total,
  onSuccess,
  onCancel,
}: CheckoutFormProps) {
  const [endereco, setEndereco] = useState('')
  const [observacao, setObservacao] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!endereco.trim()) {
      setError('Informe o endereço de entrega')
      return
    }

    setLoading(true)
    setError(null)

    try {
      const clienteId = getOrCreateClienteId()
      const pedido = await pedidosApi.criar({
        clienteId,
        restauranteId: cart.restauranteId,
        itens: cart.items.map((i) => ({
          itemId: i.itemId,
          nomeItem: i.nomeItem,
          precoUnitario: i.precoUnitario,
          quantidade: i.quantidade,
        })),
        observacao: observacao || undefined,
        enderecoEntrega: endereco,
      })
      onSuccess(pedido)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao criar pedido')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="bg-orange-50 rounded-xl p-4 border border-orange-100">
        <p className="text-sm font-semibold text-orange-800">
          🍽️ {cart.restauranteNome}
        </p>
        <p className="text-xs text-orange-600 mt-1">
          {cart.items.length} item(s) · {formatCurrency(total)}
        </p>
      </div>

      <Input
        label="Endereço de entrega"
        placeholder="Rua, número, bairro, cidade"
        value={endereco}
        onChange={(e) => setEndereco(e.target.value)}
        required
      />

      <Textarea
        label="Observações (opcional)"
        placeholder="Ex: Sem cebola, sem pimenta..."
        rows={3}
        value={observacao}
        onChange={(e) => setObservacao(e.target.value)}
      />

      {error && (
        <p className="text-sm text-red-600 bg-red-50 rounded-lg px-3 py-2">
          {error}
        </p>
      )}

      <div className="flex gap-3 pt-2">
        <Button
          type="button"
          variant="ghost"
          className="flex-1"
          onClick={onCancel}
        >
          Voltar
        </Button>
        <Button
          type="submit"
          variant="primary"
          className="flex-1"
          loading={loading}
        >
          Confirmar Pedido
        </Button>
      </div>
    </form>
  )
}
