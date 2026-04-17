import { type NextRequest, NextResponse } from 'next/server'

const PEDIDOS = process.env.MS_PEDIDOS_URL ?? 'http://localhost:8080'

export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params
  const res = await fetch(`${PEDIDOS}/v1/pedidos/${id}`, { cache: 'no-store' })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}

export async function PUT(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params
  const res = await fetch(`${PEDIDOS}/v1/pedidos/${id}/avancar`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}

export async function DELETE(
  req: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params
  const body = await req.json()
  const res = await fetch(`${PEDIDOS}/v1/pedidos/${id}`, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}
