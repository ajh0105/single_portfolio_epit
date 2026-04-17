<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-950">
    <div class="w-full max-w-md p-8 bg-gray-900 rounded-2xl shadow-xl">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-extrabold text-blue-400">E-pit</h1>
        <p class="text-gray-400 mt-1 text-sm">스마트 충전소 통합 관제 시스템</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">사용자명</label>
          <input
            v-model="form.username"
            type="text"
            class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white
                   focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="admin"
            required
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">비밀번호</label>
          <input
            v-model="form.password"
            type="password"
            class="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white
                   focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="••••••••"
            required
          />
        </div>

        <p v-if="error" class="text-red-400 text-sm">{{ error }}</p>

        <button
          type="submit"
          :disabled="loading"
          class="w-full py-2.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50
                 rounded-lg font-semibold transition-colors"
        >
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    await auth.login(form.value.username, form.value.password)
    router.push(route.query.redirect || '/dashboard')
  } catch {
    error.value = '사용자명 또는 비밀번호가 올바르지 않습니다.'
  } finally {
    loading.value = false
  }
}
</script>
