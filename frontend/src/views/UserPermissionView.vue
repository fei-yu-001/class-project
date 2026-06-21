<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import { listUsers, updateUserRole, updateUserStatus } from '@/api/admin'
import {
  Shield, ShieldCheck, ShieldAlert, UserX, UserCheck, Search
} from 'lucide-vue-next'

const users = ref<any[]>([])
const loading = ref(false)
const keyword = ref('')
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}

const filteredUsers = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return users.value
  return users.value.filter(u =>
    (u.username || '').toLowerCase().includes(kw) ||
    (u.nickname || '').toLowerCase().includes(kw)
  )
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res: any = await listUsers()
    users.value = res.data
  } catch (e: any) {
    showToast(e.message || '获取用户列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const handleRoleChange = async (user: any, newRole: string) => {
  try {
    await updateUserRole(user.id, newRole)
    user.role = newRole
    showToast('角色更新成功', 'success')
  } catch (e: any) {
    showToast(e.message || '更新失败', 'error')
  }
}

const handleStatusChange = async (user: any, enabled: boolean) => {
  try {
    await updateUserStatus(user.id, enabled)
    await fetchUsers()
    showToast(enabled ? '账号已启用' : '账号已禁用', 'success')
  } catch (e: any) {
    showToast(e.message || '更新失败', 'error')
  }
}

const getRoleBadgeClass = (role: string) => {
  if (role === 'ADMIN') return 'bg-blue-500/15 text-blue-600 border border-blue-500/25'
  return 'bg-gray-500/15 text-gray-600 border border-gray-500/25'
}

const getRoleIcon = (role: string) => {
  if (role === 'ADMIN') return ShieldCheck
  return Shield
}

const getRoleLabel = (role: string) => {
  const map: Record<string, string> = { 'ADMIN': '管理员', 'USER': '普通用户' }
  return map[role] || role
}

onMounted(fetchUsers)
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />

    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-text-primary">用户权限管理</h1>
    </div>

    <div class="glass rounded-2xl p-4 mb-5 flex items-center gap-3">
      <div class="relative flex-1 max-w-xs">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
        <input
          v-model="keyword"
          placeholder="搜索用户名/昵称..."
          class="glass-input w-full pl-9 pr-4 py-2 rounded-lg text-sm"
        />
      </div>
    </div>

    <div class="glass rounded-2xl overflow-hidden mb-6">
      <div class="overflow-x-auto">
        <table class="glass-table min-w-[700px]">
          <thead>
            <tr>
              <th class="whitespace-nowrap text-xs">ID</th>
              <th class="whitespace-nowrap text-xs">用户名</th>
              <th class="whitespace-nowrap text-xs">昵称</th>
              <th class="whitespace-nowrap text-xs">角色</th>
              <th class="whitespace-nowrap text-xs">状态</th>
              <th class="whitespace-nowrap text-xs">创建时间</th>
              <th class="whitespace-nowrap text-xs" style="text-align: center !important; width: 160px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.id">
              <td class="text-sm">{{ user.id }}</td>
              <td class="text-sm font-medium">{{ user.username }}</td>
              <td class="text-sm">{{ user.nickname || '-' }}</td>
              <td>
                <span class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium" :class="getRoleBadgeClass(user.role)">
                  <component :is="getRoleIcon(user.role)" class="w-3 h-3" />
                  {{ getRoleLabel(user.role) }}
                </span>
              </td>
              <td>
                <span class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium" :class="user.enabled ? 'bg-emerald-100 text-emerald-700 border border-emerald-200 font-semibold' : 'bg-red-100 text-red-700 border border-red-200 font-semibold'">
                  <UserCheck v-if="user.enabled" class="w-3 h-3" />
                  <UserX v-else class="w-3 h-3" />
                  {{ user.enabled ? '正常' : '禁用' }}
                </span>
              </td>
              <td class="text-sm text-text-muted">{{ user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '-' }}</td>
              <td style="text-align: center !important; width: 160px;">
                <select
                  v-model="user.role"
                  @change="handleRoleChange(user, user.role)"
                  class="glass-input px-2 py-1 rounded-lg text-xs mr-2"
                >
                  <option value="ADMIN">管理员</option>
                  <option value="USER">普通用户</option>
                </select>
                <button
                  v-if="user.role !== 'ADMIN'"
                  @click="handleStatusChange(user, !user.enabled)"
                  class="px-2 py-1 rounded-lg text-xs font-medium transition-colors"
                  :class="user.enabled ? 'bg-red-100 text-red-700 hover:bg-red-200 font-semibold' : 'bg-emerald-100 text-emerald-700 hover:bg-emerald-200 font-semibold'"
                >
                  {{ user.enabled ? '禁用' : '启用' }}
                </button>
                <span v-else class="text-xs text-text-muted" title="管理员账号不可禁用">—</span>
              </td>
            </tr>
            <tr v-if="filteredUsers.length === 0">
              <td colspan="7" class="text-center py-12 text-text-muted">暂无用户数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="glass rounded-2xl p-6">
      <h2 class="text-lg font-semibold mb-4">权限说明</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="p-4 rounded-xl bg-white/30">
          <div class="flex items-center gap-2 mb-2">
            <ShieldCheck class="w-5 h-5 text-blue-500" />
            <h3 class="font-medium">管理员</h3>
          </div>
          <ul class="text-sm text-text-muted space-y-1">
            <li>• 查看所有数据</li>
            <li>• 增删改员工信息</li>
            <li>• 管理工资发放</li>
            <li>• 配置部门和职位</li>
            <li>• 管理用户权限</li>
          </ul>
        </div>
        <div class="p-4 rounded-xl bg-white/30">
          <div class="flex items-center gap-2 mb-2">
            <Shield class="w-5 h-5 text-gray-500" />
            <h3 class="font-medium">普通用户</h3>
          </div>
          <ul class="text-sm text-text-muted space-y-1">
            <li>• 查看仪表盘</li>
            <li>• 修改个人资料</li>
            <li>• 只读访问数据</li>
          </ul>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>
