import { useEffect, useState } from 'react'
import { createCoupon, deleteCoupon, listCoupons, updateCoupon } from '../../api/coupons'
import { formatDate, formatPrice } from '../../utils/format'

const emptyForm = {
  code: '',
  discountType: 'PERCENTAGE',
  discountValue: '',
  minOrderValue: '',
  expirationDate: '',
  active: true,
}

export default function AdminCoupons() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [showForm, setShowForm] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  function loadCoupons() {
    setLoading(true)
    setError('')
    listCoupons({ size: 50 })
      .then(setData)
      .catch((err) => setError(err.message || 'Não foi possível carregar os cupons.'))
      .finally(() => setLoading(false))
  }

  useEffect(loadCoupons, [])

  function updateField(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  function openNewForm() {
    setEditingId(null)
    setForm(emptyForm)
    setShowForm(true)
  }

  function openEditForm(coupon) {
    setEditingId(coupon.id)
    setForm({
      code: coupon.code,
      discountType: coupon.discountType,
      discountValue: coupon.discountValue,
      minOrderValue: coupon.minOrderValue ?? '',
      expirationDate: coupon.expirationDate ? coupon.expirationDate.slice(0, 16) : '',
      active: coupon.active,
    })
    setShowForm(true)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    const payload = {
      code: form.code.toUpperCase(),
      discountType: form.discountType,
      discountValue: Number(form.discountValue),
      minOrderValue: form.minOrderValue !== '' ? Number(form.minOrderValue) : null,
      expirationDate: form.expirationDate ? new Date(form.expirationDate).toISOString() : null,
      active: form.active,
    }
    try {
      if (editingId) {
        await updateCoupon(editingId, payload)
      } else {
        await createCoupon(payload)
      }
      setShowForm(false)
      loadCoupons()
    } catch (err) {
      setError(err.message || 'Não foi possível salvar o cupom.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(coupon) {
    if (!window.confirm(`Excluir o cupom "${coupon.code}"?`)) return
    try {
      await deleteCoupon(coupon.id)
      loadCoupons()
    } catch (err) {
      setError(err.message || 'Não foi possível excluir o cupom.')
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-zinc-900">Cupons</h1>
        <button
          type="button"
          onClick={openNewForm}
          className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700"
        >
          Novo cupom
        </button>
      </div>

      {error && <p className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      {showForm && (
        <form onSubmit={handleSubmit} className="mt-4 flex flex-col gap-4 rounded-lg border border-zinc-200 bg-white p-4">
          <h2 className="font-semibold text-zinc-900">{editingId ? 'Editar cupom' : 'Novo cupom'}</h2>
          <div className="grid grid-cols-2 gap-4">
            <label className="block">
              <span className="mb-1 block text-sm font-medium text-zinc-700">Código</span>
              <input
                required
                value={form.code}
                onChange={(e) => updateField('code', e.target.value.toUpperCase())}
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm uppercase"
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-sm font-medium text-zinc-700">Tipo</span>
              <select
                value={form.discountType}
                onChange={(e) => updateField('discountType', e.target.value)}
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              >
                <option value="PERCENTAGE">Percentual (%)</option>
                <option value="FIXED">Valor fixo (R$)</option>
              </select>
            </label>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <label className="block">
              <span className="mb-1 block text-sm font-medium text-zinc-700">
                Valor do desconto {form.discountType === 'PERCENTAGE' ? '(%)' : '(R$)'}
              </span>
              <input
                required
                type="number"
                min="0.01"
                step="0.01"
                value={form.discountValue}
                onChange={(e) => updateField('discountValue', e.target.value)}
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-sm font-medium text-zinc-700">Valor mínimo do pedido (opcional)</span>
              <input
                type="number"
                min="0"
                step="0.01"
                value={form.minOrderValue}
                onChange={(e) => updateField('minOrderValue', e.target.value)}
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
              />
            </label>
          </div>
          <label className="block">
            <span className="mb-1 block text-sm font-medium text-zinc-700">Expira em (opcional)</span>
            <input
              type="datetime-local"
              value={form.expirationDate}
              onChange={(e) => updateField('expirationDate', e.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
            />
          </label>
          <label className="flex items-center gap-2 text-sm text-zinc-700">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => updateField('active', e.target.checked)}
              className="h-4 w-4 rounded border-zinc-300"
            />
            Cupom ativo
          </label>
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
              onClick={() => setShowForm(false)}
              className="rounded-md border border-zinc-300 px-5 py-2.5 text-sm font-medium text-zinc-700 hover:bg-zinc-100"
            >
              Cancelar
            </button>
          </div>
        </form>
      )}

      {loading && <p className="mt-4 text-zinc-500">Carregando...</p>}

      {!loading && data && (
        <div className="mt-4 overflow-x-auto rounded-lg border border-zinc-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-zinc-200 text-zinc-500">
              <tr>
                <th className="px-4 py-3 font-medium">Código</th>
                <th className="px-4 py-3 font-medium">Desconto</th>
                <th className="px-4 py-3 font-medium">Pedido mínimo</th>
                <th className="px-4 py-3 font-medium">Expira em</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((coupon) => (
                <tr key={coupon.id} className="border-b border-zinc-100 last:border-0">
                  <td className="px-4 py-3 font-medium text-zinc-900">{coupon.code}</td>
                  <td className="px-4 py-3 text-zinc-700">
                    {coupon.discountType === 'PERCENTAGE' ? `${coupon.discountValue}%` : formatPrice(coupon.discountValue)}
                  </td>
                  <td className="px-4 py-3 text-zinc-700">
                    {coupon.minOrderValue ? formatPrice(coupon.minOrderValue) : '—'}
                  </td>
                  <td className="px-4 py-3 text-zinc-700">
                    {coupon.expirationDate ? formatDate(coupon.expirationDate) : '—'}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                        coupon.active ? 'bg-emerald-100 text-emerald-800' : 'bg-zinc-100 text-zinc-600'
                      }`}
                    >
                      {coupon.active ? 'Ativo' : 'Inativo'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex justify-end gap-3">
                      <button type="button" onClick={() => openEditForm(coupon)} className="text-sm text-zinc-700 hover:underline">
                        Editar
                      </button>
                      <button type="button" onClick={() => handleDelete(coupon)} className="text-sm text-red-600 hover:underline">
                        Excluir
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {data.content.length === 0 && <p className="p-4 text-sm text-zinc-500">Nenhum cupom cadastrado.</p>}
        </div>
      )}
    </div>
  )
}
