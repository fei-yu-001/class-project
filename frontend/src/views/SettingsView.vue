<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '@/components/AdminLayout.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import {
  Palette, Bell, Moon, Globe, Database, Shield,
  Check, ChevronRight, Settings
} from 'lucide-vue-next'

const router = useRouter()

interface SettingItem {
  id: string
  icon: any
  title: string
  description: string
  type: 'toggle' | 'select' | 'action'
  value?: boolean | string
  options?: string[]
}

const settings = ref<SettingItem[]>([
  {
    id: 'theme',
    icon: Palette,
    title: '主题风格',
    description: '切换系统主题配色',
    type: 'select',
    value: 'dark',
    options: ['dark', 'light', 'auto']
  },
  {
    id: 'notifications',
    icon: Bell,
    title: '消息通知',
    description: '开启或关闭系统通知',
    type: 'toggle',
    value: true
  },
  {
    id: 'darkMode',
    icon: Moon,
    title: '深色模式',
    description: '使用深色主题保护视力',
    type: 'toggle',
    value: true
  },
  {
    id: 'language',
    icon: Globe,
    title: '语言设置',
    description: '选择系统显示语言',
    type: 'select',
    value: 'zh-CN',
    options: ['zh-CN', 'en-US']
  },
  {
    id: 'cache',
    icon: Database,
    title: '清除缓存',
    description: '清除本地缓存数据',
    type: 'action'
  },
  {
    id: 'security',
    icon: Shield,
    title: '安全设置',
    description: '管理登录设备和安全选项',
    type: 'action'
  }
])

const saved = ref(false)
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}

const handleToggle = (item: SettingItem) => {
  if (item.type === 'toggle') {
    item.value = !item.value
    saved.value = false
  }
}

const handleSelect = (item: SettingItem, option: string) => {
  item.value = option
  saved.value = false
}

const handleAction = (item: SettingItem) => {
  if (item.id === 'cache') {
    localStorage.clear()
    showToast('缓存已清除', 'success')
  } else if (item.id === 'security') {
    router.push('/profile')
  }
}

const handleSave = () => {
  const config = settings.value.reduce((acc, item) => {
    acc[item.id] = item.value
    return acc
  }, {} as any)
  localStorage.setItem('app_settings', JSON.stringify(config))
  saved.value = true
  showToast('保存成功', 'success')
  setTimeout(() => saved.value = false, 2000)
}

onMounted(() => {
  const saved = localStorage.getItem('app_settings')
  if (saved) {
    try {
      const config = JSON.parse(saved)
      settings.value.forEach(item => {
        if (config[item.id] !== undefined) {
          item.value = config[item.id]
        }
      })
    } catch { }
  }
})
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />
    <div class="max-w-3xl mx-auto">
      <h1 class="text-2xl font-bold text-text-primary mb-6">系统设置</h1>

      <div class="glass rounded-2xl p-6 mb-6">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center">
            <Settings class="w-6 h-6 text-primary" />
          </div>
          <div>
            <h2 class="text-lg font-semibold text-text-primary">偏好设置</h2>
          </div>
        </div>
      </div>

      <div class="space-y-3">
        <div
          v-for="item in settings"
          :key="item.id"
          class="glass rounded-2xl p-5 flex items-center justify-between hover:-translate-y-0.5 transition-all duration-200"
        >
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-xl flex items-center justify-center" style="background: rgba(255,255,255,0.08);">
              <component :is="item.icon" class="w-5 h-5 text-primary" />
            </div>
            <div>
              <h3 class="text-sm font-medium">{{ item.title }}</h3>
            </div>
          </div>

          <div v-if="item.type === 'toggle'" class="flex items-center">
            <button
              @click="handleToggle(item)"
              class="w-12 h-6 rounded-full transition-all duration-300 relative"
              :class="item.value ? 'bg-primary' : 'bg-white/10'"
            >
              <div
                class="w-5 h-5 rounded-full bg-white absolute top-0.5 transition-all duration-300"
                :class="item.value ? 'left-6' : 'left-0.5'"
              />
            </button>
          </div>

          <div v-else-if="item.type === 'select'" class="flex items-center gap-2">
            <select
              v-model="item.value"
              class="glass-input px-3 py-1.5 rounded-lg text-sm"
              @change="saved = false"
            >
              <option v-for="opt in item.options" :key="opt" :value="opt">
                {{ opt === 'zh-CN' ? '简体中文' : opt === 'en-US' ? 'English' : opt === 'dark' ? '深色' : opt === 'light' ? '浅色' : '自动' }}
              </option>
            </select>
          </div>

          <button
            v-else-if="item.type === 'action'"
            @click="handleAction(item)"
            class="p-2 rounded-lg hover:bg-white/5 transition-colors"
          >
            <ChevronRight class="w-5 h-5 text-text-muted" />
          </button>
        </div>
      </div>

      <div class="flex justify-end mt-6">
        <button
          @click="handleSave"
          class="glass-btn px-6 py-2.5 rounded-xl text-sm flex items-center gap-2"
        >
          <Check v-if="saved" class="w-4 h-4" />
          {{ saved ? '已保存' : '保存设置' }}
        </button>
      </div>
    </div>
  </AdminLayout>
</template>
