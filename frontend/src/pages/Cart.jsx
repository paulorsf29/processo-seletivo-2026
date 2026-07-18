import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { formatPrice } from '../utils/format'

export default function Cart() {
  const { items, updateQuantity, removeItem, totalPrice } = useCart()
  const navigate = useNavigate()
  const [error, setError] = useState('')

  async function handleQuantityChange(productId, quantity) {
    setError('')
    try {
      await updateQuantity(productId, quantity)
    } catch (err) {
      setError(err.message || 'Não foi possível atualizar a quantidade.')
    }
  }

  async function handleRemove(productId) {
    setError('')
    try {
      await removeItem(productId)
    } catch (err) {
      setError(err.message || 'Não foi possível remover o item.')
    }
  }

  if (items.length === 0) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-zinc-900">Seu carrinho está vazio</h1>
        <p className="mt-2 text-zinc-500">Explore nossos produtos e adicione camisas ao carrinho.</p>
        <Link
          to="/"
          className="mt-6 inline-block rounded-md bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
        >
          Ver produtos
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <h1 className="text-2xl font-bold text-zinc-900">Seu carrinho</h1>

      {error && <p className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      <ul className="mt-6 divide-y divide-zinc-200 rounded-lg border border-zinc-200 bg-white">
        {items.map((item) => (
          <li key={item.productId} className="flex items-center gap-4 p-4">
            <div className="h-20 w-20 shrink-0 overflow-hidden rounded-md bg-zinc-100">
              {item.imageUrl && <img src={item.imageUrl} alt={item.productName} className="h-full w-full object-cover" />}
            </div>
            <div className="flex-1">
              <p className="font-medium text-zinc-900">{item.productName}</p>
              <p className="text-sm text-zinc-500">{formatPrice(item.unitPrice)}</p>
            </div>
            <input
              type="number"
              min={1}
              max={item.stockQuantity ?? undefined}
              value={item.quantity}
              onChange={(e) => handleQuantityChange(item.productId, Math.max(1, Number(e.target.value)))}
              className="w-16 rounded-md border border-zinc-300 px-2 py-1.5 text-sm"
            />
            <span className="w-24 text-right font-medium text-zinc-900">{formatPrice(item.subtotal)}</span>
            <button
              type="button"
              onClick={() => handleRemove(item.productId)}
              className="text-sm text-red-600 hover:underline"
            >
              Remover
            </button>
          </li>
        ))}
      </ul>

      <div className="mt-6 flex items-center justify-between rounded-lg border border-zinc-200 bg-white p-4">
        <span className="text-lg font-semibold text-zinc-900">Total</span>
        <span className="text-lg font-bold text-zinc-900">{formatPrice(totalPrice)}</span>
      </div>

      <div className="mt-6 flex justify-end gap-3">
        <Link to="/" className="rounded-md border border-zinc-300 px-5 py-2.5 text-sm font-medium text-zinc-700 hover:bg-zinc-100">
          Continuar comprando
        </Link>
        <button
          type="button"
          onClick={() => navigate('/checkout')}
          className="rounded-md bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
        >
          Finalizar compra
        </button>
      </div>
    </div>
  )
}
