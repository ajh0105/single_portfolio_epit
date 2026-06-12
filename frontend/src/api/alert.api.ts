import http from './http'

export const alertApi = {
  list: (params?: { unreadOnly?: boolean; page?: number; size?: number }) =>
    http.get('/alerts', { params }),
  unreadCount: () => http.get('/alerts/unread-count'),
  markRead: (id: number) => http.patch(`/alerts/${id}/read`),
  markAllRead: () => http.patch('/alerts/read-all'),
}
