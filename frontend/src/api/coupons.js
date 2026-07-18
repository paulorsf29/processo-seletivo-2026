import client from './client'

export function validateCoupon(code) {
  return client.post('/coupons/validate', { code }).then((res) => res.data)
}

export function listCoupons({ page = 0, size = 20 } = {}) {
  return client.get('/admin/coupons', { params: { page, size } }).then((res) => res.data)
}

export function createCoupon(payload) {
  return client.post('/admin/coupons', payload).then((res) => res.data)
}

export function updateCoupon(id, payload) {
  return client.put(`/admin/coupons/${id}`, payload).then((res) => res.data)
}

export function deleteCoupon(id) {
  return client.delete(`/admin/coupons/${id}`)
}
