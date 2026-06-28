import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { logout as apiLogout } from '@/api/auth'

export interface UserInfo {
  token: string
  username: string
  nickname: string
  role: string
  avatar?: string
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const roleLabel = computed(() => {
    const roleMap: Record<string, string> = {
      'ADMIN': '管理员',
      'USER': '普通用户'
    }
    return roleMap[user.value?.role || ''] || '普通用户'
  })

  const init = () => {
    const saved = localStorage.getItem('user')
    if (saved) {
      try {
        const parsed = JSON.parse(saved)
        if (parsed.role === 'SUPER_ADMIN') {
          parsed.role = 'ADMIN'
        }
        user.value = parsed
        localStorage.setItem('user', JSON.stringify(parsed))
      } catch {
        localStorage.removeItem('user')
        localStorage.removeItem('token')
      }
    }
  }

  const setUser = (data: UserInfo) => {
    user.value = data
    localStorage.setItem('user', JSON.stringify(data))
    localStorage.setItem('token', data.token)
  }

  const logout = async () => {
    try {
      await apiLogout()
    } catch (e) {
      // Ignore errors - still clear local state
    }
    user.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  return { user, isLoggedIn, isAdmin, roleLabel, init, setUser, logout }
})