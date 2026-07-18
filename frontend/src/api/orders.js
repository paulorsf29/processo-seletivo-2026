import client from './client'

export function createOrder(couponCode) {
  return client.post('/orders', { couponCode: couponCode || null }).then((res) => res.data)
}

export function cancelOrder(id) {
  return client.patch(`/orders/${id}/cancel`).then((res) => res.data)
}

export function getMyOrders({ page = 0, size = 10 } = {}) {
  return client.get('/orders/me', { params: { page, size } }).then((res) => res.data)
}

export function getOrder(id) {
  return client.get(`/orders/${id}`).then((res) => res.data)
}

export function getAllOrders({ status, page = 0, size = 10 } = {}) {
  return client
    .get('/orders', { params: { status: status || undefined, page, size } })
    .then((res) => res.data)
}

export function updateOrderStatus(id, status) {
  return client.patch(`/orders/${id}/status`, { status }).then((res) => res.data)
}
