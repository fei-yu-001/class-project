<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import {
  searchEmployees, createEmployee, updateEmployee, deleteEmployee
} from '@/api/employee'
import { listDepartments } from '@/api/department'
import { listPositions } from '@/api/position'
import { usePermission } from '@/composables/usePermission'
import {
  Search, Plus, Pencil, Trash2, User, Building2, Briefcase
} from 'lucide-vue-next'

const { canCreate, canEdit, canDelete } = usePermission()

const employees = ref<any[]>([])
const departments = ref<any[]>([])
const positions = ref<any[]>([])
const loading = ref(false)
const page = ref(0)
const totalPages = ref(1)
const searchName = ref('')
const searchDept = ref('')
const searchStatus = ref('')

const showModal = ref(false)
const isEdit = ref(false)
const editEmpId = ref<number | null>(null)
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}
const form = ref({
  empName: '', gender: '男',
  posId: '', deptCode: '', hireDate: '', birthDate: '',
  empStatus: '在职'
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await searchEmployees({
      empName: searchName.value || undefined,
      deptCode: searchDept.value || undefined,
      empStatus: searchStatus.value || undefined,
      page: page.value,
      size: 10
    })
    employees.value = res.data?.content ?? []
    totalPages.value = res.data?.totalPages ?? 1
  } catch (e) {
    console.error(e)
    showToast('获取员工列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const fetchOptions = async () => {
  try {
    const [dRes, pRes] = await Promise.all([
      listDepartments(),
      listPositions()
    ])
    departments.value = (dRes as any).data ?? []
    positions.value = (pRes as any).data ?? []
  } catch (e) {
    console.error(e)
  }
}

const getDeptName = (deptCode: string) => {
  const dept = departments.value.find((d: any) => d.deptCode === deptCode)
  return dept ? dept.deptName : deptCode || '-'
}

const getPosName = (posId: number) => {
  const pos = positions.value.find((p: any) => p.posId === posId)
  return pos ? pos.posName : posId || '-'
}

const openAdd = () => {
  isEdit.value = false
  editEmpId.value = null
  form.value = {
    empName: '', gender: '男',
    posId: '', deptCode: '', hireDate: '', birthDate: '',
    empStatus: '在职'
  }
  showModal.value = true
}

const openEdit = (row: any) => {
  isEdit.value = true
  editEmpId.value = row.empId
  form.value = {
    empName: row.empName || '',
    gender: row.gender || '男',
    posId: row.posId?.toString() || '',
    deptCode: row.deptCode || '',
    hireDate: row.hireDate || '',
    birthDate: row.birthDate || '',
    empStatus: row.empStatus || '在职'
  }
  showModal.value = true
}

const handleSave = async () => {
  const data = {
    empName: form.value.empName,
    gender: form.value.gender,
    posId: form.value.posId ? Number(form.value.posId) : null,
    deptCode: form.value.deptCode || null,
    hireDate: form.value.hireDate,
    birthDate: form.value.birthDate,
    empStatus: form.value.empStatus
  }
  try {
    if (isEdit.value && editEmpId.value) {
      await updateEmployee(editEmpId.value, data)
    } else {
      await createEmployee(data)
    }
    showModal.value = false
    fetchData()
    showToast(isEdit.value ? '更新成功' : '新增成功', 'success')
  } catch (e: any) {
    showToast(e.message || '操作失败', 'error')
  }
}

const handleDelete = async (empId: number) => {
  if (!confirm('确定删除该员工？')) return
  try {
    await deleteEmployee(empId)
    fetchData()
    showToast('删除成功', 'success')
  } catch (e: any) {
    showToast(e.message || '删除失败', 'error')
  }
}

onMounted(() => {
  fetchData()
  fetchOptions()
})
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />


        <div class="flex justify-end mb-3">
          <button v-if="canCreate()" @click="openAdd" class="glass-btn px-5 py-2.5 rounded-xl flex items-center gap-2 text-sm">
            <Plus class="w-4 h-4" />
            新增员工
          </button>
        </div>

        <div class="glass rounded-2xl p-4 mb-5 flex items-center gap-3">
          <div class="relative flex-1 max-w-xs">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
            <input
              v-model="searchName"
              placeholder="搜索姓名..."
              class="glass-input w-full pl-9 pr-4 py-2 rounded-lg text-sm"
              @keyup.enter="page = 0; fetchData()"
            />
          </div>
          <select v-model="searchDept" class="glass-input px-4 py-2 rounded-lg text-sm" @change="page = 0; fetchData()">
            <option value="">全部部门</option>
            <option v-for="d in departments" :key="d.deptCode" :value="d.deptCode">{{ d.deptName }}</option>
          </select>
          <select v-model="searchStatus" class="glass-input px-4 py-2 rounded-lg text-sm" @change="page = 0; fetchData()">
            <option value="">全部状态</option>
            <option value="在职">在职</option>
            <option value="离职">离职</option>
          </select>
          <button @click="page = 0; fetchData()" class="glass-btn px-4 py-2 rounded-lg text-sm">
            搜索
          </button>
        </div>

        <div class="glass rounded-2xl overflow-hidden">
          <table class="glass-table">
            <thead>
              <tr>
                <th>姓名</th>
                <th>性别</th>
                <th>部门</th>
                <th>职位</th>
                <th>入职日期</th>
                <th>状态</th>
                <th style="text-align: center !important; width: 120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, i) in employees" :key="row.empId" :style="{ animationDelay: `${i * 0.05}s` }" class="animate-fade-in-up">
                <td class="font-medium">{{ row.empName }}</td>
                <td>{{ row.gender }}</td>
                <td>
                  <span class="px-2 py-0.5 rounded-md text-xs font-semibold bg-teal-100 text-teal-700">
                    {{ getDeptName(row.deptCode) }}
                  </span>
                </td>
                <td>{{ getPosName(row.posId) }}</td>
                <td>{{ row.hireDate || '-' }}</td>
                <td>
                  <span
                    class="px-2 py-0.5 rounded-md text-xs"
                    :class="row.empStatus === '在职' ? 'bg-emerald-100 text-emerald-700 font-semibold' : 'bg-red-100 text-red-700 font-semibold'"
                  >
                    {{ row.empStatus }}
                  </span>
                </td>
                <td class="emp-actions-cell" style="text-align: center; width: 120px;">
                  <div class="inline-flex items-center justify-center gap-1">
                    <button v-if="canEdit()" @click="openEdit(row)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-primary/20 text-primary transition-colors">
                      <Pencil class="w-3.5 h-3.5" />
                    </button>
                    <button v-if="canDelete()" @click="handleDelete(row.empId)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-danger/20 text-danger transition-colors">
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="employees.length === 0">
                <td colspan="7" class="text-center py-12 text-text-muted">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex justify-center gap-2 mt-5">
          <button
            @click="page--; fetchData()"
            :disabled="page <= 0"
            class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40"
          >
            上一页
          </button>
          <span class="px-4 py-2 text-sm text-text-muted">第 {{ page + 1 }} / {{ totalPages }} 页</span>
          <button
            @click="page++; fetchData()"
            :disabled="page >= totalPages - 1"
            class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40"
          >
            下一页
          </button>
        </div>
      

    <GlassModal :title="isEdit ? '编辑员工' : '新增员工'" :visible="showModal" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">姓名</label>
          <input v-model="form.empName" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" placeholder="请输入姓名" />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-text-muted mb-1 block">性别</label>
            <select v-model="form.gender" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
          </div>
          <div>
            <label class="text-sm text-text-muted mb-1 block">状态</label>
            <select v-model="form.empStatus" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
              <option value="在职">在职</option>
              <option value="离职">离职</option>
            </select>
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-text-muted mb-1 block">部门</label>
            <select v-model="form.deptCode" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
              <option value="">请选择</option>
              <option v-for="d in departments" :key="d.deptCode" :value="d.deptCode">{{ d.deptName }}</option>
            </select>
          </div>
          <div>
            <label class="text-sm text-text-muted mb-1 block">职位</label>
            <select v-model="form.posId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
              <option value="">请选择</option>
              <option v-for="p in positions" :key="p.posId" :value="p.posId">{{ p.posName }}</option>
            </select>
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-text-muted mb-1 block">出生日期</label>
            <input v-model="form.birthDate" type="date" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
          <div>
            <label class="text-sm text-text-muted mb-1 block">入职日期</label>
            <input v-model="form.hireDate" type="date" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
        </div>
      </div>
      <template #footer>
        <button @click="showModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">取消</button>
        <button @click="handleSave" class="glass-btn px-5 py-2 rounded-xl text-sm">保存</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>

<style scoped>
.emp-actions-cell {
  vertical-align: middle !important;
}
</style>
