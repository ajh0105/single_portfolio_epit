<template>
  <el-container style="height: 100vh; overflow: hidden">
    <!-- 사이드바 -->
    <el-aside width="220px" style="background: #001529; overflow-y: auto; overflow-x: hidden">
      <div class="logo">
        <el-icon size="24" color="#1890ff"><Lightning /></el-icon>
        <span>E-pit 관제</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard"><el-icon><DataLine /></el-icon>대시보드</el-menu-item>
        <el-menu-item index="/monitor"><el-icon><VideoCamera /></el-icon>실시간 관제</el-menu-item>
        <el-menu-item index="/stations"><el-icon><Location /></el-icon>충전소 관리</el-menu-item>
        <el-menu-item index="/violations"><el-icon><Warning /></el-icon>불법 주차</el-menu-item>
        <el-menu-item index="/phm"><el-icon><Cpu /></el-icon>장비 모니터링</el-menu-item>
        <el-menu-item index="/traffic"><el-icon><Odometer /></el-icon>교통 위험도</el-menu-item>
        <el-menu-item index="/alerts">
          <el-icon><Bell /></el-icon>
          알림 센터
          <span v-if="alertStore.unreadCount > 0" class="menu-badge">{{ alertStore.unreadCount }}</span>
        </el-menu-item>
        <el-menu-item v-if="authStore.hasRole('ADMIN')" index="/members">
          <el-icon><User /></el-icon>사용자 관리
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container style="overflow: hidden">
      <!-- 헤더 -->
      <el-header style="background: #fff; border-bottom: 1px solid #e8e8e8; display: flex; align-items: center; justify-content: space-between; flex-shrink: 0">
        <span style="font-weight: 600; font-size: 16px">{{ pageTitle }}</span>
        <div style="display: flex; align-items: center; gap: 16px">
          <el-badge :value="alertStore.unreadCount" :hidden="alertStore.unreadCount === 0">
            <el-button circle @click="$router.push('/alerts')"><el-icon><Bell /></el-icon></el-button>
          </el-badge>
          <span>{{ authStore.member?.name }}</span>
          <el-button size="small" @click="handleLogout">로그아웃</el-button>
        </div>
      </el-header>

      <!-- 데모 배너 -->
      <div v-if="isDemo" class="demo-banner">
        🎯 포트폴리오 데모 모드 — 목업 데이터로 동작합니다.
        AI 모델 기능은
        <a href="https://huggingface.co/spaces/simonahn/ai_e_pit" target="_blank" rel="noopener" class="demo-link">HuggingFace Spaces</a>
        에서 체험할 수 있습니다.
      </div>

      <!-- 메인 콘텐츠: padding 0, 꽉 차게 -->
      <el-main style="padding: 0; background: #f0f2f5; overflow-y: auto; flex: 1">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAlertStore } from '@/stores/alert'
import { connect } from '@/ws/stompClient'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const alertStore = useAlertStore()
const isDemo = import.meta.env.VITE_DEMO_MODE === 'true'

const titleMap: Record<string, string> = {
  '/dashboard': '통합 대시보드',
  '/monitor': '실시간 관제',
  '/stations': '충전소 관리',
  '/violations': '불법 주차 감지',
  '/phm': '장비 상태 모니터링',
  '/traffic': '교통 위험도 분석',
  '/alerts': '알림 센터',
  '/members': '사용자 관리',
}

const pageTitle = computed(() => {
  if (route.path.startsWith('/stations/')) return '충전소 상세'
  return titleMap[route.path] ?? 'E-pit 관제'
})

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}

onMounted(() => {
  connect(() => {
    alertStore.subscribeToAlerts()
  })
  alertStore.fetchUnreadCount()
})
</script>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  color: white;
  font-size: 16px;
  font-weight: 700;
  border-bottom: 1px solid #ffffff1a;
  flex-shrink: 0;
}
.demo-banner {
  background: #e6f4ff;
  border-bottom: 1px solid #91caff;
  padding: 6px 16px;
  font-size: 12px;
  color: #0958d9;
  flex-shrink: 0;
}
.demo-link { color: #0958d9; font-weight: 600; }
.menu-badge {
  margin-left: auto;
  background: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  line-height: 1;
}
</style>
