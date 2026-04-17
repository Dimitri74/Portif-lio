'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { Input, Textarea } from '@/components/ui/Input'
import type { CriarItemRequest, ItemCardapioResponse } from '@/types'
import { itensApi } from '@/lib/api'
import { formatCurrency } from '@/lib/utils'

interface ItemFormProps {
  cardapioId: string
  initial?: Partial<ItemCardapioResponse>
  onSuccess: (item: ItemCardapioResponse) => void
  onCancel: () => void
}

export default function ItemForm({ cardapioId, initial, onSuccess, onCancel }: ItemFormProps) {
  const isEdit = Boolean(initial?.id)

  const [form, setForm] = useState({
    nome: initial?.nome ?? '',
    descricao: initial?.descricao ?? '',
    preco: initial?.preco?.toString() ?? '',
    fotoUrl: initial?.fotoUrl ?? '',
    calorias: initial?.calorias?.toString() ?? '',
    vegetariano: initial?.vegetariano ?? false,
    vegano: initial?.vegano ?? false,
    semGluten: initial?.semGluten ?? false,
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  function set(field: string, value: string | boolean) {
    setForm((f) => ({ ...f, [field]: value }))
    setErrors((e) => { const n = { ...e }; delete n[field]; return n })
  }

  function validate(): boolean {
    const errs: Record<string, string> = {}
    if (!form.nome.trim()) errs.nome = 'Nome é obrigatório'
    const preco = parseFloat(form.preco)
    if (isNaN(preco) || preco <= 0) errs.preco = 'Preço inválido (deve ser > 0)'
    setErrors(errs)
    return Object.keys(errs).length === 0
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!validate()) return

    setLoading(true)
    setServerError(null)

    const payload: CriarItemRequest = {
      nome: form.nome,
      descricao: form.descricao || undefined,
      preco: parseFloat(form.preco),
      fotoUrl: form.fotoUrl || undefined,
      calorias: form.calorias ? parseInt(form.calorias) : undefined,
      vegetariano: form.vegetariano,
      vegano: form.vegano,
      semGluten: form.semGluten,
    }

    try {
      let result: ItemCardapioResponse
      if (isEdit && initial?.id) {
        result = await itensApi.atualizar(cardapioId, initial.id, payload)
      } else {
        result = await itensApi.criar(cardapioId, payload)
      }
      onSuccess(result)
    } catch (err) {
      setServerError(err instanceof Error ? err.message : 'Erro ao salvar item')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Input
        label="Nome do Item"
        required
        value={form.nome}
        onChange={(e) => set('nome', e.target.value)}
        error={errors.nome}
        placeholder="Ex: Picanha na brasa"
      />

      <Textarea
        label="Descrição"
        rows={2}
        value={form.descricao}
        onChange={(e) => set('descricao', e.target.value)}
        placeholder="Descrição do prato..."
      />

      <div className="grid grid-cols-2 gap-4">
        <div>
          <Input
            label="Preço (R$)"
            required
            type="number"
            step="0.01"
            min="0.01"
            value={form.preco}
            onChange={(e) => set('preco', e.target.value)}
            error={errors.preco}
            placeholder="0,00"
          />
          {form.preco && !isNaN(parseFloat(form.preco)) && (
            <p className="text-xs text-green-600 mt-1">
              {formatCurrency(parseFloat(form.preco))}
            </p>
          )}
        </div>
        <Input
          label="Calorias (opcional)"
          type="number"
          min="0"
          value={form.calorias}
          onChange={(e) => set('calorias', e.target.value)}
          placeholder="Ex: 850"
        />
      </div>

      <Input
        label="URL da Foto (opcional)"
        type="text"
        value={form.fotoUrl}
        onChange={(e) => set('fotoUrl', e.target.value)}
        placeholder="https://..."
      />

      {form.fotoUrl && (
        <div className="mt-1">
          <p className="text-xs text-gray-500 mb-1">Pré-visualização:</p>
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={form.fotoUrl}
            alt="Pré-visualização da foto"
            className="h-24 w-full object-cover rounded-xl border border-gray-200 bg-gray-100"
            onError={(e) => {
              (e.currentTarget as HTMLImageElement).style.display = 'none'
              const msg = e.currentTarget.nextSibling as HTMLElement | null
              if (msg) msg.style.display = 'block'
            }}
          />
          <p
            className="text-xs text-red-500 mt-1 hidden"
            aria-live="polite"
          >
            Não foi possível carregar a imagem. Verifique a URL.
          </p>
        </div>
      )}

      {/* Checkboxes */}
      <div>
        <p className="text-sm font-semibold text-gray-700 mb-2">Características</p>
        <div className="flex gap-4">
          {[
            { field: 'vegetariano', label: '🌱 Vegetariano' },
            { field: 'vegano', label: '🥦 Vegano' },
            { field: 'semGluten', label: '🌾 Sem Glúten' },
          ].map(({ field, label }) => (
            <label key={field} className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={form[field as 'vegetariano' | 'vegano' | 'semGluten']}
                onChange={(e) => set(field, e.target.checked)}
                className="rounded border-gray-300 text-red-600 focus:ring-red-500"
              />
              <span className="text-sm text-gray-700">{label}</span>
            </label>
          ))}
        </div>
      </div>

      {serverError && (
        <p className="text-sm text-red-600 bg-red-50 rounded-lg px-3 py-2">
          {serverError}
        </p>
      )}

      <div className="flex gap-3 pt-2">
        <Button type="button" variant="ghost" onClick={onCancel} className="flex-1">
          Cancelar
        </Button>
        <Button type="submit" variant="primary" loading={loading} className="flex-1">
          {isEdit ? 'Salvar Item' : 'Criar Item'}
        </Button>
      </div>
    </form>
  )
}
