import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { stationApi, dashboardApi } from '@/services/api'

export const useStationStore = defineStore('station', () => {
  const stations = ref([])
  const selectedStation = ref(null)
  const kpi = ref(null)
  const loading = ref(false)

  const activeStations = computed(() =>
    stations.value.filter(s => s.status === 'ACTIVE')
  )

  const faultStations = computed(() =>
    stations.value.filter(s => s.chargers?.some(c => c.status === 'FAULT'))
  )

  async function fetchStations(params) {
    loading.value = true
    try {
      const { data } = await stationApi.list(params)
      stations.value = data
    } finally {
      loading.value = false
    }
  }

  async function fetchStation(id) {
    const { data } = await stationApi.get(id)
    selectedStation.value = data
    return data
  }

  async function fetchKpi() {
    const { data } = await dashboardApi.getKpi()
    kpi.value = data
  }

  function updateStationStatus(stationId, newStatus) {
    const station = stations.value.find(s => s.id === stationId)
    if (station) station.status = newStatus
  }

  return { stations, selectedStation, kpi, loading,
           activeStations, faultStations,
           fetchStations, fetchStation, fetchKpi, updateStationStatus }
})
