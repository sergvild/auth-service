import { getAccessToken } from './auth'

const BASE = '/api/v1/auth'

const handle = async (res) => {
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || 'Request failed')
  return data
}

export const register = (email, password) =>
  fetch(`${BASE}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  }).then(handle)

export const login = (email, password) =>
  fetch(`${BASE}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  }).then(handle)

export const verifyEmail = (token) =>
  fetch(`${BASE}/verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token })
  }).then(handle)

export const googleAuth = (idToken) =>
  fetch(`${BASE}/google`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idToken })
  }).then(handle)

export const logout = (refreshToken) =>
  fetch(`${BASE}/logout`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  })

export const getMe = () =>
  fetch('/api/v1/users/me', {
    headers: { Authorization: `Bearer ${getAccessToken()}` }
  }).then(handle)