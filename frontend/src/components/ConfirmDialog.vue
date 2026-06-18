<script setup lang="ts">
import { AlertTriangle } from 'lucide-vue-next'

defineProps<{
  visible: boolean
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  danger?: boolean
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <Transition name="modal">
    <div v-if="visible" class="fixed inset-0 z-[1100] flex items-center justify-center">
      <div class="absolute inset-0 bg-black/30 backdrop-blur-sm" @click="emit('cancel')" />
      <div class="relative w-full max-w-sm mx-4 p-6 rounded-2xl animate-pop-in"
        style="background: rgba(255,255,255,0.85); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.5); box-shadow: 0 25px 50px rgba(0,0,0,0.15);">
        <div class="flex flex-col items-center text-center mb-6">
          <div class="w-12 h-12 rounded-full flex items-center justify-center mb-3"
            :class="danger ? 'bg-danger/10' : 'bg-amber-500/10'">
            <AlertTriangle class="w-6 h-6" :class="danger ? 'text-danger' : 'text-amber-500'" />
          </div>
          <h3 class="text-lg font-semibold mb-1">{{ title }}</h3>
          <p class="text-sm text-text-muted">{{ message }}</p>
        </div>
        <div class="flex gap-3">
          <button
            @click="emit('cancel')"
            class="flex-1 glass-btn-secondary px-4 py-2.5 rounded-xl text-sm"
          >
            {{ cancelText || '取消' }}
          </button>
          <button
            @click="emit('confirm')"
            class="flex-1 px-4 py-2.5 rounded-xl text-sm text-white font-medium"
            :class="danger ? 'bg-danger hover:bg-danger/90' : 'glass-btn'"
          >
            {{ confirmText || '确认' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-enter-active { transition: all 0.25s ease-out; }
.modal-leave-active { transition: all 0.15s ease-in; }
.modal-enter-from,
.modal-leave-to { opacity: 0; }
.modal-enter-from > div:last-child { transform: scale(0.95); }
@keyframes pop-in {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
.animate-pop-in { animation: pop-in 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
</style>