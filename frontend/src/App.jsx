import { useState } from 'react'
import { isLoggedIn, clearTokens } from './auth'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'

export default function App() {
  const [page, setPage] = useState(isLoggedIn() ? 'dashboard' : 'login')

  const onAuth = () => setPage('dashboard')
  const onLogout = () => { clearTokens(); setPage('login') }

  if (page === 'dashboard') return <Dashboard onLogout={onLogout} />
  if (page === 'register') return <Register onSuccess={onAuth} onLogin={() => setPage('login')} />
  return <Login onSuccess={onAuth} onRegister={() => setPage('register')} />
}