import { createContext, useContext, useEffect, useState } from 'react'
import * as authApi from '../api/auth'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user))
    } else {
      localStorage.removeItem('user')
    }
  }, [user])

  async function login(email, password) {
    setLoading(true)
    try {
      const data = await authApi.login(email, password)
      localStorage.setItem('token', data.token)
      const nextUser = { id: data.userId, name: data.name, email: data.email, role: data.role }
      setUser(nextUser)
      return nextUser
    } finally {
      setLoading(false)
    }
  }

  async function register(name, email, password) {
    setLoading(true)
    try {
      const data = await authApi.register(name, email, password)
      localStorage.setItem('token', data.token)
      const nextUser = { id: data.userId, name: data.name, email: data.email, role: data.role }
      setUser(nextUser)
      return nextUser
    } finally {
      setLoading(false)
    }
  }

  function logout() {
    localStorage.removeItem('token')
    setUser(null)
  }

  const value = {
    user,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'ADMIN',
    loading,
    login,
    register,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
