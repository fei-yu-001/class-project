<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import GlassModal from '@/components/GlassModal.vue'
import { listPaymentMethods, createPaymentMethod, updatePaymentMethod, deletePaymentMethod } from '@/api/payment'
import { searchEmployees } from '@/api/employee'
import { usePermission } from '@/composables/usePermission'
import { Plus, Pencil, Trash2, CreditCard, Wallet, Search } from 'lucide-vue-next'

const { canCreate, canEdit, canDelete } = usePermission()

const payments = ref<any[]>([])
const employees = ref<any[]>([])
const searchKeyword = ref('')
const showModal = ref(false)
const isEdit = ref(false)
const editEmpId = ref<number | null>(null)
const form = ref({
  empId: '',
  payType: 'BANK_CARD',
  accountNo: '',
  cardType: ''
})

const PAY_TYPE_MAP: Record<string, string> = {
  BANK_CARD: '银行卡',
  ALIPAY: '支付宝',
  WECHAT: '微信',
  银行卡: '银行卡',
  支付宝: '支付宝',
  微信: '微信'
}

const fetchData = async () => {
  try {
    const [pRes, eRes] = await Promise.all([
      listPaymentMethods(),
      searchEmployees({ page: 0, size: 1000 })
    ])
    payments.value = (pRes as any).data
    employees.value = (eRes as any).data.content
  } catch (e) {
    console.error(e)
  }
}

const getEmployeeName = (empId: number) => {
  return employees.value.find(e => e.empId === empId)?.empName || '-'
}

const getPayTypeLabel = (payType: string) => {
  return PAY_TYPE_MAP[payType] || payType
}

const isBankCard = (payType: string) => {
  return payType === 'BANK_CARD' || payType === '银行卡'
}

const filteredPayments = computed(() => {
  const kw = searchKeyword.value.toLowerCase().trim()
  if (!kw) return payments.value
  return payments.value.filter(row => {
    const name = (getEmployeeName(row.empId) || '').toLowerCase()
    const payType = (getPayTypeLabel(row.payType) || '').toLowerCase()
    const accountNo = (row.accountNo || '').toLowerCase()
    const cardType = (row.cardType || '').toLowerCase()
    return name.includes(kw) || payType.includes(kw) || accountNo.includes(kw) || cardType.includes(kw)
  })
})

const openAdd = () => {
  isEdit.value = false
  editEmpId.value = null
  form.value = { empId: '', payType: 'BANK_CARD', accountNo: '', cardType: '' }
  showModal.value = true
}

const openEdit = (row: any) => {
  isEdit.value = true
  editEmpId.value = row.empId
  form.value = {
    empId: row.empId?.toString() || '',
    payType: row.payType || 'BANK_CARD',
    accountNo: row.accountNo || '',
    cardType: row.cardType || ''
  }
  showModal.value = true
}

const handleSave = async () => {
  const data = {
    empId: Number(form.value.empId),
    payType: form.value.payType,
    accountNo: form.value.accountNo,
    cardType: form.value.cardType
  }
  try {
    if (isEdit.value && editEmpId.value) {
      await updatePaymentMethod(editEmpId.value, data)
    } else {
      await createPaymentMethod(data)
    }
    showModal.value = false
    fetchData()
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}

const handleDelete = async (empId: number) => {
  if (!confirm('确定删除？')) return
  try {
    await deletePaymentMethod(empId)
    fetchData()
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}

onMounted(fetchData)
</script>

<template>
  <AdminLayout>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-bold text-text-primary">支付方式</h1>
    </div>

    <div class="glass rounded-2xl p-5 mb-6">
      <div class="flex items-center gap-3">
        <div class="relative flex-1 max-w-md">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-muted" />
          <input v-model="searchKeyword" placeholder="搜索员工姓名、支付方式、卡类型、卡号..." class="w-full pl-9 pr-4 py-2.5 rounded-xl text-sm bg-white/80 border border-black/5 focus:outline-none focus:ring-2 focus:ring-primary/30 transition-all" />
        </div>
        <button @click="searchKeyword = ''" class="glass-btn px-5 py-2.5 rounded-xl text-sm whitespace-nowrap">搜索</button>
        <div class="flex-1"></div>
        <button v-if="canCreate()" @click="openAdd" class="glass-btn px-5 py-2.5 rounded-xl flex items-center gap-2 text-sm">
          <Plus class="w-4 h-4" /> 新增方式
        </button>
      </div>
    </div>

    <div class="glass rounded-2xl overflow-hidden">
      <table class="glass-table">
        <thead>
          <tr>
            <th>员工</th>
            <th>支付类型</th>
            <th>账号</th>
            <th>卡类型</th>
            <th style="text-align: center !important; width: 120px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in filteredPayments" :key="row.empId">
            <td class="font-medium">{{ getEmployeeName(row.empId) }}</td>
            <td>
              <span class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-md text-xs"
                :class="isBankCard(row.payType) ? 'bg-primary/20 text-primary' : 'bg-accent/20 text-accent'">
                <CreditCard v-if="isBankCard(row.payType)" class="w-3 h-3" />
                <Wallet v-else class="w-3 h-3" />
                {{ getPayTypeLabel(row.payType) }}
              </span>
            </td>
            <td class="font-mono text-sm">{{ row.accountNo || '-' }}</td>
            <td>{{ row.cardType || '-' }}</td>
            <td style="text-align: center !important; width: 120px;">
              <div class="flex items-center justify-center gap-1">
                <button v-if="canEdit()" @click="openEdit(row)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-primary/20 text-primary transition-colors">
                  <Pencil class="w-3.5 h-3.5" />
                </button>
                <button v-if="canDelete()" @click="handleDelete(row.empId)" class="flex items-center justify-center w-7 h-7 rounded-lg hover:bg-danger/20 text-danger transition-colors">
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="filteredPayments.length === 0">
            <td colspan="5" class="text-center py-12 text-text-muted">暂无支付方式数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <GlassModal :title="isEdit ? '编辑支付方式' : '新增支付方式'" :visible="showModal" @close="showModal = false">
      <div class="space-y-4">
        <div>
          <label class="text-sm text-text-muted mb-1 block">员工</label>
          <select v-model="form.empId" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" :disabled="isEdit">
            <option value="">请选择</option>
            <option v-for="e in employees" :key="e.empId" :value="e.empId">{{ e.empName }}</option>
          </select>
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">支付类型</label>
          <select v-model="form.payType" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm">
            <option value="BANK_CARD">银行卡</option>
            <option value="ALIPAY">支付宝</option>
          </select>
        </div>
        <div v-if="isBankCard(form.payType)">
          <label class="text-sm text-text-muted mb-1 block">卡类型</label>
          <input v-model="form.cardType" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" placeholder="如: 借记卡、信用卡" />
        </div>
        <div>
          <label class="text-sm text-text-muted mb-1 block">{{ isBankCard(form.payType) ? '银行卡号' : '支付宝账号' }}</label>
          <input v-model="form.accountNo" class="glass-input w-full px-4 py-2.5 rounded-xl text-sm" :placeholder="isBankCard(form.payType) ? '请输入银行卡号' : '请输入支付宝账号'" />
        </div>
      </div>
      <template #footer>
        <button @click="showModal = false" class="glass-btn-secondary px-5 py-2 rounded-xl text-sm">取消</button>
        <button @click="handleSave" class="glass-btn px-5 py-2 rounded-xl text-sm">保存</button>
      </template>
    </GlassModal>
  </AdminLayout>
</template>
