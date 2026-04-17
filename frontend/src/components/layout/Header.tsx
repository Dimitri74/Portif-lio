'use client'

import { Menu, Bell, User } from 'lucide-react'
import { usePathname } from 'next/navigation'

const PAGE_TITLES: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/pedidos': 'Faça seu Pedido',
  '/admin': 'Administrador',
}

interface HeaderProps {
  onMenuClick: () => void
}

export default function Header({ onMenuClick }: HeaderProps) {
  const pathname = usePathname()

  const title =
    Object.entries(PAGE_TITLES).find(([key]) => pathname.startsWith(key))?.[1] ??
    'Florinda Eats'

  return (
    <header className="sticky top-0 z-30 bg-white/80 backdrop-blur border-b border-orange-100 px-4 py-3 flex items-center gap-4">
      {/* Menu mobile */}
      <button
        onClick={onMenuClick}
        className="lg:hidden p-2 rounded-lg text-red-600 hover:bg-red-50 transition-colors"
        aria-label="Abrir menu"
      >
        <Menu size={22} />
      </button>

      {/* Título */}
      <div className="flex-1">
        <h2 className="font-bold text-gray-900 text-lg">{title}</h2>
      </div>

      {/* Ações */}
      <div className="flex items-center gap-2">
        <button className="relative p-2 rounded-lg text-gray-500 hover:bg-orange-50 hover:text-orange-600 transition-colors">
          <Bell size={20} />
          <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
        </button>
        <button className="p-2 rounded-lg text-gray-500 hover:bg-orange-50 hover:text-orange-600 transition-colors">
          <User size={20} />
        </button>
      </div>
    </header>
  )
}
