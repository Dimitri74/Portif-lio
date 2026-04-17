import { type NextRequest, NextResponse } from 'next/server'

const CATALOGO = process.env.MS_CATALOGO_URL ?? 'http://localhost:8082'

export async function PUT(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string; acao: string }> },
) {
  const { id, acao } = await params
  const res = await fetch(`${CATALOGO}/v1/restaurantes/${id}/${acao}`, {
    method: 'PUT',
  })
  return new NextResponse(null, { status: res.status })
}
