<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import { getAllProjects, createProject, updateProject, deleteProject } from '@/api/project'
import { searchEmployees } from '@/api/employee'
import {
  getProjectMembers,
  replaceProjectMembers,
  deleteProjectMember,
  addProjectMember
} from '@/api/projectMember'
import { usePermission } from '@/composables/usePermission'
import { Plus, Pencil, Trash2, FolderKanban, Users, AlertCircle, CheckCircle2, Save } from 'lucide-vue-next'

const { canCreate, canEdit, canDelete } = usePermission()

// 项目列表
const projects = ref<any[]>([])
const employees = ref<any[]>([])
const showProjectModal = ref(false)
const isEdit = ref(false)
const editProjId = ref<number | null>(null)

const projectForm = ref({
  projName: '',
  projStatus: '进行中'
})

const statusOptions = ['进行中', '已完成', '已暂停']
const statusColor: Record<string, string> = {
  '进行中': 'text-blue-600 bg-blue-50',
  '已完成': 'text-green-600 bg-green-50',
  '已暂停': 'text-amber-600 bg-amber-50'
}

// 项目成员管理
const showMembersModal = ref(false)
const currentProject = ref<any>(null)
const projectMembers = ref<any[]>([])
const newMember = ref({ empId: null as number | null, roleName: '', contribCoeff: 0 })

// 员工名称映射
const empMap = ref<Record<number, string>>({})

const loadEmployees = async () => {
  try {
    const res: any = await searchEmployees({ page: 0, size: 9999 })
    employees.value = res.data?.content || res.data || []
    const map: Record<number, string> = {}
    employees.value.forEach((e: any) => {
      if (e.empId) map[e.empId] = e.empName || e.name || ''
    })
    empMap.value = map
  } catch (e) {
    console.error(e)
  }
}
const getEmpName = (empId: number | string | null | undefined) => {
  if (!empId) return ''
  return empMap.value[Number(empId)] || `员工${empId}`
}

const fetchData = async () => {
  try {
    const res: any = await getAllProjects()
    projects.value = res.data?.content || res.data || []
  } catch (e) {
    console.error(e)
  }
}

// === 项目 CRUD ===
const openAddProject = () => {
  isEdit.value = false
  editProjId.value = null
  projectForm.value = { projName: '', projStatus: '进行中' }
  showProjectModal.value = true
}
const openEditProject = (row: any) => {
  isEdit.value = true
  editProjId.value = row.projId
  projectForm.value = {
    projName: row.projName,
    projStatus: row.projStatus
  }
  showProjectModal.value = true
}
const handleSaveProject = async () => {
  try {
    const data = {
      projName: projectForm.value.projName,
      projStatus: projectForm.value.projStatus
    }
    if (isEdit.value && editProjId.value !== null) {
      await updateProject(editProjId.value, data)
    } else {
      await createProject(data)
    }
    showProjectModal.value = false
    fetchData()
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}
const handleDeleteProject = async (id: number) => {
  if (!confirm('确定删除该项目？关联的成员记录也会被删除。')) return
  try {
    await deleteProject(id)
    fetchData()
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}

// === 成员管理 ===
const openMembersModal = async (project: any) => {
  currentProject.value = project
  await loadProjectMembers(project.projId)
  showMembersModal.value = true
}

const loadProjectMembers = async (projId: number) => {
  try {
    const res: any = await getProjectMembers(projId)
    projectMembers.value = res.data || []
  } catch (e) {
    console.error(e)
    projectMembers.value = []
  }
}

// 贡献系数总和（实时）
const coeffSum = computed(() => {
  return projectMembers.value.reduce(
    (sum, m) => sum + (Number(m.contribCoeff) || 0),
    0
  )
})
const coeffValid = computed(() => Math.abs(coeffSum.value - 1) < 0.01)

// 百分比形式
const getPercent = (v: number) => (Number(v) * 100).toFixed(1) + '%'

// 添加成员
const handleAddMember = async () => {
  if (!newMember.value.empId || newMember.value.contribCoeff < 0) {
    alert('请选择员工并填写有效的贡献系数')
    return
  }
  // 重复员工检查
  if (projectMembers.value.some(m => m.empId === newMember.value.empId)) {
    alert('该员工已在项目成员中')
    return
  }
  try {
    await addProjectMember({
      empId: newMember.value.empId,
      projId: currentProject.value.projId,
      roleName: newMember.value.roleName,
      contribCoeff: newMember.value.contribCoeff
    })
    newMember.value = { empId: null, roleName: '', contribCoeff: 0 }
    await loadProjectMembers(currentProject.value.projId)
  } catch (e: any) {
    alert(e.message || '添加失败')
  }
}

// 删除成员
const handleRemoveMember = async (empId: number) => {
  if (!confirm('确定移除该成员？')) return
  try {
    await deleteProjectMember(empId, currentProject.value.projId)
    await loadProjectMembers(currentProject.value.projId)
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}

// 实时调整贡献系数
const handleCoeffChange = (index: number, value: number) => {
  const v = Math.max(0, Math.min(1, value))
  projectMembers.value[index].contribCoeff = v
}

// 平均分配按钮 - 自动平分剩余系数
const handleEqualize = () => {
  if (projectMembers.value.length === 0) return
  const equal = Number((1 / projectMembers.value.length).toFixed(2))
  projectMembers.value.forEach(m => {
    m.contribCoeff = equal
  })
  // 处理精度损失
  const last = projectMembers.value[projectMembers.value.length - 1]
  last.contribCoeff = Number((1 - equal * (projectMembers.value.length - 1)).toFixed(2))
}

// 保存所有成员（批量提交）
const handleSaveMembers = async () => {
  if (!coeffValid.value) {
    alert(`贡献系数之和必须等于 1.00 (100%)，当前总和 = ${getPercent(coeffSum.value)}`)
    return
  }
  try {
    await replaceProjectMembers({
      projId: currentProject.value.projId,
      members: projectMembers.value.map(m => ({
        empId: m.empId,
        roleName: m.roleName,
        contribCoeff: m.contribCoeff
      }))
    })
    alert('保存成功！')
    await loadProjectMembers(currentProject.value.projId)
  } catch (e: any) {
    alert(e.message || '保存失败')
  }
}

onMounted(() => {
  fetchData()
  loadEmployees()
})
</script>

<template>
  <AdminLayout>
        <div class="flex items-center justify-between mb-8">
          <div></div>
          <button v-if="canCreate()" @click="openAddProject" class="glass-btn px-5 py-2.5 rounded-xl flex items-center gap-2 text-sm">
            <Plus class="w-4 h-4" /> 新增项目
          </button>
        </div>

        <div class="glass rounded-2xl overflow-hidden">
          <table class="w-full">
            <thead>
              <tr class="border-b border-black/5">
                <th class="px-5 py-3.5 text-left text-sm font-medium text-gray-600">项目编号</th>
                <th class="px-5 py-3.5 text-left text-sm font-medium text-gray-600">项目名称</th>
                <th class="px-5 py-3.5 text-left text-sm font-medium text-gray-600">项目状态</th>
                <th class="px-5 py-3.5 text-sm font-medium text-gray-600" style="text-align: center !important; width: 120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in projects"
                :key="row.projId"
                class="border-b border-black/5 hover:bg-white/20 transition-colors"
              >
                <td class="px-5 py-3.5 text-sm text-gray-700">{{ row.projId }}</td>
                <td class="px-5 py-3.5 text-sm text-gray-700 font-medium">{{ row.projName }}</td>
                <td class="px-5 py-3.5 text-sm">
                  <span
                    class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium"
                    :class="statusColor[row.projStatus] || 'text-gray-600 bg-gray-50'"
                  >
                    <FolderKanban class="w-3 h-3" />
                    {{ row.projStatus }}
                  </span>
                </td>
                <td class="px-5 py-3.5 text-center">
                  <div class="inline-flex items-center justify-center gap-1">
                    <button v-if="canEdit()" @click="openMembersModal(row)" class="px-2.5 py-1 rounded-lg text-primary hover:bg-primary/10 transition-colors flex items-center gap-1 text-xs">
                      <Users class="w-3.5 h-3.5" /> 成员
                    </button>
                    <button v-if="canEdit()" @click="openEditProject(row)" class="flex items-center justify-center w-7 h-7 rounded-lg text-primary hover:bg-primary/10 transition-colors">
                      <Pencil class="w-3.5 h-3.5" />
                    </button>
                    <button v-if="canDelete()" @click="handleDeleteProject(row.projId)" class="flex items-center justify-center w-7 h-7 rounded-lg text-danger hover:bg-danger/10 transition-colors">
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="projects.length === 0">
                <td colspan="4" class="px-5 py-12 text-center text-text-muted">暂无项目数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      

    <!-- 项目编辑弹窗 -->
    <GlassModal :title="isEdit ? '编辑项目' : '新增项目'" :visible="showProjectModal" @close="showProjectModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">项目名称</label>
          <input
            v-model="projectForm.projName"
            class="glass-input w-full px-4 py-2.5 rounded-xl text-sm"
            placeholder="请输入项目名称"
          />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">项目状态</label>
          <select v-model="projectForm.projStatus" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
            <option v-for="opt in statusOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
        </div>
      </div>
      <template #footer>
        <button @click="showProjectModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">取消</button>
        <button @click="handleSaveProject" class="glass-btn px-5 py-2 rounded-xl text-sm">保存</button>
      </template>
    </GlassModal>

    <!-- 成员管理弹窗（贡献系数调整） -->
    <GlassModal
      :title="`项目成员管理 - ${currentProject?.projName || ''}`"
      :visible="showMembersModal"
      @close="showMembersModal = false"
      width="800px"
    >
      <div class="space-y-4">
        <!-- 总和状态条 -->
        <div
          class="rounded-xl p-3 flex items-center justify-between text-sm"
          :class="coeffValid
            ? 'bg-green-50 text-green-700'
            : 'bg-amber-50 text-amber-700'"
        >
          <div class="flex items-center gap-2">
            <component :is="coeffValid ? CheckCircle2 : AlertCircle" class="w-5 h-5" />
            <span>
              贡献系数总和:
              <strong>{{ getPercent(coeffSum) }}</strong>
              <span class="ml-1">/ 100%</span>
              <span v-if="!coeffValid" class="ml-2 text-xs">
                ({{ coeffSum > 1 ? '超出' : '不足' }} {{ Math.abs((coeffSum - 1) * 100).toFixed(1) }}%)
              </span>
            </span>
          </div>
          <div class="flex items-center gap-2">
            <button
              v-if="canEdit()"
              @click="handleEqualize"
              class="px-3 py-1 rounded-lg text-xs bg-white/60 hover:bg-white border border-current/20"
              title="平均分配 1/N 给每个成员"
            >
              平均分配
            </button>
            <button
              v-if="canEdit()"
              @click="handleSaveMembers"
              :disabled="!coeffValid"
              class="glass-btn px-3 py-1 rounded-lg text-xs flex items-center gap-1 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Save class="w-3 h-3" /> 保存
            </button>
          </div>
        </div>

        <!-- 成员列表 -->
        <div v-if="projectMembers.length > 0" class="space-y-2 max-h-72 overflow-y-auto">
          <div
            v-for="(m, idx) in projectMembers"
            :key="m.empId"
            class="border border-black/5 rounded-xl p-3 bg-white/30"
          >
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <span class="font-medium text-sm text-gray-700">{{ getEmpName(m.empId) }}</span>
                <span v-if="m.roleName" class="text-xs text-text-muted">({{ m.roleName }})</span>
              </div>
              <div class="flex items-center gap-3">
                <span
                  class="text-sm font-mono font-semibold"
                  :class="coeffValid ? 'text-green-600' : 'text-amber-600'"
                >
                  {{ getPercent(m.contribCoeff) }}
                </span>
                <button
                  v-if="canEdit()"
                  @click="handleRemoveMember(m.empId)"
                  class="p-1 rounded text-danger hover:bg-danger/10"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <input
                type="range"
                min="0"
                max="1"
                step="0.05"
                :value="m.contribCoeff"
                :disabled="!canEdit()"
                @input="(e: any) => handleCoeffChange(idx, parseFloat(e.target.value))"
                class="flex-1 h-2 accent-primary"
              />
              <input
                type="number"
                min="0"
                max="1"
                step="0.01"
                :value="m.contribCoeff"
                :disabled="!canEdit()"
                @input="(e: any) => handleCoeffChange(idx, parseFloat(e.target.value))"
                class="glass-input w-20 px-2 py-1 rounded-lg text-sm text-right"
              />
            </div>
          </div>
        </div>
        <div v-else class="text-center py-8 text-text-muted text-sm">
          暂无项目成员，请在下方添加
        </div>

        <!-- 添加新成员 -->
        <div v-if="canEdit()" class="border-t border-black/10 pt-3 mt-3">
          <div class="text-sm font-medium text-gray-700 mb-2">添加新成员</div>
          <div class="flex items-end gap-2">
            <div class="flex-1">
              <label class="text-xs text-text-muted mb-1 block">员工</label>
              <select v-model="newMember.empId" class="glass-input w-full px-3 py-2 rounded-lg text-sm">
                <option :value="null">请选择员工</option>
                <option v-for="e in employees" :key="e.empId" :value="e.empId">
                  {{ e.empName }} (#{{ e.empId }})
                </option>
              </select>
            </div>
            <div class="w-32">
              <label class="text-xs text-text-muted mb-1 block">角色</label>
              <input
                v-model="newMember.roleName"
                placeholder="如:前端"
                class="glass-input w-full px-3 py-2 rounded-lg text-sm"
              />
            </div>
            <div class="w-24">
              <label class="text-xs text-text-muted mb-1 block">贡献系数</label>
              <input
                v-model.number="newMember.contribCoeff"
                type="number"
                min="0"
                max="1"
                step="0.05"
                class="glass-input w-full px-3 py-2 rounded-lg text-sm text-right"
              />
            </div>
            <button @click="handleAddMember" class="glass-btn px-3 py-2 rounded-lg text-sm flex items-center gap-1 whitespace-nowrap">
              <Plus class="w-3.5 h-3.5" /> 添加
            </button>
          </div>
        </div>
      </div>
      <template #footer>
        <button @click="showMembersModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">关闭</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>
