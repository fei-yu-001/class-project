<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { UserCircle, Power, Bell } from 'lucide-vue-next'
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const showLogoutConfirm = ref(false)

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/dashboard': '仪表盘',
    '/employees': '员工管理',
    '/salaries': '工资管理',
    '/departments': '部门管理',
    '/positions': '职位管理',
    '/payment-methods': '支付方式',
    '/profile': '个人中心',
    '/settings': '设置',
    '/system': '系统中心',
  }
  return titles[route.path] || '工资管理系统'
})

const handleLogout = () => {
  showLogoutConfirm.value = true
}

const confirmLogout = () => {
  showLogoutConfirm.value = false
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <header
    class="fixed top-0 right-0 h-16 flex items-center justify-between px-6 z-40 transition-all duration-300"
    style="left: 14rem; background: rgba(255,255,255,0.25); backdrop-filter: blur(20px);"
  >
    <h1 class="text-xl font-serif font-semibold text-gray-800">{{ pageTitle }}</h1>

    <div class="flex items-center gap-4">
      <button class="p-2 rounded-xl hover:bg-white/20 transition-colors relative">
        <Bell class="w-5 h-5 text-gray-600" />
        <span class="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500"></span>
      </button>

      <div class="flex items-center gap-3 pl-4" style="border-left: 1px solid rgba(0,0,0,0.08);">
        <img
          v-if="auth.user?.avatar"
          :src="auth.user.avatar"
          class="w-9 h-9 rounded-full object-cover border-2 border-white/50 shadow-sm"
        />
        <div
          v-else
          class="w-9 h-9 rounded-full flex items-center justify-center border-2 border-white/50 shadow-sm"
          style="background: rgba(45,139,132,0.15);"
        >
          <UserCircle class="w-5 h-5 text-primary" />
        </div>
        <div class="hidden sm:block">
          <p class="text-sm font-medium text-gray-800">{{ auth.user?.nickname || auth.user?.username }}</p>
          <p class="text-xs" :class="auth.user?.role === 'SUPER_ADMIN' ? 'text-amber-600 font-semibold' : auth.user?.role === 'ADMIN' ? 'text-blue-600' : 'text-gray-500'">{{ auth.roleLabel }}</p>
        </div>
        <button
          @click="handleLogout"
          class="p-2 rounded-xl text-gray-500 hover:text-red-500 hover:bg-red-50 transition-all"
          title="退出登录"
        >
          <Power class="w-4 h-4" />
        </button>
      </div>
    </div>
  </header>

  <Transition name="fade">
    <div
      v-if="showLogoutConfirm"
      class="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-[100]"
      @click="showLogoutConfirm = false"
    >
      <div
        class="rounded-3xl p-8 w-full max-w-sm mx-4 text-center"
        style="background: rgba(255,255,255,0.85); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.6); box-shadow: 0 25px 50px rgba(0,0,0,0.15);"
        @click.stop
      >
        <div class="w-16 h-16 rounded-full mx-auto mb-4 flex items-center justify-center" style="background: rgba(217,79,79,0.1);">
          <Power class="w-8 h-8 text-red-500" />
        </div>

        <h3 class="text-lg font-semibold mb-2 text-gray-800">确认退出登录？</h3>
        <p class="text-sm text-gray-500 mb-6">退出后需要重新登录才能访问系统</p>

        <div class="flex items-center justify-center gap-3 mb-6">
          <img
            v-if="auth.user?.avatar"
            :src="auth.user.avatar"
            class="w-10 h-10 rounded-full object-cover border border-white/50 shadow-sm"
          />
          <div
            v-else
            class="w-10 h-10 rounded-full flex items-center justify-center"
            style="background: rgba(45,139,132,0.15);"
          >
            <UserCircle class="w-5 h-5 text-primary" />
          </div>
          <span class="text-sm font-medium text-gray-800">{{ auth.user?.nickname || auth.user?.username }}</span>
        </div>

        <div class="flex gap-3">
          <button
            @click="showLogoutConfirm = false"
            class="flex-1 py-2.5 rounded-xl text-sm font-medium bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors"
          >
            取消
          </button>
          <button
            @click="confirmLogout"
            class="flex-1 py-2.5 rounded-xl text-sm font-medium text-white bg-red-500 hover:bg-red-600 transition-colors"
          >
            确认退出
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
