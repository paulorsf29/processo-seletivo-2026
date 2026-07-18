import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyOrders } from '../api/orders'
import { formatDate, formatPrice, ORDER_STATUS_BADGE, ORDER_STATUS_LABELS } from '../utils/format'

export default function MyOrders() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getMyOrders()
      .then(setData)
      .catch((err) => setError(err.message || 'Não foi possível carregar seus pedidos.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="mx-auto max-w-4xl px-4 py-8 text-zinc-500">Carregando pedidos...</p>
  if (error) return <p className="mx-auto max-w-4xl px-4 py-8 text-sm text-red-700">{error}</p>

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="text-2xl font-bold text-zinc-900">Meus pedidos</h1>

      {data.content.length === 0 ? (
        <p className="mt-4 text-zinc-500">Você ainda não fez nenhum pedido.</p>
      ) : (
        <ul className="mt-6 flex flex-col gap-4">
          {data.content.map((order) => (
            <li key={order.id}>
              <Link
                to={`/pedidos/${order.id}`}
                className="flex items-center justify-between rounded-lg border border-zinc-200 bg-white p-4 hover:shadow-sm"
              >
                <div>
                  <p className="font-medium text-zinc-900">Pedido #{order.id}</p>
                  <p className="text-sm text-zinc-500">{formatDate(order.createdAt)}</p>
                </div>
                <div className="flex items-center gap-4">
                  <span className="font-semibold text-zinc-900">{formatPrice(order.totalAmount)}</span>
                  <span className={`rounded-full px-3 py-1 text-xs font-medium ${ORDER_STATUS_BADGE[order.status]}`}>
                    {ORDER_STATUS_LABELS[order.status]}
                  </span>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
