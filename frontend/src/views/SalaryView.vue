<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import ToastMessage from '@/components/ToastMessage.vue'
import {
  searchSalaries, createSalary, updateSalary, deleteSalary,
  previewSalaries, generateSalaries, approveSalary, paySalary,
  getSalariesByEmployee, batchApproveSalaries, batchPaySalaries
} from '@/api/salary'
import { searchEmployees } from '@/api/employee'
import { usePermission } from '@/composables/usePermission'
import {
  Search, Plus, Pencil, Trash2, Calendar, Calculator, RefreshCw,
  CheckCircle, WalletCards, FileSpreadsheet, FileText, History,
  X, ChevronDown, Zap
} from 'lucide-vue-next'
import * as XLSX from 'xlsx'

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

const selectedRowIds = ref<number[]>([])
const showSelectedOnly = ref(false)
const showPrintMenu = ref(false)

const showModal = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)

const showHistoryModal = ref(false)
const historyRows = ref<any[]>([])
const historyLoading = ref(false)

const form = ref({
  empId: '',
  payPeriod: '',
  baseSnap: 0,
  grossTotal: 0,
  deductTotal: 0,
  netPay: 0
})

const getEmployeeName = (empId: number) => {
  const emp = employees.value.find(e => e.empId === empId)
  return emp?.empName || '-'
}

const fetchData = async () => {
  try {
    const params: any = {
      payPeriod: searchPayPeriod.value || undefined,
      page: page.value,
      size: 10
    }
    if (searchEmpId.value) params.empId = Number(searchEmpId.value)
    const res: any = await searchSalaries(params)
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

const statusLabel = (status?: string) => {
  const map: Record<string, string> = {
    GENERATED: '已生成',
    APPROVED: '已审核',
    PAID: '已发放'
  }
  return map[status || ''] || '已生成'
}

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
        const payload: any = {
          payPeriod: previewPeriod.value,
          overwriteUnpaid: overwriteUnpaid.value
        }
        if (selectedRowIds.value.length > 0) {
          payload.empIds = salaries.value
            .filter(s => selectedRowIds.value.includes(s.salaryId))
            .map(s => s.empId)
        }
        await generateSalaries(payload)
        await handlePreview()
        searchPayPeriod.value = previewPeriod.value
        page.value = 0
        selectedRowIds.value = []
        showSelectedOnly.value = false
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
  if (!form.value.empId) {
    showToast('请选择员工', 'error')
    return
  }
  if (!form.value.payPeriod || !/^\d{4}-\d{2}$/.test(form.value.payPeriod)) {
    showToast('工资周期格式应为 YYYY-MM', 'error')
    return
  }
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
        const idx = selectedRowIds.value.indexOf(row.salaryId); if (idx >= 0) selectedRowIds.value.splice(idx, 1)
        showToast('删除成功', 'success')
        fetchData()
      } catch (e: any) {
        showToast(e.message || '删除失败', 'error')
      }
    }
  })
}

const handleBatchApprove = () => {
  const ids = [...selectedRowIds.value]
  if (ids.length === 0) { showToast('请先选择要审核的记录', 'error'); return }
  openConfirm({
    title: '批量审核',
    message: `确认审核选中的 ${ids.length} 条工资记录？`,
    confirmText: '批量审核',
    action: async () => {
      try {
        await batchApproveSalaries(ids)
        selectedRowIds.value = []
        showSelectedOnly.value = false
        fetchData()
        showToast('批量审核成功', 'success')
      } catch (e: any) {
        showToast(e.message || '批量审核失败', 'error')
      }
    }
  })
}

const handleBatchPay = () => {
  const ids = [...selectedRowIds.value]
  if (ids.length === 0) { showToast('请先选择要发放的记录', 'error'); return }
  openConfirm({
    title: '批量发放',
    message: `确认发放选中的 ${ids.length} 条工资记录？`,
    confirmText: '批量发放',
    danger: false,
    action: async () => {
      try {
        await batchPaySalaries(ids)
        selectedRowIds.value = []
        showSelectedOnly.value = false
        fetchData()
        showToast('批量发放成功', 'success')
      } catch (e: any) {
        showToast(e.message || '批量发放失败', 'error')
      }
    }
  })
}

// 选中框逻辑
const displaySalaries = computed(() => {
  if (showSelectedOnly.value) {
    return salaries.value.filter(s => selectedRowIds.value.includes(s.salaryId))
  }
  return salaries.value
})

const allSelected = computed(() => {
  return displaySalaries.value.length > 0 && displaySalaries.value.every(r => selectedRowIds.value.includes(r.salaryId))
})

const toggleSelectAll = () => {
  if (allSelected.value) {
    const displayIds = new Set(displaySalaries.value.map(r => r.salaryId))
    selectedRowIds.value = selectedRowIds.value.filter(id => !displayIds.has(id))
  } else {
    const existing = new Set(selectedRowIds.value)
    displaySalaries.value.forEach(r => {
      if (!existing.has(r.salaryId)) {
        selectedRowIds.value.push(r.salaryId)
      }
    })
  }
}

const toggleRow = (id: number) => {
  const idx = selectedRowIds.value.indexOf(id)
  if (idx >= 0) {
    selectedRowIds.value.splice(idx, 1)
  } else {
    selectedRowIds.value.push(id)
  }
}

const selectedRows = computed(() => {
  return salaries.value.filter(s => selectedRowIds.value.includes(s.salaryId))
})

const exportTargetRows = computed(() => {
  return selectedRows.value.length > 0 ? selectedRows.value : displaySalaries.value
})

const historyEmpId = computed(() => {
  if (searchEmpId.value) return Number(searchEmpId.value)
  if (selectedRows.value.length === 1) return selectedRows.value[0].empId
  return null
})

const handleSearch = () => {
  page.value = 0
  if (selectedRowIds.value.length > 0) {
    showSelectedOnly.value = true
  } else {
    showSelectedOnly.value = false
    fetchData()
  }
}

const clearSelection = () => {
  selectedRowIds.value = []
  showSelectedOnly.value = false
}

const baseColumns = [
  { key: 'empName', title: '员工' },
  { key: 'payPeriod', title: '工资周期' },
  { key: 'baseSnap', title: '基本工资' },
  { key: 'performanceBonus', title: '绩效奖金' },
  { key: 'fullAttendanceBonus', title: '全勤奖' },
  { key: 'overtimePay', title: '加班工资' },
  { key: 'extraBonus', title: '其他奖金' },
  { key: 'leaveDeduction', title: '请假扣款' },
  { key: 'attendanceDeduction', title: '考勤扣款' },
  { key: 'extraDeduction', title: '其他罚款' },
  { key: 'taxDeduction', title: '个税扣除' },
  { key: 'insuranceDeduction', title: '社保扣除' },
  { key: 'grossTotal', title: '应发总额' },
  { key: 'deductTotal', title: '扣除总额' },
  { key: 'netPay', title: '实发工资' },
  { key: 'status', title: '状态' }
]

const exportExcel = () => {
  const rows = exportTargetRows.value.map(r => ({
    员工: getEmployeeName(r.empId),
    工资周期: r.payPeriod,
    基本工资: Number(r.baseSnap || 0),
    绩效奖金: Number(r.performanceBonus || 0),
    全勤奖: Number(r.fullAttendanceBonus || 0),
    加班工资: Number(r.overtimePay || 0),
    其他奖金: Number(r.extraBonus || 0),
    请假扣款: Number(r.leaveDeduction || 0),
    考勤扣款: Number(r.attendanceDeduction || 0),
    其他罚款: Number(r.extraDeduction || 0),
    个税扣除: Number(r.taxDeduction || 0),
    社保扣除: Number(r.insuranceDeduction || 0),
    应发总额: Number(r.grossTotal || 0),
    扣除总额: Number(r.deductTotal || 0),
    实发工资: Number(r.netPay || 0),
    状态: statusLabel(r.status)
  }))
  const ws = XLSX.utils.json_to_sheet(rows)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '工资表')
  XLSX.writeFile(wb, `工资表_${new Date().toISOString().slice(0, 10)}.xlsx`)
}

const printPDF = () => {
  const rows = exportTargetRows.value.map(r => {
    const esc = (s: any) => String(s ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
    return `
    <tr>
      <td>${esc(getEmployeeName(r.empId))}</td>
      <td>${esc(r.payPeriod)}</td>
      <td>${money(r.baseSnap)}</td>
      <td>${money(r.performanceBonus)}</td>
      <td>${money(r.fullAttendanceBonus)}</td>
      <td>${money(r.overtimePay)}</td>
      <td>${money(r.extraBonus)}</td>
      <td>${money(r.leaveDeduction)}</td>
      <td>${money(r.attendanceDeduction)}</td>
      <td>${money(r.extraDeduction)}</td>
      <td>${money(r.taxDeduction)}</td>
      <td>${money(r.insuranceDeduction)}</td>
      <td>${money(r.grossTotal)}</td>
      <td>${money(r.deductTotal)}</td>
      <td>${money(r.netPay)}</td>
      <td>${esc(statusLabel(r.status))}</td>
    </tr>
  `
  }).join('')
  const html = `
    <html>
      <head>
        <title>工资表</title>
        <style>
          body { font-family: "Microsoft YaHei", sans-serif; padding: 24px; }
          h2 { text-align: center; }
          table { width: 100%; border-collapse: collapse; font-size: 12px; }
          th, td { border: 1px solid #ccc; padding: 6px; text-align: center; }
          th { background: #4f46e5; color: #fff; }
        </style>
      </head>
      <body>
        <h2>工资表</h2>
        <table>
          <thead>
            <tr>${baseColumns.map(c => `<th>${c.title}</th>`).join('')}</tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>
      </body>
    </html>
  `
  const printWindow = window.open('', '_blank')
  if (!printWindow) return
  printWindow.document.write(html)
  printWindow.document.close()
  printWindow.focus()
  setTimeout(() => printWindow.print(), 300)
}

const openHistory = async () => {
  const empId = historyEmpId.value
  if (!empId) return
  historyLoading.value = true
  showHistoryModal.value = true
  try {
    const res: any = await getSalariesByEmployee(empId)
    historyRows.value = res.data || []
  } catch (e: any) {
    showToast(e.message || '获取历史工资失败', 'error')
    historyRows.value = []
  } finally {
    historyLoading.value = false
  }
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
    <div class="mb-8">
      <h1 class="text-2xl font-semibold text-gray-800">工资核算</h1>
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
          v-if="canEdit()"
          @click="handleGenerate"
          :disabled="generateLoading || !previewRows.length"
          class="glass-btn px-4 py-2 rounded-lg text-sm flex items-center gap-2 disabled:opacity-50"
        >
          <Zap class="w-4 h-4" />
          {{ generateLoading ? '生成中...' : '生成工资' }}
        </button>
        <div class="relative">
          <button
            @click="showPrintMenu = !showPrintMenu"
            class="glass-btn px-4 py-2 rounded-lg text-sm flex items-center gap-2"
          >
            <FileText class="w-4 h-4" />
            打印
            <ChevronDown class="w-3 h-3" />
          </button>
          <div
            v-if="showPrintMenu"
            class="absolute right-0 mt-2 w-40 glass rounded-xl shadow-lg py-1 z-20"
          >
            <button
              @click="exportExcel(); showPrintMenu = false"
              class="w-full text-left px-4 py-2 text-sm hover:bg-white/10 flex items-center gap-2"
            >
              <FileSpreadsheet class="w-4 h-4" /> 导出 Excel
            </button>
            <button
              @click="printPDF(); showPrintMenu = false"
              class="w-full text-left px-4 py-2 text-sm hover:bg-white/10 flex items-center gap-2"
            >
              <FileText class="w-4 h-4" /> 打印 PDF
            </button>
          </div>
        </div>
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
              <th>其他奖金</th>
              <th>请假扣款</th>
              <th>考勤扣款</th>
              <th>其他罚款</th>
              <th>个税</th>
              <th>社保</th>
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
              <td class="text-amber-700">+¥{{ money(row.extraBonus) }}</td>
              <td>{{ row.approvedLeaveDays }}天 / -¥{{ money(row.leaveDeduction) }}</td>
              <td>
                缺勤{{ row.absenceCount }} 迟到{{ row.lateCount }} 早退{{ row.earlyLeaveCount }}
                / -¥{{ money(row.attendanceDeduction) }}
              </td>
              <td class="text-red-700">-¥{{ money(row.extraDeduction) }}</td>
              <td class="text-orange-700">-¥{{ money(row.taxDeduction) }}</td>
              <td class="text-blue-700">-¥{{ money(row.insuranceDeduction) }}</td>
              <td class="font-bold text-primary">¥{{ money(row.netPay) }}</td>
              <td>
                <span
                  class="px-2 py-0.5 rounded-md text-xs"
                  :class="row.generated ? 'bg-emerald-100 text-emerald-700 font-semibold' : 'bg-amber-100 text-amber-700 font-semibold'"
                >
                  {{ row.status || (row.generated ? '已生成' : '未生成') }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="glass rounded-2xl p-4 mb-5 flex flex-wrap items-center gap-3">
      <select
        v-model="searchEmpId"
        class="glass-input px-3 py-2 rounded-lg text-sm min-w-[160px]"
      >
        <option value="">全部员工</option>
        <option v-for="e in employees" :key="e.empId" :value="String(e.empId)">{{ e.empName }}</option>
      </select>
      <div class="relative">
        <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
        <input
          v-model="searchPayPeriod"
          placeholder="工资周期 (如: 2024-05)"
          class="glass-input pl-9 pr-4 py-2 rounded-lg text-sm w-52"
          @keyup.enter="handleSearch"
        />
      </div>
      <button @click="handleSearch" class="glass-btn px-4 py-2 rounded-lg text-sm flex items-center gap-2">
        <Search class="w-4 h-4" />
        {{ selectedRowIds.length > 0 ? '显示选中' : '搜索' }}
      </button>
      <button
        v-if="selectedRowIds.length > 0 || showSelectedOnly"
        @click="clearSelection"
        class="glass-btn-secondary px-3 py-2 rounded-lg text-sm flex items-center gap-1"
      >
        <X class="w-3.5 h-3.5" /> 清除选中
      </button>
      <button
        v-if="selectedRowIds.length > 0 && canEdit()"
        @click="handleBatchApprove"
        class="glass-btn px-3 py-2 rounded-lg text-sm flex items-center gap-1"
      >
        <CheckCircle class="w-3.5 h-3.5" /> 批量审核 ({{ selectedRowIds.length }})
      </button>
      <button
        v-if="selectedRowIds.length > 0 && canEdit()"
        @click="handleBatchPay"
        class="glass-btn px-3 py-2 rounded-lg text-sm flex items-center gap-1"
      >
        <WalletCards class="w-3.5 h-3.5" /> 批量发放 ({{ selectedRowIds.length }})
      </button>
      <button
        v-if="historyEmpId"
        @click="openHistory"
        class="glass-btn-secondary px-4 py-2 rounded-lg text-sm flex items-center gap-2"
      >
        <History class="w-4 h-4" /> 历史工资记录
      </button>
    </div>

    <div class="flex justify-end mb-3">
      <button v-if="canCreate()" @click="openAdd" class="glass-btn px-4 py-2 rounded-lg flex items-center gap-2 text-sm">
        <Plus class="w-4 h-4" /> 手工新增
      </button>
    </div>

    <div class="glass rounded-2xl overflow-hidden">
      <div class="overflow-x-auto">
        <table class="glass-table min-w-[1600px]">
          <thead>
            <tr>
              <th class="w-10 text-center">
                <input
                  type="checkbox"
                  :checked="allSelected"
                  @change="toggleSelectAll"
                  class="accent-primary"
                />
              </th>
              <th class="whitespace-nowrap text-xs">员工</th>
              <th class="whitespace-nowrap text-xs">工资周期</th>
              <th class="whitespace-nowrap text-xs">基本工资</th>
              <th class="whitespace-nowrap text-xs">绩效奖金</th>
              <th class="whitespace-nowrap text-xs">全勤奖</th>
              <th class="whitespace-nowrap text-xs">加班工资</th>
              <th class="whitespace-nowrap text-xs">其他奖金</th>
              <th class="whitespace-nowrap text-xs">请假扣款</th>
              <th class="whitespace-nowrap text-xs">考勤扣款</th>
              <th class="whitespace-nowrap text-xs">其他罚款</th>
              <th class="whitespace-nowrap text-xs">个税扣除</th>
              <th class="whitespace-nowrap text-xs">社保扣除</th>
              <th class="whitespace-nowrap text-xs">应发总额</th>
              <th class="whitespace-nowrap text-xs">扣除总额</th>
              <th class="whitespace-nowrap text-xs">实发工资</th>
              <th class="whitespace-nowrap text-xs">状态</th>
              <th class="whitespace-nowrap text-xs" style="text-align: center !important; width: 140px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in displaySalaries" :key="row.salaryId">
              <td class="text-center">
                <input
                  type="checkbox"
                  :checked="selectedRowIds.includes(row.salaryId)"
                  @change="toggleRow(row.salaryId)"
                  class="accent-primary"
                />
              </td>
              <td class="font-medium whitespace-nowrap">{{ getEmployeeName(row.empId) }}</td>
              <td class="whitespace-nowrap">{{ row.payPeriod }}</td>
              <td class="whitespace-nowrap">¥{{ money(row.baseSnap) }}</td>
              <td class="whitespace-nowrap">¥{{ money(row.performanceBonus) }}</td>
              <td class="whitespace-nowrap">¥{{ money(row.fullAttendanceBonus) }}</td>
              <td class="whitespace-nowrap">¥{{ money(row.overtimePay) }}</td>
              <td class="whitespace-nowrap text-amber-700">¥{{ money(row.extraBonus) }}</td>
              <td class="whitespace-nowrap">¥{{ money(row.leaveDeduction) }}</td>
              <td class="whitespace-nowrap">¥{{ money(row.attendanceDeduction) }}</td>
              <td class="whitespace-nowrap text-red-700">¥{{ money(row.extraDeduction) }}</td>
              <td class="whitespace-nowrap text-orange-700">¥{{ money(row.taxDeduction) }}</td>
              <td class="whitespace-nowrap text-blue-700">¥{{ money(row.insuranceDeduction) }}</td>
              <td class="text-success whitespace-nowrap">+¥{{ money(row.grossTotal) }}</td>
              <td class="text-danger whitespace-nowrap">-¥{{ money(row.deductTotal) }}</td>
              <td class="font-bold text-primary whitespace-nowrap">¥{{ money(row.netPay) }}</td>
              <td class="whitespace-nowrap">
                <span class="px-2 py-0.5 rounded-md text-xs"
                  :class="{
                    'bg-amber-100 text-amber-700 font-semibold': row.status === 'GENERATED',
                    'bg-teal-100 text-teal-700 font-semibold': row.status === 'APPROVED',
                    'bg-emerald-100 text-emerald-700 font-semibold': row.status === 'PAID'
                  }">
                  {{ statusLabel(row.status) }}
                </span>
              </td>
              <td style="text-align: center !important; width: 140px;">
                <div class="flex items-center justify-center gap-1 flex-nowrap">
                  <button v-if="canEdit() && row.status === 'GENERATED'" @click="handleApprove(row)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-primary/20 text-primary transition-colors" title="审核">
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
            <tr v-if="displaySalaries.length === 0">
              <td colspan="18" class="text-center py-12 text-text-muted">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="!showSelectedOnly" class="flex justify-center gap-2 mt-5">
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
              <option v-for="e in employees" :key="e.empId" :value="e.empId">{{ e.empName }}</option>
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

    <GlassModal title="历史工资记录" :visible="showHistoryModal" @close="showHistoryModal = false" width="900px">
      <div class="overflow-x-auto">
        <table class="glass-table text-sm">
          <thead>
            <tr>
              <th>工资周期</th>
              <th>基本工资</th>
              <th>应发总额</th>
              <th>扣除总额</th>
              <th>实发工资</th>
              <th>状态</th>
              <th>生成时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="historyLoading">
              <td colspan="7" class="text-center py-8 text-text-muted">加载中...</td>
            </tr>
            <tr v-else-if="historyRows.length === 0">
              <td colspan="7" class="text-center py-8 text-text-muted">暂无历史工资记录</td>
            </tr>
            <tr v-for="h in historyRows" :key="h.salaryId">
              <td>{{ h.payPeriod }}</td>
              <td>¥{{ money(h.baseSnap) }}</td>
              <td class="text-success">+¥{{ money(h.grossTotal) }}</td>
              <td class="text-danger">-¥{{ money(h.deductTotal) }}</td>
              <td class="font-bold text-primary">¥{{ money(h.netPay) }}</td>
              <td>{{ statusLabel(h.status) }}</td>
              <td>{{ h.generatedAt ? new Date(h.generatedAt).toLocaleString('zh-CN') : '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <template #footer>
        <button @click="showHistoryModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">关闭</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>
