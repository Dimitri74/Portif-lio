'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/Button'
import { Input, Textarea, Select } from '@/components/ui/Input'
import type { CriarRestauranteRequest, RestauranteResponse } from '@/types'
import { restaurantesApi } from '@/lib/api'

interface RestaurantFormProps {
  initial?: Partial<RestauranteResponse>
  onSuccess: (r: RestauranteResponse) => void
  onCancel: () => void
}

const CATEGORIAS = [
  { value: 'CHURRASCARIA', label: '🥩 Churrascaria' },
  { value: 'PIZZARIA', label: '🍕 Pizzaria' },
  { value: 'JAPONES', label: '🍱 Japonês' },
  { value: 'HAMBURGUER', label: '🍔 Hamburguer' },
  { value: 'SAUDAVEL', label: '🥗 Saudável' },
  { value: 'LANCHES', label: '🥪 Lanches' },
  { value: 'ITALIANA', label: '🍝 Italiana' },
  { value: 'MEXICANA', label: '🌮 Mexicana' },
  { value: 'NORDESTINO', label: '🫙 Nordestino' },
  { value: 'FRUTOS_DO_MAR', label: '🦐 Frutos do Mar' },
  { value: 'OUTROS', label: '🍽️ Outros' },
]

const UFS = [
  'AC','AL','AP','AM','BA','CE','DF','ES','GO','MA',
  'MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN',
  'RS','RO','RR','SC','SP','SE','TO',
].map((uf) => ({ value: uf, label: uf }))

export default function RestaurantForm({ initial, onSuccess, onCancel }: RestaurantFormProps) {
  const isEdit = Boolean(initial?.id)

  const [form, setForm] = useState({
    nome: initial?.nome ?? '',
    descricao: initial?.descricao ?? '',
    categoria: initial?.categoria ?? '',
    telefone: initial?.telefone ?? '',
    email: initial?.email ?? '',
    horarioAbertura: initial?.horarioAbertura ?? '11:00',
    horarioFechamento: initial?.horarioFechamento ?? '23:00',
    logradouro: initial?.endereco?.logradouro ?? '',
    numero: initial?.endereco?.numero ?? '',
    bairro: initial?.endereco?.bairro ?? '',
    cidade: initial?.endereco?.cidade ?? '',
    uf: initial?.endereco?.uf ?? 'CE',
    cep: initial?.endereco?.cep ?? '',
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  function set(field: string, value: string) {
    setForm((f) => ({ ...f, [field]: value }))
    setErrors((e) => { const n = { ...e }; delete n[field]; return n })
  }

  function validate(): boolean {
    const errs: Record<string, string> = {}
    if (!form.nome.trim()) errs.nome = 'Nome é obrigatório'
    if (!form.categoria) errs.categoria = 'Categoria é obrigatória'
    if (!form.logradouro.trim()) errs.logradouro = 'Logradouro é obrigatório'
    if (!form.numero.trim()) errs.numero = 'Número é obrigatório'
    if (!form.bairro.trim()) errs.bairro = 'Bairro é obrigatório'
    if (!form.cidade.trim()) errs.cidade = 'Cidade é obrigatória'
    if (!form.uf) errs.uf = 'UF é obrigatória'
    if (!form.cep.match(/^\d{5}-\d{3}$/)) errs.cep = 'CEP inválido (XXXXX-XXX)'
    setErrors(errs)
    return Object.keys(errs).length === 0
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!validate()) return

    setLoading(true)
    setServerError(null)

    const payload: CriarRestauranteRequest = {
      nome: form.nome,
      descricao: form.descricao || undefined,
      categoria: form.categoria,
      telefone: form.telefone || undefined,
      email: form.email || undefined,
      horarioAbertura: form.horarioAbertura || undefined,
      horarioFechamento: form.horarioFechamento || undefined,
      endereco: {
        logradouro: form.logradouro,
        numero: form.numero,
        bairro: form.bairro,
        cidade: form.cidade,
        uf: form.uf,
        cep: form.cep,
      },
    }

    try {
      let result: RestauranteResponse
      if (isEdit && initial?.id) {
        result = await restaurantesApi.atualizar(initial.id, payload)
      } else {
        result = await restaurantesApi.criar(payload)
      }
      onSuccess(result)
    } catch (err) {
      setServerError(err instanceof Error ? err.message : 'Erro ao salvar')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Input
          label="Nome do Restaurante"
          required
          value={form.nome}
          onChange={(e) => set('nome', e.target.value)}
          error={errors.nome}
          placeholder="Ex: Churrascaria Florinda"
        />
        <Select
          label="Categoria"
          required
          value={form.categoria}
          onChange={(e) => set('categoria', e.target.value)}
          error={errors.categoria}
          options={CATEGORIAS}
          placeholder="Selecione..."
        />
      </div>

      <Textarea
        label="Descrição"
        rows={2}
        value={form.descricao}
        onChange={(e) => set('descricao', e.target.value)}
        placeholder="Descreva o restaurante..."
      />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Input
          label="Telefone"
          value={form.telefone}
          onChange={(e) => set('telefone', e.target.value)}
          placeholder="(88) 99999-9999"
        />
        <Input
          label="E-mail"
          type="email"
          value={form.email}
          onChange={(e) => set('email', e.target.value)}
          placeholder="contato@restaurante.com"
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Input
          label="Horário de Abertura"
          type="time"
          value={form.horarioAbertura}
          onChange={(e) => set('horarioAbertura', e.target.value)}
        />
        <Input
          label="Horário de Fechamento"
          type="time"
          value={form.horarioFechamento}
          onChange={(e) => set('horarioFechamento', e.target.value)}
        />
      </div>

      {/* Endereço */}
      <div className="border-t border-gray-100 pt-4">
        <p className="text-sm font-bold text-gray-700 mb-3">Endereço</p>
        <div className="space-y-3">
          <div className="grid grid-cols-3 gap-3">
            <div className="col-span-2">
              <Input
                label="Logradouro"
                required
                value={form.logradouro}
                onChange={(e) => set('logradouro', e.target.value)}
                error={errors.logradouro}
                placeholder="Rua, Av., Alameda..."
              />
            </div>
            <Input
              label="Número"
              required
              value={form.numero}
              onChange={(e) => set('numero', e.target.value)}
              error={errors.numero}
              placeholder="100"
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Bairro"
              required
              value={form.bairro}
              onChange={(e) => set('bairro', e.target.value)}
              error={errors.bairro}
            />
            <Input
              label="Cidade"
              required
              value={form.cidade}
              onChange={(e) => set('cidade', e.target.value)}
              error={errors.cidade}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="UF"
              required
              value={form.uf}
              onChange={(e) => set('uf', e.target.value)}
              error={errors.uf}
              options={UFS}
            />
            <Input
              label="CEP"
              required
              value={form.cep}
              onChange={(e) => set('cep', e.target.value)}
              error={errors.cep}
              placeholder="63000-000"
              maxLength={9}
            />
          </div>
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
          {isEdit ? 'Salvar Alterações' : 'Criar Restaurante'}
        </Button>
      </div>
    </form>
  )
}
