import client from './client'

export function getCart() {
  return client.get('/cart').then((res) => res.data)
}

export function addCartItem(productId, quantity) {
  return client.post('/cart/items', { productId, quantity }).then((res) => res.data)
}

export function updateCartItem(productId, quantity) {
  return client.put(`/cart/items/${productId}`, null, { params: { quantity } }).then((res) => res.data)
}

export function removeCartItem(productId) {
  return client.delete(`/cart/items/${productId}`).then((res) => res.data)
}

export function clearCart() {
  return client.delete('/cart')
}
