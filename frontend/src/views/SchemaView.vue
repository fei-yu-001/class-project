<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import { getSchema } from '@/api/schema'
import {
  Database, Table2, Eye, Key, Link, ArrowRightLeft,
  ChevronDown, ChevronRight, Shield, Hash
} from 'lucide-vue-next'

const schema = ref<any>(null)
const loading = ref(true)
const activeTab = ref<'tables' | 'views' | 'indexes'>('tables')
const expandedTables = ref<Set<string>>(new Set())

const toggleTable = (code: string) => {
  if (expandedTables.value.has(code)) {
    expandedTables.value.delete(code)
  } else {
    expandedTables.value.add(code)
  }
}

const isExpanded = (code: string) => expandedTables.value.has(code)

const colorMap: Record<string, string> = {
  department: '#4ECDC4',
  position: '#FFD93D',
  employee: '#FF6B6B',
  users: '#45B7D1',
  payment_method: '#96CEB4',
  project: '#FFEAA7',
  project_member: '#845EC2',
  performance: '#FF9671',
  attendance: '#00C9A7',
  leave_req: '#4B4453',
  overtime: '#C34A36',
  pos_change: '#B0A8B9',
  salary_record: '#008E9B',
  bonus: '#D5CABD',
  deduction: '#C34A36'
}

const cascadeDesc: Record<string, string> = {
  'ON DELETE CASCADE': '级联删除',
  'ON DELETE SET NULL': '设为空',
  'ON DELETE RESTRICT': '禁止删除',
  'ON DELETE SET NULL / ON UPDATE CASCADE': '删除设为空 / 更新级联',
  'ON DELETE RESTRICT / ON UPDATE CASCADE': '禁止删除 / 更新级联'
}

const tabs: { key: 'tables' | 'views' | 'indexes'; label: string; icon: any }[] = [
  { key: 'tables', label: '数据表 (14)', icon: Table2 },
  { key: 'views', label: '视图 (5)', icon: Eye },
  { key: 'indexes', label: '索引 (14+)', icon: Hash },
]

onMounted(async () => {
  try {
    const res: any = await getSchema()
    schema.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

const getColor = (code: string) => {
  const key = Object.keys(colorMap).find(k => code.startsWith(k)) || ''
  return colorMap[key] || '#888'
}
</script>

<template>
  <AdminLayout>
    <div class="glass rounded-2xl p-6 mb-6">
      <div class="flex items-center gap-3 mb-2">
        <div class="w-10 h-10 rounded-xl flex items-center justify-center" style="background: rgba(78,205,196,0.15);">
          <Database class="w-5 h-5" style="color: #4ECDC4;" />
        </div>
        <div>
          <h2 class="text-xl font-bold font-serif text-gray-800">数据库 Schema</h2>
          <p class="text-sm text-text-muted">基于 data.sql 设计，共 14 张表、5 个视图、14+ 个索引</p>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex gap-2 mb-6">
      <button
        v-for="tab in tabs" :key="tab.key"
        @click="activeTab = tab.key"
        class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition-all"
        :class="activeTab === tab.key ? 'glass text-primary' : 'glass-btn-secondary'"
      >
        <component :is="tab.icon" class="w-4 h-4" />
        {{ tab.label }}
      </button>
    </div>

    <!-- Tables Tab -->
    <div v-if="activeTab === 'tables'" class="space-y-4">
      <div
        v-for="table in schema?.tables" :key="table.code"
        class="glass rounded-2xl overflow-hidden transition-all"
      >
        <div
          class="flex items-center gap-3 p-4 cursor-pointer hover:bg-white/10 transition-colors"
          @click="toggleTable(table.code)"
        >
          <div class="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
               :style="{ background: getColor(table.code) + '20' }">
            <Table2 class="w-4 h-4" :style="{ color: getColor(table.code) }" />
          </div>
          <div class="flex-1 min-w-0">
            <span class="font-semibold text-gray-800 text-sm">{{ table.code }}</span>
            <span class="text-text-muted text-xs ml-2">{{ table.name }}</span>
          </div>
          <div class="flex items-center gap-2 text-xs text-text-muted">
            <Key class="w-3 h-3" />
            <span>{{ table.primaryKey }}</span>
          </div>
          <ChevronDown v-if="!isExpanded(table.code)" class="w-4 h-4 text-text-muted flex-shrink-0" />
          <ChevronRight v-else class="w-4 h-4 text-text-muted flex-shrink-0" />
        </div>

        <div v-if="isExpanded(table.code)" class="px-4 pb-4 border-t border-gray-200/30 pt-4 space-y-3">
          <!-- Columns -->
          <div>
            <h4 class="text-xs font-semibold text-text-muted uppercase mb-2">字段列表</h4>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="col in table.columns" :key="col"
                class="px-3 py-1.5 rounded-lg text-xs font-medium"
                style="background: rgba(0,0,0,0.04); color: #555;"
              >{{ col }}</span>
            </div>
          </div>

          <!-- Foreign Keys -->
          <div v-if="table.foreignKeys?.length">
            <h4 class="text-xs font-semibold text-text-muted uppercase mb-2 flex items-center gap-1">
              <Link class="w-3 h-3" /> 外键约束
            </h4>
            <div class="space-y-1.5">
              <div
                v-for="fk in table.foreignKeys" :key="fk"
                class="flex items-center gap-2 text-xs px-3 py-2 rounded-lg"
                style="background: rgba(78,205,196,0.06); color: #4ECDC4;"
              >
                <ArrowRightLeft class="w-3 h-3 flex-shrink-0" />
                {{ fk }}
              </div>
            </div>
          </div>

          <!-- Cascade -->
          <div v-if="table.cascade">
            <h4 class="text-xs font-semibold text-text-muted uppercase mb-2 flex items-center gap-1">
              <Shield class="w-3 h-3" /> 级联规则
            </h4>
            <span class="text-xs px-3 py-1.5 rounded-lg inline-block"
                  style="background: rgba(255,107,107,0.06); color: #FF6B6B;">
              {{ cascadeDesc[table.cascade] || table.cascade }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Views Tab -->
    <div v-if="activeTab === 'views'" class="space-y-4">
      <div
        v-for="v in schema?.views" :key="v.code"
        class="glass rounded-2xl p-5"
      >
        <div class="flex items-center gap-3 mb-3">
          <div class="w-8 h-8 rounded-lg flex items-center justify-center" style="background: rgba(255,217,61,0.15);">
            <Eye class="w-4 h-4" style="color: #C49420;" />
          </div>
          <div>
            <span class="font-semibold text-gray-800 text-sm font-mono">{{ v.code }}</span>
            <span class="text-text-muted text-xs ml-2">{{ v.name }}</span>
          </div>
        </div>
        <div class="text-sm text-gray-600 mb-2">{{ v.description }}</div>
        <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs"
             style="background: rgba(78,205,196,0.06); color: #4ECDC4;">
          <Link class="w-3 h-3" />
          {{ v.joins }}
        </div>
      </div>
    </div>

    <!-- Indexes Tab -->
    <div v-if="activeTab === 'indexes'">
      <div class="glass rounded-2xl overflow-hidden">
        <div class="grid grid-cols-4 gap-0 text-xs font-semibold text-text-muted uppercase bg-white/5 px-5 py-3 border-b border-gray-200/20">
          <span>索引名</span>
          <span>所属表</span>
          <span>字段</span>
          <span>用途</span>
        </div>
        <div
          v-for="idx in schema?.indexes" :key="idx.name"
          class="grid grid-cols-4 gap-0 px-5 py-3 text-sm border-b border-gray-200/10 last:border-0 hover:bg-white/5 transition-colors"
        >
          <span class="text-primary font-mono text-xs">{{ idx.name }}</span>
          <span class="text-gray-600">{{ idx.table }}</span>
          <span class="text-gray-600 font-mono text-xs">{{ idx.column }}</span>
          <span class="text-text-muted text-xs">{{ idx.purpose }}</span>
        </div>
      </div>
    </div>

    <!-- ER Summary -->
    <div class="glass rounded-2xl p-6 mt-6">
      <h3 class="text-lg font-semibold font-serif mb-4 text-gray-700 flex items-center gap-2">
        <Link class="w-5 h-5" style="color: #4ECDC4;" /> ER 关系概览
      </h3>
      <div class="space-y-3 text-sm text-gray-600">
        <div class="flex items-center gap-2 p-3 rounded-xl" style="background: rgba(78,205,196,0.05);">
          <span style="color: #4ECDC4; font-weight: 600;">核心链:</span>
          department → position → employee → salary_record ⇄ bonus / deduction
        </div>
        <div class="flex items-center gap-2 p-3 rounded-xl" style="background: rgba(255,107,107,0.05);">
          <span style="color: #FF6B6B; font-weight: 600;">M:N:</span>
          employee ↔ project_member ↔ project（含角色和贡献系数）
        </div>
        <div class="flex items-center gap-2 p-3 rounded-xl" style="background: rgba(69,183,209,0.05);">
          <span style="color: #45B7D1; font-weight: 600;">1:1:</span>
          employee ↔ payment_method（每个员工唯一支付方式）
        </div>
        <div class="flex items-center gap-2 p-3 rounded-xl" style="background: rgba(150,206,180,0.05);">
          <span style="color: #96CEB4; font-weight: 600;">业务流:</span>
          performance / attendance / leave_req / overtime / pos_change → employee
        </div>
        <div class="flex items-center gap-2 p-3 rounded-xl" style="background: rgba(132,94,194,0.05);">
          <span style="color: #845EC2; font-weight: 600;">自引用:</span>
          department.parent_dept_code → department（树形组织架构）
        </div>
      </div>
    </div>

  </AdminLayout>
</template>