<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import { listPositions, createPosition, updatePosition, deletePosition } from '@/api/position'
import { listDepartments } from '@/api/department'
import { usePermission } from '@/composables/usePermission'
import { Plus, Pencil, Trash2, Briefcase } from 'lucide-vue-next'

const { canCreate, canEdit, canDelete } = usePermission()

const positions = ref<any[]>([])
const departments = ref<any[]>([])
const showModal = ref(false)
const isEdit = ref(false)
const editPosId = ref<string | null>(null)
const form = ref({ posId: '', posName: '', baseSalary: 0, deptCode: '' })

const fetchData = async () => {
  try {
    const [pRes, dRes] = await Promise.all([listPositions(), listDepartments()])
    positions.value = (pRes as any).data
    departments.value = (dRes as any).data
  } catch (e) {
    console.error(e)
  }
}

const getDeptName = (deptCode: string) => {
  return departments.value.find(d => d.deptCode === deptCode)?.deptName || '-'
}

const openAdd = () => {
  isEdit.value = false
  editPosId.value = null
  form.value = { posId: '', posName: '', baseSalary: 0, deptCode: '' }
  showModal.value = true
}

const openEdit = (row: any) => {
  isEdit.value = true
  editPosId.value = row.posId
  form.value = {
    posId: row.posId,
    posName: row.posName,
    baseSalary: row.baseSalary,
    deptCode: row.deptCode || ''
  }
  showModal.value = true
}

const handleSave = async () => {
  try {
    const data = {
      posId: form.value.posId,
      posName: form.value.posName,
      baseSalary: form.value.baseSalary,
      deptCode: form.value.deptCode
    }
    if (isEdit.value && editPosId.value) {
      await updatePosition(editPosId.value, data)
    } else {
      await createPosition(data)
    }
    showModal.value = false
    fetchData()
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}

const handleDelete = async (posId: string) => {
  if (!confirm('确定删除该职位？')) return
  try {
    await deletePosition(posId)
    fetchData()
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}

onMounted(fetchData)
</script>

<template>
  <AdminLayout>
        <div class="flex items-center justify-between mb-8">
          <div></div>
          <button v-if="canCreate()" @click="openAdd" class="glass-btn px-5 py-2.5 rounded-xl flex items-center gap-2 text-sm">
            <Plus class="w-4 h-4" /> 新增职位
          </button>
        </div>

        <div class="glass rounded-2xl overflow-hidden">
          <table class="glass-table">
            <thead>
              <tr>
                <th>职位编号</th>
                <th>职位名称</th>
                <th>基本工资</th>
                <th>所属部门</th>
                <th style="text-align: center !important; width: 120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in positions" :key="row.posId">
                <td class="font-medium">{{ row.posId }}</td>
                <td class="font-medium">{{ row.posName }}</td>
                <td class="text-primary font-medium">¥{{ row.baseSalary?.toFixed(2) }}</td>
                <td>
                  <span class="px-2 py-0.5 rounded-md text-xs bg-primary/20 text-primary">
                    {{ getDeptName(row.deptCode) }}
                  </span>
                </td>
                <td style="text-align: center !important; width: 120px;">
                  <div class="flex items-center justify-center gap-1">
                    <button v-if="canEdit()" @click="openEdit(row)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-primary/20 text-primary transition-colors">
                      <Pencil class="w-3.5 h-3.5" />
                    </button>
                    <button v-if="canDelete()" @click="handleDelete(row.posId)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-danger/20 text-danger transition-colors">
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="positions.length === 0">
                <td colspan="5" class="text-center py-12 text-text-muted">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      

    <GlassModal :title="isEdit ? '编辑职位' : '新增职位'" :visible="showModal" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">职位编号</label>
          <input v-model="form.posId" :disabled="isEdit" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" placeholder="请输入职位编号" />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">职位名称</label>
          <input v-model="form.posName" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" placeholder="请输入职位名称" />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">基本工资</label>
          <input v-model.number="form.baseSalary" type="number" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" placeholder="请输入基本工资" />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">所属部门</label>
          <select v-model="form.deptCode" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
            <option value="">请选择</option>
            <option v-for="d in departments" :key="d.deptCode" :value="d.deptCode">{{ d.deptName }}</option>
          </select>
        </div>
      </div>
      <template #footer>
        <button @click="showModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">取消</button>
        <button @click="handleSave" class="glass-btn px-5 py-2 rounded-xl text-sm">保存</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>
