import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { listProducts } from '../api/products'
import ProductCard from '../components/ProductCard'

const CATEGORIES = ['Seleções', 'Clubes Brasileiros', 'Clubes Europeus', 'Retrô']

export default function Home() {
  const [searchParams, setSearchParams] = useSearchParams()
  const search = searchParams.get('busca') || ''
  const category = searchParams.get('categoria') || ''
  const minPrice = searchParams.get('precoMin') || ''
  const maxPrice = searchParams.get('precoMax') || ''
  const page = Number(searchParams.get('page') || 0)

  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchInput, setSearchInput] = useState(search)
  const [minPriceInput, setMinPriceInput] = useState(minPrice)
  const [maxPriceInput, setMaxPriceInput] = useState(maxPrice)

  useEffect(() => {
    setSearchInput(search)
    setMinPriceInput(minPrice)
    setMaxPriceInput(maxPrice)
  }, [search, minPrice, maxPrice])

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')
    listProducts({ search, category, minPrice, maxPrice, page, size: 12 })
      .then((res) => {
        if (!cancelled) setData(res)
      })
      .catch((err) => {
        if (!cancelled) setError(err.message || 'Não foi possível carregar os produtos.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [search, category, minPrice, maxPrice, page])

  function buildParams(overrides) {
    const params = {}
    if (search) params.busca = search
    if (category) params.categoria = category
    if (minPrice) params.precoMin = minPrice
    if (maxPrice) params.precoMax = maxPrice
    return { ...params, ...overrides }
  }

  function handleFiltersSubmit(e) {
    e.preventDefault()
    setSearchParams(
      buildParams({
        busca: searchInput || undefined,
        precoMin: minPriceInput || undefined,
        precoMax: maxPriceInput || undefined,
        page: undefined,
      })
    )
  }

  function handleCategoryChange(nextCategory) {
    setSearchParams(buildParams({ categoria: nextCategory || undefined, page: undefined }))
  }

  function goToPage(nextPage) {
    setSearchParams(buildParams({ page: String(nextPage) }))
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-zinc-900">Camisas de time</h1>
        <p className="text-zinc-500">Encontre a camisa do seu time favorito.</p>
      </div>

      <form onSubmit={handleFiltersSubmit} className="mb-6 flex flex-wrap items-end gap-3">
        <div>
          <label className="mb-1 block text-xs font-medium text-zinc-500">Buscar</label>
          <input
            type="search"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="Nome, time ou marca..."
            className="w-56 rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-zinc-500 focus:outline-none"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-zinc-500">Preço mín.</label>
          <input
            type="number"
            min="0"
            step="0.01"
            value={minPriceInput}
            onChange={(e) => setMinPriceInput(e.target.value)}
            className="w-28 rounded-md border border-zinc-300 px-3 py-2 text-sm"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-zinc-500">Preço máx.</label>
          <input
            type="number"
            min="0"
            step="0.01"
            value={maxPriceInput}
            onChange={(e) => setMaxPriceInput(e.target.value)}
            className="w-28 rounded-md border border-zinc-300 px-3 py-2 text-sm"
          />
        </div>
        <button
          type="submit"
          className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700"
        >
          Filtrar
        </button>
      </form>

      <div className="mb-6 flex flex-wrap gap-2">
        <button
          type="button"
          onClick={() => handleCategoryChange('')}
          className={`rounded-full px-3 py-1 text-sm font-medium ${
            !category ? 'bg-zinc-900 text-white' : 'bg-zinc-100 text-zinc-700 hover:bg-zinc-200'
          }`}
        >
          Todas
        </button>
        {CATEGORIES.map((option) => (
          <button
            key={option}
            type="button"
            onClick={() => handleCategoryChange(option)}
            className={`rounded-full px-3 py-1 text-sm font-medium ${
              category === option ? 'bg-zinc-900 text-white' : 'bg-zinc-100 text-zinc-700 hover:bg-zinc-200'
            }`}
          >
            {option}
          </button>
        ))}
      </div>

      {loading && <p className="text-zinc-500">Carregando produtos...</p>}
      {error && <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      {!loading && !error && data && (
        <>
          {data.content.length === 0 ? (
            <p className="text-zinc-500">Nenhum produto encontrado.</p>
          ) : (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
              {data.content.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}

          {data.totalPages > 1 && (
            <div className="mt-8 flex items-center justify-center gap-2">
              <button
                type="button"
                disabled={data.first}
                onClick={() => goToPage(page - 1)}
                className="rounded-md border border-zinc-300 px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Anterior
              </button>
              <span className="text-sm text-zinc-500">
                Página {data.number + 1} de {data.totalPages}
              </span>
              <button
                type="button"
                disabled={data.last}
                onClick={() => goToPage(page + 1)}
                className="rounded-md border border-zinc-300 px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Próxima
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
