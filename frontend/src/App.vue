<template>
  <div id="app" class="min-h-screen bg-gray-950 text-gray-100">
    <template v-if="auth.isAuthenticated">
      <SideNav />
      <div class="ml-64 flex flex-col min-h-screen">
        <TopBar />
        <main class="flex-1 p-6">
          <RouterView />
        </main>
      </div>
    </template>
    <template v-else>
      <RouterView />
    </template>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useAlertStore } from '@/stores/alertStore'
import { connectWebSocket, disconnectWebSocket } from '@/services/websocket'
import SideNav from '@/components/SideNav.vue'
import TopBar from '@/components/TopBar.vue'

const auth = useAuthStore()
const alertStore = useAlertStore()

onMounted(() => {
  if (auth.isAuthenticated) {
    connectWebSocket(
      (alert) => alertStore.addRealtime(alert),
      (violation) => console.log('위반 감지:', violation)
    )
  }
})

onUnmounted(() => disconnectWebSocket())
</script>
