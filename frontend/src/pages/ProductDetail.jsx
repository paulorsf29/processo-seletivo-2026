import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getProduct } from '../api/products'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { formatPrice } from '../utils/format'

export default function ProductDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const { addItem } = useCart()

  const [product, setProduct] = useState(null)
  const [quantity, setQuantity] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [added, setAdded] = useState(false)
  const [addError, setAddError] = useState('')
  const [adding, setAdding] = useState(false)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')
    setAdded(false)
    setQuantity(1)
    getProduct(id)
      .then((res) => {
        if (!cancelled) setProduct(res)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message || 'Produto não encontrado.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [id])

  async function handleAddToCart() {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/produtos/${id}` } } })
      return
    }
    setAdding(true)
    setAddError('')
    try {
      await addItem(product.id, quantity)
      setAdded(true)
    } catch (err) {
      setAddError(err.message || 'Não foi possível adicionar ao carrinho.')
    } finally {
      setAdding(false)
    }
  }

  if (loading) {
    return <p className="mx-auto max-w-6xl px-4 py-8 text-zinc-500">Carregando produto...</p>
  }

  if (error || !product) {
    return (
      <div className="mx-auto max-w-6xl px-4 py-8">
        <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>
        <Link to="/" className="mt-4 inline-block text-sm text-zinc-700 underline">
          Voltar aos produtos
        </Link>
      </div>
    )
  }

  const outOfStock = product.stockQuantity <= 0

  return (
    <div className="mx-auto max-w-6xl px-4 py-8">
      <Link to="/" className="text-sm text-zinc-500 hover:text-zinc-800">
        &larr; Voltar aos produtos
      </Link>

      <div className="mt-4 grid grid-cols-1 gap-8 md:grid-cols-2">
        <div className="aspect-square overflow-hidden rounded-lg bg-zinc-100">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-zinc-400">Sem imagem</div>
          )}
        </div>

        <div>
          {product.team && (
            <span className="text-sm font-medium uppercase tracking-wide text-zinc-500">{product.team}</span>
          )}
          <h1 className="mt-1 text-3xl font-bold text-zinc-900">{product.name}</h1>
          {product.brand && <p className="mt-1 text-zinc-500">{product.brand}</p>}
          <p className="mt-4 text-2xl font-bold text-zinc-900">{formatPrice(product.price)}</p>

          {product.description && <p className="mt-4 whitespace-pre-line text-zinc-600">{product.description}</p>}

          <div className="mt-4 flex flex-wrap gap-2 text-sm">
            {product.size && (
              <span className="rounded-full bg-zinc-100 px-3 py-1 text-zinc-700">Tamanho: {product.size}</span>
            )}
            <span className="rounded-full bg-zinc-100 px-3 py-1 text-zinc-700">
              {outOfStock ? 'Sem estoque' : `${product.stockQuantity} em estoque`}
            </span>
          </div>

          {!outOfStock && (
            <div className="mt-6 flex items-center gap-3">
              <input
                type="number"
                min={1}
                max={product.stockQuantity}
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, Math.min(product.stockQuantity, Number(e.target.value))))}
                className="w-20 rounded-md border border-zinc-300 px-3 py-2 text-sm"
              />
              <button
                type="button"
                onClick={handleAddToCart}
                disabled={adding}
                className="rounded-md bg-zinc-900 px-6 py-2.5 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-60"
              >
                {adding ? 'Adicionando...' : 'Adicionar ao carrinho'}
              </button>
            </div>
          )}

          {addError && <p className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{addError}</p>}

          {added && (
            <div className="mt-4 flex items-center gap-3 rounded-md bg-emerald-50 p-3 text-sm text-emerald-800">
              <span>Produto adicionado ao carrinho.</span>
              <button type="button" onClick={() => navigate('/carrinho')} className="font-semibold underline">
                Ver carrinho
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
