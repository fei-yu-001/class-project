<script setup lang="ts">
defineProps<{
  variant?: 'success' | 'danger' | 'warning' | 'info' | 'default'
  size?: 'sm' | 'md'
}>()

const colorMap: Record<string, string> = {
  success: 'bg-success/15 text-success border border-success/25',
  danger: 'bg-danger/15 text-danger border border-danger/25',
  warning: 'bg-amber-500/15 text-amber-600 border border-amber-500/25',
  info: 'bg-blue-500/15 text-blue-600 border border-blue-500/25',
  default: 'bg-gray-500/15 text-gray-600 border border-gray-500/25'
}

const sizeMap: Record<string, string> = {
  sm: 'px-2 py-0.5 text-xs',
  md: 'px-3 py-1 text-xs font-medium'
}
</script>

<template>
  <span
    class="inline-flex items-center gap-1 rounded-full"
    :class="[colorMap[variant || 'default'], sizeMap[size || 'sm']]"
  >
    <slot />
  </span>
</template>