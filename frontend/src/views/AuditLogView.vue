<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import { getAuditLogs } from '@/api/auditLog'

const logs = ref<any[]>([])
const loading = ref(false)
const page = ref(0)
const totalPages = ref(1)
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAuditLogs({ page: page.value, size: 20 })
    logs.value = res.data?.content || []
    totalPages.value = res.data?.totalPages || 1
  } catch (e: any) {
    toast.value = { message: e.message || '获取日志失败', type: 'error' }
  } finally {
    loading.value = false
  }
}

const formatTime = (t: string) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(fetchData)
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />
    <div class="mb-8">
      <h1 class="text-2xl font-semibold text-gray-800">操作日志</h1>
      <p class="text-sm text-text-muted mt-1">记录所有管理操作的审计追踪</p>
    </div>

    <div class="glass rounded-2xl overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b border-black/5">
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">时间</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">操作人</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">操作</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">实体类型</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">实体ID</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">详情</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="px-4 py-8 text-center text-gray-500">加载中...</td>
            </tr>
            <tr v-else-if="logs.length === 0">
              <td colspan="6" class="px-4 py-8 text-center text-gray-500">暂无日志</td>
            </tr>
            <tr v-for="log in logs" :key="log.logId" class="border-b border-black/5 hover:bg-white/20 transition-colors">
              <td class="px-4 py-3 text-sm text-gray-700 whitespace-nowrap">{{ formatTime(log.createdAt) }}</td>
              <td class="px-4 py-3 text-sm text-gray-700">{{ log.username || '-' }}</td>
              <td class="px-4 py-3 text-sm font-mono text-primary">{{ log.action }}</td>
              <td class="px-4 py-3 text-sm text-gray-700">{{ log.entityType }}</td>
              <td class="px-4 py-3 text-sm text-gray-700">{{ log.entityId || '-' }}</td>
              <td class="px-4 py-3 text-sm text-gray-500 max-w-xs truncate">{{ log.detail || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="totalPages > 1" class="flex justify-center gap-2 mt-5">
      <button @click="page--; fetchData()" :disabled="page <= 0" class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40">上一页</button>
      <span class="px-4 py-2 text-sm text-text-muted">第 {{ page + 1 }} / {{ totalPages }} 页</span>
      <button @click="page++; fetchData()" :disabled="page >= totalPages - 1" class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40">下一页</button>
    </div>
  </AdminLayout>
</template>
