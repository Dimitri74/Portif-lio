'use client'

import { useState, useCallback, useEffect } from 'react'
import type { Cart, CartItem } from '@/types'

const CART_KEY = 'florinda_cart'

function loadCart(): Cart | null {
  if (typeof window === 'undefined') return null
  try {
    const raw = localStorage.getItem(CART_KEY)
    return raw ? (JSON.parse(raw) as Cart) : null
  } catch {
    return null
  }
}

function saveCart(cart: Cart | null) {
  if (typeof window === 'undefined') return
  if (cart) {
    localStorage.setItem(CART_KEY, JSON.stringify(cart))
  } else {
    localStorage.removeItem(CART_KEY)
  }
}

export function useCart() {
  const [cart, setCart] = useState<Cart | null>(null)

  useEffect(() => {
    setCart(loadCart())
  }, [])

  const addItem = useCallback(
    (
      restauranteId: string,
      restauranteNome: string,
      item: Omit<CartItem, 'quantidade'> & { quantidade?: number },
    ) => {
      setCart((prev) => {
        // Se tiver itens de outro restaurante, limpa o carrinho
        if (prev && prev.restauranteId !== restauranteId) {
          const newCart: Cart = {
            restauranteId,
            restauranteNome,
            items: [{ ...item, quantidade: item.quantidade ?? 1 }],
          }
          saveCart(newCart)
          return newCart
        }

        const existing = prev?.items.find((i) => i.itemId === item.itemId)
        let updatedItems: CartItem[]

        if (existing) {
          updatedItems = (prev?.items ?? []).map((i) =>
            i.itemId === item.itemId
              ? { ...i, quantidade: i.quantidade + (item.quantidade ?? 1) }
              : i,
          )
        } else {
          updatedItems = [
            ...(prev?.items ?? []),
            { ...item, quantidade: item.quantidade ?? 1 },
          ]
        }

        const newCart: Cart = {
          restauranteId,
          restauranteNome,
          items: updatedItems,
        }
        saveCart(newCart)
        return newCart
      })
    },
    [],
  )

  const removeItem = useCallback((itemId: string) => {
    setCart((prev) => {
      if (!prev) return null
      const items = prev.items.filter((i) => i.itemId !== itemId)
      if (items.length === 0) {
        saveCart(null)
        return null
      }
      const updated = { ...prev, items }
      saveCart(updated)
      return updated
    })
  }, [])

  const updateQuantity = useCallback((itemId: string, quantidade: number) => {
    if (quantidade <= 0) {
      return
    }
    setCart((prev) => {
      if (!prev) return null
      const items = prev.items.map((i) =>
        i.itemId === itemId ? { ...i, quantidade } : i,
      )
      const updated = { ...prev, items }
      saveCart(updated)
      return updated
    })
  }, [])

  const clearCart = useCallback(() => {
    saveCart(null)
    setCart(null)
  }, [])

  const total = cart?.items.reduce(
    (sum, i) => sum + i.precoUnitario * i.quantidade,
    0,
  ) ?? 0

  const itemCount = cart?.items.reduce((sum, i) => sum + i.quantidade, 0) ?? 0

  return { cart, addItem, removeItem, updateQuantity, clearCart, total, itemCount }
}
