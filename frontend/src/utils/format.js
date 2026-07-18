export function formatPrice(value) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value ?? 0)
}

export function formatDate(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
}

export const ORDER_STATUS_LABELS = {
  PENDING_PAYMENT: 'Aguardando pagamento',
  PAID: 'Pago',
  SHIPPED: 'Enviado',
  DELIVERED: 'Entregue',
  CANCELED: 'Cancelado',
}

export const PAYMENT_STATUS_LABELS = {
  PENDING: 'Pendente',
  APPROVED: 'Aprovado',
  REJECTED: 'Rejeitado',
  REFUNDED: 'Reembolsado',
}

export const PAYMENT_METHOD_LABELS = {
  CREDIT_CARD: 'Cartão de crédito',
  PIX: 'Pix',
  BOLETO: 'Boleto',
}

export const ORDER_STATUS_BADGE = {
  PENDING_PAYMENT: 'bg-amber-100 text-amber-800',
  PAID: 'bg-emerald-100 text-emerald-800',
  SHIPPED: 'bg-blue-100 text-blue-800',
  DELIVERED: 'bg-zinc-200 text-zinc-800',
  CANCELED: 'bg-red-100 text-red-800',
}
