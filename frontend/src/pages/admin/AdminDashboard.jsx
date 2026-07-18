import { useEffect, useState } from 'react'
import { getDashboard } from '../../api/metrics'
import { formatPrice, ORDER_STATUS_LABELS, PAYMENT_STATUS_LABELS } from '../../utils/format'

export default function AdminDashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getDashboard()
      .then(setData)
      .catch((err) => setError(err.message || 'Não foi possível carregar as métricas.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="text-zinc-500">Carregando métricas...</p>
  if (error) return <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>

  return (
    <div>
      <h1 className="text-2xl font-bold text-zinc-900">Dashboard</h1>

      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard label="Receita total" value={formatPrice(data.totalRevenue)} />
        <StatCard label="Pagamentos" value={data.totalPayments} />
        <StatCard label="Pedidos" value={data.totalOrders} />
      </div>

      <div className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
        <div className="rounded-lg border border-zinc-200 bg-white p-4">
          <h2 className="font-semibold text-zinc-900">Pedidos por status</h2>
          <ul className="mt-3 flex flex-col gap-2 text-sm">
            {Object.entries(data.ordersByStatus).map(([status, count]) => (
              <li key={status} className="flex justify-between">
                <span className="text-zinc-600">{ORDER_STATUS_LABELS[status] || status}</span>
                <span className="font-medium text-zinc-900">{count}</span>
              </li>
            ))}
          </ul>
        </div>
        <div className="rounded-lg border border-zinc-200 bg-white p-4">
          <h2 className="font-semibold text-zinc-900">Pagamentos por status</h2>
          <ul className="mt-3 flex flex-col gap-2 text-sm">
            {Object.entries(data.paymentsByStatus).map(([status, count]) => (
              <li key={status} className="flex justify-between">
                <span className="text-zinc-600">{PAYMENT_STATUS_LABELS[status] || status}</span>
                <span className="font-medium text-zinc-900">{count}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
        <h2 className="font-semibold text-zinc-900">Produtos mais vendidos</h2>
        {data.topSellingProducts.length === 0 ? (
          <p className="mt-3 text-sm text-zinc-500">Ainda não há vendas registradas.</p>
        ) : (
          <table className="mt-3 w-full text-left text-sm">
            <thead>
              <tr className="text-zinc-500">
                <th className="py-2 font-medium">Produto</th>
                <th className="py-2 font-medium">Unidades vendidas</th>
                <th className="py-2 font-medium">Receita</th>
              </tr>
            </thead>
            <tbody>
              {data.topSellingProducts.map((product) => (
                <tr key={product.productId} className="border-t border-zinc-100">
                  <td className="py-2 text-zinc-900">{product.productName}</td>
                  <td className="py-2 text-zinc-700">{product.totalSold}</td>
                  <td className="py-2 text-zinc-700">{formatPrice(product.totalRevenue)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}

function StatCard({ label, value }) {
  return (
    <div className="rounded-lg border border-zinc-200 bg-white p-4">
      <p className="text-sm text-zinc-500">{label}</p>
      <p className="mt-1 text-2xl font-bold text-zinc-900">{value}</p>
    </div>
  )
}
