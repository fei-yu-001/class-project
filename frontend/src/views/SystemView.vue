<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import { listUsers, updateUserRole, updateUserStatus } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'
import { Users, Shield, ShieldCheck, ShieldAlert, UserX, UserCheck, Crown } from 'lucide-vue-next'

const auth = useAuthStore()
const users = ref<any[]>([])
const loading = ref(false)

const fetchUsers = async () => {
  loading.value = true
  try {
    const res: any = await listUsers()
    users.value = res.data
  } catch (e: any) {
    alert(e.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleRoleChange = async (user: any, newRole: string) => {
  try {
    await updateUserRole(user.id, newRole)
    user.role = newRole
    alert('角色更新成功')
  } catch (e: any) {
    alert(e.message || '更新失败')
  }
}

const handleStatusChange = async (user: any, enabled: boolean) => {
  try {
    await updateUserStatus(user.id, enabled)
    user.enabled = enabled
    alert('状态更新成功')
  } catch (e: any) {
    alert(e.message || '更新失败')
  }
}

const getRoleBadgeClass = (role: string) => {
  if (role === 'SUPER_ADMIN') return 'bg-amber-500/15 text-amber-600 border border-amber-500/25'
  if (role === 'ADMIN') return 'bg-blue-500/15 text-blue-600 border border-blue-500/25'
  return 'bg-gray-500/15 text-gray-600 border border-gray-500/25'
}

const getRoleIcon = (role: string) => {
  if (role === 'SUPER_ADMIN') return Crown
  if (role === 'ADMIN') return ShieldCheck
  return Shield
}

const getRoleLabel = (role: string) => {
  const map: Record<string, string> = { 'SUPER_ADMIN': '超级管理员', 'ADMIN': '管理员', 'USER': '普通用户' }
  return map[role] || role
}

const canChangeRole = (user: any) => {
  return auth.isSuperAdmin && user.role !== 'SUPER_ADMIN'
}

const canChangeStatus = (user: any) => {
  if (user.role === 'SUPER_ADMIN') return false
  return auth.isSuperAdmin
}

onMounted(fetchUsers)
</script>

<template>
  <AdminLayout>

    <div class="glass rounded-2xl p-6 mb-6">
          <div class="flex items-center gap-3 mb-6">
            <Users class="w-6 h-6 text-primary" />
            <h2 class="text-xl font-semibold">用户列表</h2>
            <span class="text-sm text-text-muted">({{ users.length }} 位用户)</span>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="border-b border-white/20">
                  <th class="text-left py-3 px-4 text-sm font-medium text-text-muted">ID</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-text-muted">用户名</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-text-muted">昵称</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-text-muted">角色</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-text-muted">状态</th>
                  <th class="text-left py-3 px-4 text-sm font-medium text-text-muted">创建时间</th>
                  <th class="py-3 px-4 text-sm font-medium text-text-muted" style="text-align: center !important; width: 120px;">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id" class="border-b border-white/10 hover:bg-white/5 transition-colors">
                  <td class="py-3 px-4 text-sm">{{ user.id }}</td>
                  <td class="py-3 px-4 text-sm font-medium">{{ user.username }}</td>
                  <td class="py-3 px-4 text-sm">{{ user.nickname || '-' }}</td>
                  <td class="py-3 px-4">
                    <span
                      class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium"
                      :class="getRoleBadgeClass(user.role)"
                    >
                      <component :is="getRoleIcon(user.role)" class="w-3 h-3" />
                      {{ getRoleLabel(user.role) }}
                    </span>
                  </td>
                  <td class="py-3 px-4">
                    <span
                      class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium"
                      :class="user.enabled
                        ? 'bg-success/15 text-success border border-success/25'
                        : 'bg-danger/15 text-danger border border-danger/25'"
                    >
                      <UserCheck v-if="user.enabled" class="w-3 h-3" />
                      <UserX v-else class="w-3 h-3" />
                      {{ user.enabled ? '正常' : '禁用' }}
                    </span>
                  </td>
                  <td class="py-3 px-4 text-sm text-text-muted">
                    {{ user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '-' }}
                  </td>
                  <td class="py-3 px-4 text-center">
                    <template v-if="user.role === 'SUPER_ADMIN'">
                      <span class="text-xs text-amber-600 font-medium">超级管理员账户不可修改</span>
                    </template>
                    <template v-else>
                      <select
                        v-if="canChangeRole(user)"
                        v-model="user.role"
                        @change="handleRoleChange(user, user.role)"
                        class="glass-input px-2 py-1 rounded-lg text-xs mr-2"
                      >
                        <option value="SUPER_ADMIN">超级管理员</option>
                        <option value="ADMIN">管理员</option>
                        <option value="USER">普通用户</option>
                      </select>
                      <button
                        v-if="canChangeStatus(user)"
                        @click="handleStatusChange(user, !user.enabled)"
                        class="px-2 py-1 rounded-lg text-xs font-medium transition-colors"
                        :class="user.enabled
                          ? 'bg-danger/10 text-danger hover:bg-danger/20'
                          : 'bg-success/10 text-success hover:bg-success/20'"
                      >
                        {{ user.enabled ? '禁用' : '启用' }}
                      </button>
                    </template>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="glass rounded-2xl p-6">
          <h2 class="text-xl font-semibold mb-4">权限说明</h2>
          <p class="text-sm text-text-muted mb-4">角色层级：超级管理员 > 管理员 > 普通用户</p>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="p-4 rounded-xl bg-white/30">
              <div class="flex items-center gap-2 mb-2">
                <Crown class="w-5 h-5 text-amber-500" />
                <h3 class="font-medium">超级管理员 (SUPER_ADMIN)</h3>
              </div>
              <ul class="text-sm text-text-muted space-y-1">
                <li>• 拥有所有权限</li>
                <li>• 管理用户角色和状态</li>
                <li>• 访问系统中心</li>
                <li>• 不可被修改或禁用</li>
              </ul>
            </div>
            <div class="p-4 rounded-xl bg-white/30">
              <div class="flex items-center gap-2 mb-2">
                <ShieldCheck class="w-5 h-5 text-blue-500" />
                <h3 class="font-medium">管理员 (ADMIN)</h3>
              </div>
              <ul class="text-sm text-text-muted space-y-1">
                <li>• 查看所有数据</li>
                <li>• 增删改员工信息</li>
                <li>• 管理工资发放</li>
                <li>• 配置部门和职位</li>
                <li>• 禁用/启用普通用户</li>
              </ul>
            </div>
            <div class="p-4 rounded-xl bg-white/30">
              <div class="flex items-center gap-2 mb-2">
                <Shield class="w-5 h-5 text-gray-500" />
                <h3 class="font-medium">普通用户 (USER)</h3>
              </div>
              <ul class="text-sm text-text-muted space-y-1">
                <li>• 查看仪表盘</li>
                <li>• 修改个人资料</li>
                <li>• 只读访问数据</li>
                <li>• 不能修改数据</li>
              </ul>
            </div>
          </div>
        </div>
  </AdminLayout>
</template>
