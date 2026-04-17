import { NextResponse } from 'next/server'

const IA = process.env.MS_IA_URL ?? 'http://localhost:8083'

export async function GET() {
  try {
    const res = await fetch(`${IA}/v1/ia/health`, { cache: 'no-store' })
    const data = await res.json()
    return NextResponse.json(data, { status: res.status })
  } catch {
    return NextResponse.json({ status: 'OFFLINE' }, { status: 503 })
  }
}
