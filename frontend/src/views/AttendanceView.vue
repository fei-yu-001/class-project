<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import { Plus, Pencil, Trash2 } from 'lucide-vue-next'
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

const defaultForm = {
  empId: null as number | null,
  attDate: new Date().toISOString().slice(0, 10),
  attStatus: '正常',
  lateMinutes: 0,
  earlyLeaveMinutes: 0,
  absent: false
}
const form = ref({ ...defaultForm })

// 员工姓名查找
const getEmpName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.name : `员工${empId}`
}

// 获取员工列表
const fetchEmployees = async () => {
  try {
    const res = await searchEmployees({ page: 0, size: 1000 })
    employees.value = res.data?.content || res.data || []
  } catch (e) {
    console.error('获取员工列表失败', e)
  }
}

// 获取考勤列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllAttendance()
    items.value = res.data || []
  } catch (e) {
    console.error('获取考勤数据失败', e)
  } finally {
    loading.value = false
  }
}

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

const handleSubmit = async () => {
  try {
    const payload = { ...form.value }
    if (editingId.value) {
      await updateAttendance(editingId.value, payload)
    } else {
      await createAttendance(payload)
    }
    showModal.value = false
    fetchData()
  } catch (e) {
    console.error('保存考勤记录失败', e)
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确认删除该考勤记录？')) return
  try {
    await deleteAttendance(id)
    fetchData()
  } catch (e) {
    console.error('删除考勤记录失败', e)
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
          <h1 class="text-2xl font-bold text-gray-800">考勤管理</h1>
          <button v-if="canCreate()" @click="openAdd" class="glass-btn flex items-center gap-2 px-4 py-2 rounded-xl">
            <Plus class="w-4 h-4" /> 新增
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
              <tr v-else-if="items.length === 0">
                <td colspan="5" class="px-4 py-8 text-center text-sm text-gray-500">暂无数据</td>
              </tr>
              <tr v-for="item in items" :key="item.attId" class="border-b border-black/5 hover:bg-white/20 transition-colors">
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
      
    <GlassModal :title="editingId ? '编辑考勤' : '新增考勤'" :visible="showModal" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">员工</label>
          <select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option :value="null" disabled>请选择员工</option>
            <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.name }}</option>
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
