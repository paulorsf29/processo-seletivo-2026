import { Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import ProtectedRoute from './components/ProtectedRoute'
import AdminRoute from './components/AdminRoute'
import Home from './pages/Home'
import ProductDetail from './pages/ProductDetail'
import Cart from './pages/Cart'
import Checkout from './pages/Checkout'
import Login from './pages/Login'
import Register from './pages/Register'
import MyOrders from './pages/MyOrders'
import OrderDetail from './pages/OrderDetail'
import NotFound from './pages/NotFound'
import AdminLayout from './pages/admin/AdminLayout'
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminProducts from './pages/admin/AdminProducts'
import AdminProductForm from './pages/admin/AdminProductForm'
import AdminOrders from './pages/admin/AdminOrders'
import AdminCoupons from './pages/admin/AdminCoupons'

function App() {
  return (
    <>
      <Navbar />
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/produtos/:id" element={<ProductDetail />} />
          <Route path="/carrinho" element={<Cart />} />
          <Route path="/login" element={<Login />} />
          <Route path="/registrar" element={<Register />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/checkout" element={<Checkout />} />
            <Route path="/meus-pedidos" element={<MyOrders />} />
            <Route path="/pedidos/:id" element={<OrderDetail />} />
          </Route>

          <Route element={<AdminRoute />}>
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<AdminDashboard />} />
              <Route path="produtos" element={<AdminProducts />} />
              <Route path="produtos/novo" element={<AdminProductForm />} />
              <Route path="produtos/:id/editar" element={<AdminProductForm />} />
              <Route path="pedidos" element={<AdminOrders />} />
              <Route path="cupons" element={<AdminCoupons />} />
            </Route>
          </Route>

          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
      <Footer />
    </>
  )
}

export default App
