<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import { Plus, Trash2, Search } from 'lucide-vue-next'
import { getAllChanges, createChange, deleteChange } from '@/api/positionChange'
import { searchEmployees } from '@/api/employee'
import { listPositions } from '@/api/position'
import { usePermission } from '@/composables/usePermission'

const { canCreate, canDelete } = usePermission()

const items = ref<any[]>([])
const employees = ref<any[]>([])
const positions = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}

const form = ref({
  empId: null as number | null,
  newPosId: '',
  changeDate: new Date().toISOString().slice(0, 10),
  changeReason: ''
})

const searchForm = ref({
  empId: '',
  newPosId: '',
  startDate: '',
  endDate: '',
  keyword: ''
})

const posMap = computed(() => {
  const map: Record<string, string> = {}
  positions.value.forEach((p: any) => {
    map[p.posId] = p.posName
  })
  return map
})

const getEmpName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.empName : String(empId)
}

const getPosName = (posId: string | null | undefined) => {
  return posId ? posMap.value[posId] || posId : '-'
}

const filteredItems = computed(() => {
  return items.value.filter(item => {
    if (searchForm.value.empId && String(item.empId) !== searchForm.value.empId) return false
    if (searchForm.value.newPosId && item.newPosId !== searchForm.value.newPosId) return false
    if (searchForm.value.startDate && item.changeDate && item.changeDate < searchForm.value.startDate) return false
    if (searchForm.value.endDate && item.changeDate && item.changeDate > searchForm.value.endDate) return false
    if (searchForm.value.keyword && !(item.changeReason || '').includes(searchForm.value.keyword)) return false
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
watch(searchForm, () => { page.value = 0 }, { deep: true })

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
    employees.value = res.data?.content || res.data || []
  } catch {
    employees.value = []
  }
}

const fetchPositions = async () => {
  try {
    const res = await listPositions()
    positions.value = res.data || []
  } catch {
    positions.value = []
  }
}

const openAdd = () => {
  form.value = {
    empId: null,
    newPosId: '',
    changeDate: new Date().toISOString().slice(0, 10),
    changeReason: ''
  }
  showModal.value = true
}

const handleSubmit = async () => {
  if (!form.value.empId || !form.value.newPosId) {
    showToast('请选择员工和目标职位', 'error')
    return
  }
  try {
    await createChange({
      empId: form.value.empId,
      newPosId: form.value.newPosId,
      changeDate: form.value.changeDate,
      changeReason: form.value.changeReason
    })
    showModal.value = false
    await fetchData()
    await fetchEmployees()
    showToast('保存成功', 'success')
  } catch (e: any) {
    showToast(e.message || '保存失败', 'error')
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确认删除？')) return
  try {
    await deleteChange(id)
    await fetchData()
    showToast('删除成功', 'success')
  } catch (e: any) {
    showToast(e.message || '删除失败', 'error')
  }
}

onMounted(() => {
  fetchData()
  fetchEmployees()
  fetchPositions()
})
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-800">职位变动</h1>
    </div>

    <div class="flex justify-end mb-3">
      <button v-if="canCreate()" @click="openAdd" class="glass-btn-primary flex items-center gap-2 px-4 py-2 rounded-xl text-sm">
        <Plus class="w-4 h-4" /> 新增
      </button>
    </div>

    <div class="glass rounded-2xl p-4 mb-5 flex flex-wrap items-end gap-3">
      <div>
        <label class="text-xs text-text-muted mb-1 block">员工</label>
        <select v-model="searchForm.empId" class="glass-input px-3 py-2 rounded-lg text-sm min-w-[140px]">
          <option value="">全部</option>
          <option v-for="emp in employees" :key="emp.empId" :value="String(emp.empId)">{{ emp.empName }}</option>
        </select>
      </div>
      <div>
        <label class="text-xs text-text-muted mb-1 block">新职位</label>
        <select v-model="searchForm.newPosId" class="glass-input px-3 py-2 rounded-lg text-sm min-w-[140px]">
          <option value="">全部</option>
          <option v-for="pos in positions" :key="pos.posId" :value="pos.posId">{{ pos.posName }}</option>
        </select>
      </div>
      <div>
        <label class="text-xs text-text-muted mb-1 block">日期起</label>
        <input v-model="searchForm.startDate" type="date" class="glass-input px-3 py-2 rounded-lg text-sm" />
      </div>
      <div>
        <label class="text-xs text-text-muted mb-1 block">日期止</label>
        <input v-model="searchForm.endDate" type="date" class="glass-input px-3 py-2 rounded-lg text-sm" />
      </div>
      <div>
        <label class="text-xs text-text-muted mb-1 block">原因关键词</label>
        <input v-model="searchForm.keyword" placeholder="输入关键词" class="glass-input px-3 py-2 rounded-lg text-sm" />
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
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">原职位</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">新职位</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">变动日期</th>
            <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">变动原因</th>
            <th class="px-4 py-3 text-sm font-medium text-gray-600" style="text-align: center !important; width: 120px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="px-4 py-8 text-center text-gray-400">加载中...</td>
          </tr>
          <tr v-else-if="pagedItems.length === 0">
                <td colspan="6" class="px-4 py-8 text-center text-sm text-gray-500">暂无数据</td>
              </tr>
              <tr v-for="item in pagedItems" :key="item.changeId" class="border-b border-black/5 hover:bg-white/20 transition-colors">
            <td class="px-4 py-3 text-sm text-gray-700">{{ getEmpName(item.empId) }}</td>
            <td class="px-4 py-3 text-sm text-gray-700">{{ getPosName(item.oldPosId) }}</td>
            <td class="px-4 py-3 text-sm text-gray-700 font-medium text-primary">{{ getPosName(item.newPosId) }}</td>
            <td class="px-4 py-3 text-sm text-gray-700">{{ item.changeDate }}</td>
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

    <div v-if="totalPages > 1" class="flex justify-center gap-2 mt-5">
      <button @click="page--;" :disabled="page <= 0" class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40">上一页</button>
      <span class="px-4 py-2 text-sm text-text-muted">第 {{ page + 1 }} / {{ totalPages }} 页</span>
      <button @click="page++;" :disabled="page >= totalPages - 1" class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40">下一页</button>
    </div>

    <GlassModal :visible="showModal" :title="'新增职位变动'" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">员工</label>
          <select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option :value="null" disabled>请选择员工</option>
            <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.empName }}</option>
          </select>
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">目标职位</label>
          <select v-model="form.newPosId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option value="" disabled>请选择职位</option>
            <option v-for="pos in positions" :key="pos.posId" :value="pos.posId">{{ pos.posName }} ({{ pos.posId }})</option>
          </select>
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">变动日期</label>
          <input v-model="form.changeDate" type="date" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required />
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
