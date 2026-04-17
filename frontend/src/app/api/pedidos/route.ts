import { type NextRequest, NextResponse } from 'next/server'

const PEDIDOS = process.env.MS_PEDIDOS_URL ?? 'http://localhost:8080'

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url)
  const qs = searchParams.toString()
  const url = `${PEDIDOS}/v1/pedidos${qs ? `?${qs}` : ''}`
  const res = await fetch(url, { cache: 'no-store' })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}

export async function POST(req: NextRequest) {
  const body = await req.json()
  const res = await fetch(`${PEDIDOS}/v1/pedidos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}
