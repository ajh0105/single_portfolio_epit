import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { alertApi } from '@/services/api'

export const useAlertStore = defineStore('alert', () => {
  const alerts = ref([])
  const loading = ref(false)
  const totalPages = ref(0)

  const criticalCount = computed(() =>
    alerts.value.filter(a => a.severity === 'CRITICAL' && !a.resolved).length
  )

  const warningCount = computed(() =>
    alerts.value.filter(a => a.severity === 'WARNING' && !a.resolved).length
  )

  async function fetchActive(page = 0) {
    loading.value = true
    try {
      const { data } = await alertApi.listActive(page)
      alerts.value = data.content
      totalPages.value = data.totalPages
    } finally {
      loading.value = false
    }
  }

  function addRealtime(alert) {
    alerts.value.unshift(alert)
    // 최대 100개 유지
    if (alerts.value.length > 100) alerts.value.pop()
  }

  async function acknowledge(alertId) {
    await alertApi.acknowledge(alertId)
    const target = alerts.value.find(a => a.id === alertId)
    if (target) target.acknowledgedAt = new Date().toISOString()
  }

  async function resolve(alertId) {
    await alertApi.resolve(alertId)
    alerts.value = alerts.value.filter(a => a.id !== alertId)
  }

  return { alerts, loading, totalPages, criticalCount, warningCount,
           fetchActive, addRealtime, acknowledge, resolve }
})
