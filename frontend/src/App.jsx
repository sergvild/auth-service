import { useState } from 'react'
import { isLoggedIn, clearTokens } from './auth'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Verify from './pages/Verify'

const getInitialPage = () => {
  if (window.location.pathname === '/verify') return 'verify'
  return isLoggedIn() ? 'dashboard' : 'login'
}

export default function App() {
  const [page, setPage] = useState(getInitialPage)

  const onAuth = () => {
    window.history.replaceState({}, '', '/')
    setPage('dashboard')
  }
  const onLogout = () => { clearTokens(); setPage('login') }

  if (page === 'verify') return <Verify onSuccess={onAuth} />
  if (page === 'dashboard') return <Dashboard onLogout={onLogout} />
  if (page === 'register') return <Register onSuccess={() => setPage('registered')} onLogin={() => setPage('login')} />
  if (page === 'registered') return <Registered onLogin={() => setPage('login')} />
  return <Login onSuccess={onAuth} onRegister={() => setPage('register')} />
}

function Registered({ onLogin }) {
  return (
    <div className="card">
      <h2>Check your email</h2>
      <p className="muted">We sent a verification link to your email address. Click it to activate your account.</p>
      <button onClick={onLogin}>Back to Sign In</button>
    </div>
  )
}