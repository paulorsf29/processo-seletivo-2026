import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createProduct, getProduct, updateProduct } from '../../api/products'

const SIZES = ['PP', 'P', 'M', 'G', 'GG', 'XG']

const emptyForm = {
  name: '',
  description: '',
  price: '',
  team: '',
  brand: '',
  category: '',
  size: 'M',
  stockQuantity: '',
  imageUrl: '',
  active: true,
}

export default function AdminProductForm() {
  const { id } = useParams()
  const isEditing = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState(emptyForm)
  const [loading, setLoading] = useState(isEditing)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!isEditing) return
    getProduct(id)
      .then((product) =>
        setForm({
          name: product.name,
          description: product.description || '',
          price: product.price,
          team: product.team || '',
          brand: product.brand || '',
          category: product.category || '',
          size: product.size || 'M',
          stockQuantity: product.stockQuantity,
          imageUrl: product.imageUrl || '',
          active: product.active,
        })
      )
      .catch((err) => setError(err.message || 'Produto não encontrado.'))
      .finally(() => setLoading(false))
  }, [id, isEditing])

  function updateField(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    const payload = {
      name: form.name,
      description: form.description || null,
      price: Number(form.price),
      team: form.team || null,
      brand: form.brand || null,
      category: form.category || null,
      size: form.size || null,
      stockQuantity: Number(form.stockQuantity),
      imageUrl: form.imageUrl || null,
      active: form.active,
    }
    try {
      if (isEditing) {
        await updateProduct(id, payload)
      } else {
        await createProduct(payload)
      }
      navigate('/admin/produtos')
    } catch (err) {
      setError(err.message || 'Não foi possível salvar o produto.')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <p className="text-zinc-500">Carregando...</p>

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-zinc-900">{isEditing ? 'Editar produto' : 'Novo produto'}</h1>

      <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4">
        <Field label="Nome">
          <input
            required
            value={form.name}
            onChange={(e) => updateField('name', e.target.value)}
            className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
          />
        </Field>

        <Field label="Descrição">
          <textarea
            rows={3}
            value={form.description}
            onChange={(e) => updateField('description', e.target.value)}
            className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
          />
        </Field>

        <div className="grid grid-cols-2 gap-4">
          <Field label="Preço (R$)">
            <input
              required
              type="number"
              min="0.01"
              step="0.01"
              value={form.price}
              onChange={(e) => updateField('price', e.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
            />
          </Field>
          <Field label="Estoque">
            <input
              required
              type="number"
              min="0"
              step="1"
              value={form.stockQuantity}
              onChange={(e) => updateField('stockQuantity', e.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
            />
          </Field>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Field label="Time">
            <input
              value={form.team}
              onChange={(e) => updateField('team', e.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
            />
          </Field>
          <Field label="Marca">
            <input
              value={form.brand}
              onChange={(e) => updateField('brand', e.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
            />
          </Field>
        </div>

        <Field label="Categoria">
          <input
            value={form.category}
            onChange={(e) => updateField('category', e.target.value)}
            placeholder="Ex.: Seleções, Clubes Brasileiros, Retrô..."
            className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
          />
        </Field>

        <Field label="Tamanho">
          <select
            value={form.size}
            onChange={(e) => updateField('size', e.target.value)}
            className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
          >
            {SIZES.map((size) => (
              <option key={size} value={size}>
                {size}
              </option>
            ))}
          </select>
        </Field>

        <Field label="URL da imagem">
          <input
            value={form.imageUrl}
            onChange={(e) => updateField('imageUrl', e.target.value)}
            placeholder="https://..."
            className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
          />
        </Field>

        <label className="flex items-center gap-2 text-sm text-zinc-700">
          <input
            type="checkbox"
            checked={form.active}
            onChange={(e) => updateField('active', e.target.checked)}
            className="h-4 w-4 rounded border-zinc-300"
          />
          Produto ativo (visível na loja)
        </label>

        {error && <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>}

        <div className="flex gap-3">
          <button
            type="submit"
            disabled={submitting}
            className="rounded-md bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-60"
          >
            {submitting ? 'Salvando...' : 'Salvar'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/admin/produtos')}
            className="rounded-md border border-zinc-300 px-5 py-2.5 text-sm font-medium text-zinc-700 hover:bg-zinc-100"
          >
            Cancelar
          </button>
        </div>
      </form>
    </div>
  )
}

function Field({ label, children }) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium text-zinc-700">{label}</span>
      {children}
    </label>
  )
}
