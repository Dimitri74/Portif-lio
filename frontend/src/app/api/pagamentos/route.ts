import { type NextRequest, NextResponse } from 'next/server'

const PAGAMENTOS = process.env.MS_PAGAMENTOS_URL ?? 'http://localhost:8081'

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url)
  const qs = searchParams.toString()
  const url = `${PAGAMENTOS}/v1/pagamentos${qs ? `?${qs}` : ''}`
  const res = await fetch(url, { cache: 'no-store' })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}

export async function POST(req: NextRequest) {
  const body = await req.json()
  const res = await fetch(`${PAGAMENTOS}/v1/pagamentos`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}
