import client from './client'

export function getDashboard() {
  return client.get('/admin/metrics/dashboard').then((res) => res.data)
}

export function getBestSellingProducts(limit = 10) {
  return client.get('/admin/metrics/best-selling-products', { params: { limit } }).then((res) => res.data)
}
