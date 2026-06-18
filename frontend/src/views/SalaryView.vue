<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import {
  searchSalaries, createSalary, updateSalary, deleteSalary,
  previewSalaries, generateSalaries, approveSalary, paySalary
} from '@/api/salary'
import { searchEmployees } from '@/api/employee'
import { usePermission } from '@/composables/usePermission'
import {
  Search, Plus, Pencil, Trash2, Calendar, Calculator, RefreshCw,
  CheckCircle, WalletCards
} from 'lucide-vue-next'

const { canCreate, canEdit, canDelete } = usePermission()

const salaries = ref<any[]>([])
const employees = ref<any[]>([])
const page = ref(0)
const totalPages = ref(1)
const searchEmpId = ref('')
const searchPayPeriod = ref('')
const previewPeriod = ref('')
const previewRows = ref<any[]>([])
const previewLoading = ref(false)
const generateLoading = ref(false)
const overwriteUnpaid = ref(true)
const toast = ref({ message: '', type: 'info' as 'success' | 'error' | 'info' })
const confirmState = ref({
  visible: false,
  title: '',
  message: '',
  confirmText: '确认',
  danger: false,
  action: null as null | (() => Promise<void>)
})

const showModal = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)

const form = ref({
  empId: '',
  payPeriod: '',
  baseSnap: 0,
  grossTotal: 0,
  deductTotal: 0,
  netPay: 0
})

const getEmployeeName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId || e.id === empId)
  return emp?.empName || emp?.name || '-'
}

const fetchData = async () => {
  try {
    const res: any = await searchSalaries({
      empId: searchEmpId.value || undefined,
      payPeriod: searchPayPeriod.value || undefined,
      page: page.value,
      size: 10
    })
    salaries.value = res.data.content
    totalPages.value = res.data.totalPages
  } catch (e) {
    console.error(e)
  }
}

const fetchEmployees = async () => {
  try {
    const res: any = await searchEmployees({ page: 0, size: 1000 })
    employees.value = res.data?.content || res.data || []
  } catch (e) {
    console.error(e)
  }
}

const currentPeriod = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const money = (value: any) => Number(value || 0).toLocaleString('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  toast.value = { message: `${message} `, type }
}

const openConfirm = (options: {
  title: string
  message: string
  confirmText?: string
  danger?: boolean
  action: () => Promise<void>
}) => {
  confirmState.value = {
    visible: true,
    title: options.title,
    message: options.message,
    confirmText: options.confirmText || '确认',
    danger: options.danger || false,
    action: options.action
  }
}

const runConfirm = async () => {
  const action = confirmState.value.action
  confirmState.value.visible = false
  if (action) await action()
}

const openAdd = () => {
  isEdit.value = false
  editId.value = null
  form.value = {
    empId: '', payPeriod: '',
    baseSnap: 0, grossTotal: 0, deductTotal: 0, netPay: 0
  }
  showModal.value = true
}

const handlePreview = async () => {
  if (!previewPeriod.value) {
    showToast('请先填写计薪周期', 'error')
    return
  }
  previewLoading.value = true
  try {
    const res: any = await previewSalaries(previewPeriod.value)
    previewRows.value = res.data || []
  } catch (e: any) {
    showToast(e.message || '计算预览失败', 'error')
  } finally {
    previewLoading.value = false
  }
}

const handleGenerate = async () => {
  if (!previewPeriod.value) {
    showToast('请先填写计薪周期', 'error')
    return
  }
  openConfirm({
    title: '生成工资',
    message: `确认生成 ${previewPeriod.value} 的工资记录？`,
    confirmText: '生成',
    action: async () => {
      generateLoading.value = true
      try {
        await generateSalaries({
          payPeriod: previewPeriod.value,
          overwriteUnpaid: overwriteUnpaid.value
        })
        await handlePreview()
        searchPayPeriod.value = previewPeriod.value
        page.value = 0
        fetchData()
        showToast('工资已生成', 'success')
      } catch (e: any) {
        showToast(e.message || '生成失败', 'error')
      } finally {
        generateLoading.value = false
      }
    }
  })
}

const openEdit = (row: any) => {
  isEdit.value = true
  editId.value = row.salaryId
  form.value = {
    empId: row.empId?.toString() || '',
    payPeriod: row.payPeriod || '',
    baseSnap: row.baseSnap || 0,
    grossTotal: row.grossTotal || 0,
    deductTotal: row.deductTotal || 0,
    netPay: row.netPay || 0
  }
  showModal.value = true
}

const handleSave = async () => {
  const data = {
    empId: Number(form.value.empId),
    payPeriod: form.value.payPeriod,
    baseSnap: Number(form.value.baseSnap),
    grossTotal: Number(form.value.grossTotal),
    deductTotal: Number(form.value.deductTotal),
    netPay: Number(form.value.netPay)
  }
  try {
    if (isEdit.value && editId.value) {
      await updateSalary(editId.value, data)
    } else {
      await createSalary(data)
    }
    showModal.value = false
    showToast('保存成功', 'success')
    fetchData()
  } catch (e: any) {
    showToast(e.message || '操作失败', 'error')
  }
}

const handleApprove = (row: any) => {
  openConfirm({
    title: '审核工资',
    message: `确认审核 ${getEmployeeName(row.empId)} ${row.payPeriod} 的工资？`,
    confirmText: '审核',
    action: async () => {
      try {
        await approveSalary(row.salaryId)
        showToast('工资已审核', 'success')
        fetchData()
      } catch (e: any) {
        showToast(e.message || '审核失败', 'error')
      }
    }
  })
}

const handlePay = (row: any) => {
  openConfirm({
    title: '发放工资',
    message: `确认发放 ${getEmployeeName(row.empId)} ${row.payPeriod} 的工资？`,
    confirmText: '发放',
    action: async () => {
      try {
        await paySalary(row.salaryId)
        showToast('工资已发放', 'success')
        fetchData()
      } catch (e: any) {
        showToast(e.message || '发放失败', 'error')
      }
    }
  })
}

const handleDelete = async (row: any) => {
  openConfirm({
    title: '删除工资',
    message: `确定删除 ${getEmployeeName(row.empId)} ${row.payPeriod} 的工资记录？`,
    confirmText: '删除',
    danger: true,
    action: async () => {
      try {
        await deleteSalary(row.salaryId)
        showToast('删除成功', 'success')
        fetchData()
      } catch (e: any) {
        showToast(e.message || '删除失败', 'error')
      }
    }
  })
}

onMounted(() => {
  previewPeriod.value = currentPeriod()
  fetchData()
  fetchEmployees()
})
</script>

<template>
  <AdminLayout>
    <ToastMessage :message="toast.message" :type="toast.type" :duration="2600" />
    <ConfirmDialog
      :visible="confirmState.visible"
      :title="confirmState.title"
      :message="confirmState.message"
      :confirm-text="confirmState.confirmText"
      :danger="confirmState.danger"
      @confirm="runConfirm"
      @cancel="confirmState.visible = false"
    />
        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-2xl font-semibold text-gray-800">工资核算</h1>
          </div>
          <button v-if="canCreate()" @click="openAdd" class="glass-btn px-5 py-2.5 rounded-xl flex items-center gap-2 text-sm">
            <Plus class="w-4 h-4" />
            手工新增
          </button>
        </div>

        <div class="glass rounded-2xl p-4 mb-5">
          <div class="flex flex-wrap items-center gap-3 mb-4">
            <div class="relative w-44">
              <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
              <input
                v-model="previewPeriod"
                placeholder="计薪周期 2026-05"
                class="glass-input w-full pl-9 pr-4 py-2 rounded-lg text-sm"
              />
            </div>
            <label class="inline-flex items-center gap-2 text-sm text-gray-700">
              <input v-model="overwriteUnpaid" type="checkbox" class="accent-primary" />
              覆盖未发放记录
            </label>
            <button
              v-if="canEdit()"
              @click="handlePreview"
              :disabled="previewLoading"
              class="glass-btn-secondary px-4 py-2 rounded-lg text-sm flex items-center gap-2 disabled:opacity-50"
            >
              <Calculator class="w-4 h-4" />
              {{ previewLoading ? '计算中...' : '计算预览' }}
            </button>
            <button
              v-if="canCreate()"
              @click="handleGenerate"
              :disabled="generateLoading"
              class="glass-btn px-4 py-2 rounded-lg text-sm flex items-center gap-2 disabled:opacity-50"
            >
              <RefreshCw class="w-4 h-4" />
              {{ generateLoading ? '生成中...' : '生成本期工资' }}
            </button>
          </div>

          <div v-if="previewRows.length > 0" class="overflow-x-auto">
            <table class="glass-table text-sm">
              <thead>
                <tr>
                  <th>员工</th>
                  <th>基本工资</th>
                  <th>绩效</th>
                  <th>全勤奖</th>
                  <th>加班</th>
                  <th>请假扣款</th>
                  <th>考勤扣款</th>
                  <th>实发</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in previewRows" :key="row.empId">
                  <td>{{ row.empName }}</td>
                  <td>¥{{ money(row.baseSalary) }}</td>
                  <td>{{ row.performanceGrade || '-' }} / ¥{{ money(row.performanceBonus) }}</td>
                  <td>+¥{{ money(row.fullAttendanceBonus) }}</td>
                  <td>{{ row.approvedOvertimeHours }}h / +¥{{ money(row.overtimePay) }}</td>
                  <td>{{ row.approvedLeaveDays }}天 / -¥{{ money(row.leaveDeduction) }}</td>
                  <td>
                    缺勤{{ row.absenceCount }} 迟到{{ row.lateCount }} 早退{{ row.earlyLeaveCount }}
                    / -¥{{ money(row.attendanceDeduction) }}
                  </td>
                  <td class="font-bold text-primary">¥{{ money(row.netPay) }}</td>
                  <td>
                    <span
                      class="px-2 py-0.5 rounded-md text-xs"
                      :class="row.generated ? 'bg-success/20 text-success' : 'bg-accent/20 text-accent'"
                    >
                      {{ row.status || (row.generated ? '已生成' : '未生成') }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="glass rounded-2xl p-4 mb-5 flex items-center gap-3">
          <div class="relative flex-1 max-w-xs">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
            <input
              v-model="searchEmpId"
              placeholder="员工ID"
              class="glass-input w-full pl-9 pr-4 py-2 rounded-lg text-sm"
              @keyup.enter="page = 0; fetchData()"
            />
          </div>
          <div class="relative flex-1 max-w-xs">
            <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
            <input
              v-model="searchPayPeriod"
              placeholder="工资周期 (如: 2024-05)"
              class="glass-input w-full pl-9 pr-4 py-2 rounded-lg text-sm"
              @keyup.enter="page = 0; fetchData()"
            />
          </div>
          <button @click="page = 0; fetchData()" class="glass-btn px-4 py-2 rounded-lg text-sm">
            搜索
          </button>
        </div>

        <div class="glass rounded-2xl overflow-hidden">
          <table class="glass-table">
            <thead>
              <tr>
                <th>员工</th>
                <th>工资周期</th>
                <th>基本工资</th>
                <th>绩效/全勤/加班</th>
                <th>请假/考勤扣款</th>
                <th>应发总额</th>
                <th>扣除总额</th>
                <th>实发工资</th>
                <th>状态</th>
                <th style="text-align: center !important; width: 160px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in salaries" :key="row.salaryId">
                <td class="font-medium">{{ getEmployeeName(row.empId) }}</td>
                <td>{{ row.payPeriod }}</td>
                <td>¥{{ money(row.baseSnap) }}</td>
                <td>¥{{ money(row.performanceBonus) }} / ¥{{ money(row.fullAttendanceBonus) }} / ¥{{ money(row.overtimePay) }}</td>
                <td>¥{{ money(row.leaveDeduction) }} / ¥{{ money(row.attendanceDeduction) }}</td>
                <td class="text-success">+¥{{ money(row.grossTotal) }}</td>
                <td class="text-danger">-¥{{ money(row.deductTotal) }}</td>
                <td class="font-bold text-primary">¥{{ money(row.netPay) }}</td>
                <td>
                  <span class="px-2 py-0.5 rounded-md text-xs"
                    :class="{
                      'bg-accent/20 text-accent': row.status === 'GENERATED',
                      'bg-primary/20 text-primary': row.status === 'APPROVED',
                      'bg-success/20 text-success': row.status === 'PAID'
                    }">
                    {{ row.status || 'GENERATED' }}
                  </span>
                </td>
                <td style="text-align: center !important; width: 160px;">
                  <div class="flex items-center justify-center gap-1">
                    <button v-if="canEdit() && row.status !== 'PAID'" @click="handleApprove(row)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-primary/20 text-primary transition-colors" title="审核">
                      <CheckCircle class="w-3.5 h-3.5" />
                    </button>
                    <button v-if="canEdit() && row.status === 'APPROVED'" @click="handlePay(row)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-success/20 text-success transition-colors" title="发放">
                      <WalletCards class="w-3.5 h-3.5" />
                    </button>
                    <button v-if="canEdit() && row.status !== 'PAID'" @click="openEdit(row)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-primary/20 text-primary transition-colors" title="编辑">
                      <Pencil class="w-3.5 h-3.5" />
                    </button>
                    <button v-if="canDelete() && row.status !== 'PAID'" @click="handleDelete(row)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-danger/20 text-danger transition-colors" title="删除">
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="salaries.length === 0">
                <td colspan="10" class="text-center py-12 text-text-muted">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex justify-center gap-2 mt-5">
          <button @click="page--; fetchData()" :disabled="page <= 0" class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40">上一页</button>
          <span class="px-4 py-2 text-sm text-text-muted">第 {{ page + 1 }} / {{ totalPages }} 页</span>
          <button @click="page++; fetchData()" :disabled="page >= totalPages - 1" class="glass-btn-secondary px-4 py-2 rounded-lg text-sm disabled:opacity-40">下一页</button>
        </div>
      

    <GlassModal :title="isEdit ? '编辑工资' : '新增工资'" :visible="showModal" @close="showModal = false">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-text-muted mb-1 block">员工</label>
            <select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
              <option value="">请选择</option>
              <option v-for="e in employees" :key="e.id" :value="e.id">{{ e.name }}</option>
            </select>
          </div>
          <div>
            <label class="text-sm text-text-muted mb-1 block">工资周期</label>
            <input v-model="form.payPeriod" placeholder="2024-05" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-text-muted mb-1 block">基本工资</label>
            <input v-model.number="form.baseSnap" type="number" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
          <div>
            <label class="text-sm text-text-muted mb-1 block">应发总额</label>
            <input v-model.number="form.grossTotal" type="number" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-sm text-text-muted mb-1 block">扣除总额</label>
            <input v-model.number="form.deductTotal" type="number" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
          <div>
            <label class="text-sm text-text-muted mb-1 block">实发工资</label>
            <input v-model.number="form.netPay" type="number" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" />
          </div>
        </div>
      </div>
      <template #footer>
        <button @click="showModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">取消</button>
        <button @click="handleSave" class="glass-btn px-5 py-2 rounded-xl text-sm">保存</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>
