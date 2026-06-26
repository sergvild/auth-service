import { useState } from 'react'
import { register } from '../api'
import { setTokens } from '../auth'

export default function Register({ onSuccess, onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await register(email, password)
      setTokens(data.accessToken, data.refreshToken)
      onSuccess()
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card">
      <h2>Create Account</h2>
      {error && <div className="error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <input
          type="email" placeholder="Email" value={email} autoComplete="email"
          onChange={e => setEmail(e.target.value)} required
        />
        <input
          type="password" placeholder="Password (min 8 characters)" value={password}
          autoComplete="new-password" minLength={8}
          onChange={e => setPassword(e.target.value)} required
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Creating account…' : 'Register'}
        </button>
      </form>
      <p className="switch">
        Already have an account?{' '}
        <button className="link-btn" onClick={onLogin}>Sign In</button>
      </p>
    </div>
  )
}