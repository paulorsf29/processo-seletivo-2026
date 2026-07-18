export default function Footer() {
  return (
    <footer className="mt-auto border-t border-zinc-200 bg-white">
      <div className="mx-auto max-w-6xl px-4 py-6 text-sm text-zinc-500">
        © {new Date().getFullYear()} FestWear. Todos os direitos reservados.
      </div>
    </footer>
  )
}
