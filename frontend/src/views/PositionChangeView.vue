<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import { Plus, Trash2 } from 'lucide-vue-next'
import { getAllChanges, createChange, deleteChange } from '@/api/positionChange'
import { searchEmployees } from '@/api/employee'
import { usePermission } from '@/composables/usePermission'

const { canCreate, canDelete } = usePermission()

// 数据列表
const items = ref<any[]>([])
const employees = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)

const form = ref({
  empId: null as number | null,
  changeReason: ''
})

// 员工名称查找
const getEmpName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.empName : String(empId)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllChanges()
    items.value = res.data || res
  } finally {
    loading.value = false
  }
}

const fetchEmployees = async () => {
  try {
    const res = await searchEmployees({ page: 0, size: 1000 })
    const data = res.data || res
    employees.value = data.content || data
  } catch {
    employees.value = []
  }
}

const openAdd = () => {
  form.value = { empId: null, changeReason: '' }
  showModal.value = true
}

const handleSubmit = async () => {
  try {
    await createChange({
      empId: form.value.empId,
      changeReason: form.value.changeReason
    })
    showModal.value = false
    await fetchData()
  } catch (e) {
    console.error('保存失败', e)
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确认删除？')) return
  try {
    await deleteChange(id)
    await fetchData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

onMounted(() => {
  fetchData()
  fetchEmployees()
})
</script>

<template>
  <AdminLayout>
        <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold text-gray-800">职位变动</h1>
          <button v-if="canCreate()" @click="openAdd" class="glass-btn-primary flex items-center gap-2 px-4 py-2 rounded-xl">
            <Plus class="w-4 h-4" /> 新增
          </button>
        </div>
        <div class="glass rounded-2xl overflow-hidden">
          <table class="w-full">
            <thead>
              <tr class="border-b border-black/5">
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">员工</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">变动原因</th>
                <th class="px-4 py-3 text-sm font-medium text-gray-600" style="text-align: center !important; width: 120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="3" class="px-4 py-8 text-center text-gray-400">加载中...</td>
              </tr>
              <tr v-else-if="items.length === 0">
                <td colspan="3" class="px-4 py-8 text-center text-gray-400">暂无数据</td>
              </tr>
              <tr v-for="item in items" :key="item.changeId" class="border-b border-black/5 hover:bg-white/20 transition-colors">
                <td class="px-4 py-3 text-sm text-gray-700">{{ getEmpName(item.empId) }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.changeReason }}</td>
                <td class="px-4 py-3 text-center">
                  <div class="inline-flex items-center justify-center gap-1">
                    <button v-if="canDelete()" @click="handleDelete(item.changeId)" class="flex items-center justify-center w-7 h-7 rounded-lg text-danger hover:bg-danger/10 transition-colors"><Trash2 class="w-3.5 h-3.5" /></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      
    <GlassModal :visible="showModal" :title="'新增变动'" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">员工</label>
          <select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option :value="null" disabled>请选择员工</option>
            <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.empName }}</option>
          </select>
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">变动原因</label>
          <textarea v-model="form.changeReason" rows="3" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm"></textarea>
        </div>
        </div>
      <template #footer>
        <button @click="showModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">取消</button>
        <button @click="handleSubmit" class="glass-btn px-5 py-2 rounded-xl text-sm">保存</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>
