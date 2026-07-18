import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { createOrder } from '../api/orders'
import { createPayment } from '../api/payments'
import { validateCoupon } from '../api/coupons'
import { formatPrice, PAYMENT_METHOD_LABELS } from '../utils/format'

const METHODS = ['CREDIT_CARD', 'PIX', 'BOLETO']

export default function Checkout() {
  const { items, totalPrice, refresh } = useCart()
  const navigate = useNavigate()

  const [method, setMethod] = useState('CREDIT_CARD')
  const [couponCode, setCouponCode] = useState('')
  const [couponPreview, setCouponPreview] = useState(null)
  const [couponError, setCouponError] = useState('')
  const [validatingCoupon, setValidatingCoupon] = useState(false)

  const [order, setOrder] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  async function handleValidateCoupon(e) {
    e.preventDefault()
    if (!couponCode.trim()) return
    setValidatingCoupon(true)
    setCouponError('')
    try {
      const preview = await validateCoupon(couponCode.trim())
      setCouponPreview(preview)
    } catch (err) {
      setCouponPreview(null)
      setCouponError(err.message || 'Cupom inválido.')
    } finally {
      setValidatingCoupon(false)
    }
  }

  function handleRemoveCoupon() {
    setCouponCode('')
    setCouponPreview(null)
    setCouponError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      let currentOrder = order
      if (!currentOrder) {
        currentOrder = await createOrder(couponPreview ? couponPreview.code : null)
        setOrder(currentOrder)
        refresh()
      }

      const payment = await createPayment(currentOrder.id, method)
      if (payment.status === 'APPROVED') {
        navigate(`/pedidos/${currentOrder.id}`, { replace: true })
      } else {
        setError('Pagamento recusado pela operadora. Você pode tentar novamente ou escolher outra forma de pagamento.')
      }
    } catch (err) {
      setError(err.message || 'Não foi possível concluir o pedido.')
    } finally {
      setSubmitting(false)
    }
  }

  const displayItems = order
    ? order.items.map((item) => ({ productId: item.productId, name: item.productName, quantity: item.quantity, subtotal: item.subtotal }))
    : items.map((item) => ({ productId: item.productId, name: item.productName, quantity: item.quantity, subtotal: item.subtotal }))

  const subtotal = order ? order.subtotal : totalPrice
  const discount = order ? order.discountAmount : couponPreview?.discountAmount ?? 0
  const total = order ? order.totalAmount : couponPreview?.finalTotal ?? totalPrice

  if (items.length === 0 && !order) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center">
        <p className="text-zinc-500">Seu carrinho está vazio.</p>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <h1 className="text-2xl font-bold text-zinc-900">Finalizar compra</h1>

      <div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
        <h2 className="font-semibold text-zinc-900">Resumo do pedido</h2>
        <ul className="mt-3 divide-y divide-zinc-100">
          {displayItems.map((item) => (
            <li key={item.productId} className="flex justify-between py-2 text-sm">
              <span className="text-zinc-700">
                {item.name} × {item.quantity}
              </span>
              <span className="font-medium text-zinc-900">{formatPrice(item.subtotal)}</span>
            </li>
          ))}
        </ul>
        <div className="mt-3 flex flex-col gap-1 border-t border-zinc-200 pt-3 text-sm">
          <div className="flex justify-between text-zinc-600">
            <span>Subtotal</span>
            <span>{formatPrice(subtotal)}</span>
          </div>
          {discount > 0 && (
            <div className="flex justify-between text-emerald-700">
              <span>Desconto {order?.couponCode || couponPreview?.code ? `(${order?.couponCode || couponPreview.code})` : ''}</span>
              <span>-{formatPrice(discount)}</span>
            </div>
          )}
          <div className="flex justify-between text-base font-semibold text-zinc-900">
            <span>Total</span>
            <span>{formatPrice(total)}</span>
          </div>
        </div>
      </div>

      <div className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
        <h2 className="font-semibold text-zinc-900">Cupom de desconto</h2>
        {couponPreview ? (
          <div className="mt-3 flex items-center justify-between rounded-md bg-emerald-50 p-3 text-sm text-emerald-800">
            <span>
              Cupom <strong>{couponPreview.code}</strong> aplicado: -{formatPrice(couponPreview.discountAmount)}
            </span>
            {!order && (
              <button type="button" onClick={handleRemoveCoupon} className="font-medium underline">
                Remover
              </button>
            )}
          </div>
        ) : (
          <form onSubmit={handleValidateCoupon} className="mt-3 flex gap-2">
            <input
              type="text"
              value={couponCode}
              onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
              disabled={Boolean(order)}
              placeholder="Digite o código do cupom"
              className="flex-1 rounded-md border border-zinc-300 px-3 py-2 text-sm uppercase focus:border-zinc-500 focus:outline-none disabled:bg-zinc-100"
            />
            <button
              type="submit"
              disabled={validatingCoupon || Boolean(order)}
              className="rounded-md border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100 disabled:opacity-60"
            >
              {validatingCoupon ? 'Validando...' : 'Aplicar'}
            </button>
          </form>
        )}
        {couponError && <p className="mt-2 text-sm text-red-700">{couponError}</p>}
      </div>

      <form onSubmit={handleSubmit} className="mt-6 rounded-lg border border-zinc-200 bg-white p-4">
        <h2 className="font-semibold text-zinc-900">Forma de pagamento</h2>
        <div className="mt-3 grid grid-cols-3 gap-3">
          {METHODS.map((option) => (
            <label
              key={option}
              className={`cursor-pointer rounded-md border px-3 py-2 text-center text-sm font-medium ${
                method === option
                  ? 'border-zinc-900 bg-zinc-900 text-white'
                  : 'border-zinc-300 text-zinc-700 hover:bg-zinc-50'
              }`}
            >
              <input
                type="radio"
                name="method"
                value={option}
                checked={method === option}
                onChange={() => setMethod(option)}
                className="sr-only"
              />
              {PAYMENT_METHOD_LABELS[option]}
            </label>
          ))}
        </div>

        {error && <p className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="mt-6 w-full rounded-md bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-60"
        >
          {submitting ? 'Processando...' : order ? 'Tentar pagamento novamente' : 'Confirmar pedido'}
        </button>
      </form>
    </div>
  )
}
