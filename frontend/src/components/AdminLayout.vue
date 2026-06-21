<script setup lang="ts">
import SideBar from '@/components/SideBar.vue'
import TopNav from '@/components/TopNav.vue'
</script>

<template>
  <div class="relative min-h-screen overflow-hidden">
    <div
      class="fixed inset-0 bg-cover bg-center bg-no-repeat brightness-[0.72] saturate-[0.7]"
      style="background-image: url('/dashboard-bg.jpg');"
    />
    <div class="fixed inset-0 bg-white/55 backdrop-blur-[1px]" />
    <SideBar />
    <TopNav />
    <div class="ml-56 pt-20 p-8 relative z-10">
      <div class="page-enter">
        <slot />
      </div>
    </div>
  </div>
</template>