import { type NextRequest, NextResponse } from 'next/server'

const CATALOGO = process.env.MS_CATALOGO_URL ?? 'http://localhost:8082'

// Endpoint adicional: GET /v1/restaurantes/{id}/cardapios
// Usa a query no banco via Cardapio.findByRestaurante
export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params

  const res = await fetch(`${CATALOGO}/v1/restaurantes/${id}/cardapios`, {
    cache: 'no-store',
  })

  if (res.ok) {
    const data = await res.json()
    return NextResponse.json(data)
  }

  return NextResponse.json([], { status: 200 })
}

// POST /api/restaurantes/{id}/cardapios — cria cardápio padrão para restaurante legado
export async function POST(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params

  // Busca nome do restaurante para nomear o cardápio
  const rRes = await fetch(`${CATALOGO}/v1/restaurantes/${id}`, { cache: 'no-store' })
  const rData = rRes.ok ? await rRes.json() : { nome: 'Restaurante' }
  const nome = rData.nome ?? 'Restaurante'

  // Cria cardápio diretamente no banco via endpoint dedicado (se existir)
  // Como não há POST /v1/restaurantes/{id}/cardapios no backend, usamos SQL via
  // Flyway não é viável em runtime — solução: inserir via endpoint de criação direto
  const res = await fetch(`${CATALOGO}/v1/restaurantes/${id}/cardapios`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nome: `Cardápio de ${nome}`, descricao: 'Cardápio principal' }),
  })

  if (res.ok) {
    const data = await res.json()
    return NextResponse.json(data, { status: 201 })
  }

  return NextResponse.json({ error: 'Falha ao criar cardápio' }, { status: res.status })
}

