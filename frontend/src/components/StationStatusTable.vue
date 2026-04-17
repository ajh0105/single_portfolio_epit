<template>
  <div class="bg-gray-900 rounded-xl p-4">
    <h2 class="font-semibold text-gray-200 mb-4">충전소 현황</h2>

    <div v-if="loading" class="text-center text-gray-500 py-8">불러오는 중...</div>

    <table v-else class="w-full text-sm">
      <thead>
        <tr class="text-gray-400 border-b border-gray-800">
          <th class="text-left py-2 font-medium">충전소명</th>
          <th class="text-left py-2 font-medium">지역</th>
          <th class="text-left py-2 font-medium">충전기</th>
          <th class="text-left py-2 font-medium">상태</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="station in stations"
          :key="station.id"
          class="border-b border-gray-800 hover:bg-gray-800 cursor-pointer transition-colors"
          @click="$router.push(`/stations/${station.id}`)"
        >
          <td class="py-3 text-white">{{ station.name }}</td>
          <td class="py-3 text-gray-400">{{ station.region ?? '-' }}</td>
          <td class="py-3 text-gray-300">{{ station.totalChargers }}기</td>
          <td class="py-3">
            <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="statusClass(station.status)">
              {{ statusLabel(station.status) }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { useStationStore } from '@/stores/stationStore'

defineProps({ stations: { type: Array, default: () => [] } })

const stationStore = useStationStore()
const loading = stationStore.loading

function statusClass(status) {
  return {
    ACTIVE:      'bg-green-900 text-green-300',
    INACTIVE:    'bg-gray-800 text-gray-400',
    MAINTENANCE: 'bg-yellow-900 text-yellow-300',
  }[status] ?? 'bg-gray-800 text-gray-400'
}

function statusLabel(status) {
  return { ACTIVE: '정상', INACTIVE: '비활성', MAINTENANCE: '점검 중' }[status] ?? status
}
</script>
