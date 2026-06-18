<script setup lang="ts">
import { X } from 'lucide-vue-next'

defineProps<{
  title: string
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()
</script>

<template>
  <Transition name="modal">
    <div v-if="visible" class="fixed inset-0 flex items-center justify-center" style="z-index: 100">
      <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="emit('close')" />
      <div class="relative glass-strong rounded-2xl w-full max-w-lg mx-4 overflow-hidden" style="animation: modalIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)">
        <div class="flex items-center justify-between px-6 py-4 border-b border-white/10">
          <h3 class="text-lg font-semibold font-serif">{{ title }}</h3>
          <button @click="emit('close')" class="p-1 rounded-lg hover:bg-white/10 transition-colors">
            <X class="w-5 h-5" />
          </button>
        </div>
        <div class="px-6 py-4">
          <slot />
        </div>
        <div class="flex justify-end gap-3 px-6 py-4 border-t border-white/10">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.25s ease;
}
.modal-enter-from, .modal-leave-to {
  opacity: 0;
}
@keyframes modalIn {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
</style>
