<script setup lang="ts">
import { ref, watch } from 'vue'
import { CheckCircle, AlertCircle, XCircle, X } from 'lucide-vue-next'

const props = defineProps<{
  message: string
  type?: 'success' | 'error' | 'info'
  duration?: number
}>()

const emit = defineEmits<{
  close: []
}>()

const visible = ref(false)
let timer: ReturnType<typeof setTimeout> | null = null

const show = () => {
  if (timer) clearTimeout(timer)
  visible.value = true
  if (props.duration && props.duration > 0) {
    timer = setTimeout(() => {
      visible.value = false
    }, props.duration)
  }
}

const close = () => {
  visible.value = false
  emit('close')
}

watch(() => props.message, (val) => {
  if (val) show()
})

const iconMap: Record<string, any> = {
  success: CheckCircle,
  error: XCircle,
  info: AlertCircle
}

const colorMap: Record<string, string> = {
  success: 'bg-success/90 text-white',
  error: 'bg-danger/90 text-white',
  info: 'bg-primary/90 text-white'
}
</script>

<template>
  <Transition name="toast">
    <div
      v-if="visible"
      class="fixed top-6 right-6 z-[1000] px-5 py-3 rounded-2xl shadow-lg backdrop-blur-md flex items-center gap-3 min-w-[240px]"
      :class="colorMap[type || 'info']"
    >
      <component :is="iconMap[type || 'info']" class="w-5 h-5 flex-shrink-0" />
      <span class="text-sm flex-1">{{ message }}</span>
      <button @click="close" class="flex-shrink-0 p-0.5 rounded-lg hover:bg-white/20 transition-colors">
        <X class="w-4 h-4" />
      </button>
    </div>
  </Transition>
</template>

<style scoped>
.toast-enter-active { transition: all 0.3s ease-out; }
.toast-leave-active { transition: all 0.2s ease-in; }
.toast-enter-from { opacity: 0; transform: translateX(40px); }
.toast-leave-to { opacity: 0; transform: translateX(40px); }
</style>