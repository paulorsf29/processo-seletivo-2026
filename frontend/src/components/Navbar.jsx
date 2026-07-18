import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth()
  const { totalItems } = useCart()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/')
  }

  return (
    <header className="sticky top-0 z-20 border-b border-zinc-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
        <Link to="/" className="text-xl font-extrabold tracking-tight text-zinc-900">
          FestWear
        </Link>

        <nav className="hidden items-center gap-6 text-sm font-medium text-zinc-700 sm:flex">
          <Link to="/" className="hover:text-zinc-900">
            Produtos
          </Link>
          {isAuthenticated && (
            <Link to="/meus-pedidos" className="hover:text-zinc-900">
              Meus pedidos
            </Link>
          )}
          {isAdmin && (
            <Link to="/admin" className="hover:text-zinc-900">
              Painel admin
            </Link>
          )}
        </nav>

        <div className="flex items-center gap-4">
          <Link to="/carrinho" className="relative text-zinc-700 hover:text-zinc-900" aria-label="Carrinho">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="h-6 w-6">
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 1.994-4.694 2.573-7.163a1.125 1.125 0 00-1.11-1.337H5.106M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
            </svg>
            {totalItems > 0 && (
              <span className="absolute -right-2 -top-2 flex h-5 w-5 items-center justify-center rounded-full bg-zinc-900 text-xs font-bold text-white">
                {totalItems}
              </span>
            )}
          </Link>

          {isAuthenticated ? (
            <div className="flex items-center gap-3">
              <span className="hidden text-sm text-zinc-600 sm:inline">Olá, {user.name?.split(' ')[0]}</span>
              <button
                type="button"
                onClick={handleLogout}
                className="rounded-md border border-zinc-300 px-3 py-1.5 text-sm font-medium text-zinc-700 hover:bg-zinc-100"
              >
                Sair
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="rounded-md px-3 py-1.5 text-sm font-medium text-zinc-700 hover:bg-zinc-100"
              >
                Entrar
              </Link>
              <Link
                to="/registrar"
                className="rounded-md bg-zinc-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-zinc-700"
              >
                Criar conta
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}
