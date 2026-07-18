import client from './client'

export function createPayment(orderId, method) {
  return client.post('/payments', { orderId, method }).then((res) => res.data)
}

export function getPayment(id) {
  return client.get(`/payments/${id}`).then((res) => res.data)
}

export function updatePaymentStatus(id, status) {
  return client.patch(`/payments/${id}/status`, { status }).then((res) => res.data)
}
