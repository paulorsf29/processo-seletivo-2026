import { NavLink, Outlet } from 'react-router-dom'

const links = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/produtos', label: 'Produtos' },
  { to: '/admin/pedidos', label: 'Pedidos' },
  { to: '/admin/cupons', label: 'Cupons' },
]

export default function AdminLayout() {
  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-6 px-4 py-8 md:flex-row">
      <aside className="md:w-48 md:shrink-0">
        <nav className="flex gap-2 overflow-x-auto md:flex-col">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) =>
                `whitespace-nowrap rounded-md px-3 py-2 text-sm font-medium ${
                  isActive ? 'bg-zinc-900 text-white' : 'text-zinc-600 hover:bg-zinc-100'
                }`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="flex-1">
        <Outlet />
      </div>
    </div>
  )
}
