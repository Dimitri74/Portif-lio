import { type NextRequest, NextResponse } from 'next/server'

const IA = process.env.MS_IA_URL ?? 'http://localhost:8083'

// Timeout de 160s para dar margem ao @Timeout(150s) do AgenteService
export const maxDuration = 160

export async function POST(req: NextRequest) {
  const body = await req.json()

  // Sanitiza a pergunta para evitar prompt injection
  const sanitized = {
    ...body,
    pergunta: String(body.pergunta ?? '').slice(0, 2000),
    sessaoId: body.sessaoId ? String(body.sessaoId).slice(0, 100) : undefined,
    clienteId: body.clienteId ? String(body.clienteId).slice(0, 100) : undefined,
  }

  try {
    const controller = new AbortController()
    const timeout = setTimeout(() => controller.abort(), 160_000)

    const res = await fetch(`${IA}/v1/ia/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(sanitized),
      signal: controller.signal,
    })
    clearTimeout(timeout)

    const data = await res.json()
    return NextResponse.json(data, { status: res.status })
  } catch (err: unknown) {
    const isAbort = err instanceof Error && err.name === 'AbortError'
    return NextResponse.json(
      {
        resposta: isAbort
          ? 'A resposta demorou muito. O modelo de IA pode estar carregando. Tente novamente em alguns segundos.'
          : 'Não foi possível conectar ao serviço de IA. Verifique se o ms-ia-suporte está rodando na porta 8083.',
        sessaoId: sanitized.sessaoId ?? null,
        fontes: [],
        guardrailAtivado: false,
        timestamp: new Date().toISOString(),
      },
      { status: 503 },
    )
  }
}
