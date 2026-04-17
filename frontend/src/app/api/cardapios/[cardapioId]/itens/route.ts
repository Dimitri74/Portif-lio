import { type NextRequest, NextResponse } from 'next/server'

const CATALOGO = process.env.MS_CATALOGO_URL ?? 'http://localhost:8082'

export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ cardapioId: string }> },
) {
  const { cardapioId } = await params
  const res = await fetch(`${CATALOGO}/v1/cardapios/${cardapioId}/itens`, {
    cache: 'no-store',
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}

export async function POST(
  req: NextRequest,
  { params }: { params: Promise<{ cardapioId: string }> },
) {
  const { cardapioId } = await params
  const body = await req.json()
  const res = await fetch(`${CATALOGO}/v1/cardapios/${cardapioId}/itens`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}
