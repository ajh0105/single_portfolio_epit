import { defineStore } from 'pinia'
import { ref } from 'vue'
import { dashboardApi } from '@/api/dashboard.api'
import type { DashboardOverviewResponse } from '@/types'

export const useDashboardStore = defineStore('dashboard', () => {
  const overview = ref<DashboardOverviewResponse | null>(null)
  const loading = ref(false)

  async function fetchOverview() {
    loading.value = true
    try {
      const res = await dashboardApi.overview()
      overview.value = res.data.data
    } finally {
      loading.value = false
    }
  }

  return { overview, loading, fetchOverview }
})
