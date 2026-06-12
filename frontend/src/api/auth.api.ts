import http from './http'
import type { LoginRequest, TokenResponse } from '@/types'

export const authApi = {
  login: (data: LoginRequest) => http.post<{ data: TokenResponse }>('/auth/login', data),
  refresh: (refreshToken: string) => http.post<{ data: TokenResponse }>('/auth/refresh', { refreshToken }),
  logout: () => http.post('/auth/logout'),
  me: () => http.get('/auth/me'),
}
