import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="mx-auto max-w-3xl px-4 py-24 text-center">
      <h1 className="text-3xl font-bold text-zinc-900">Página não encontrada</h1>
      <p className="mt-2 text-zinc-500">A página que você procura não existe.</p>
      <Link to="/" className="mt-6 inline-block rounded-md bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700">
        Voltar para a loja
      </Link>
    </div>
  )
}
