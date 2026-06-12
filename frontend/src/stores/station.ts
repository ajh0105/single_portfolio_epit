import { defineStore } from 'pinia'
import { ref } from 'vue'
import { stationApi } from '@/api/station.api'
import type { StationResponse } from '@/types'

export const useStationStore = defineStore('station', () => {
  const stations = ref<StationResponse[]>([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchAll(page = 0, size = 50) {
    loading.value = true
    try {
      const res = await stationApi.list({ page, size })
      stations.value = res.data.data?.content ?? []
      total.value = res.data.data?.totalElements ?? 0
    } finally {
      loading.value = false
    }
  }

  async function fetchOne(id: number): Promise<StationResponse | null> {
    const res = await stationApi.get(id)
    return res.data.data
  }

  return { stations, total, loading, fetchAll, fetchOne }
})
