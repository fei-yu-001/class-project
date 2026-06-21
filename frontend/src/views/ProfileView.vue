<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import { getProfile, updateProfile, uploadAvatar, changePassword } from '@/api/user'
import { Camera, User, Lock, Save, Shield, ShieldCheck, Mail } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const profile = ref<any>({})
const loading = ref(false)
const avatarLoading = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}

const editForm = ref({ nickname: '', username: '' })
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const fetchProfile = async () => {
  try {
    const res: any = await getProfile()
    profile.value = res.data
    editForm.value.nickname = res.data.nickname || ''
    editForm.value.username = res.data.username || ''
    if (auth.user && res.data.avatar) {
      auth.user.avatar = res.data.avatar
      localStorage.setItem('user', JSON.stringify(auth.user))
    }
  } catch (e) {
    console.error(e)
  }
}

const handleAvatarChange = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  avatarLoading.value = true
  try {
    const res: any = await uploadAvatar(file)
    profile.value.avatar = res.data
    if (auth.user) {
      auth.user.avatar = res.data
      localStorage.setItem('user', JSON.stringify(auth.user))
    }
    await fetchProfile()
  } catch (e: any) {
    showToast(e.message || '上传失败', 'error')
  } finally {
    avatarLoading.value = false
  }
}

const handleSaveProfile = async () => {
  loading.value = true
  try {
    await updateProfile(editForm.value)
    await fetchProfile()
    if (auth.user) {
      if (editForm.value.nickname) {
        auth.user.nickname = editForm.value.nickname
      }
      if (editForm.value.username) {
        auth.user.username = editForm.value.username
      }
      localStorage.setItem('user', JSON.stringify(auth.user))
    }
    showToast('保存成功', 'success')
  } catch (e: any) {
    showToast(e.message || '保存失败', 'error')
  } finally {
    loading.value = false
  }
}

const handleChangePassword = async () => {
  passwordError.value = ''
  passwordSuccess.value = ''

  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    passwordError.value = '请填写完整'
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = '两次密码不一致'
    return
  }

  try {
    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    passwordSuccess.value = '密码修改成功'
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e: any) {
    passwordError.value = e.message || '修改失败'
  }
}

onMounted(fetchProfile)
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />
    <div class="max-w-5xl mx-auto">
      <h1 class="text-2xl font-bold text-text-primary mb-6">个人中心</h1>

      <!-- 顶部资料卡 -->
      <div class="glass rounded-2xl overflow-hidden mb-6">
        <div class="h-32 bg-gradient-to-r from-primary/40 to-accent/40"></div>
        <div class="px-8 pb-8 -mt-16 relative">
          <div class="flex flex-col items-center">
            <div class="relative group cursor-pointer" @click="($refs.avatarInput as HTMLInputElement).click()">
              <div
                class="w-32 h-32 rounded-full overflow-hidden border-4 border-white/80 shadow-lg"
                :class="avatarLoading ? 'opacity-50' : ''"
              >
                <img v-if="profile.avatar" :src="profile.avatar" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full bg-white/80 flex items-center justify-center">
                  <User class="w-14 h-14 text-text-muted/50" />
                </div>
              </div>
              <div class="absolute inset-0 rounded-full bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                <Camera class="w-7 h-7 text-white" />
              </div>
              <input ref="avatarInput" type="file" accept="image/*" class="hidden" @change="handleAvatarChange" />
            </div>

            <h2 class="text-xl font-bold text-text-primary mt-4">
              {{ editForm.nickname || profile.username || '未设置昵称' }}
            </h2>

            <div class="mt-3 flex items-center gap-2">
              <span
                class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium border"
                :class="auth.user?.role === 'ADMIN'
                  ? 'bg-blue-500/15 text-blue-600 border-blue-500/25'
                  : 'bg-gray-500/15 text-gray-600 border-gray-500/25'"
              >
                <ShieldCheck v-if="auth.isAdmin" class="w-3.5 h-3.5" />
                <Shield v-else class="w-3.5 h-3.5" />
                {{ auth.roleLabel }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 下方内容 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 基本信息 -->
        <div class="lg:col-span-2 glass rounded-2xl p-6">
          <h3 class="text-lg font-semibold text-text-primary mb-5 flex items-center gap-2">
            <User class="w-5 h-5 text-accent" /> 基本信息
          </h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div>
              <label class="text-sm text-text-muted mb-2 block">用户名</label>
              <div class="relative">
                <Mail class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
                <input
                  v-model="editForm.username"
                  class="glass-input w-full pl-9 pr-4 py-3 rounded-xl text-sm"
                />
              </div>
              <p class="text-xs text-text-muted mt-1">用户名可用于登录</p>
            </div>
            <div>
              <label class="text-sm text-text-muted mb-2 block">昵称</label>
              <input
                v-model="editForm.nickname"
                class="glass-input w-full px-4 py-3 rounded-xl text-sm"
                placeholder="请输入昵称"
              />
            </div>
          </div>

          <button
            @click="handleSaveProfile"
            :disabled="loading"
            class="glass-btn w-full mt-6 py-3 rounded-xl text-sm font-medium flex items-center justify-center gap-2 disabled:opacity-50"
          >
            <Save class="w-4 h-4" />
            {{ loading ? '保存中...' : '保存资料' }}
          </button>
        </div>

        <!-- 修改密码 -->
        <div class="glass rounded-2xl p-6">
          <h3 class="text-lg font-semibold text-text-primary mb-5 flex items-center gap-2">
            <Lock class="w-5 h-5 text-accent" /> 修改密码
          </h3>
          <div class="space-y-4">
            <div>
              <label class="text-sm text-text-muted mb-2 block">原密码</label>
              <input
                v-model="passwordForm.oldPassword"
                type="password"
                class="glass-input w-full px-4 py-3 rounded-xl text-sm"
                placeholder="请输入原密码"
              />
            </div>
            <div>
              <label class="text-sm text-text-muted mb-2 block">新密码</label>
              <input
                v-model="passwordForm.newPassword"
                type="password"
                class="glass-input w-full px-4 py-3 rounded-xl text-sm"
                placeholder="请输入新密码"
              />
            </div>
            <div>
              <label class="text-sm text-text-muted mb-2 block">确认新密码</label>
              <input
                v-model="passwordForm.confirmPassword"
                type="password"
                class="glass-input w-full px-4 py-3 rounded-xl text-sm"
                placeholder="请再次输入新密码"
              />
            </div>

            <p v-if="passwordError" class="text-danger text-sm">{{ passwordError }}</p>
            <p v-if="passwordSuccess" class="text-success text-sm">{{ passwordSuccess }}</p>

            <button
              @click="handleChangePassword"
              class="glass-btn w-full py-3 rounded-xl text-sm font-medium"
            >
              修改密码
            </button>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>
