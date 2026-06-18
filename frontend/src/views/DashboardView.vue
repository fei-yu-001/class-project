<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import AdminLayout from '@/components/AdminLayout.vue'
import StatCard from '@/components/StatCard.vue'
import { getStats } from '@/api/dashboard'
import {
  Users, UserCheck, Building2, Briefcase, CreditCard, Wallet
} from 'lucide-vue-next'
import * as echarts from 'echarts'

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

const fetchStats = async () => {
  try {
    const res: any = await getStats()
    stats.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
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

onMounted(async () => {
  await fetchStats()
  setTimeout(initCharts, 100)
  window.addEventListener('resize', () => {
    pieChart?.resize()
    lineChart?.resize()
    gradeChart?.resize()
  })
})

onUnmounted(() => {
  pieChart?.dispose()
  lineChart?.dispose()
  gradeChart?.dispose()
})
</script>

<template>
  <AdminLayout>

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
            <p class="text-xl font-bold font-serif text-gray-800">¥{{ (stats.totalPayment || 0).toLocaleString ? Number(stats.totalPayment || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00' }}</p>
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

  </AdminLayout>
</template>