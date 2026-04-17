<template>
  <aside class="fixed left-0 top-0 h-full w-64 bg-gray-900 border-r border-gray-800 flex flex-col">
    <div class="p-5 border-b border-gray-800">
      <h1 class="text-xl font-extrabold text-blue-400">E-pit Control</h1>
      <p class="text-xs text-gray-500 mt-0.5">스마트 충전소 관제</p>
    </div>

    <nav class="flex-1 p-4 space-y-1">
      <RouterLink
        v-for="item in navItems"
        :key="item.to"
        :to="item.to"
        class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-gray-400
               hover:bg-gray-800 hover:text-white transition-colors"
        active-class="bg-blue-900 text-blue-300"
      >
        <span>{{ item.icon }}</span>
        {{ item.label }}
        <span
          v-if="item.badge && item.badge > 0"
          class="ml-auto text-xs bg-red-600 text-white px-1.5 py-0.5 rounded-full"
        >
          {{ item.badge }}
        </span>
      </RouterLink>
    </nav>

    <div class="p-4 border-t border-gray-800">
      <button
        @click="auth.logout(); $router.push('/login')"
        class="w-full px-3 py-2 text-sm text-gray-400 hover:text-white hover:bg-gray-800 rounded-lg transition-colors"
      >
        로그아웃
      </button>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useAlertStore } from '@/stores/alertStore'

const auth = useAuthStore()
const alertStore = useAlertStore()

const navItems = computed(() => [
  { to: '/dashboard',  icon: '📊', label: '대시보드' },
  { to: '/stations',   icon: '⚡', label: '충전소 관리' },
  { to: '/violations', icon: '🚫', label: '불법주차 감지', badge: alertStore.criticalCount },
  { to: '/equipment',  icon: '🔧', label: '장비 모니터링' },
])
</script>
