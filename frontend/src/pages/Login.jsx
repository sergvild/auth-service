import { useState, useEffect, useRef } from 'react'
import { login, googleAuth } from '../api'
import { setTokens } from '../auth'

export default function Login({ onSuccess, onRegister }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const googleBtnRef = useRef(null)

  useEffect(() => {
    const init = () => {
      if (!window.google || !googleBtnRef.current) return
      window.google.accounts.id.initialize({
        client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID,
        callback: async ({ credential }) => {
          setError('')
          setLoading(true)
          try {
            const data = await googleAuth(credential)
            setTokens(data.accessToken, data.refreshToken)
            onSuccess()
          } catch (e) {
            setError(e.message)
          } finally {
            setLoading(false)
          }
        }
      })
      window.google.accounts.id.renderButton(googleBtnRef.current, {
        theme: 'outline', size: 'large', width: googleBtnRef.current.offsetWidth
      })
    }

    if (window.google) init()
    else {
      const script = document.querySelector('script[src*="accounts.google.com"]')
      script?.addEventListener('load', init)
      return () => script?.removeEventListener('load', init)
    }
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await login(email, password)
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
      <h2>Sign In</h2>
      {error && <div className="error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <input
          type="email" placeholder="Email" value={email} autoComplete="email"
          onChange={e => setEmail(e.target.value)} required
        />
        <input
          type="password" placeholder="Password" value={password} autoComplete="current-password"
          onChange={e => setPassword(e.target.value)} required
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Signing in…' : 'Sign In'}
        </button>
      </form>
      <div className="divider">or</div>
      <div ref={googleBtnRef} style={{ minHeight: 44 }} />
      <p className="switch">
        No account?{' '}
        <button className="link-btn" onClick={onRegister}>Register</button>
      </p>
    </div>
  )
}