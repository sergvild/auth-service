import { useEffect, useRef, useState } from 'react'
import { verifyEmail } from '../api'
import { setTokens } from '../auth'

export default function Verify({ onSuccess }) {
  const [status, setStatus] = useState('verifying')
  const [error, setError] = useState('')
  const called = useRef(false)

  useEffect(() => {
    if (called.current) return
    called.current = true

    const token = new URLSearchParams(window.location.search).get('token')
    if (!token) {
      setStatus('error')
      setError('No verification token found.')
      return
    }
    verifyEmail(token)
      .then(data => {
        setTokens(data.accessToken, data.refreshToken)
        setStatus('success')
        setTimeout(onSuccess, 1500)
      })
      .catch(e => {
        setStatus('error')
        setError(e.message)
      })
  }, [])

  return (
    <div className="card">
      <h2>Email Verification</h2>
      {status === 'verifying' && <p className="muted">Verifying your email…</p>}
      {status === 'success' && <p style={{ color: '#27ae60' }}>Email verified! Redirecting…</p>}
      {status === 'error' && <div className="error">{error}</div>}
    </div>
  )
}