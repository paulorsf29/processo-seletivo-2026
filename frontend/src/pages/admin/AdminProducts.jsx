import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { deleteProduct, listProducts } from '../../api/products'
import { formatPrice } from '../../utils/format'

export default function AdminProducts() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deletingId, setDeletingId] = useState(null)

  function loadProducts() {
    setLoading(true)
    setError('')
    listProducts({ size: 50 })
      .then(setData)
      .catch((err) => setError(err.message || 'Não foi possível carregar os produtos.'))
      .finally(() => setLoading(false))
  }

  useEffect(loadProducts, [])

  async function handleDelete(product) {
    if (!window.confirm(`Excluir o produto "${product.name}"?`)) return
    setDeletingId(product.id)
    try {
      await deleteProduct(product.id)
      setData((prev) => ({ ...prev, content: prev.content.filter((p) => p.id !== product.id) }))
    } catch (err) {
      setError(err.message || 'Não foi possível excluir o produto.')
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-zinc-900">Produtos</h1>
        <Link
          to="/admin/produtos/novo"
          className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700"
        >
          Novo produto
        </Link>
      </div>

      {loading && <p className="mt-4 text-zinc-500">Carregando...</p>}
      {error && <p className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      {!loading && data && (
        <div className="mt-4 overflow-x-auto rounded-lg border border-zinc-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-zinc-200 text-zinc-500">
              <tr>
                <th className="px-4 py-3 font-medium">Produto</th>
                <th className="px-4 py-3 font-medium">Preço</th>
                <th className="px-4 py-3 font-medium">Estoque</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((product) => (
                <tr key={product.id} className="border-b border-zinc-100 last:border-0">
                  <td className="px-4 py-3">
                    <p className="font-medium text-zinc-900">{product.name}</p>
                    <p className="text-xs text-zinc-500">
                      {product.team} {product.brand && `· ${product.brand}`}
                    </p>
                  </td>
                  <td className="px-4 py-3 text-zinc-700">{formatPrice(product.price)}</td>
                  <td className="px-4 py-3 text-zinc-700">{product.stockQuantity}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                        product.active ? 'bg-emerald-100 text-emerald-800' : 'bg-zinc-100 text-zinc-600'
                      }`}
                    >
                      {product.active ? 'Ativo' : 'Inativo'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex justify-end gap-3">
                      <Link to={`/admin/produtos/${product.id}/editar`} className="text-sm text-zinc-700 hover:underline">
                        Editar
                      </Link>
                      <button
                        type="button"
                        onClick={() => handleDelete(product)}
                        disabled={deletingId === product.id}
                        className="text-sm text-red-600 hover:underline disabled:opacity-50"
                      >
                        Excluir
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {data.content.length === 0 && <p className="p-4 text-sm text-zinc-500">Nenhum produto cadastrado.</p>}
        </div>
      )}
    </div>
  )
}
