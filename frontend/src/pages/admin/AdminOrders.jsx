import { useEffect, useState } from 'react'
import { getAllOrders, updateOrderStatus } from '../../api/orders'
import { formatDate, formatPrice, ORDER_STATUS_BADGE, ORDER_STATUS_LABELS } from '../../utils/format'

const STATUSES = ['PENDING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELED']

export default function AdminOrders() {
  const [statusFilter, setStatusFilter] = useState('')
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updatingId, setUpdatingId] = useState(null)

  function loadOrders() {
    setLoading(true)
    setError('')
    getAllOrders({ status: statusFilter, size: 50 })
      .then(setData)
      .catch((err) => setError(err.message || 'Não foi possível carregar os pedidos.'))
      .finally(() => setLoading(false))
  }

  useEffect(loadOrders, [statusFilter])

  async function handleStatusChange(order, status) {
    setUpdatingId(order.id)
    try {
      const updated = await updateOrderStatus(order.id, status)
      setData((prev) => ({ ...prev, content: prev.content.map((o) => (o.id === order.id ? updated : o)) }))
    } catch (err) {
      setError(err.message || 'Não foi possível atualizar o status.')
    } finally {
      setUpdatingId(null)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-zinc-900">Pedidos</h1>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="rounded-md border border-zinc-300 px-3 py-2 text-sm"
        >
          <option value="">Todos os status</option>
          {STATUSES.map((status) => (
            <option key={status} value={status}>
              {ORDER_STATUS_LABELS[status]}
            </option>
          ))}
        </select>
      </div>

      {loading && <p className="mt-4 text-zinc-500">Carregando...</p>}
      {error && <p className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      {!loading && data && (
        <div className="mt-4 overflow-x-auto rounded-lg border border-zinc-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-zinc-200 text-zinc-500">
              <tr>
                <th className="px-4 py-3 font-medium">Pedido</th>
                <th className="px-4 py-3 font-medium">Cliente</th>
                <th className="px-4 py-3 font-medium">Data</th>
                <th className="px-4 py-3 font-medium">Total</th>
                <th className="px-4 py-3 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((order) => (
                <tr key={order.id} className="border-b border-zinc-100 last:border-0">
                  <td className="px-4 py-3 font-medium text-zinc-900">#{order.id}</td>
                  <td className="px-4 py-3 text-zinc-700">{order.userName}</td>
                  <td className="px-4 py-3 text-zinc-700">{formatDate(order.createdAt)}</td>
                  <td className="px-4 py-3 text-zinc-700">{formatPrice(order.totalAmount)}</td>
                  <td className="px-4 py-3">
                    <select
                      value={order.status}
                      disabled={updatingId === order.id}
                      onChange={(e) => handleStatusChange(order, e.target.value)}
                      className={`rounded-full border-0 px-2 py-1 text-xs font-medium ${ORDER_STATUS_BADGE[order.status]}`}
                    >
                      {STATUSES.map((status) => (
                        <option key={status} value={status}>
                          {ORDER_STATUS_LABELS[status]}
                        </option>
                      ))}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {data.content.length === 0 && <p className="p-4 text-sm text-zinc-500">Nenhum pedido encontrado.</p>}
        </div>
      )}
    </div>
  )
}
