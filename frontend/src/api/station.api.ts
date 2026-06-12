import http from './http'
import type { StationResponse } from '@/types'

export const stationApi = {
  list: (params?: { page?: number; size?: number }) => http.get('/stations', { params }),
  get: (id: number) => http.get(`/stations/${id}`),
  create: (data: Partial<StationResponse>) => http.post('/stations', data),
  update: (id: number, data: Partial<StationResponse>) => http.put(`/stations/${id}`, data),
  delete: (id: number) => http.delete(`/stations/${id}`),
  summary: (id: number) => http.get(`/stations/${id}/summary`),
}
