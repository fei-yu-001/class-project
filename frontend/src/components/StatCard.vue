<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import type { Component } from 'vue'

const props = defineProps<{
  title: string
  value: number
  icon: Component
  color: string
  suffix?: string
}>()

const displayValue = ref(0)

const animate = (target: number) => {
  const duration = 800
  const start = displayValue.value
  const diff = target - start
  const startTime = performance.now()

  const step = (currentTime: number) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    displayValue.value = Math.round(start + diff * eased)
    if (progress < 1) requestAnimationFrame(step)
  }

  requestAnimationFrame(step)
}

onMounted(() => animate(props.value))
watch(() => props.value, (v) => animate(v))
</script>

<template>
  <div class="glass rounded-2xl p-5 hover:-translate-y-1 transition-all duration-300 group">
    <div class="flex items-start justify-between">
      <div>
        <p class="text-sm text-text-muted mb-1">{{ title }}</p>
        <p class="text-3xl font-bold font-serif" :style="{ color }">
          {{ displayValue.toLocaleString() }}{{ suffix || '' }}
        </p>
      </div>
      <div
        class="w-11 h-11 rounded-xl flex items-center justify-center transition-transform group-hover:scale-110"
        :style="{ background: color + '20' }"
      >
        <component :is="icon" class="w-5 h-5" :style="{ color }" />
      </div>
    </div>
  </div>
</template>
