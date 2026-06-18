<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  LayoutDashboard, Users, DollarSign, Building2,
  Briefcase, CreditCard, ChevronLeft, ChevronRight,
  UserCircle, Settings, Info, FolderKanban, ClipboardCheck,
  CalendarCheck, FileText, Clock, ArrowRightLeft, Crown, Database
} from 'lucide-vue-next'
import { ref, computed } from 'vue'

const route = useRoute()
const auth = useAuthStore()
const collapsed = ref(false)

const menuItems = [
  { path: '/dashboard', label: '仪表盘', icon: LayoutDashboard, roles: ['USER', 'ADMIN', 'SUPER_ADMIN'] },
  { path: '/employees', label: '员工管理', icon: Users, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/salaries', label: '工资管理', icon: DollarSign, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/departments', label: '部门管理', icon: Building2, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/positions', label: '职位管理', icon: Briefcase, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/payment-methods', label: '支付方式', icon: CreditCard, roles: ['ADMIN', 'SUPER_ADMIN'] },
]

const hrItems = [
  { path: '/projects', label: '项目管理', icon: FolderKanban, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/performance', label: '绩效考核', icon: ClipboardCheck, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/attendance', label: '考勤管理', icon: CalendarCheck, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/leaves', label: '请假申请', icon: FileText, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/overtime', label: '加班记录', icon: Clock, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/position-changes', label: '职位变动', icon: ArrowRightLeft, roles: ['ADMIN', 'SUPER_ADMIN'] },
  { path: '/schema', label: '数据库 Schema', icon: Database, roles: ['ADMIN', 'SUPER_ADMIN'] },
]

const systemItems = [
  { path: '/profile', label: '个人中心', icon: UserCircle, roles: ['USER', 'ADMIN', 'SUPER_ADMIN'] },
  { path: '/settings', label: '设置', icon: Settings, roles: ['USER', 'ADMIN', 'SUPER_ADMIN'] },
  { path: '/system', label: '系统中心', icon: Info, roles: ['SUPER_ADMIN'] },
]

const currentRole = computed(() => auth.user?.role || 'USER')

const filteredMenuItems = computed(() => menuItems.filter(item => item.roles.includes(currentRole.value)))
const filteredHrItems = computed(() => hrItems.filter(item => item.roles.includes(currentRole.value)))
const filteredSystemItems = computed(() => systemItems.filter(item => item.roles.includes(currentRole.value)))

const isActive = (path: string) => route.path === path
</script>

<template>
  <aside
    class="fixed left-0 top-0 h-full flex flex-col transition-all duration-300"
    :class="collapsed ? 'w-16' : 'w-56'"
    style="z-index: 50; background: rgba(255,255,255,0.35); backdrop-filter: blur(20px);"
  >
    <div class="flex items-center justify-between p-4">
      <div v-if="!collapsed" class="flex items-center gap-2">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-primary-dark flex items-center justify-center">
          <DollarSign class="w-5 h-5 text-white" />
        </div>
        <span class="font-serif font-bold text-lg text-gradient">工资系统</span>
      </div>
      <button
        @click="collapsed = !collapsed"
        class="p-1.5 rounded-lg hover:bg-white/10 transition-colors"
        :class="collapsed ? 'mx-auto' : ''"
      >
        <ChevronRight v-if="collapsed" class="w-5 h-5" />
        <ChevronLeft v-else class="w-5 h-5" />
      </button>
    </div>

    <nav class="flex-1 py-4 px-2 space-y-1 overflow-y-auto scrollbar-thin">
      <router-link
        v-for="item in filteredMenuItems"
        :key="item.path"
        :to="item.path"
        class="flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 group"
        :class="isActive(item.path)
          ? 'bg-primary/20 text-primary border border-primary/30'
          : 'text-text-secondary hover:bg-white/5 hover:text-white'"
      >
        <component :is="item.icon" class="w-5 h-5 shrink-0" />
        <span v-if="!collapsed" class="text-sm font-medium">{{ item.label }}</span>
      </router-link>

      <template v-if="filteredHrItems.length > 0">
        <div v-if="!collapsed" class="mt-4 mb-2 px-3">
          <p class="text-xs text-text-muted uppercase tracking-wider">人事</p>
        </div>
        <div v-else class="mt-4 mb-2 flex justify-center">
          <div class="w-6 h-px bg-white/10"></div>
        </div>

        <router-link
          v-for="item in filteredHrItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 group"
          :class="isActive(item.path)
            ? 'bg-primary/20 text-primary border border-primary/30'
            : 'text-text-secondary hover:bg-white/5 hover:text-white'"
        >
          <component :is="item.icon" class="w-5 h-5 shrink-0" />
          <span v-if="!collapsed" class="text-sm font-medium">{{ item.label }}</span>
        </router-link>
      </template>

      <div v-if="!collapsed" class="mt-4 mb-2 px-3">
        <p class="text-xs text-text-muted uppercase tracking-wider">系统</p>
      </div>
      <div v-else class="mt-4 mb-2 flex justify-center">
        <div class="w-6 h-px bg-white/10"></div>
      </div>

      <router-link
        v-for="item in filteredSystemItems"
        :key="item.path"
        :to="item.path"
        class="flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 group"
        :class="isActive(item.path)
          ? 'bg-primary/20 text-primary border border-primary/30'
          : 'text-text-secondary hover:bg-white/5 hover:text-white'"
      >
        <component :is="item.icon" class="w-5 h-5 shrink-0" />
        <span v-if="!collapsed" class="text-sm font-medium">{{ item.label }}</span>
        <Crown v-if="item.path === '/system' && !collapsed" class="w-3 h-3 text-amber-500" />
      </router-link>
    </nav>
  </aside>
</template>
