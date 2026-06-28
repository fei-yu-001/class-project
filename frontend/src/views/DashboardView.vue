<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import StatCard from '@/components/StatCard.vue'
import { getStats } from '@/api/dashboard'
import { useAuthStore } from '@/stores/auth'
import {
  Users, UserCheck, Building2, Briefcase, CreditCard, Wallet,
  User, DollarSign, Award, TrendingDown
} from 'lucide-vue-next'
import * as echarts from 'echarts'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'ADMIN')

const stats = ref<any>({})
const loading = ref(true)

const pieChartRef = ref<HTMLDivElement>()
const lineChartRef = ref<HTMLDivElement>()
const gradeChartRef = ref<HTMLDivElement>()
let pieChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null
let gradeChart: echarts.ECharts | null = null

const chartColor = '#4ECDC4'
const chartColors = ['#4ECDC4', '#FFD93D', '#FF6B6B', '#45B7D1', '#96CEB4', '#FFEAA7']

const money = (value: any) => Number(value || 0).toLocaleString('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

const fetchStats = async () => {
  try {
    const res: any = await getStats()
    stats.value = res.data || {}
  } catch (e) {
    console.error(e)
    stats.value = {}
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  if (!isAdmin.value) return

  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    const payTypes = stats.value.payTypeChart || [
      { type: '银行卡', count: stats.value.bankCardCount || 0 },
      { type: '支付宝', count: stats.value.alipayCount || 0 }
    ]
    pieChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item', backgroundColor: 'rgba(255,255,255,0.9)', borderColor: '#ddd', textStyle: { color: '#333' } },
      legend: { bottom: '5%', textStyle: { color: '#555' } },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        itemStyle: { borderRadius: 8, borderColor: 'rgba(255,255,255,0.6)', borderWidth: 2 },
        label: { show: false },
        data: payTypes.map((p: any, i: number) => ({
          value: p.count || 0,
          name: p.type || p.payType || '未知',
          itemStyle: { color: chartColors[i % chartColors.length] }
        }))
      }]
    })
  }

  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
    const monthlyData = stats.value.monthlyNetPayChart || []
    const xData = monthlyData.map((m: any) => m.period || '')
    const yData = monthlyData.map((m: any) => m.netPay || 0)
    lineChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.9)', borderColor: '#ddd', textStyle: { color: '#333' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: xData.length > 0 ? xData : ['暂无数据'],
        axisLine: { lineStyle: { color: 'rgba(0,0,0,0.15)' } },
        axisLabel: { color: '#555' }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: 'rgba(0,0,0,0.06)' } },
        axisLabel: { color: '#555' }
      },
      series: [{
        data: yData.length > 0 ? yData : [0],
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: chartColor, width: 3 },
        itemStyle: { color: chartColor, borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new (echarts as any).graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(78, 205, 196, 0.25)' },
            { offset: 1, color: 'rgba(78, 205, 196, 0)' }
          ])
        }
      }]
    })
  }

  if (gradeChartRef.value) {
    gradeChart = echarts.init(gradeChartRef.value)
    const grades = stats.value.gradeChart || []
    gradeChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.9)', borderColor: '#ddd', textStyle: { color: '#333' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: grades.map((g: any) => g.grade || '未考核'),
        axisLine: { lineStyle: { color: 'rgba(0,0,0,0.15)' } },
        axisLabel: { color: '#555' }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLine: { show: false },
        splitLine: { lineStyle: { color: 'rgba(0,0,0,0.06)' } },
        axisLabel: { color: '#555' }
      },
      series: [{
        type: 'bar',
        data: grades.map((g: any, i: number) => ({
          value: g.count || 0,
          itemStyle: {
            color: chartColors[i % chartColors.length],
            borderRadius: [6, 6, 0, 0]
          }
        })),
        barWidth: '50%'
      }]
    })
  }
}

const gradeColor = (grade?: string) => {
  if (!grade) return '#999'
  const map: Record<string, string> = {
    'A+': '#10B981', 'A': '#10B981', 'A-': '#34D399',
    'B+': '#3B82F6', 'B': '#3B82F6', 'C': '#F59E0B', 'D': '#EF4444'
  }
  return map[grade.toUpperCase()] || '#999'
}

const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
  gradeChart?.resize()
}

onMounted(async () => {
  await fetchStats()
  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  lineChart?.dispose()
  gradeChart?.dispose()
})
</script>

<template>
  <AdminLayout>

    <template v-if="isAdmin">
      <!-- 管理员仪表盘：全局统计 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
        <StatCard title="员工总数" :value="stats.totalEmployees || 0" :icon="Users" color="#2D8B84" />
        <StatCard title="活跃员工" :value="stats.activeEmployees || 0" :icon="UserCheck" color="#4CAF50" />
        <StatCard title="部门数量" :value="stats.departmentCount || 0" :icon="Building2" color="#E8A838" />
        <StatCard title="职位数量" :value="stats.positionCount || 0" :icon="Briefcase" color="#2D8B84" />
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
        <div class="lg:col-span-2 glass rounded-2xl p-6">
          <h3 class="text-lg font-semibold font-serif mb-4 text-gray-700">月度工资趋势（实发）</h3>
          <div ref="lineChartRef" style="height: 300px" />
        </div>
        <div class="glass rounded-2xl p-6">
          <h3 class="text-lg font-semibold font-serif mb-4 text-gray-700">支付方式分布</h3>
          <div ref="pieChartRef" style="height: 300px" />
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-5 mt-5">
        <div class="lg:col-span-2 glass rounded-2xl p-6">
          <h3 class="text-lg font-semibold font-serif mb-4 text-gray-700">绩效等级分布</h3>
          <div ref="gradeChartRef" style="height: 280px" />
        </div>
        <div class="glass rounded-2xl p-6 flex flex-col gap-4">
          <h3 class="text-lg font-semibold font-serif text-gray-700">数据概览</h3>
          <div class="flex items-center gap-4 p-4 rounded-xl" style="background: rgba(45,139,132,0.08);">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: rgba(45,139,132,0.15);">
              <Building2 class="w-5 h-5" style="color: #2D8B84;" />
            </div>
            <div>
              <p class="text-gray-500 text-xs">部门数量</p>
              <p class="text-xl font-bold font-serif text-gray-800">{{ stats.departmentCount || 0 }}</p>
            </div>
          </div>
          <div class="flex items-center gap-4 p-4 rounded-xl" style="background: rgba(232,168,56,0.08);">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: rgba(232,168,56,0.15);">
              <Briefcase class="w-5 h-5" style="color: #C49420;" />
            </div>
            <div>
              <p class="text-gray-500 text-xs">职位数量</p>
              <p class="text-xl font-bold font-serif text-gray-800">{{ stats.positionCount || 0 }}</p>
            </div>
          </div>
          <div class="flex items-center gap-4 p-4 rounded-xl" style="background: rgba(76,175,80,0.08);">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: rgba(76,175,80,0.15);">
              <Wallet class="w-5 h-5" style="color: #3A9E4F;" />
            </div>
            <div>
              <p class="text-gray-500 text-xs">总发放金额</p>
              <p class="text-xl font-bold font-serif text-gray-800">¥{{ money(stats.totalPayment) }}</p>
            </div>
          </div>
          <div class="flex items-center gap-4 p-4 rounded-xl" style="background: rgba(78,205,196,0.08);">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: rgba(78,205,196,0.15);">
              <CreditCard class="w-5 h-5" style="color: #3BA99E;" />
            </div>
            <div>
              <p class="text-gray-500 text-xs">银行卡/支付宝</p>
              <p class="text-xl font-bold font-serif text-gray-800">{{ stats.bankCardCount || 0 }} / {{ stats.alipayCount || 0 }}</p>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <!-- 普通用户仪表盘：个人数据 -->
      <div class="mb-8">
        <h1 class="text-2xl font-semibold text-gray-800">我的工作台</h1>
        <p class="text-sm text-text-muted mt-1">欢迎回来，{{ stats.myName || auth.user?.nickname || auth.user?.username }}</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
        <div class="glass rounded-2xl p-5">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: rgba(45,139,132,0.15);">
              <User class="w-5 h-5" style="color: #2D8B84;" />
            </div>
            <span class="text-sm text-gray-500">姓名</span>
          </div>
          <p class="text-xl font-bold font-serif text-gray-800">{{ stats.myName || '-' }}</p>
        </div>
        <div class="glass rounded-2xl p-5">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: rgba(78,205,196,0.15);">
              <DollarSign class="w-5 h-5" style="color: #3BA99E;" />
            </div>
            <span class="text-sm text-gray-500">基本工资</span>
          </div>
          <p class="text-xl font-bold font-serif text-gray-800">¥{{ money(stats.myBaseSalary) }}</p>
        </div>
        <div class="glass rounded-2xl p-5">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: rgba(76,175,80,0.15);">
              <Wallet class="w-5 h-5" style="color: #3A9E4F;" />
            </div>
            <span class="text-sm text-gray-500">最近实发工资</span>
          </div>
          <p class="text-xl font-bold font-serif" style="color: #2D8B84;">¥{{ money(stats.myLatestNetPay) }}</p>
        </div>
        <div class="glass rounded-2xl p-5">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: rgba(232,168,56,0.15);">
              <Award class="w-5 h-5" style="color: #C49420;" />
            </div>
            <span class="text-sm text-gray-500">绩效等级</span>
          </div>
          <p class="text-xl font-bold font-serif" :style="{ color: gradeColor(stats.myPerformanceGrade) }">
            {{ stats.myPerformanceGrade || '暂无' }}
          </p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-5">
        <div class="glass rounded-2xl p-6">
          <h3 class="text-lg font-semibold font-serif mb-4 text-gray-700">最近工资明细</h3>
          <div v-if="stats.myLatestNetPay > 0" class="space-y-3">
            <div class="flex justify-between items-center p-3 rounded-xl" style="background: rgba(76,175,80,0.06);">
              <span class="text-sm text-gray-600">基本工资</span>
              <span class="font-semibold text-gray-800">¥{{ money(stats.myBaseSalary) }}</span>
            </div>
            <div class="flex justify-between items-center p-3 rounded-xl" style="background: rgba(76,175,80,0.06);">
              <span class="text-sm text-gray-600">奖金合计</span>
              <span class="font-semibold text-success">+¥{{ money(stats.myTotalBonus) }}</span>
            </div>
            <div class="flex justify-between items-center p-3 rounded-xl" style="background: rgba(239,68,68,0.06);">
              <span class="text-sm text-gray-600">扣除合计</span>
              <span class="font-semibold text-danger">-¥{{ money(stats.myTotalDeduction) }}</span>
            </div>
            <div class="border-t border-gray-200 pt-3 flex justify-between items-center">
              <span class="text-sm font-medium text-gray-700">实发工资</span>
              <span class="text-2xl font-bold font-serif" style="color: #2D8B84;">¥{{ money(stats.myLatestNetPay) }}</span>
            </div>
          </div>
          <div v-else class="text-center py-8 text-text-muted">
            <Wallet class="w-12 h-12 mx-auto mb-3 opacity-30" />
            <p>暂无工资记录</p>
          </div>
        </div>

        <div class="glass rounded-2xl p-6">
          <h3 class="text-lg font-semibold font-serif mb-4 text-gray-700">个人信息</h3>
          <div class="space-y-3">
            <div class="flex justify-between items-center p-3 rounded-xl" style="background: rgba(45,139,132,0.06);">
              <span class="text-sm text-gray-600">用户名</span>
              <span class="text-sm font-medium text-gray-800">{{ auth.user?.username }}</span>
            </div>
            <div class="flex justify-between items-center p-3 rounded-xl" style="background: rgba(45,139,132,0.06);">
              <span class="text-sm text-gray-600">昵称</span>
              <span class="text-sm font-medium text-gray-800">{{ auth.user?.nickname || '-' }}</span>
            </div>
            <div class="flex justify-between items-center p-3 rounded-xl" style="background: rgba(45,139,132,0.06);">
              <span class="text-sm text-gray-600">角色</span>
              <span class="px-2 py-0.5 rounded-md text-xs bg-teal-100 text-teal-700 font-semibold">
                {{ auth.roleLabel }}
              </span>
            </div>
            <div class="flex justify-between items-center p-3 rounded-xl" style="background: rgba(45,139,132,0.06);">
              <span class="text-sm text-gray-600">绩效等级</span>
              <span class="font-semibold" :style="{ color: gradeColor(stats.myPerformanceGrade) }">
                {{ stats.myPerformanceGrade || '暂无考核' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>

  </AdminLayout>
</template>
