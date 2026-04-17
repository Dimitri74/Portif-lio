import type { Metadata } from 'next'
import './globals.css'
import MainLayout from '@/components/layout/MainLayout'
import ChatWidget from '@/components/chat/ChatWidget'

export const metadata: Metadata = {
  title: 'Florinda Eats',
  description: 'Plataforma de food delivery — Peça já!',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="pt-BR" suppressHydrationWarning>
      <body suppressHydrationWarning>
        <MainLayout>{children}</MainLayout>
        <ChatWidget />
      </body>
    </html>
  )
}
