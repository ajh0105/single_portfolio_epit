import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken'))
  const userRole = ref(localStorage.getItem('userRole'))
  const username = ref(localStorage.getItem('username'))

  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => userRole.value === 'ADMIN')

  async function login(usernameVal, password) {
    const { data } = await authApi.login(usernameVal, password)
    accessToken.value = data.accessToken
    userRole.value = data.role
    username.value = usernameVal
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('userRole', data.role)
    localStorage.setItem('username', usernameVal)
  }

  function logout() {
    accessToken.value = null
    userRole.value = null
    username.value = null
    localStorage.clear()
  }

  return { accessToken, userRole, username, isAuthenticated, isAdmin, login, logout }
})
