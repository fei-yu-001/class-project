<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { UserCircle, Power, Bell, Megaphone, Pin, Trash2, X, Check } from 'lucide-vue-next'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { listAnnouncements, createAnnouncement, deleteAnnouncement, toggleAnnouncementTop } from '@/api/announcement'
import ToastMessage from '@/components/ToastMessage.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const showLogoutConfirm = ref(false)

const announcements = ref<any[]>([])
const showAnnouncements = ref(false)
const showPublishModal = ref(false)
const announceForm = ref({ title: '', content: '', isTop: false })
const publishLoading = ref(false)
const announceDropdownRef = ref<HTMLElement | null>(null)

const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}

const isAdmin = computed(() => auth.user?.role === 'ADMIN' || auth.user?.role === 'SUPER_ADMIN')
const unreadCount = computed(() => announcements.value.length)

const fetchAnnouncements = async () => {
  try {
    const res: any = await listAnnouncements()
    announcements.value = res.data || []
  } catch {
    announcements.value = []
  }
}

const openPublish = () => {
  announceForm.value = { title: '', content: '', isTop: false }
  showPublishModal.value = true
  showAnnouncements.value = false
}

const handlePublish = async () => {
  if (!announceForm.value.title.trim() || !announceForm.value.content.trim()) {
    showToast('请输入标题和内容', 'error')
    return
  }
  publishLoading.value = true
  try {
    await createAnnouncement({
      title: announceForm.value.title.trim(),
      content: announceForm.value.content.trim(),
      isTop: announceForm.value.isTop
    })
    showPublishModal.value = false
    await fetchAnnouncements()
    showToast('公告发布成功', 'success')
  } catch (e: any) {
    showToast(e.message || '发布失败', 'error')
  } finally {
    publishLoading.value = false
  }
}

const handleDeleteAnnounce = async (id: number) => {
  if (!confirm('确认删除该公告？')) return
  try {
    await deleteAnnouncement(id)
    await fetchAnnouncements()
    showToast('公告已删除', 'success')
  } catch (e: any) {
    showToast(e.message || '删除失败', 'error')
  }
}

const handleToggleTop = async (id: number) => {
  try {
    await toggleAnnouncementTop(id)
    await fetchAnnouncements()
  } catch (e: any) {
    showToast(e.message || '操作失败', 'error')
  }
}

const handleClickOutside = (e: MouseEvent) => {
  if (announceDropdownRef.value && !announceDropdownRef.value.contains(e.target as Node)) {
    showAnnouncements.value = false
  }
}

onMounted(() => {
  fetchAnnouncements()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/dashboard': '仪表盘',
    '/employees': '员工管理',
    '/salaries': '工资管理',
    '/departments': '部门管理',
    '/positions': '职位管理',
    '/payment-methods': '支付方式',
    '/profile': '个人中心',
    '/settings': '设置',
    '/user-permissions': '用户权限管理',
    '/employee-comprehensive': '员工综合管理',
  }
  return titles[route.path] || '工资管理系统'
})

const handleLogout = () => {
  showLogoutConfirm.value = true
}

const confirmLogout = () => {
  showLogoutConfirm.value = false
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <header
    class="fixed top-0 right-0 h-16 flex items-center justify-between px-6 z-40 transition-all duration-300"
    style="left: 14rem; background: rgba(255,255,255,0.25); backdrop-filter: blur(20px);"
  >
    <h1 class="text-xl font-serif font-semibold text-gray-800">{{ pageTitle }}</h1>

    <div class="flex items-center gap-4">
      <div ref="announceDropdownRef" class="relative">
        <button
          @click.stop="showAnnouncements = !showAnnouncements"
          class="p-2 rounded-xl hover:bg-white/20 transition-colors relative"
        >
          <Bell class="w-5 h-5 text-gray-600" />
          <span v-if="unreadCount > 0" class="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500"></span>
        </button>

        <Transition name="dropdown">
          <div
            v-if="showAnnouncements"
            class="absolute right-0 top-full mt-2 w-80 max-h-[32rem] overflow-hidden rounded-2xl shadow-xl border border-white/40"
            style="background: rgba(255,255,255,0.92); backdrop-filter: blur(20px); z-index: 200;"
          >
            <div class="flex items-center justify-between px-4 py-3 border-b border-black/5">
              <h3 class="font-semibold text-sm text-gray-800">系统公告</h3>
              <button
                v-if="isAdmin"
                @click="openPublish"
                class="text-xs flex items-center gap-1 px-2 py-1 rounded-lg bg-primary/10 text-primary hover:bg-primary/20 transition-colors"
              >
                <Megaphone class="w-3 h-3" /> 发布公告
              </button>
            </div>

            <div class="overflow-y-auto max-h-80">
              <div v-if="announcements.length === 0" class="px-4 py-8 text-center text-sm text-gray-500">
                暂无公告
              </div>
              <div
                v-for="item in announcements"
                :key="item.announceId"
                class="px-4 py-3 border-b border-black/5 hover:bg-black/5 transition-colors"
              >
                <div class="flex items-start justify-between gap-2">
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-1.5 mb-1">
                      <Pin v-if="item.isTop" class="w-3 h-3 text-amber-500 flex-shrink-0" />
                      <h4 class="text-sm font-semibold text-gray-800 truncate">{{ item.title }}</h4>
                    </div>
                    <p class="text-xs text-gray-600 leading-relaxed whitespace-pre-wrap">{{ item.content }}</p>
                    <div class="mt-1.5 flex items-center gap-2 text-[10px] text-gray-400">
                      <span>{{ item.publisher || '管理员' }}</span>
                      <span>·</span>
                      <span>{{ item.createdAt ? new Date(item.createdAt).toLocaleString() : '' }}</span>
                    </div>
                  </div>
                  <div v-if="isAdmin" class="flex flex-col gap-1 flex-shrink-0">
                    <button
                      @click="handleToggleTop(item.announceId)"
                      :title="item.isTop ? '取消置顶' : '置顶'"
                      class="p-1.5 rounded-lg hover:bg-black/5 transition-colors"
                      :class="item.isTop ? 'text-amber-500' : 'text-gray-400'"
                    >
                      <Pin class="w-3.5 h-3.5" />
                    </button>
                    <button
                      @click="handleDeleteAnnounce(item.announceId)"
                      title="删除"
                      class="p-1.5 rounded-lg text-gray-400 hover:text-danger hover:bg-danger/10 transition-colors"
                    >
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>

      <div class="flex items-center gap-3 pl-4" style="border-left: 1px solid rgba(0,0,0,0.08);">
        <router-link
          to="/profile"
          class="flex items-center gap-3 cursor-pointer group"
          title="进入个人主页"
        >
          <img
            v-if="auth.user?.avatar"
            :src="auth.user.avatar"
            class="w-9 h-9 rounded-full object-cover border-2 border-white/50 shadow-sm group-hover:ring-2 group-hover:ring-primary/40 transition-all"
          />
          <div
            v-else
            class="w-9 h-9 rounded-full flex items-center justify-center border-2 border-white/50 shadow-sm group-hover:ring-2 group-hover:ring-primary/40 transition-all"
            style="background: rgba(45,139,132,0.15);"
          >
            <UserCircle class="w-5 h-5 text-primary" />
          </div>
          <div class="hidden sm:block">
            <p class="text-sm font-medium text-gray-800 group-hover:text-primary transition-colors">{{ auth.user?.nickname || auth.user?.username }}</p>
            <p class="text-xs" :class="auth.user?.role === 'ADMIN' ? 'text-blue-600 font-semibold' : 'text-gray-500'">{{ auth.roleLabel }}</p>
          </div>
        </router-link>
        <button
          @click="handleLogout"
          class="p-2 rounded-xl text-gray-500 hover:text-red-500 hover:bg-red-50 transition-all"
          title="退出登录"
        >
          <Power class="w-4 h-4" />
        </button>
      </div>
    </div>
  </header>

  <Transition name="fade">
    <div
      v-if="showLogoutConfirm"
      class="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-[100]"
      @click="showLogoutConfirm = false"
    >
      <div
        class="rounded-3xl p-8 w-full max-w-sm mx-4 text-center"
        style="background: rgba(255,255,255,0.85); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.6); box-shadow: 0 25px 50px rgba(0,0,0,0.15);"
        @click.stop
      >
        <div class="w-16 h-16 rounded-full mx-auto mb-4 flex items-center justify-center" style="background: rgba(217,79,79,0.1);">
          <Power class="w-8 h-8 text-red-500" />
        </div>

        <h3 class="text-lg font-semibold mb-2 text-gray-800">确认退出登录？</h3>
        <p class="text-sm text-gray-500 mb-6">退出后需要重新登录才能访问系统</p>

        <div class="flex items-center justify-center gap-3 mb-6">
          <img
            v-if="auth.user?.avatar"
            :src="auth.user.avatar"
            class="w-10 h-10 rounded-full object-cover border border-white/50 shadow-sm"
          />
          <div
            v-else
            class="w-10 h-10 rounded-full flex items-center justify-center"
            style="background: rgba(45,139,132,0.15);"
          >
            <UserCircle class="w-5 h-5 text-primary" />
          </div>
          <span class="text-sm font-medium text-gray-800">{{ auth.user?.nickname || auth.user?.username }}</span>
        </div>

        <div class="flex gap-3">
          <button
            @click="showLogoutConfirm = false"
            class="flex-1 py-2.5 rounded-xl text-sm font-medium bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors"
          >
            取消
          </button>
          <button
            @click="confirmLogout"
            class="flex-1 py-2.5 rounded-xl text-sm font-medium text-white bg-red-500 hover:bg-red-600 transition-colors"
          >
            确认退出
          </button>
        </div>
      </div>
    </div>
  </Transition>

  <!-- 发布公告弹窗 -->
  <Transition name="fade">
    <div
      v-if="showPublishModal"
      class="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-[300]"
      @click="showPublishModal = false"
    >
      <div
        class="rounded-3xl p-6 w-full max-w-md mx-4"
        style="background: rgba(255,255,255,0.9); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.6); box-shadow: 0 25px 50px rgba(0,0,0,0.15);"
        @click.stop
      >
        <div class="flex items-center justify-between mb-5">
          <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
            <Megaphone class="w-5 h-5 text-primary" /> 发布公告
          </h3>
          <button @click="showPublishModal = false" class="p-1 rounded-lg hover:bg-black/5 text-gray-500">
            <X class="w-5 h-5" />
          </button>
        </div>

        <div class="space-y-4">
          <div>
            <label class="text-sm text-gray-600 mb-1.5 block">标题</label>
            <input
              v-model="announceForm.title"
              placeholder="请输入公告标题"
              class="w-full px-4 py-2.5 rounded-xl text-sm border border-gray-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
            />
          </div>
          <div>
            <label class="text-sm text-gray-600 mb-1.5 block">内容</label>
            <textarea
              v-model="announceForm.content"
              rows="5"
              placeholder="请输入公告内容"
              class="w-full px-4 py-2.5 rounded-xl text-sm border border-gray-200 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all resize-none"
            ></textarea>
          </div>
          <label class="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
            <input v-model="announceForm.isTop" type="checkbox" class="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary" />
            置顶公告
          </label>
        </div>

        <div class="flex gap-3 mt-6">
          <button
            @click="showPublishModal = false"
            class="flex-1 py-2.5 rounded-xl text-sm font-medium bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors"
          >
            取消
          </button>
          <button
            @click="handlePublish"
            :disabled="publishLoading"
            class="flex-1 py-2.5 rounded-xl text-sm font-medium text-white bg-primary hover:bg-primary/90 transition-colors disabled:opacity-50"
          >
            {{ publishLoading ? '发布中...' : '发布' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>

  <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />
</template>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.dropdown-enter-active, .dropdown-leave-active {
  transition: all 0.2s ease;
}
.dropdown-enter-from, .dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
