import { type NextRequest, NextResponse } from 'next/server'

const CATALOGO = process.env.MS_CATALOGO_URL ?? 'http://localhost:8082'

export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ cardapioId: string; itemId: string }> },
) {
  const { cardapioId, itemId } = await params
  const res = await fetch(`${CATALOGO}/v1/cardapios/${cardapioId}/itens/${itemId}`, {
    cache: 'no-store',
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}

export async function PUT(
  req: NextRequest,
  { params }: { params: Promise<{ cardapioId: string; itemId: string }> },
) {
  const { cardapioId, itemId } = await params
  const body = await req.json()
  const res = await fetch(`${CATALOGO}/v1/cardapios/${cardapioId}/itens/${itemId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}

export async function DELETE(
  _req: NextRequest,
  { params }: { params: Promise<{ cardapioId: string; itemId: string }> },
) {
  const { cardapioId, itemId } = await params
  const res = await fetch(`${CATALOGO}/v1/cardapios/${cardapioId}/itens/${itemId}`, {
    method: 'DELETE',
  })
  return new NextResponse(null, { status: res.status })
}
