<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import { getProfile, updateProfile, uploadAvatar, changePassword } from '@/api/user'
import { Camera, User, Lock, Save, ChevronDown, ChevronUp, Shield, ShieldCheck } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const profile = ref<any>({})
const loading = ref(false)
const avatarLoading = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')

const editForm = ref({ nickname: '', username: '' })
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const showPasswordSection = ref(false)

const fetchProfile = async () => {
  try {
    const res: any = await getProfile()
    profile.value = res.data
    editForm.value.nickname = res.data.nickname || ''
    editForm.value.username = res.data.username || ''
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
    await fetchProfile()
  } catch (e: any) {
    alert(e.message || '上传失败')
  } finally {
    avatarLoading.value = false
  }
}

const handleSaveProfile = async () => {
  loading.value = true
  try {
    await updateProfile(editForm.value)
    await fetchProfile()
    alert('保存成功')
  } catch (e: any) {
    alert(e.message || '保存失败')
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
      <div class="max-w-3xl">


        <div class="glass rounded-2xl p-8 mb-6">
          <div class="flex flex-col items-center mb-8">
            <div class="relative group cursor-pointer" @click="($refs.avatarInput as HTMLInputElement).click()">
              <div class="w-28 h-28 rounded-full overflow-hidden border-2 border-white/10"
                :class="avatarLoading ? 'opacity-50' : ''">
                <img
                  v-if="profile.avatar"
                  :src="profile.avatar"
                  class="w-full h-full object-cover"
                />
                <div v-else class="w-full h-full bg-white/5 flex items-center justify-center">
                  <User class="w-12 h-12 text-white/20" />
                </div>
              </div>
              <div class="absolute inset-0 rounded-full bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                <Camera class="w-6 h-6 text-white" />
              </div>
              <input
                ref="avatarInput"
                type="file"
                accept="image/*"
                class="hidden"
                @change="handleAvatarChange"
              />
            </div>
            <p class="text-sm text-text-muted mt-3">点击头像上传新图片</p>

            <div class="mt-4 flex items-center gap-2">
              <span
                class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium"
                :class="auth.isAdmin
                  ? 'bg-amber-500/15 text-amber-600 border border-amber-500/25'
                  : 'bg-blue-500/15 text-blue-600 border border-blue-500/25'"
              >
                <ShieldCheck v-if="auth.isAdmin" class="w-3.5 h-3.5" />
                <Shield v-else class="w-3.5 h-3.5" />
                {{ auth.isAdmin ? '管理员' : '普通用户' }}
              </span>
            </div>
          </div>

          <div class="space-y-5">
            <div>
              <label class="text-sm text-text-muted mb-2 block">用户名</label>
              <input
                v-model="editForm.username"
                class="glass-input w-full px-4 py-3 rounded-xl text-sm"
                placeholder="用户名"
              />
            </div>
            <div>
              <label class="text-sm text-text-muted mb-2 block">昵称</label>
              <input
                v-model="editForm.nickname"
                class="glass-input w-full px-4 py-3 rounded-xl text-sm"
                placeholder="昵称"
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

        <div class="glass rounded-2xl p-8">
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center gap-3">
              <Lock class="w-5 h-5 text-accent" />
              <h3 class="text-lg font-semibold">修改密码</h3>
            </div>
            <button
              @click="showPasswordSection = !showPasswordSection"
              class="w-8 h-8 rounded-lg flex items-center justify-center hover:bg-black/5 transition-colors"
            >
              <ChevronUp v-if="showPasswordSection" class="w-5 h-5 text-primary" />
              <ChevronDown v-else class="w-5 h-5 text-primary" />
            </button>
          </div>

          <div v-if="showPasswordSection" class="space-y-4">
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
  </AdminLayout>
</template>
