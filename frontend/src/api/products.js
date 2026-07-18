import client from './client'

export function listProducts({ search, category, minPrice, maxPrice, page = 0, size = 12, sort } = {}) {
  return client
    .get('/products', {
      params: {
        search: search || undefined,
        category: category || undefined,
        minPrice: minPrice || undefined,
        maxPrice: maxPrice || undefined,
        page,
        size,
        sort,
      },
    })
    .then((res) => res.data)
}

export function getProduct(id) {
  return client.get(`/products/${id}`).then((res) => res.data)
}

export function createProduct(payload) {
  return client.post('/products', payload).then((res) => res.data)
}

export function updateProduct(id, payload) {
  return client.put(`/products/${id}`, payload).then((res) => res.data)
}

export function deleteProduct(id) {
  return client.delete(`/products/${id}`)
}
