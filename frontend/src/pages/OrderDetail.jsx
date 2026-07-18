import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { cancelOrder, getOrder } from '../api/orders'
import { formatDate, formatPrice, ORDER_STATUS_BADGE, ORDER_STATUS_LABELS } from '../utils/format'

const CANCELABLE_STATUSES = ['PENDING_PAYMENT', 'PAID']

export default function OrderDetail() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [canceling, setCanceling] = useState(false)

  useEffect(() => {
    getOrder(id)
      .then(setOrder)
      .catch((err) => setError(err.message || 'Pedido não encontrado.'))
      .finally(() => setLoading(false))
  }, [id])

  async function handleCancel() {
    if (!window.confirm('Cancelar este pedido? O estoque será devolvido.')) return
    setCanceling(true)
    setError('')
    try {
      const updated = await cancelOrder(id)
      setOrder(updated)
    } catch (err) {
      setError(err.message || 'Não foi possível cancelar o pedido.')
    } finally {
      setCanceling(false)
    }
  }

  if (loading) return <p className="mx-auto max-w-3xl px-4 py-8 text-zinc-500">Carregando pedido...</p>
  if (error && !order) return <p className="mx-auto max-w-3xl px-4 py-8 text-sm text-red-700">{error}</p>
  if (!order) return null

  const discount = order.discountAmount || 0

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <Link to="/meus-pedidos" className="text-sm text-zinc-500 hover:text-zinc-800">
        &larr; Meus pedidos
      </Link>

      <div className="mt-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-zinc-900">Pedido #{order.id}</h1>
        <span className={`rounded-full px-3 py-1 text-xs font-medium ${ORDER_STATUS_BADGE[order.status]}`}>
          {ORDER_STATUS_LABELS[order.status]}
        </span>
      </div>
      <p className="mt-1 text-sm text-zinc-500">Realizado em {formatDate(order.createdAt)}</p>

      {error && <p className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      <ul className="mt-6 divide-y divide-zinc-100 rounded-lg border border-zinc-200 bg-white">
        {order.items.map((item) => (
          <li key={item.productId} className="flex justify-between p-4 text-sm">
            <span className="text-zinc-700">
              {item.productName} × {item.quantity}
            </span>
            <span className="font-medium text-zinc-900">{formatPrice(item.subtotal)}</span>
          </li>
        ))}
      </ul>

      <div className="mt-4 flex flex-col gap-1 rounded-lg border border-zinc-200 bg-white p-4 text-sm">
        <div className="flex justify-between text-zinc-600">
          <span>Subtotal</span>
          <span>{formatPrice(order.subtotal)}</span>
        </div>
        {discount > 0 && (
          <div className="flex justify-between text-emerald-700">
            <span>Desconto {order.couponCode ? `(${order.couponCode})` : ''}</span>
            <span>-{formatPrice(discount)}</span>
          </div>
        )}
        <div className="flex justify-between text-base font-semibold text-zinc-900">
          <span>Total</span>
          <span>{formatPrice(order.totalAmount)}</span>
        </div>
      </div>

      {CANCELABLE_STATUSES.includes(order.status) && (
        <div className="mt-6 flex justify-end">
          <button
            type="button"
            onClick={handleCancel}
            disabled={canceling}
            className="rounded-md border border-red-300 px-4 py-2 text-sm font-medium text-red-700 hover:bg-red-50 disabled:opacity-60"
          >
            {canceling ? 'Cancelando...' : 'Cancelar pedido'}
          </button>
        </div>
      )}
    </div>
  )
}
