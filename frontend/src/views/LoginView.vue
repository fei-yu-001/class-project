<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { login, register } from '@/api/auth'
import BubbleBackground from '@/components/BubbleBackground.vue'
import { User, Lock, Leaf, Eye, EyeOff } from 'lucide-vue-next'

const router = useRouter()
const auth = useAuthStore()
const showLogin = ref(false)
const showRegister = ref(false)
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)
const registrationEnabled = import.meta.env.VITE_REGISTRATION_ENABLED === 'true'

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '', nickname: '' })

const openLogin = () => { showLogin.value = true; showRegister.value = false; error.value = '' }
const openRegister = () => {
  if (!registrationEnabled) return
  showRegister.value = true
  showLogin.value = false
  error.value = ''
}
const closeAll = () => { showLogin.value = false; showRegister.value = false; error.value = '' }

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    error.value = '请填写完整信息'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res: any = await login(loginForm.value)
    auth.setUser(res.data)
    router.push('/dashboard')
  } catch (e: any) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password) {
    error.value = '请填写完整信息'
    return
  }
  if (registerForm.value.username.length < 3) {
    error.value = '用户名至少3个字符'
    return
  }
  if (registerForm.value.password.length < 6) {
    error.value = '密码至少6个字符'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await register(registerForm.value)
    registerForm.value = { username: '', password: '', nickname: '' }
    showRegister.value = false
    error.value = '注册成功，请登录'
    setTimeout(() => {
      error.value = ''
      openLogin()
    }, 1500)
  } catch (e: any) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="relative w-full h-screen overflow-hidden">
    <div
      class="absolute inset-0 bg-cover bg-center bg-no-repeat"
      style="background-image: url('/bg.jpg');"
    />
    <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-black/20 to-black/40" />

    <BubbleBackground />

    <div class="relative z-10 flex flex-col items-center justify-center h-full px-4">
      <div class="animate-float mb-6">
        <img
          src="/logo.gif"
          alt="logo"
          class="w-36 h-36 rounded-3xl object-cover"
          style="box-shadow: 0 8px 32px rgba(0,0,0,0.3)"
        />
      </div>

      <h1 class="text-5xl md:text-6xl font-bold text-white mb-10 text-center tracking-widest"
        style="font-family: 'ZCOOL XiaoWei', 'Ma Shan Zheng', 'Noto Serif SC', serif; text-shadow: 0 4px 20px rgba(0,0,0,0.4);">
        工资管理系统
      </h1>

      <div class="flex gap-4">
        <button
          @click="openLogin"
          class="px-10 py-3 rounded-2xl text-base font-medium text-white border border-white/20 bg-white/5 backdrop-blur-md hover:bg-white/10 hover:border-white/40 transition-all duration-300"
        >
          登录
        </button>
        <button
          v-if="registrationEnabled"
          @click="openRegister"
          class="px-10 py-3 rounded-2xl text-base font-medium text-white border border-white/20 bg-white/5 backdrop-blur-md hover:bg-white/10 hover:border-white/40 transition-all duration-300"
        >
          注册
        </button>
      </div>

      <Transition name="slide-up">
        <div
          v-if="showLogin || showRegister"
          class="fixed inset-0 flex items-center justify-center"
          style="z-index: 100"
        >
          <div class="absolute inset-0 bg-black/40" @click="closeAll" />

          <div
            v-if="showLogin"
            class="relative w-full max-w-sm mx-4 p-8 rounded-3xl"
            style="background: rgba(255,255,255,0.03); backdrop-filter: blur(40px); -webkit-backdrop-filter: blur(40px); border: 1px solid rgba(255,255,255,0.08); box-shadow: 0 25px 50px rgba(0,0,0,0.3); animation: popIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1)"
          >
            <div class="text-center mb-8">
              <div class="w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-4"
                style="background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.1);">
                <Leaf class="w-5 h-5 text-white/60" />
              </div>
              <h2 class="text-xl font-medium text-white/90">欢迎回来</h2>
            </div>

            <div class="space-y-4">
              <div class="relative">
                <User class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-white/30" />
                <input
                  v-model="loginForm.username"
                  type="text"
                  placeholder="用户名"
                  class="w-full pl-11 pr-4 py-3 rounded-xl text-sm text-white placeholder-white/25 bg-white/[0.03] border border-white/[0.06] focus:border-white/20 focus:bg-white/[0.06] focus:outline-none transition-all"
                  @keyup.enter="handleLogin"
                />
              </div>
              <div class="relative">
                <Lock class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-white/30" />
                <input
                  v-model="loginForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="密码"
                  class="w-full pl-11 pr-11 py-3 rounded-xl text-sm text-white placeholder-white/25 bg-white/[0.03] border border-white/[0.06] focus:border-white/20 focus:bg-white/[0.06] focus:outline-none transition-all"
                  @keyup.enter="handleLogin"
                />
                <button
                  @click="showPassword = !showPassword"
                  class="absolute right-4 top-1/2 -translate-y-1/2 text-white/25 hover:text-white/50"
                >
                  <Eye v-if="showPassword" class="w-4 h-4" />
                  <EyeOff v-else class="w-4 h-4" />
                </button>
              </div>
            </div>

            <p v-if="error" class="text-red-300/80 text-sm mt-3 text-center">{{ error }}</p>

            <button
              @click="handleLogin"
              :disabled="loading"
              class="w-full mt-6 py-3 rounded-xl text-sm font-medium text-white disabled:opacity-40 transition-all"
              style="background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.12);"
              @mouseover="($event.target as HTMLElement).style.background='rgba(255,255,255,0.14)'"
              @mouseout="($event.target as HTMLElement).style.background='rgba(255,255,255,0.08)'"
            >
              {{ loading ? '登录中...' : '登录' }}
            </button>

            <p class="text-center text-sm text-white/30 mt-5">
              <span v-if="registrationEnabled">
                还没有账号？
                <button @click="openRegister" class="text-white/60 hover:text-white/90 transition-colors">立即注册</button>
              </span>
              <span v-else>账号由管理员创建</span>
            </p>
          </div>

          <div
            v-if="showRegister"
            class="relative w-full max-w-sm mx-4 p-8 rounded-3xl"
            style="background: rgba(255,255,255,0.03); backdrop-filter: blur(40px); -webkit-backdrop-filter: blur(40px); border: 1px solid rgba(255,255,255,0.08); box-shadow: 0 25px 50px rgba(0,0,0,0.3); animation: popIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1)"
          >
            <div class="text-center mb-8">
              <div class="w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-4"
                style="background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.1);">
                <Leaf class="w-5 h-5 text-white/60" />
              </div>
              <h2 class="text-xl font-medium text-white/90">创建账号</h2>
            </div>

            <div class="space-y-4">
              <div class="relative">
                <User class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-white/30" />
                <input
                  v-model="registerForm.username"
                  type="text"
                  placeholder="用户名"
                  class="w-full pl-11 pr-4 py-3 rounded-xl text-sm text-white placeholder-white/25 bg-white/[0.03] border border-white/[0.06] focus:border-white/20 focus:bg-white/[0.06] focus:outline-none transition-all"
                />
              </div>
              <div class="relative">
                <User class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-white/30" />
                <input
                  v-model="registerForm.nickname"
                  type="text"
                  placeholder="昵称（可选）"
                  class="w-full pl-11 pr-4 py-3 rounded-xl text-sm text-white placeholder-white/25 bg-white/[0.03] border border-white/[0.06] focus:border-white/20 focus:bg-white/[0.06] focus:outline-none transition-all"
                />
              </div>
              <div class="relative">
                <Lock class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-white/30" />
                <input
                  v-model="registerForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="密码"
                  class="w-full pl-11 pr-11 py-3 rounded-xl text-sm text-white placeholder-white/25 bg-white/[0.03] border border-white/[0.06] focus:border-white/20 focus:bg-white/[0.06] focus:outline-none transition-all"
                  @keyup.enter="handleRegister"
                />
                <button
                  @click="showPassword = !showPassword"
                  class="absolute right-4 top-1/2 -translate-y-1/2 text-white/25 hover:text-white/50"
                >
                  <Eye v-if="showPassword" class="w-4 h-4" />
                  <EyeOff v-else class="w-4 h-4" />
                </button>
              </div>
            </div>

            <p v-if="error" class="text-red-300/80 text-sm mt-3 text-center">{{ error }}</p>

            <button
              @click="handleRegister"
              :disabled="loading"
              class="w-full mt-6 py-3 rounded-xl text-sm font-medium text-white disabled:opacity-40 transition-all"
              style="background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.12);"
              @mouseover="($event.target as HTMLElement).style.background='rgba(255,255,255,0.14)'"
              @mouseout="($event.target as HTMLElement).style.background='rgba(255,255,255,0.08)'"
            >
              {{ loading ? '注册中...' : '注册' }}
            </button>

            <p class="text-center text-sm text-white/30 mt-5">
              已有账号？
              <button @click="openLogin" class="text-white/60 hover:text-white/90 transition-colors">立即登录</button>
            </p>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Ma+Shan+Zheng&family=ZCOOL+XiaoWei&display=swap');

.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.3s ease;
}
.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0;
}
.slide-up-enter-from > div:last-child,
.slide-up-leave-to > div:last-child {
  transform: translateY(30px);
}
@keyframes popIn {
  from { opacity: 0; transform: scale(0.9) translateY(20px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
</style>
