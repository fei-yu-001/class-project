<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import { Plus, Pencil, Trash2, Search, Calendar } from 'lucide-vue-next'
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
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}

const form = ref({
  empId: null as number | null,
  grade: '',
  reviewPeriod: new Date().toISOString().slice(0, 7)
})

const search = ref({
  empId: '',
  reviewPeriod: '',
  grade: ''
})

const gradeOptions = ['A', 'B', 'C', 'D']

// 员工姓名查找
const getEmployeeName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp ? emp.empName : `员工${empId}`
}

// 获取员工列表
const fetchEmployees = async () => {
  try {
    const res = await searchEmployees({ page: 0, size: 9999 })
    employees.value = res.data?.content || []
  } catch (e: any) {
    console.error('获取员工列表失败', e)
    showToast(e.message || '获取员工列表失败', 'error')
  }
}

// 获取绩效列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllReviews()
    items.value = res.data || []
  } catch (e: any) {
    console.error('获取绩效列表失败', e)
    showToast(e.message || '获取绩效列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const filteredItems = computed(() => {
  return items.value.filter((item: any) => {
    if (search.value.empId && String(item.empId) !== search.value.empId) return false
    if (search.value.reviewPeriod && item.reviewPeriod !== search.value.reviewPeriod) return false
    if (search.value.grade && item.grade !== search.value.grade) return false
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
  if (!form.value.empId) { showToast('请选择员工', 'error'); return }
  if (!form.value.grade) { showToast('请选择绩效等级', 'error'); return }
  if (!form.value.reviewPeriod || !/^\d{4}-\d{2}$/.test(form.value.reviewPeriod)) { showToast('考核周期格式应为 YYYY-MM', 'error'); return }
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
    showToast('保存成功', 'success')
  } catch (e: any) {
    console.error('保存失败', e)
    showToast(e.message || '保存失败', 'error')
  }
}

const handleDelete = async (id: number) => {
  if (!confirm('确认删除该绩效记录？')) return
  try {
    await deleteReview(id)
    fetchData()
    showToast('删除成功', 'success')
  } catch (e: any) {
    console.error('删除失败', e)
    showToast(e.message || '删除失败', 'error')
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
          <h1 class="text-2xl font-bold text-gray-800">绩效考核</h1>
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
          <div class="relative">
            <label class="text-xs text-text-muted mb-1 block">考核周期</label>
            <Calendar class="absolute left-3 top-[27px] w-4 h-4 text-text-muted pointer-events-none" />
            <input v-model="search.reviewPeriod" type="text" placeholder="2026-01" maxlength="7" class="glass-input pl-9 pr-3 py-2 rounded-lg text-sm w-36" />
          </div>
          <div>
            <label class="text-xs text-text-muted mb-1 block">等级</label>
            <select v-model="search.grade" class="glass-input px-3 py-2 rounded-lg text-sm w-28">
              <option value="">全部</option>
              <option v-for="g in gradeOptions" :key="g" :value="g">{{ g }}</option>
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
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">考核周期</th>
                <th class="px-4 py-3 text-left text-sm font-medium text-gray-600">等级</th>
                <th class="px-4 py-3 text-sm font-medium text-gray-600" style="text-align: center !important; width: 120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="4" class="px-4 py-8 text-center text-sm text-gray-500">加载中...</td>
              </tr>
              <tr v-else-if="pagedItems.length === 0">
                <td colspan="4" class="px-4 py-8 text-center text-sm text-gray-500">暂无数据</td>
              </tr>
              <tr v-for="item in pagedItems" :key="item.reviewId" class="border-b border-black/5 hover:bg-white/20 transition-colors">
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

        <div v-if="totalPages > 1" class="flex justify-center gap-2 mt-5">
          <button @click="page--;" :disabled="page <= 0" class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40">上一页</button>
          <span class="px-4 py-2 text-sm text-text-muted">第 {{ page + 1 }} / {{ totalPages }} 页</span>
          <button @click="page++;" :disabled="page >= totalPages - 1" class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40">下一页</button>
        </div>
      
    <GlassModal :title="editingId ? '编辑绩效' : '新增绩效'" :visible="showModal" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">员工</label>
          <select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" required>
            <option :value="null" disabled>请选择员工</option>
            <option v-for="emp in employees" :key="emp.empId" :value="emp.empId">{{ emp.empName }}</option>
          </select>
        </div>
        <div class="relative">
          <label class="text-sm text-text-muted mb-1 block">考核周期</label>
          <Calendar class="absolute left-3 top-[38px] w-4 h-4 text-text-muted pointer-events-none" />
          <input v-model="form.reviewPeriod" type="text" placeholder="2026-01" maxlength="7" class="glass-input w-full pl-9 pr-4 py-2.5 rounded-xl text-sm" required />
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
