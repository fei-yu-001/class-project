<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import { Plus, Pencil, Trash2, Search } from 'lucide-vue-next'
import { getAllAttendance, createAttendance, updateAttendance, deleteAttendance } from '@/api/attendance'
import { searchEmployees } from '@/api/employee'
import { usePermission } from '@/composables/usePermission'

const { canCreate, canEdit, canDelete } = usePermission()

// 数据列表
const items = ref<any[]>([])
const employees = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const editingId = ref<number | null>(null)
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}

const defaultForm = {
  empId: null as number | null,
  attDate: new Date().toISOString().slice(0, 10),
  attStatus: '正常',
  lateMinutes: 0,
  earlyLeaveMinutes: 0,
  absent: false
}
const form = ref({ ...defaultForm })

const search = ref({
  empId: '',
  attDate: '',
  attStatus: ''
})

const statusOptions = ['正常', '迟到', '早退', '缺勤']

// 员工姓名查找
const getEmpName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.empName : `员工${empId}`
}

// 获取员工列表
const fetchEmployees = async () => {
  try {
    const res = await searchEmployees({ page: 0, size: 1000 })
    employees.value = res.data?.content || []
  } catch (e: any) {
    console.error('获取员工列表失败', e)
    showToast(e.message || '获取员工列表失败', 'error')
  }
}

// 获取考勤列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllAttendance()
    items.value = res.data || []
  } catch (e: any) {
    console.error('获取考勤数据失败', e)
    showToast(e.message || '获取考勤数据失败', 'error')
  } finally {
    loading.value = false
  }
}

const filteredItems = computed(() => {
  return items.value.filter((item: any) => {
    if (search.value.empId && String(item.empId) !== search.value.empId) return false
    if (search.value.attDate && item.attDate !== search.value.attDate) return false
    if (search.value.attStatus && item.attStatus !== search.value.attStatus) return false
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
  editingId.value = null
  form.value = { ...defaultForm }
  showModal.value = true
}

const openEdit = (item: any) => {
  editingId.value = item.attId
  form.value = {
    empId: item.empId,
    attDate: item.attDate || new Date().toISOString().slice(0, 10),
    attStatus: item.attStatus || '正常',
    lateMinutes: item.lateMinutes || 0,
    earlyLeaveMinutes: item.earlyLeaveMinutes || 0,
    absent: item.absent || false
  }
  showModal.value = true
}

watch(() => form.value.attStatus, (status) => {
  if (status === '正常') {
    form.value.lateMinutes = 0
    form.value.earlyLeaveMinutes = 0
    form.value.absent = false
  } else if (status === '缺勤') {
    form.value.lateMinutes = 0
    form.value.earlyLeaveMinutes = 0
    form.value.absent = true
  }
})

const handleSubmit = async () => {
  if (!form.value.empId) { showToast('请选择员工', 'error'); return }
  try {
    const payload = { ...form.value }
    if (editingId.value) {
      await updateAttendance(editingId.value, payload)
    } else {
      await createAttendance(payload)
    }
    showModal.value = false
    fetchData()
    showToast('保存成功', 'success')
  } catch (e: any) {
    console.error('保存考勤记录失败', e)
    showToast(e.message || '保存考勤记录失败', 'error')
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确认删除该考勤记录？')) return
  try {
    await deleteAttendance(id)
    fetchData()
    showToast('删除成功', 'success')
  } catch (e: any) {
    console.error('删除考勤记录失败', e)
    showToast(e.message || '删除考勤记录失败', 'error')
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
          <h1 class="text-2xl font-bold text-gray-800">考勤管理</h1>
        </div>

        <div class="flex justify-end mb-3">
          <button v-if="canCreate()" @click="openAdd" class="glass-btn flex items-center gap-2 px-4 py-2 rounded-xl text-sm">
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
            <label class="text-xs text-text-muted mb-1 block">考勤日期</label>
            <input v-model="search.attDate" type="date" class="glass-input px-3 py-2 rounded-lg text-sm" />
          </div>
          <div>
            <label class="text-xs text-text-muted mb-1 block">状态</label>
            <select v-model="search.attStatus" class="glass-input px-3 py-2 rounded-lg text-sm w-28">
              <option value="">全部</option>
              <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
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
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">员工姓名</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">日期</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">状态</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">迟到/早退</th>
                <th class="px-4 py-3 text-sm font-medium text-gray-600" style="text-align: center !important; width: 120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="5" class="px-4 py-8 text-center text-sm text-gray-500">加载中...</td>
              </tr>
              <tr v-else-if="pagedItems.length === 0">
                <td colspan="5" class="px-4 py-8 text-center text-sm text-gray-500">暂无数据</td>
              </tr>
              <tr v-for="item in pagedItems" :key="item.attId" class="border-b border-black/5 hover:bg-white/20 transition-colors">
                <td class="px-4 py-3 text-sm text-gray-700">{{ getEmpName(item.empId) }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.attDate }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">
                  <span :class="{
                    'text-green-600': item.attStatus === '正常',
                    'text-red-600': item.attStatus === '缺勤',
                    'text-yellow-600': item.attStatus === '迟到',
                    'text-orange-600': item.attStatus === '早退'
                  }">
                    {{ item.attStatus }}
                  </span>
                </td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.lateMinutes || 0 }} / {{ item.earlyLeaveMinutes || 0 }} 分钟</td>
                <td class="px-4 py-3 text-center">
                  <div class="inline-flex items-center justify-center gap-1">
                    <button v-if="canEdit()" @click="openEdit(item)" class="flex items-center justify-center w-7 h-7 rounded-lg text-primary hover:bg-primary/10 transition-colors"><Pencil class="w-3.5 h-3.5" /></button>
                    <button v-if="canDelete()" @click="handleDelete(item.attId)" class="flex items-center justify-center w-7 h-7 rounded-lg text-danger hover:bg-danger/10 transition-colors"><Trash2 class="w-3.5 h-3.5" /></button>
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
      
    <GlassModal :title="editingId ? '编辑考勤' : '新增考勤'" :visible="showModal" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">员工</label>
          <select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option :value="null" disabled>请选择员工</option>
            <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.empName }}</option>
          </select>
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">考勤日期</label>
          <input v-model="form.attDate" type="date" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">考勤状态</label>
          <select v-model="form.attStatus" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
            <option value="正常">正常</option>
            <option value="迟到">迟到</option>
            <option value="早退">早退</option>
            <option value="缺勤">缺勤</option>
          </select>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-text-muted mb-1 block">迟到分钟</label>
            <input v-model.number="form.lateMinutes" type="number" min="0" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
          <div>
            <label class="text-sm text-text-muted mb-1 block">早退分钟</label>
            <input v-model.number="form.earlyLeaveMinutes" type="number" min="0" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
        </div>
        <label class="inline-flex items-center gap-2 text-sm text-gray-700">
          <input v-model="form.absent" type="checkbox" class="accent-primary" />
          记为缺勤
        </label>
      </div>
      <template #footer>
        <button @click="showModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">取消</button>
        <button @click="handleSubmit" class="glass-btn px-5 py-2 rounded-xl text-sm">保存</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>
