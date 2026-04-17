import { type NextRequest, NextResponse } from 'next/server'

const PAGAMENTOS = process.env.MS_PAGAMENTOS_URL ?? 'http://localhost:8081'

export async function POST(
  req: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params
  const body = await req.json()
  const res = await fetch(`${PAGAMENTOS}/v1/pagamentos/${id}/estorno`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}
