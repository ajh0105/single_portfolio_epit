<template>
  <div class="login-wrapper">
    <el-card class="login-card">
      <div class="login-header">
        <el-icon size="48" color="#1890ff"><Lightning /></el-icon>
        <h2>E-pit 통합 관제 시스템</h2>
        <p>현대자동차그룹 초고속 전기차 충전 네트워크</p>
      </div>

      <el-form :model="form" @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="아이디">
          <el-input v-model="form.username" placeholder="아이디 입력" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item label="비밀번호">
          <el-input v-model="form.password" type="password" placeholder="비밀번호 입력"
            prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
          로그인
        </el-button>
        <p v-if="errorMsg" style="color: #f56c6c; text-align: center; margin-top: 12px">{{ errorMsg }}</p>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const errorMsg = ref('')
const form = reactive({ username: 'admin', password: 'admin1234' })

async function handleLogin() {
  loading.value = true
  errorMsg.value = ''
  try {
    await authStore.login(form.username, form.password)
    router.push('/dashboard')
  } catch (e: any) {
    errorMsg.value = e.response?.data?.error?.message ?? '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #001529 0%, #003a8c 100%);
}
.login-card {
  width: 420px;
  border-radius: 12px;
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-header h2 { margin: 12px 0 4px; font-size: 20px; }
.login-header p { color: #909399; font-size: 13px; }
</style>
