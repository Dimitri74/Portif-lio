import { type NextRequest, NextResponse } from 'next/server'

const CATALOGO = process.env.MS_CATALOGO_URL ?? 'http://localhost:8082'

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url)
  const qs = searchParams.toString()
  const url = `${CATALOGO}/v1/restaurantes${qs ? `?${qs}` : ''}`

  const res = await fetch(url, { cache: 'no-store' })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}

export async function POST(req: NextRequest) {
  const body = await req.json()
  const res = await fetch(`${CATALOGO}/v1/restaurantes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}
