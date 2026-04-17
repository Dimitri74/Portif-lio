import { type NextRequest, NextResponse } from 'next/server'

const PAGAMENTOS = process.env.MS_PAGAMENTOS_URL ?? 'http://localhost:8081'

export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params
  const res = await fetch(`${PAGAMENTOS}/v1/pagamentos/${id}`, { cache: 'no-store' })
  const data = await res.json()
  return NextResponse.json(data, { status: res.status })
}
