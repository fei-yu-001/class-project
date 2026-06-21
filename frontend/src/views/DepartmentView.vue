<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import { listDepartments, createDepartment, updateDepartment, deleteDepartment } from '@/api/department'
import { usePermission } from '@/composables/usePermission'
import { Plus, Pencil, Trash2, Building2, Search } from 'lucide-vue-next'

const { canCreate, canEdit, canDelete } = usePermission()

const departments = ref<any[]>([])
const showModal = ref(false)
const isEdit = ref(false)
const editDeptCode = ref<string | null>(null)
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}
const form = ref({ deptCode: '', deptName: '', parentDeptCode: '', deptManagerEmp: '' })

const search = ref({
  deptCode: '',
  deptName: '',
  parentDeptCode: '',
  deptManagerEmp: ''
})

const filteredDepartments = computed(() => {
  return departments.value.filter((row: any) => {
    if (search.value.deptCode && !(row.deptCode || '').toLowerCase().includes(search.value.deptCode.toLowerCase())) return false
    if (search.value.deptName && !(row.deptName || '').toLowerCase().includes(search.value.deptName.toLowerCase())) return false
    if (search.value.parentDeptCode && !(row.parentDeptCode || '').toLowerCase().includes(search.value.parentDeptCode.toLowerCase())) return false
    if (search.value.deptManagerEmp && !(row.deptManagerEmp || '').toLowerCase().includes(search.value.deptManagerEmp.toLowerCase())) return false
    return true
  })
})

const fetchData = async () => {
  try {
    const res: any = await listDepartments()
    departments.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const openAdd = () => {
  isEdit.value = false
  editDeptCode.value = null
  form.value = { deptCode: '', deptName: '', parentDeptCode: '', deptManagerEmp: '' }
  showModal.value = true
}

const openEdit = (row: any) => {
  isEdit.value = true
  editDeptCode.value = row.deptCode
  form.value = {
    deptCode: row.deptCode,
    deptName: row.deptName,
    parentDeptCode: row.parentDeptCode || '',
    deptManagerEmp: row.deptManagerEmp || ''
  }
  showModal.value = true
}

const handleSave = async () => {
  try {
    const requestData = {
      deptName: form.value.deptName,
      parentDeptCode: form.value.parentDeptCode,
      deptManagerEmp: form.value.deptManagerEmp
    }
    if (isEdit.value && editDeptCode.value) {
      await updateDepartment(editDeptCode.value, requestData)
    } else {
      await createDepartment(requestData)
    }
    showModal.value = false
    fetchData()
    showToast(isEdit.value ? '更新成功' : '新增成功', 'success')
  } catch (e: any) {
    showToast(e.message || '操作失败', 'error')
  }
}

const handleDelete = async (deptCode: string) => {
  if (!confirm('确定删除该部门？')) return
  try {
    await deleteDepartment(deptCode)
    fetchData()
    showToast('删除成功', 'success')
  } catch (e: any) {
    showToast(e.message || '删除失败', 'error')
  }
}

onMounted(fetchData)
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-bold text-text-primary">部门管理</h1>
    </div>

    <div class="flex justify-end mb-3">
      <button v-if="canCreate()" @click="openAdd" class="glass-btn px-5 py-2.5 rounded-xl flex items-center gap-2 text-sm">
        <Plus class="w-4 h-4" /> 新增部门
      </button>
    </div>

    <div class="glass rounded-2xl p-4 mb-5 flex flex-wrap items-center gap-3">
      <input v-model="search.deptCode" placeholder="部门编码" class="glass-input px-3 py-2 rounded-lg text-sm w-32" />
      <input v-model="search.deptName" placeholder="部门名称" class="glass-input px-3 py-2 rounded-lg text-sm w-44" />
      <input v-model="search.parentDeptCode" placeholder="上级部门" class="glass-input px-3 py-2 rounded-lg text-sm w-32" />
      <input v-model="search.deptManagerEmp" placeholder="部门主管" class="glass-input px-3 py-2 rounded-lg text-sm w-32" />
      <button @click="fetchData" class="glass-btn px-4 py-2 rounded-lg text-sm flex items-center gap-2">
        <Search class="w-4 h-4" /> 检索
      </button>
    </div>

    <div class="glass rounded-2xl overflow-hidden">
      <table class="glass-table">
        <thead>
          <tr>
            <th>部门编码</th>
            <th>部门名称</th>
            <th>上级部门</th>
            <th>部门主管</th>
            <th style="text-align: center !important; width: 120px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in filteredDepartments" :key="row.deptCode">
            <td class="font-medium">{{ row.deptCode }}</td>
            <td>{{ row.deptName }}</td>
            <td>{{ row.parentDeptCode || '无' }}</td>
            <td>{{ row.deptManagerEmp || '未指定' }}</td>
            <td style="text-align: center !important; width: 120px;">
              <div class="flex items-center justify-center gap-1">
                <button v-if="canEdit()" @click="openEdit(row)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-primary/20 text-primary transition-colors">
                  <Pencil class="w-3.5 h-3.5" />
                </button>
                <button v-if="canDelete()" @click="handleDelete(row.deptCode)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-danger/20 text-danger transition-colors">
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="filteredDepartments.length === 0">
            <td colspan="5" class="text-center py-12 text-text-muted">暂无部门数据</td>
          </tr>
        </tbody>
      </table>
    </div>
      

    <GlassModal :title="isEdit ? '编辑部门' : '新增部门'" :visible="showModal" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">部门编码</label>
          <input v-model="form.deptCode" :disabled="isEdit" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" placeholder="请输入部门编码" />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">部门名称</label>
          <input v-model="form.deptName" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" placeholder="请输入部门名称" />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">上级部门编码</label>
          <input v-model="form.parentDeptCode" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" placeholder="请输入上级部门编码" />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">部门主管工号</label>
          <input v-model="form.deptManagerEmp" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" placeholder="请输入部门主管工号" />
        </div>
      </div>
      <template #footer>
        <button @click="showModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">取消</button>
        <button @click="handleSave" class="glass-btn px-5 py-2 rounded-xl text-sm">保存</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>
