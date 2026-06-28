<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import { getAllConfigs, updateConfig } from '@/api/sysConfig'
import { Pencil, Save, X } from 'lucide-vue-next'

const configs = ref<any[]>([])
const loading = ref(false)
const editingKey = ref<string | null>(null)
const editValue = ref('')
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllConfigs()
    configs.value = res.data || []
  } catch (e: any) {
    showToast(e.message || '获取配置失败', 'error')
  } finally {
    loading.value = false
  }
}

const startEdit = (key: string, value: string) => {
  editingKey.value = key
  editValue.value = value
}

const cancelEdit = () => {
  editingKey.value = null
  editValue.value = ''
}

const saveConfig = async (key: string) => {
  try {
    await updateConfig(key, editValue.value)
    showToast('配置已更新', 'success')
    editingKey.value = null
    fetchData()
  } catch (e: any) {
    showToast(e.message || '更新失败', 'error')
  }
}

onMounted(fetchData)
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />
    <div class="mb-8">
      <h1 class="text-2xl font-semibold text-gray-800">系统配置</h1>
      <p class="text-sm text-text-muted mt-1">管理薪资计算规则、社保费率等系统参数</p>
    </div>

    <div class="glass rounded-2xl overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-black/5">
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">配置项</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">当前值</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">说明</th>
            <th class="px-4 py-3 text-sm font-medium text-gray-600" style="text-align: center; width: 120px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="4" class="px-4 py-8 text-center text-gray-500">加载中...</td>
          </tr>
          <tr v-else-if="configs.length === 0">
            <td colspan="4" class="px-4 py-8 text-center text-gray-500">暂无配置</td>
          </tr>
          <tr v-for="item in configs" :key="item.configKey" class="border-b border-black/5 hover:bg-white/20 transition-colors">
            <td class="px-4 py-3 text-sm font-medium text-gray-700">{{ item.configKey }}</td>
            <td class="px-4 py-3 text-sm">
              <template v-if="editingKey === item.configKey">
                <input v-model="editValue" class="glass-input px-3 py-1.5 rounded-lg text-sm w-40" />
              </template>
              <template v-else>
                <span class="font-mono text-primary font-semibold">{{ item.configValue }}</span>
              </template>
            </td>
            <td class="px-4 py-3 text-sm text-gray-500">{{ item.description || '-' }}</td>
            <td class="px-4 py-3 text-center">
              <div class="inline-flex items-center gap-1">
                <template v-if="editingKey === item.configKey">
                  <button @click="saveConfig(item.configKey)" class="w-7 h-7 rounded-lg text-success hover:bg-success/10 flex items-center justify-center" title="保存">
                    <Save class="w-3.5 h-3.5" />
                  </button>
                  <button @click="cancelEdit" class="w-7 h-7 rounded-lg text-gray-500 hover:bg-gray-100 flex items-center justify-center" title="取消">
                    <X class="w-3.5 h-3.5" />
                  </button>
                </template>
                <template v-else>
                  <button @click="startEdit(item.configKey, item.configValue)" class="w-7 h-7 rounded-lg text-primary hover:bg-primary/10 flex items-center justify-center" title="编辑">
                    <Pencil class="w-3.5 h-3.5" />
                  </button>
                </template>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminLayout>
</template>
