import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth.api'
import type { MemberSummary } from '@/types'
import { connect, disconnect } from '@/ws/stompClient'

export const useAuthStore = defineStore('auth', () => {
  const member = ref<MemberSummary | null>(null)
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))

  const isLoggedIn = computed(() => !!accessToken.value)
  const role = computed(() => member.value?.role ?? '')

  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password })
    const data = res.data.data
    accessToken.value = data.accessToken
    member.value = data.member
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    connect()
  }

  async function logout() {
    try { await authApi.logout() } catch { /* ignore */ }
    accessToken.value = null
    member.value = null
    localStorage.clear()
    disconnect()
  }

  function hasRole(required: string): boolean {
    const hierarchy: Record<string, number> = { ADMIN: 3, OPERATOR: 2, VIEWER: 1 }
    return (hierarchy[role.value] ?? 0) >= (hierarchy[required] ?? 0)
  }

  return { member, accessToken, isLoggedIn, role, login, logout, hasRole }
})
