<template>
  <header class="bg-gray-900 border-b border-gray-800 px-6 py-3 flex items-center justify-between">
    <div>
      <p class="text-sm text-gray-400">
        {{ routeTitle }}
      </p>
    </div>
    <div class="flex items-center gap-4">
      <span
        v-if="alertStore.criticalCount > 0"
        class="flex items-center gap-1.5 text-sm text-red-400"
      >
        🔴 긴급 알림 {{ alertStore.criticalCount }}건
      </span>
      <span class="text-sm text-gray-400">{{ auth.username }}</span>
      <span class="text-xs bg-blue-900 text-blue-300 px-2 py-0.5 rounded-full">
        {{ auth.userRole }}
      </span>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useAlertStore } from '@/stores/alertStore'

const auth = useAuthStore()
const alertStore = useAlertStore()
const route = useRoute()

const titleMap = {
  Dashboard:     '통합 관제 대시보드',
  Stations:      '충전소 관리',
  StationDetail: '충전소 상세',
  Violations:    '불법주차 감지 현황',
  Equipment:     '장비 건강 모니터링',
}

const routeTitle = computed(() => titleMap[route.name] ?? route.name)
</script>
