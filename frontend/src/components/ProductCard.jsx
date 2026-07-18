import { Link } from 'react-router-dom'
import { formatPrice } from '../utils/format'

export default function ProductCard({ product }) {
  const outOfStock = product.stockQuantity <= 0

  return (
    <Link
      to={`/produtos/${product.id}`}
      className="group flex flex-col overflow-hidden rounded-lg border border-zinc-200 bg-white transition hover:shadow-md"
    >
      <div className="aspect-square w-full overflow-hidden bg-zinc-100">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="h-full w-full object-cover transition group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-zinc-400">
            Sem imagem
          </div>
        )}
      </div>
      <div className="flex flex-1 flex-col gap-1 p-4">
        {product.team && (
          <span className="text-xs font-medium uppercase tracking-wide text-zinc-500">
            {product.team}
          </span>
        )}
        <h3 className="font-semibold text-zinc-900">{product.name}</h3>
        {product.brand && <span className="text-sm text-zinc-500">{product.brand}</span>}
        <div className="mt-auto flex items-center justify-between pt-2">
          <span className="text-lg font-bold text-zinc-900">{formatPrice(product.price)}</span>
          {outOfStock && (
            <span className="rounded bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700">
              Esgotado
            </span>
          )}
        </div>
      </div>
    </Link>
  )
}
