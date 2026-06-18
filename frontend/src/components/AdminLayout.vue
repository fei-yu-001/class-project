<script setup lang="ts">
import SideBar from '@/components/SideBar.vue'
import TopNav from '@/components/TopNav.vue'
</script>

<template>
  <div class="relative min-h-screen overflow-hidden">
    <div
      class="fixed inset-0 bg-cover bg-center bg-no-repeat"
      style="background-image: url('/dashboard-bg.jpg');"
    />
    <div class="fixed inset-0 bg-white/30 backdrop-blur-[2px]" />
    <SideBar />
    <TopNav />
    <div class="ml-56 pt-20 p-8 relative z-10">
      <div class="page-enter">
        <slot />
      </div>
    </div>
  </div>
</template>