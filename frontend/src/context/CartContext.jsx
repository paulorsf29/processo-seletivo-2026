import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import * as cartApi from '../api/cart'
import { useAuth } from './AuthContext'

const CartContext = createContext(null)
const EMPTY_CART = { items: [], totalAmount: 0 }

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth()
  const [cart, setCart] = useState(EMPTY_CART)
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(() => {
    if (!isAuthenticated) {
      setCart(EMPTY_CART)
      return Promise.resolve()
    }
    setLoading(true)
    return cartApi
      .getCart()
      .then(setCart)
      .finally(() => setLoading(false))
  }, [isAuthenticated])

  useEffect(() => {
    refresh()
  }, [refresh])

  async function addItem(productId, quantity = 1) {
    const updated = await cartApi.addCartItem(productId, quantity)
    setCart(updated)
    return updated
  }

  async function updateQuantity(productId, quantity) {
    const updated = await cartApi.updateCartItem(productId, quantity)
    setCart(updated)
  }

  async function removeItem(productId) {
    const updated = await cartApi.removeCartItem(productId)
    setCart(updated)
  }

  async function clearCart() {
    await cartApi.clearCart()
    setCart(EMPTY_CART)
  }

  const totalItems = cart.items.reduce((sum, item) => sum + item.quantity, 0)

  const value = {
    items: cart.items,
    totalItems,
    totalPrice: cart.totalAmount,
    loading,
    addItem,
    updateQuantity,
    removeItem,
    clearCart,
    refresh,
  }

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within CartProvider')
  return ctx
}
