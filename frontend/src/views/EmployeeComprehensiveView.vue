<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import { searchEmployees, updateEmployee } from '@/api/employee'
import { listDepartments } from '@/api/department'
import { listPositions } from '@/api/position'
import { listBonuses, createBonus, deleteBonus, listDeductions, createDeduction, deleteDeduction } from '@/api/adjustment'
import {
  Search, Pencil, Plus, Trash2, ArrowRightLeft, Banknote, Receipt,
  UserCog, Award, X, Calendar
} from 'lucide-vue-next'

const employees = ref<any[]>([])
const departments = ref<any[]>([])
const positions = ref<any[]>([])
const empSearch = ref('')
const loading = ref(false)
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message, type }
}

const showEmpModal = ref(false)
const editingEmp = ref<any>(null)
const empForm = ref({
  empName: '', gender: '男', posId: '', deptCode: '', hireDate: '', birthDate: '', empStatus: '在职'
})

const currentPayPeriod = ref(new Date().toISOString().slice(0, 7))
const bonuses = ref<any[]>([])
const deductions = ref<any[]>([])
const bonusForm = ref({ type: '项目奖', amount: 0, remark: '' })
const deductionForm = ref({ type: '迟到罚款', amount: 0, remark: '' })
const bonusTypes = ['项目奖', '绩效奖', '全勤奖', '节日补贴', '其他奖金']
const deductionTypes = ['迟到罚款', '早退罚款', '旷工罚款', '其他扣款']

const filteredEmployees = computed(() => {
  const kw = empSearch.value.trim().toLowerCase()
  if (!kw) return employees.value
  return employees.value.filter(e => (e.empName || '').toLowerCase().includes(kw))
})

const fetchEmployees = async () => {
  loading.value = true
  try {
    const res: any = await searchEmployees({ empName: empSearch.value || undefined, page: 0, size: 9999 })
    employees.value = res.data?.content || []
  } catch (e: any) {
    showToast(e.message || '获取员工列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const fetchOptions = async () => {
  try {
    const [dRes, pRes] = await Promise.all([listDepartments(), listPositions()])
    departments.value = (dRes as any).data || []
    positions.value = (pRes as any).data || []
  } catch (e) {
    console.error(e)
  }
}

const getDeptName = (deptCode: string) => {
  const dept = departments.value.find((d: any) => d.deptCode === deptCode)
  return dept ? dept.deptName : deptCode || '-'
}

const getPosName = (posId: number | string) => {
  const pos = positions.value.find((p: any) => p.posId === posId)
  return pos ? pos.posName : posId || '-'
}

const openEmpEdit = (emp: any) => {
  editingEmp.value = emp
  empForm.value = {
    empName: emp.empName || '',
    gender: emp.gender || '男',
    posId: emp.posId?.toString() || '',
    deptCode: emp.deptCode || '',
    hireDate: emp.hireDate || '',
    birthDate: emp.birthDate || '',
    empStatus: emp.empStatus || '在职'
  }
  currentPayPeriod.value = new Date().toISOString().slice(0, 7)
  showEmpModal.value = true
  loadAdjustments()
}

const handleSaveEmployee = async () => {
  if (!editingEmp.value) return
  try {
    await updateEmployee(editingEmp.value.empId, {
      empName: empForm.value.empName,
      gender: empForm.value.gender,
      posId: empForm.value.posId,
      deptCode: empForm.value.deptCode,
      hireDate: empForm.value.hireDate,
      birthDate: empForm.value.birthDate,
      empStatus: empForm.value.empStatus
    })
    Object.assign(editingEmp.value, empForm.value)
    showEmpModal.value = false
    showToast('员工信息已更新', 'success')
    fetchEmployees()
  } catch (e: any) {
    showToast(e.message || '更新失败', 'error')
  }
}

const loadAdjustments = async () => {
  if (!editingEmp.value) return
  try {
    const [bRes, dRes]: any = await Promise.all([
      listBonuses({ empId: editingEmp.value.empId, payPeriod: currentPayPeriod.value }),
      listDeductions({ empId: editingEmp.value.empId, payPeriod: currentPayPeriod.value })
    ])
    bonuses.value = bRes.data || []
    deductions.value = dRes.data || []
  } catch (e: any) {
    showToast(e.message || '加载奖金罚款失败', 'error')
  }
}

const handleAddBonus = async () => {
  if (!editingEmp.value) return
  if (!bonusForm.value.amount || bonusForm.value.amount <= 0) {
    showToast('请输入有效金额', 'error')
    return
  }
  try {
    await createBonus({
      empId: editingEmp.value.empId,
      payPeriod: currentPayPeriod.value,
      type: bonusForm.value.type,
      amount: bonusForm.value.amount,
      remark: bonusForm.value.remark
    })
    bonusForm.value = { type: '项目奖', amount: 0, remark: '' }
    await loadAdjustments()
    showToast('奖金已添加', 'success')
  } catch (e: any) {
    showToast(e.message || '添加失败', 'error')
  }
}

const handleDeleteBonus = async (id: number) => {
  if (!confirm('确定删除该奖金记录？')) return
  try {
    await deleteBonus(id)
    await loadAdjustments()
    showToast('奖金已删除', 'success')
  } catch (e: any) {
    showToast(e.message || '删除失败', 'error')
  }
}

const handleAddDeduction = async () => {
  if (!editingEmp.value) return
  if (!deductionForm.value.amount || deductionForm.value.amount <= 0) {
    showToast('请输入有效金额', 'error')
    return
  }
  try {
    await createDeduction({
      empId: editingEmp.value.empId,
      payPeriod: currentPayPeriod.value,
      type: deductionForm.value.type,
      amount: deductionForm.value.amount,
      remark: deductionForm.value.remark
    })
    deductionForm.value = { type: '迟到罚款', amount: 0, remark: '' }
    await loadAdjustments()
    showToast('罚款已添加', 'success')
  } catch (e: any) {
    showToast(e.message || '添加失败', 'error')
  }
}

const handleDeleteDeduction = async (id: number) => {
  if (!confirm('确定删除该罚款记录？')) return
  try {
    await deleteDeduction(id)
    await loadAdjustments()
    showToast('罚款已删除', 'success')
  } catch (e: any) {
    showToast(e.message || '删除失败', 'error')
  }
}

const bonusTotal = computed(() => bonuses.value.reduce((sum, b) => sum + (Number(b.preTaxAmt) || 0), 0))
const deductionTotal = computed(() => deductions.value.reduce((sum, d) => sum + (Number(d.deductAmt) || 0), 0))

onMounted(() => {
  fetchEmployees()
  fetchOptions()
})
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />

    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-text-primary">员工综合管理</h1>
    </div>

    <div class="glass rounded-2xl p-4 mb-5 flex items-center gap-3">
      <div class="relative flex-1 max-w-xs">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
        <input
          v-model="empSearch"
          @input="fetchEmployees"
          placeholder="搜索姓名..."
          class="glass-input w-full pl-9 pr-4 py-2 rounded-lg text-sm"
        />
      </div>
    </div>

    <div class="glass rounded-2xl overflow-hidden">
      <div class="overflow-x-auto">
        <table class="glass-table min-w-[800px]">
          <thead>
            <tr>
              <th class="whitespace-nowrap text-xs">工号</th>
              <th class="whitespace-nowrap text-xs">姓名</th>
              <th class="whitespace-nowrap text-xs">性别</th>
              <th class="whitespace-nowrap text-xs">部门</th>
              <th class="whitespace-nowrap text-xs">职位</th>
              <th class="whitespace-nowrap text-xs">入职日期</th>
              <th class="whitespace-nowrap text-xs">状态</th>
              <th class="whitespace-nowrap text-xs" style="text-align: center !important; width: 120px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="emp in filteredEmployees" :key="emp.empId">
              <td class="text-sm">{{ emp.empId }}</td>
              <td class="text-sm font-medium">{{ emp.empName }}</td>
              <td class="text-sm">{{ emp.gender }}</td>
              <td class="text-sm">
                <span class="px-2 py-0.5 rounded-md text-xs font-semibold bg-teal-100 text-teal-700">{{ getDeptName(emp.deptCode) }}</span>
              </td>
              <td class="text-sm">{{ getPosName(emp.posId) }}</td>
              <td class="text-sm text-text-muted">{{ emp.hireDate || '-' }}</td>
              <td class="text-sm">
                <span class="px-2 py-0.5 rounded-md text-xs font-semibold" :class="emp.empStatus === '在职' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
                  {{ emp.empStatus }}
                </span>
              </td>
              <td style="text-align: center !important; width: 120px;">
                <button @click="openEmpEdit(emp)" class="px-3 py-1.5 rounded-lg text-xs font-medium bg-primary/10 text-primary hover:bg-primary/20 transition-colors flex items-center gap-1.5 justify-center">
                  <Pencil class="w-3.5 h-3.5" /> 调整
                </button>
              </td>
            </tr>
            <tr v-if="filteredEmployees.length === 0">
              <td colspan="8" class="text-center py-12 text-text-muted">暂无员工数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 员工调整弹窗 -->
    <Transition name="modal">
      <div v-if="showEmpModal" class="fixed inset-0 flex items-center justify-center" style="z-index: 100">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="showEmpModal = false" />
        <div class="relative glass-strong rounded-2xl w-full mx-4 overflow-hidden" style="max-width: 720px; animation: modalIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)">
          <div class="flex items-center justify-between px-6 py-4 border-b border-white/10">
            <h3 class="text-lg font-semibold font-serif flex items-center gap-2">
              <UserCog class="w-5 h-5 text-primary" />
              调整员工 - {{ editingEmp?.empName }}
            </h3>
            <button @click="showEmpModal = false" class="p-1 rounded-lg hover:bg-white/10 transition-colors">
              <X class="w-5 h-5" />
            </button>
          </div>

          <div class="px-6 py-5 max-h-[80vh] overflow-y-auto">
            <div class="mb-6">
              <h4 class="text-sm font-medium mb-3 flex items-center gap-2 text-text-muted">
                <ArrowRightLeft class="w-4 h-4" /> 基本信息与职位调整
              </h4>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="text-xs text-text-muted mb-1 block">姓名</label>
                  <input v-model="empForm.empName" class="glass-input w-full px-3 py-2 rounded-lg text-sm" />
                </div>
                <div>
                  <label class="text-xs text-text-muted mb-1 block">性别</label>
                  <select v-model="empForm.gender" class="glass-input w-full px-3 py-2 rounded-lg text-sm">
                    <option value="男">男</option>
                    <option value="女">女</option>
                  </select>
                </div>
                <div>
                  <label class="text-xs text-text-muted mb-1 block">部门</label>
                  <select v-model="empForm.deptCode" class="glass-input w-full px-3 py-2 rounded-lg text-sm">
                    <option v-for="d in departments" :key="d.deptCode" :value="d.deptCode">{{ d.deptName }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-xs text-text-muted mb-1 block">职位</label>
                  <select v-model="empForm.posId" class="glass-input w-full px-3 py-2 rounded-lg text-sm">
                    <option v-for="p in positions" :key="p.posId" :value="p.posId">{{ p.posName }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-xs text-text-muted mb-1 block">入职日期</label>
                  <input v-model="empForm.hireDate" type="date" class="glass-input w-full px-3 py-2 rounded-lg text-sm" />
                </div>
                <div>
                  <label class="text-xs text-text-muted mb-1 block">出生日期</label>
                  <input v-model="empForm.birthDate" type="date" class="glass-input w-full px-3 py-2 rounded-lg text-sm" />
                </div>
                <div>
                  <label class="text-xs text-text-muted mb-1 block">在职状态</label>
                  <select v-model="empForm.empStatus" class="glass-input w-full px-3 py-2 rounded-lg text-sm">
                    <option value="在职">在职</option>
                    <option value="离职">离职</option>
                  </select>
                </div>
              </div>
              <button @click="handleSaveEmployee" class="glass-btn w-full mt-4 py-2 rounded-xl text-sm font-medium">
                保存员工信息
              </button>
            </div>

            <div class="border-t border-white/10 pt-5">
              <div class="flex items-center justify-between mb-5">
                <h4 class="text-sm font-medium flex items-center gap-2 text-text-muted">
                  <Banknote class="w-4 h-4" /> 月度奖金与罚款
                </h4>
                <div class="flex items-center gap-2 text-sm relative">
                  <span class="text-text-muted">计薪月份</span>
                  <Calendar class="absolute left-[68px] top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-text-muted pointer-events-none" />
                  <input v-model="currentPayPeriod" @change="loadAdjustments" type="text" placeholder="2026-01" maxlength="7" class="glass-input pl-7 pr-2 py-1.5 rounded-lg text-sm w-28" />
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <!-- 奖金 -->
                <div class="bg-white/20 rounded-xl p-5">
                  <div class="flex items-center justify-between mb-4 pb-3 border-b border-white/10">
                    <div class="flex items-center gap-2">
                      <div class="w-8 h-8 rounded-lg bg-amber-500/15 flex items-center justify-center">
                        <Award class="w-4 h-4 text-amber-600" />
                      </div>
                      <span class="font-medium">奖金记录</span>
                    </div>
                    <span class="text-sm font-semibold text-amber-600">合计 +{{ bonusTotal.toFixed(2) }}</span>
                  </div>

                  <div class="space-y-2 max-h-44 overflow-y-auto mb-4 pr-1">
                    <div v-for="b in bonuses" :key="b.bonusId" class="flex items-center justify-between bg-white/30 rounded-lg px-4 py-3 text-sm">
                      <div class="min-w-0">
                        <div class="font-medium truncate">{{ b.bonusType }}</div>
                        <div class="text-xs text-text-muted truncate">{{ b.remark || '无备注' }}</div>
                      </div>
                      <div class="flex items-center gap-3 flex-shrink-0 ml-3">
                        <span class="font-semibold text-amber-600">+{{ Number(b.preTaxAmt).toFixed(2) }}</span>
                        <button @click="handleDeleteBonus(b.bonusId)" class="p-1.5 rounded-lg text-danger hover:bg-danger/10 transition-colors">
                          <Trash2 class="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                    <div v-if="bonuses.length === 0" class="text-sm text-text-muted text-center py-6 bg-white/10 rounded-lg">暂无奖金记录</div>
                  </div>

                  <div class="space-y-3">
                    <div>
                      <label class="text-xs text-text-muted mb-1 block">奖金类型</label>
                      <select v-model="bonusForm.type" class="glass-input w-full px-3 py-2 rounded-lg text-sm">
                        <option v-for="t in bonusTypes" :key="t" :value="t">{{ t }}</option>
                      </select>
                    </div>
                    <div class="grid grid-cols-2 gap-3">
                      <div>
                        <label class="text-xs text-text-muted mb-1 block">金额</label>
                        <input v-model.number="bonusForm.amount" type="number" min="0" step="0.01" placeholder="0.00" class="glass-input w-full px-3 py-2 rounded-lg text-sm" />
                      </div>
                      <div>
                        <label class="text-xs text-text-muted mb-1 block">备注</label>
                        <input v-model="bonusForm.remark" placeholder="可选" class="glass-input w-full px-3 py-2 rounded-lg text-sm" />
                      </div>
                    </div>
                    <button @click="handleAddBonus" class="glass-btn w-full py-2 rounded-lg text-sm flex items-center justify-center gap-1.5">
                      <Plus class="w-4 h-4" /> 添加奖金
                    </button>
                  </div>
                </div>

                <!-- 罚款 -->
                <div class="bg-white/20 rounded-xl p-5">
                  <div class="flex items-center justify-between mb-4 pb-3 border-b border-white/10">
                    <div class="flex items-center gap-2">
                      <div class="w-8 h-8 rounded-lg bg-red-500/15 flex items-center justify-center">
                        <Receipt class="w-4 h-4 text-red-600" />
                      </div>
                      <span class="font-medium">罚款记录</span>
                    </div>
                    <span class="text-sm font-semibold text-red-600">合计 -{{ deductionTotal.toFixed(2) }}</span>
                  </div>

                  <div class="space-y-2 max-h-44 overflow-y-auto mb-4 pr-1">
                    <div v-for="d in deductions" :key="d.deductId" class="flex items-center justify-between bg-white/30 rounded-lg px-4 py-3 text-sm">
                      <div class="min-w-0">
                        <div class="font-medium truncate">{{ d.deductType }}</div>
                        <div class="text-xs text-text-muted truncate">{{ d.remark || '无备注' }}</div>
                      </div>
                      <div class="flex items-center gap-3 flex-shrink-0 ml-3">
                        <span class="font-semibold text-red-600">-{{ Number(d.deductAmt).toFixed(2) }}</span>
                        <button @click="handleDeleteDeduction(d.deductId)" class="p-1.5 rounded-lg text-danger hover:bg-danger/10 transition-colors">
                          <Trash2 class="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                    <div v-if="deductions.length === 0" class="text-sm text-text-muted text-center py-6 bg-white/10 rounded-lg">暂无罚款记录</div>
                  </div>

                  <div class="space-y-3">
                    <div>
                      <label class="text-xs text-text-muted mb-1 block">罚款类型</label>
                      <select v-model="deductionForm.type" class="glass-input w-full px-3 py-2 rounded-lg text-sm">
                        <option v-for="t in deductionTypes" :key="t" :value="t">{{ t }}</option>
                      </select>
                    </div>
                    <div class="grid grid-cols-2 gap-3">
                      <div>
                        <label class="text-xs text-text-muted mb-1 block">金额</label>
                        <input v-model.number="deductionForm.amount" type="number" min="0" step="0.01" placeholder="0.00" class="glass-input w-full px-3 py-2 rounded-lg text-sm" />
                      </div>
                      <div>
                        <label class="text-xs text-text-muted mb-1 block">备注</label>
                        <input v-model="deductionForm.remark" placeholder="可选" class="glass-input w-full px-3 py-2 rounded-lg text-sm" />
                      </div>
                    </div>
                    <button @click="handleAddDeduction" class="glass-btn w-full py-2 rounded-lg text-sm flex items-center justify-center gap-1.5">
                      <Plus class="w-4 h-4" /> 添加罚款
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </AdminLayout>
</template>

<style scoped>
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.25s ease;
}
.modal-enter-from, .modal-leave-to {
  opacity: 0;
}
@keyframes modalIn {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
</style>
