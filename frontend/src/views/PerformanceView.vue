<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import { Plus, Pencil, Trash2 } from 'lucide-vue-next'
import { getAllReviews, createReview, updateReview, deleteReview } from '@/api/performance'
import { searchEmployees } from '@/api/employee'
import { usePermission } from '@/composables/usePermission'

const { canCreate, canEdit, canDelete } = usePermission()

// 数据列表
const items = ref<any[]>([])
const employees = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const editingId = ref<number | null>(null)

const form = ref({
  empId: null as number | null,
  grade: '',
  reviewPeriod: new Date().toISOString().slice(0, 7)
})

// 员工姓名查找
const getEmployeeName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.name : `员工${empId}`
}

// 获取员工列表
const fetchEmployees = async () => {
  try {
    const res = await searchEmployees({ page: 0, size: 9999 })
    employees.value = res.data?.content || res.data || []
  } catch (e) {
    console.error('获取员工列表失败', e)
  }
}

// 获取绩效列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllReviews()
    items.value = res.data || []
  } catch (e) {
    console.error('获取绩效列表失败', e)
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  editingId.value = null
  form.value = { empId: null, grade: '', reviewPeriod: new Date().toISOString().slice(0, 7) }
  showModal.value = true
}

const openEdit = (item: any) => {
  editingId.value = item.reviewId
  form.value = {
    empId: item.empId,
    grade: item.grade,
    reviewPeriod: item.reviewPeriod || new Date().toISOString().slice(0, 7)
  }
  showModal.value = true
}

const handleSubmit = async () => {
  try {
    const payload = {
      empId: form.value.empId,
      grade: form.value.grade,
      reviewPeriod: form.value.reviewPeriod
    }
    if (editingId.value) {
      await updateReview(editingId.value, payload)
    } else {
      await createReview(payload)
    }
    showModal.value = false
    fetchData()
  } catch (e) {
    console.error('保存失败', e)
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确认删除该绩效记录？')) return
  try {
    await deleteReview(id)
    fetchData()
  } catch (e) {
    console.error('删除失败', e)
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
          <h1 class="text-2xl font-bold text-gray-800">绩效考核</h1>
          <button v-if="canCreate()" @click="openAdd" class="glass-btn-primary flex items-center gap-2 px-4 py-2 rounded-xl">
            <Plus class="w-4 h-4" /> 新增
          </button>
        </div>
        <div class="glass rounded-2xl overflow-hidden">
          <table class="w-full">
            <thead>
              <tr class="border-b border-black/5">
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">员工姓名</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">考核周期</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">等级</th>
                <th class="px-4 py-3 text-sm font-medium text-gray-600" style="text-align: center !important; width: 120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="4" class="px-4 py-8 text-center text-sm text-gray-500">加载中...</td>
              </tr>
              <tr v-else-if="items.length === 0">
                <td colspan="4" class="px-4 py-8 text-center text-sm text-gray-500">暂无数据</td>
              </tr>
              <tr v-for="item in items" :key="item.reviewId" class="border-b border-black/5 hover:bg-white/20 transition-colors">
                <td class="px-4 py-3 text-sm text-gray-700">{{ getEmployeeName(item.empId) }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.reviewPeriod }}</td>
                <td class="px-4 py-3 text-sm text-gray-700">{{ item.grade }}</td>
                <td class="px-4 py-3 text-center">
                  <div class="inline-flex items-center justify-center gap-1">
                    <button v-if="canEdit()" @click="openEdit(item)" class="flex items-center justify-center w-7 h-7 rounded-lg text-primary hover:bg-primary/10 transition-colors"><Pencil class="w-3.5 h-3.5" /></button>
                    <button v-if="canDelete()" @click="handleDelete(item.reviewId)" class="flex items-center justify-center w-7 h-7 rounded-lg text-danger hover:bg-danger/10 transition-colors"><Trash2 class="w-3.5 h-3.5" /></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      
    <GlassModal :title="editingId ? '编辑绩效' : '新增绩效'" :visible="showModal" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">员工</label>
          <select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option :value="null" disabled>请选择员工</option>
            <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.name }}</option>
          </select>
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">考核周期</label>
          <input v-model="form.reviewPeriod" type="month" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">绩效等级</label>
          <select v-model="form.grade" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option value="">请选择</option>
            <option value="A">A - 优秀</option>
            <option value="B">B - 良好</option>
            <option value="C">C - 合格</option>
            <option value="D">D - 待改进</option>
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
