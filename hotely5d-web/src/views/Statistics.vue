<template>
  <div class="statistics-page" v-loading="loading">
    <div class="stat-cards">
      <div class="stat-card" v-for="card in statCards" :key="card.label">
        <div class="card-icon" :style="{ background: card.color }">
          <i :class="card.icon"></i>
        </div>
        <div class="card-info">
          <div class="card-value">{{ card.value }}</div>
          <div class="card-label">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="chart-header"><i class="el-icon-data-line"></i> 近七天入住趋势</div>
          <div ref="trendChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="chart-header"><i class="el-icon-pie-chart"></i> 各房型入住占比</div>
          <div ref="categoryChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="chart-header"><i class="el-icon-money"></i> 近六月营收趋势</div>
          <div ref="revenueChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getOverviewAPI, getCheckInTrendAPI, getCategoryOccupancyAPI, getRevenueTrendAPI } from '@/api/stats'

export default {
  name: 'Statistics',
  data() {
    return {
      loading: false,
      overview: {},
      charts: []
    }
  },
  computed: {
    statCards() {
      const o = this.overview
      return [
        { label: '总房间数', value: o.totalRooms || 0, icon: 'el-icon-office-building', color: '#c9a962' },
        { label: '今日入住率', value: (o.todayOccupancyRate || 0) + '%', icon: 'el-icon-data-analysis', color: '#67c23a' },
        { label: '可用房间', value: o.availableRooms || 0, icon: 'el-icon-key', color: '#e6a23c' },
        { label: '本月营收', value: '¥' + (o.monthRevenue || 0), icon: 'el-icon-coin', color: '#f56c6c' },
        { label: '注册会员', value: o.totalMembers || 0, icon: 'el-icon-user', color: '#909399' },
        { label: '待处理预约', value: o.pendingAppointments || 0, icon: 'el-icon-bell', color: '#b88230' }
      ]
    }
  },
  mounted() {
    this.fetchData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    this.charts.forEach(c => c.dispose())
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const [overview, trend, category, revenue] = await Promise.all([
          getOverviewAPI(), getCheckInTrendAPI(7),
          getCategoryOccupancyAPI(), getRevenueTrendAPI(6)
        ])
        this.overview = overview.data
        this.$nextTick(() => {
          this.renderTrendChart(trend.data)
          this.renderCategoryChart(category.data)
          this.renderRevenueChart(revenue.data)
        })
      } finally {
        this.loading = false
      }
    },
    initChart(ref) {
      const chart = echarts.init(this.$refs[ref])
      this.charts.push(chart)
      return chart
    },
    renderTrendChart(data) {
      const chart = this.initChart('trendChart')
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 50, right: 20, top: 30, bottom: 30 },
        xAxis: { type: 'category', data: data.dates, axisLabel: { color: '#666' } },
        yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#666' } },
        series: [{
          name: '入住数', type: 'line', smooth: true, data: data.counts,
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(201,169,98,0.3)' }, { offset: 1, color: 'rgba(201,169,98,0.02)' }
          ])},
          lineStyle: { color: '#c9a962', width: 3 },
          itemStyle: { color: '#c9a962' }
        }]
      })
    },
    renderCategoryChart(data) {
      const chart = this.initChart('categoryChart')
      const pieData = (data.categoryNames || []).map((name, i) => ({
        name,
        value: (data.occupied || [])[i] || 0
      }))
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c}间 ({d}%)' },
        legend: { bottom: 0, type: 'scroll' },
        color: ['#c9a962', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#a88b4a'],
        series: [{
          type: 'pie', radius: ['40%', '65%'], center: ['50%', '45%'],
          data: pieData,
          label: { formatter: '{b}\n{d}%' },
          emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' } }
        }]
      })
    },
    renderRevenueChart(data) {
      const chart = this.initChart('revenueChart')
      chart.setOption({
        tooltip: { trigger: 'axis', formatter: '{b}<br/>营收: ¥{c}' },
        grid: { left: 60, right: 20, top: 30, bottom: 30 },
        xAxis: { type: 'category', data: data.months, axisLabel: { color: '#666' } },
        yAxis: { type: 'value', axisLabel: { color: '#666', formatter: '¥{value}' } },
        series: [{
          name: '营收', type: 'bar', barWidth: '40%',
          data: (data.revenues || []).map(v => Number(v)),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#67c23a' }, { offset: 1, color: '#95d475' }
            ]),
            borderRadius: [6, 6, 0, 0]
          }
        }]
      })
    },
    handleResize() {
      this.charts.forEach(c => c.resize())
    }
  }
}
</script>

<style lang="less" scoped>
.statistics-page { padding: 0; }

.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s;

  &:hover { transform: translateY(-2px); }

  .card-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 22px;
  }

  .card-value { font-size: 24px; font-weight: 700; color: #303133; }
  .card-label { font-size: 13px; color: #909399; margin-top: 4px; }
}

.chart-row { margin-bottom: 16px; }

.chart-card {
  border-radius: 8px;
  .chart-header { font-weight: 600; i { margin-right: 6px; color: #c9a962; } }
}

.chart-box { height: 320px; }
</style>
