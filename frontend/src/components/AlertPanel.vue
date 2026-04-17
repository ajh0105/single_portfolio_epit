<template>
  <div class="bg-gray-900 rounded-xl p-4">
    <div class="flex items-center justify-between mb-4">
      <h2 class="font-semibold text-gray-200">실시간 알림</h2>
      <span class="text-xs bg-red-600 text-white px-2 py-0.5 rounded-full">
        {{ alertStore.criticalCount }} 긴급
      </span>
    </div>

    <div v-if="alerts.length === 0" class="text-center text-gray-500 py-8 text-sm">
      알림 없음
    </div>

    <ul class="space-y-2 overflow-y-auto max-h-96">
      <li
        v-for="alert in alerts"
        :key="alert.id"
        class="flex items-start gap-3 p-3 rounded-lg"
        :class="severityClass(alert.severity)"
      >
        <span class="mt-0.5 text-lg">{{ severityIcon(alert.severity) }}</span>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium truncate">{{ alert.title }}</p>
          <p class="text-xs text-gray-400 mt-0.5">{{ formatTime(alert.createdAt) }}</p>
        </div>
        <button
          v-if="!alert.acknowledgedAt"
          @click="alertStore.acknowledge(alert.id)"
          class="text-xs text-blue-400 hover:text-blue-300 whitespace-nowrap"
        >
          확인
        </button>
      </li>
    </ul>
  </div>
</template>

<script setup>
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/ko'
import { useAlertStore } from '@/stores/alertStore'

dayjs.extend(relativeTime)
dayjs.locale('ko')

defineProps({ alerts: { type: Array, default: () => [] } })

const alertStore = useAlertStore()

function severityClass(severity) {
  return {
    CRITICAL: 'bg-red-950 border border-red-800',
    WARNING:  'bg-yellow-950 border border-yellow-800',
    INFO:     'bg-gray-800 border border-gray-700',
  }[severity] ?? 'bg-gray-800'
}

function severityIcon(severity) {
  return { CRITICAL: '🔴', WARNING: '🟡', INFO: '🔵' }[severity] ?? '⚪'
}

function formatTime(iso) {
  return dayjs(iso).fromNow()
}
</script>
