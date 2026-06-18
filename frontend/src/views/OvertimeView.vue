<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import { Plus, Pencil, Trash2 } from 'lucide-vue-next'
import {
  getAllOvertime,
  createOvertime,
  updateOvertime,
  deleteOvertime
} from '@/api/attendance'
import { searchEmployees } from '@/api/employee'
import { usePermission } from '@/composables/usePermission'

const { canCreate, canEdit, canDelete } = usePermission()

// 数据列表
const items = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const editingItem = ref<any>(null)
const employees = ref<any[]>([])

const defaultForm = () => ({
  empId: null as number | null,
  otHours: 0,
  otDate: new Date().toISOString().slice(0, 10),
  approvalStatus: 'APPROVED'
})

const form = ref(defaultForm())

const fetchEmployees = async () => {
  try {
    const res = await searchEmployees({})
    employees.value = res.data || res
  } catch {
    employees.value = []
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllOvertime()
    items.value = res.data || res
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  editingItem.value = null
  form.value = defaultForm()
  showModal.value = true
}

const openEdit = (item: any) => {
  editingItem.value = item
  form.value = {
    empId: item.empId,
    otHours: item.otHours,
    otDate: item.otDate || new Date().toISOString().slice(0, 10),
    approvalStatus: item.approvalStatus || 'APPROVED'
  }
  showModal.value = true
}

const handleSubmit = async () => {
  try {
    const payload = { ...form.value }
    if (editingItem.value) {
      await updateOvertime(editingItem.value.otId, payload)
    } else {
      await createOvertime(payload)
    }
    showModal.value = false
    fetchData()
  } catch (e) {
    console.error('保存失败', e)
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确认删除该加班记录？')) return
  try {
    await deleteOvertime(id)
    fetchData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

const getEmpName = (empId: number) => {
  const emp = employees.value.find((e: any) => e.empId === empId)
  return emp ? emp.name : empId
}

onMounted(() => {
  fetchEmployees()
  fetchData()
})
</script>

<template>
  <AdminLayout>
        <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold text-gray-800">加班记录</h1>
          <button v-if="canCreate()" @click="openAdd" class="glass-btn-primary flex items-center gap-2 px-4 py-2 rounded-xl">
            <Plus class="w-4 h-4" /> 新增
          </button>
        </div>
        <div class="glass rounded-2xl overflow-hidden">
          <table class="w-full">
            <thead>
              <tr class="border-b border-black/5">
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">员工</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">日期</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">时长</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">审批状态</th>
                <th class="px-4 py-3 text-sm font-medium text-gray-600" style="text-align: center !important; width: 120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="5" class="px-4 py-8 text-center text-gray-500">加载中...</td>
              </tr>
              <tr v-else-if="items.length === 0">
                <td colspan="5" class="px-4 py-8 text-center text-gray-500">暂无数据</td>
              </tr>
              <tr v-for="item in items" :key="item.otId" class="border-b border-black/5 hover:bg-white/20 transition-colors">
                <td class="px-4 py-3 text-sm text-gray-700">{{ getEmpName(item.empId) }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.otDate }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.otHours }} 小时</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.approvalStatus }}</td>
                <td class="px-4 py-3 text-center">
                  <div class="inline-flex items-center justify-center gap-1">
                    <button v-if="canEdit()" @click="openEdit(item)" class="flex items-center justify-center w-7 h-7 rounded-lg text-primary hover:bg-primary/10 transition-colors" title="编辑">
                      <Pencil class="w-3.5 h-3.5" />
                    </button>
                    <button v-if="canDelete()" @click="handleDelete(item.otId)" class="flex items-center justify-center w-7 h-7 rounded-lg text-danger hover:bg-danger/10 transition-colors" title="删除">
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      

    <GlassModal :visible="showModal" :title="editingItem ? '编辑加班' : '新增加班'" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">员工</label>
          <select v-model.number="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option :value="null" disabled>请选择员工</option>
            <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.name }}</option>
          </select>
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">加班时长（小时）</label>
          <input v-model.number="form.otHours" type="number" min="0.5" step="0.5" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">加班日期</label>
          <input v-model="form.otDate" type="date" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
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
      <template #footer>
        <button @click="showModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">取消</button>
        <button @click="handleSubmit" class="glass-btn px-5 py-2 rounded-xl text-sm">保存</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>
