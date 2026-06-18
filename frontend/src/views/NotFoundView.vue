<script setup lang="ts">
import { useRouter } from 'vue-router'
import AdminLayout from '@/components/AdminLayout.vue'
import { Home } from 'lucide-vue-next'

const router = useRouter()
</script>

<template>
  <AdminLayout>
    <div class="flex flex-col items-center justify-center py-20">
      <div class="text-8xl font-bold text-primary/30 mb-4">404</div>
      <h1 class="text-2xl font-semibold text-gray-700 mb-2">页面未找到</h1>
      <p class="text-text-muted mb-8">您访问的页面不存在或已被移除</p>
      <button @click="router.push('/dashboard')" class="glass-btn px-6 py-3 rounded-xl text-sm flex items-center gap-2">
        <Home class="w-4 h-4" /> 返回首页
      </button>
    </div>
  </AdminLayout>
</template>