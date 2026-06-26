import { useState, useEffect } from 'react'
import { getMe, logout } from '../api'
import { getRefreshToken } from '../auth'

export default function Dashboard({ onLogout }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getMe()
      .then(setUser)
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  const handleLogout = async () => {
    try { await logout(getRefreshToken()) } catch { /* ignore */ }
    onLogout()
  }

  return (
    <div className="card">
      <h2>Dashboard</h2>
      {loading ? (
        <p className="muted">Loading…</p>
      ) : user ? (
        <div className="user-info">
          <div className="info-row"><span>Email</span><strong>{user.email}</strong></div>
          <div className="info-row"><span>Roles</span><strong>{[...user.roles].join(', ')}</strong></div>
        </div>
      ) : (
        <p className="error">Failed to load user</p>
      )}
      <button onClick={handleLogout}>Logout</button>
    </div>
  )
}