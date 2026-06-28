<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import { Plus, Pencil, Trash2, Search, CheckCircle, XCircle } from 'lucide-vue-next'
import { getAllLeaves, createLeave, updateLeave, deleteLeave, approveLeave, rejectLeave } from '@/api/attendance'
import { searchEmployees } from '@/api/employee'
import { usePermission } from '@/composables/usePermission'

const { canCreate, canEdit, canDelete } = usePermission()

// 数据列表
const items = ref<any[]>([])
const employees = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const editingItem = ref<any>(null)
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}

const leaveTypeOptions = [
  { value: '年假', label: '年假' },
  { value: '病假', label: '病假' },
  { value: '事假', label: '事假' },
  { value: '婚假', label: '婚假' },
  { value: '产假', label: '产假' }
]

const approvalStatusOptions = [
  { value: 'PENDING', label: '待审批' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' }
]

const approvalStatusLabel = (status?: string) => {
  return approvalStatusOptions.find(o => o.value === status)?.label || status || '-'
}

const defaultForm = {
  empId: null as number | null,
  leaveType: '年假',
  startDate: new Date().toISOString().slice(0, 10),
  endDate: new Date().toISOString().slice(0, 10),
  leaveDays: 1,
  approvalStatus: 'PENDING'
}

const form = ref({ ...defaultForm })

const handleApprove = async (item: any) => {
  if (!confirm('确认通过该请假申请？')) return
  try {
    await approveLeave(item.leaveId)
    fetchData()
    showToast('请假已通过', 'success')
  } catch (e: any) {
    showToast(e.message || '操作失败', 'error')
  }
}

const handleReject = async (item: any) => {
  if (!confirm('确认驳回该请假申请？')) return
  try {
    await rejectLeave(item.leaveId)
    fetchData()
    showToast('请假已驳回', 'success')
  } catch (e: any) {
    showToast(e.message || '操作失败', 'error')
  }
}

const search = ref({
  empId: '',
  leaveType: '',
  startDate: '',
  endDate: '',
  approvalStatus: ''
})

const getEmpName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.empName : `员工${empId}`
}

const fetchEmployees = async () => {
  try {
    const res = await searchEmployees({ page: 0, size: 1000 })
    employees.value = res.data?.content || []
  } catch (e) {
    console.error('获取员工列表失败', e)
    showToast('获取员工列表失败', 'error')
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllLeaves()
    items.value = res.data || []
  } catch (e) {
    showToast('获取请假列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const filteredItems = computed(() => {
  return items.value.filter((item: any) => {
    if (search.value.empId && String(item.empId) !== search.value.empId) return false
    if (search.value.leaveType && item.leaveType !== search.value.leaveType) return false
    if (search.value.approvalStatus && item.approvalStatus !== search.value.approvalStatus) return false
    if (search.value.startDate && item.endDate && item.endDate < search.value.startDate) return false
    if (search.value.endDate && item.startDate && item.startDate > search.value.endDate) return false
    return true
  })
})

const page = ref(0)
const pageSize = ref(10)
const totalPages = computed(() => Math.max(1, Math.ceil(filteredItems.value.length / pageSize.value)))
const pagedItems = computed(() => {
  const start = page.value * pageSize.value
  return filteredItems.value.slice(start, start + pageSize.value)
})
watch(search, () => { page.value = 0 }, { deep: true })

const openAdd = () => {
  editingItem.value = null
  form.value = { ...defaultForm }
  showModal.value = true
}

const openEdit = (item: any) => {
  editingItem.value = item
  form.value = {
    empId: item.empId,
    leaveType: item.leaveType,
    startDate: item.startDate || new Date().toISOString().slice(0, 10),
    endDate: item.endDate || new Date().toISOString().slice(0, 10),
    leaveDays: item.leaveDays || 1,
    approvalStatus: item.approvalStatus || 'PENDING'
  }
  showModal.value = true
}

const handleSubmit = async () => {
  if (!form.value.empId) { showToast('请选择员工', 'error'); return }
  if (!form.value.startDate || !form.value.endDate) { showToast('请选择日期', 'error'); return }
  if (form.value.endDate < form.value.startDate) { showToast('结束日期不能早于开始日期', 'error'); return }
  try {
    const payload = {
      empId: form.value.empId,
      leaveType: form.value.leaveType,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      leaveDays: form.value.leaveDays,
      approvalStatus: editingItem.value ? form.value.approvalStatus : 'PENDING'
    }
    if (editingItem.value) {
      await updateLeave(editingItem.value.leaveId, payload)
    } else {
      await createLeave(payload)
    }
    showModal.value = false
    fetchData()
    showToast('保存成功', 'success')
  } catch (e: any) {
    console.error('保存请假记录失败', e)
    showToast(e.message || '保存失败，请重试', 'error')
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确认删除该请假记录？')) return
  try {
    await deleteLeave(id)
    fetchData()
    showToast('删除成功', 'success')
  } catch (e: any) {
    console.error('删除请假记录失败', e)
    showToast(e.message || '删除失败，请重试', 'error')
  }
}

onMounted(() => {
  fetchEmployees()
  fetchData()
})
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />
        <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold text-gray-800">请假申请</h1>
        </div>

        <div class="flex justify-end mb-3">
          <button v-if="canCreate()" @click="openAdd" class="glass-btn-primary flex items-center gap-2 px-4 py-2 rounded-xl text-sm">
            <Plus class="w-4 h-4" /> 新增
          </button>
        </div>

        <div class="glass rounded-2xl p-4 mb-5 flex flex-wrap items-end gap-3">
          <div>
            <label class="text-xs text-text-muted mb-1 block">员工</label>
            <select v-model="search.empId" class="glass-input px-3 py-2 rounded-lg text-sm min-w-[140px]">
              <option value="">全部</option>
              <option v-for="emp in employees" :key="emp.empId" :value="String(emp.empId)">{{ emp.empName }}</option>
            </select>
          </div>
          <div>
            <label class="text-xs text-text-muted mb-1 block">请假类型</label>
            <select v-model="search.leaveType" class="glass-input px-3 py-2 rounded-lg text-sm w-28">
              <option value="">全部</option>
              <option v-for="opt in leaveTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <div>
            <label class="text-xs text-text-muted mb-1 block">日期起</label>
            <input v-model="search.startDate" type="date" class="glass-input px-3 py-2 rounded-lg text-sm" />
          </div>
          <div>
            <label class="text-xs text-text-muted mb-1 block">日期止</label>
            <input v-model="search.endDate" type="date" class="glass-input px-3 py-2 rounded-lg text-sm" />
          </div>
          <div>
            <label class="text-xs text-text-muted mb-1 block">审批状态</label>
            <select v-model="search.approvalStatus" class="glass-input px-3 py-2 rounded-lg text-sm w-32">
              <option value="">全部</option>
              <option v-for="opt in approvalStatusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <button @click="fetchData" class="glass-btn px-4 py-2 rounded-lg text-sm flex items-center gap-2">
            <Search class="w-4 h-4" /> 检索
          </button>
        </div>

        <div class="glass rounded-2xl overflow-hidden">
          <table class="w-full">
            <thead>
              <tr class="border-b border-black/5">
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">员工</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">请假类型</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">日期</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">天数</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">审批状态</th>
                <th class="px-4 py-3 text-sm font-medium text-gray-600" style="text-align: center !important; width: 120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="6" class="px-4 py-8 text-center text-gray-500">加载中...</td>
              </tr>
              <tr v-else-if="pagedItems.length === 0">
                <td colspan="6" class="px-4 py-8 text-center text-sm text-gray-500">暂无数据</td>
              </tr>
              <tr v-for="item in pagedItems" :key="item.leaveId" class="border-b border-black/5 hover:bg-white/20 transition-colors">
                <td class="px-4 py-3 text-sm text-gray-700">{{ getEmpName(item.empId) }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.leaveType }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.startDate }} 至 {{ item.endDate }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.leaveDays }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ approvalStatusLabel(item.approvalStatus) }}</td>
                <td class="px-4 py-3 text-center">
                  <div class="inline-flex items-center justify-center gap-1">
                    <button v-if="canEdit() && item.approvalStatus === 'PENDING'" @click="handleApprove(item)" class="flex items-center justify-center w-7 h-7 rounded-lg text-success hover:bg-success/10 transition-colors" title="通过">
                      <CheckCircle class="w-3.5 h-3.5" />
                    </button>
                    <button v-if="canEdit() && item.approvalStatus === 'PENDING'" @click="handleReject(item)" class="flex items-center justify-center w-7 h-7 rounded-lg text-amber-600 hover:bg-amber-50 transition-colors" title="驳回">
                      <XCircle class="w-3.5 h-3.5" />
                    </button>
                    <button v-if="canEdit() && item.approvalStatus === 'PENDING'" @click="openEdit(item)" class="flex items-center justify-center w-7 h-7 rounded-lg text-primary hover:bg-primary/10 transition-colors"><Pencil class="w-3.5 h-3.5" /></button>
                    <button v-if="canDelete() && item.approvalStatus !== 'APPROVED'" @click="handleDelete(item.leaveId)" class="flex items-center justify-center w-7 h-7 rounded-lg text-danger hover:bg-danger/10 transition-colors"><Trash2 class="w-3.5 h-3.5" /></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="totalPages > 1" class="flex justify-center gap-2 mt-5">
          <button @click="page--;" :disabled="page <= 0" class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40">上一页</button>
          <span class="px-4 py-2 text-sm text-text-muted">第 {{ page + 1 }} / {{ totalPages }} 页</span>
          <button @click="page++;" :disabled="page >= totalPages - 1" class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40">下一页</button>
        </div>
      

    <GlassModal :visible="showModal" :title="editingItem ? '编辑请假' : '新增请假'" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">员工</label>
          <select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option :value="null" disabled>请选择员工</option>
            <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.empName }}</option>
          </select>
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">请假类型</label>
          <select v-model="form.leaveType" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
            <option v-for="opt in leaveTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-text-muted mb-1 block">开始日期</label>
            <input v-model="form.startDate" type="date" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
          <div>
            <label class="text-sm text-text-muted mb-1 block">结束日期</label>
            <input v-model="form.endDate" type="date" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-text-muted mb-1 block">请假天数</label>
            <input v-model.number="form.leaveDays" type="number" min="0.5" step="0.5" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
          <div>
            <label class="text-sm text-text-muted mb-1 block">审批状态</label>
            <select v-model="form.approvalStatus" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
              <option v-for="opt in approvalStatusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>
        </div>
      <template #footer>
        <button @click="showModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">取消</button>
        <button @click="handleSubmit" class="glass-btn px-5 py-2 rounded-xl text-sm">保存</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>
