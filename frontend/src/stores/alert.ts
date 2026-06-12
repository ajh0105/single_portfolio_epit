import { defineStore } from 'pinia'
import { ref } from 'vue'
import { alertApi } from '@/api/alert.api'
import { subscribe } from '@/ws/stompClient'
import type { AlertResponse } from '@/types'
import { ElNotification } from 'element-plus'

export const useAlertStore = defineStore('alert', () => {
  const alerts = ref<AlertResponse[]>([])
  const unreadCount = ref(0)

  async function fetchAlerts() {
    const res = await alertApi.list({ size: 50 })
    const data = res.data.data?.content ?? []
    alerts.value = data
    unreadCount.value = data.filter((a: AlertResponse) => !a.isRead).length
  }

  async function fetchUnreadCount() {
    const res = await alertApi.unreadCount()
    unreadCount.value = res.data.data?.count ?? 0
  }

  async function markRead(id: number) {
    await alertApi.markRead(id)
    const idx = alerts.value.findIndex((a) => a.id === id)
    if (idx !== -1) {
      alerts.value.splice(idx, 1, { ...alerts.value[idx], isRead: true })
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  }

  async function markAllRead() {
    await alertApi.markAllRead()
    alerts.value = alerts.value.map((a) => ({ ...a, isRead: true }))
    unreadCount.value = 0
  }

  // Backend에 unread API가 없으므로 로컬 토글만 수행 (낙관적 처리)
  function markUnread(id: number) {
    const idx = alerts.value.findIndex((a) => a.id === id)
    if (idx !== -1 && alerts.value[idx].isRead) {
      alerts.value.splice(idx, 1, { ...alerts.value[idx], isRead: false })
      unreadCount.value++
    }
  }

  function subscribeToAlerts() {
    subscribe('/topic/alerts', (body) => {
      const alert = body as AlertResponse
      alerts.value.unshift(alert)
      unreadCount.value++
      ElNotification({
        title: alert.title,
        message: alert.message,
        type: alert.severity === 'CRITICAL' ? 'error' : alert.severity === 'WARNING' ? 'warning' : 'info',
        duration: 5000,
      })
    })
  }

  return { alerts, unreadCount, fetchAlerts, fetchUnreadCount, markRead, markAllRead, markUnread, subscribeToAlerts }
})
