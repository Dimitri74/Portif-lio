'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import {
  LayoutDashboard,
  ShoppingBag,
  Settings,
  UtensilsCrossed,
  X,
  ChevronRight,
} from 'lucide-react'
import FlorindaLogo from './FlorindaLogo'

interface NavItem {
  href: string
  label: string
  icon: React.ComponentType<{ size?: number; className?: string }>
  sublabel?: string
}

const navItems: NavItem[] = [
  {
    href: '/dashboard',
    label: 'Dashboard',
    icon: LayoutDashboard,
    sublabel: 'Visão Geral',
  },
  {
    href: '/pedidos',
    label: 'Faça seu Pedido',
    icon: ShoppingBag,
    sublabel: 'Peça agora',
  },
  {
    href: '/admin',
    label: 'Administrador',
    icon: Settings,
    sublabel: 'Gerenciar',
  },
]

interface SidebarProps {
  open: boolean
  onClose: () => void
}

export default function Sidebar({ open, onClose }: SidebarProps) {
  const pathname = usePathname()

  return (
    <>
      {/* Overlay mobile */}
      {open && (
        <div
          className="fixed inset-0 bg-black/40 z-40 lg:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      {/* Sidebar */}
      <aside
        className={`
          fixed top-0 left-0 h-full w-64 z-50
          bg-gradient-to-b from-red-700 to-red-800
          shadow-2xl
          flex flex-col
          transition-transform duration-300 ease-in-out
          lg:translate-x-0 lg:static lg:z-auto
          ${open ? 'translate-x-0' : '-translate-x-full'}
        `}
      >
        {/* Header da sidebar */}
        <div className="flex items-center justify-between p-5 border-b border-red-600">
          <div className="flex items-center gap-3">
            <FlorindaLogo size={44} />
            <div>
              <h1 className="text-white font-black text-lg leading-none tracking-tight">
                FLORINDA
              </h1>
              <span className="text-orange-300 font-bold text-sm tracking-widest">
                EATS
              </span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="lg:hidden text-red-200 hover:text-white p-1 rounded"
            aria-label="Fechar menu"
          >
            <X size={20} />
          </button>
        </div>

        {/* Nav */}
        <nav className="flex-1 p-4 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon
            const isActive =
              pathname === item.href ||
              (item.href !== '/dashboard' && pathname.startsWith(item.href))

            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={onClose}
                className={`
                  flex items-center gap-3 px-4 py-3 rounded-xl
                  transition-all duration-200 group
                  ${
                    isActive
                      ? 'bg-orange-500 text-white shadow-lg shadow-orange-500/30'
                      : 'text-red-100 hover:bg-red-600 hover:text-white'
                  }
                `}
              >
                <Icon
                  size={20}
                  className={isActive ? 'text-white' : 'text-red-300 group-hover:text-white'}
                />
                <div className="flex-1 min-w-0">
                  <div className="font-semibold text-sm truncate">{item.label}</div>
                  {item.sublabel && (
                    <div
                      className={`text-xs truncate ${
                        isActive ? 'text-orange-100' : 'text-red-400 group-hover:text-red-200'
                      }`}
                    >
                      {item.sublabel}
                    </div>
                  )}
                </div>
                {isActive && <ChevronRight size={16} className="text-orange-200" />}
              </Link>
            )
          })}
        </nav>

        {/* Footer */}
        <div className="p-4 border-t border-red-600">
          <div className="flex items-center gap-2 px-2">
            <UtensilsCrossed size={16} className="text-orange-300" />
            <span className="text-red-300 text-xs">
              Food Delivery · v2.0
            </span>
          </div>
        </div>
      </aside>
    </>
  )
}
