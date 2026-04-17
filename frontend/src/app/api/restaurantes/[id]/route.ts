import { type NextRequest, NextResponse } from 'next/server'

const CATALOGO = process.env.MS_CATALOGO_URL ?? 'http://localhost:8082'

export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params
  const res = await fetch(`${CATALOGO}/v1/restaurantes/${id}`, { cache: 'no-store' })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}

export async function PUT(
  req: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params
  const body = await req.json()
  const res = await fetch(`${CATALOGO}/v1/restaurantes/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}

export async function DELETE(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params
  const res = await fetch(`${CATALOGO}/v1/restaurantes/${id}`, {
    method: 'DELETE',
  })
  return new NextResponse(null, { status: res.status })
}
