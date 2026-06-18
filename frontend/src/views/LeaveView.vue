<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import { Plus, Pencil, Trash2 } from 'lucide-vue-next'
import { getAllLeaves, createLeave, updateLeave, deleteLeave } from '@/api/attendance'
import { searchEmployees } from '@/api/employee'
import { usePermission } from '@/composables/usePermission'

const { canCreate, canEdit, canDelete } = usePermission()

// 数据列表
const items = ref<any[]>([])
const employees = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const editingItem = ref<any>(null)

const leaveTypeOptions = [
  { value: '年假', label: '年假' },
  { value: '病假', label: '病假' },
  { value: '事假', label: '事假' },
  { value: '婚假', label: '婚假' },
  { value: '产假', label: '产假' }
]

const defaultForm = {
  empId: null as number | null,
  leaveType: '年假',
  startDate: new Date().toISOString().slice(0, 10),
  endDate: new Date().toISOString().slice(0, 10),
  leaveDays: 1,
  approvalStatus: 'APPROVED'
}

const form = ref({ ...defaultForm })

const getEmpName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.name : `员工${empId}`
}

const fetchEmployees = async () => {
  try {
    const res = await searchEmployees({ page: 0, size: 1000 })
    employees.value = res.data?.content || res.data || []
  } catch (e) {
    console.error('获取员工列表失败', e)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllLeaves()
    items.value = res.data || []
  } catch (e) {
    console.error('获取请假列表失败', e)
  } finally {
    loading.value = false
  }
}

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
    approvalStatus: item.approvalStatus || 'APPROVED'
  }
  showModal.value = true
}

const handleSubmit = async () => {
  try {
    const payload = {
      empId: form.value.empId,
      leaveType: form.value.leaveType,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      leaveDays: form.value.leaveDays,
      approvalStatus: form.value.approvalStatus
    }
    if (editingItem.value) {
      await updateLeave(editingItem.value.leaveId, payload)
    } else {
      await createLeave(payload)
    }
    showModal.value = false
    fetchData()
  } catch (e) {
    console.error('保存请假记录失败', e)
    alert('保存失败，请重试')
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确认删除该请假记录？')) return
  try {
    await deleteLeave(id)
    fetchData()
  } catch (e) {
    console.error('删除请假记录失败', e)
    alert('删除失败，请重试')
  }
}

onMounted(() => {
  fetchEmployees()
  fetchData()
})
</script>

<template>
  <AdminLayout>
        <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold text-gray-800">请假申请</h1>
          <button v-if="canCreate()" @click="openAdd" class="glass-btn-primary flex items-center gap-2 px-4 py-2 rounded-xl">
            <Plus class="w-4 h-4" /> 新增
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
              <tr v-else-if="items.length === 0">
                <td colspan="6" class="px-4 py-8 text-center text-gray-500">暂无数据</td>
              </tr>
              <tr v-for="item in items" :key="item.leaveId" class="border-b border-black/5 hover:bg-white/20 transition-colors">
                <td class="px-4 py-3 text-sm text-gray-700">{{ getEmpName(item.empId) }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.leaveType }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.startDate }} 至 {{ item.endDate }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.leaveDays }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.approvalStatus }}</td>
                <td class="px-4 py-3 text-center">
                  <div class="inline-flex items-center justify-center gap-1">
                    <button v-if="canEdit()" @click="openEdit(item)" class="flex items-center justify-center w-7 h-7 rounded-lg text-primary hover:bg-primary/10 transition-colors"><Pencil class="w-3.5 h-3.5" /></button>
                    <button v-if="canDelete()" @click="handleDelete(item.leaveId)" class="flex items-center justify-center w-7 h-7 rounded-lg text-danger hover:bg-danger/10 transition-colors"><Trash2 class="w-3.5 h-3.5" /></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      

    <GlassModal :visible="showModal" :title="editingItem ? '编辑请假' : '新增请假'" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">员工</label>
          <select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option :value="null" disabled>请选择员工</option>
            <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.name }}</option>
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
              <option value="PENDING">PENDING</option>
              <option value="APPROVED">APPROVED</option>
              <option value="REJECTED">REJECTED</option>
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
