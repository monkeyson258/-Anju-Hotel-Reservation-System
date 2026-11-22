<template>
  <div class="intelligence-page" v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="hover" class="rate-card">
          <div class="rate-circle">
            <el-progress
              type="circle"
              :percentage="Math.min(avgRate, 100)"
              :width="160"
              :color="rateColor"
              :format="() => avgRate + '%'"
            ></el-progress>
          </div>
          <div class="rate-label">近7天平均入住率</div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover" class="warning-card">
          <div slot="header"><i class="el-icon-warning-outline"></i> 智能预警</div>
          <div v-for="(w, i) in warnings" :key="i" class="warning-item">
            <el-alert
              :title="w.title"
              :type="w.level === 'danger' ? 'error' : w.level"
              :description="w.message"
              show-icon
              :closable="false"
              class="mb-10"
            ></el-alert>
            <div class="suggestion" v-if="w.suggestion">
              <i class="el-icon-magic-stick"></i> {{ w.suggestion }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt-16">
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header"><i class="el-icon-data-line"></i> 每日入住率走势</div>
          <div ref="rateChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <div slot="header"><i class="el-icon-s-data"></i> 房型销量分析（近30天）</div>
          <div ref="salesChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="mt-16">
      <div slot="header"><i class="el-icon-s-opportunity"></i> 智能运营建议</div>
      <el-timeline>
        <el-timeline-item
          v-for="(s, i) in suggestions"
          :key="i"
          :type="suggestionType(s.type)"
          :icon="suggestionIcon(s.type)"
        >
          <h4>{{ s.title }}</h4>
          <p>{{ s.message }}</p>
          <p v-if="s.suggestion" class="suggestion-text">{{ s.suggestion }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!suggestions.length" description="暂无建议"></el-empty>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getIntelligenceAPI } from '@/api/stats'

export default {
  name: 'Intelligence',
  data() {
    return {
      loading: false,
      avgRate: 0,
      warnings: [],
      suggestions: [],
      dailyRates: [],
      categorySales: [],
      charts: []
    }
  },
  computed: {
    rateColor() {
      if (this.avgRate < 30) return '#f56c6c'
      if (this.avgRate > 80) return '#e6a23c'
      return '#67c23a'
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
        const { data } = await getIntelligenceAPI()
        this.avgRate = data.avgOccupancyRate || 0
        this.warnings = data.warnings || []
        this.suggestions = data.categorySuggestions || []
        this.dailyRates = data.dailyRates || []
        this.categorySales = data.categorySales || []
        this.$nextTick(() => {
          this.renderRateChart()
          this.renderSalesChart()
        })
      } finally {
        this.loading = false
      }
    },
    suggestionType(type) {
      return { hot: 'success', cold: 'warning', info: 'primary' }[type] || 'primary'
    },
    suggestionIcon(type) {
      return { hot: 'el-icon-top', cold: 'el-icon-bottom', info: 'el-icon-info' }[type] || 'el-icon-info'
    },
    renderRateChart() {
      const chart = echarts.init(this.$refs.rateChart)
      this.charts.push(chart)
      chart.setOption({
        tooltip: { trigger: 'axis', formatter: '{b}<br/>入住率: {c}%' },
        grid: { left: 50, right: 20, top: 20, bottom: 30 },
        xAxis: {
          type: 'category',
          data: this.dailyRates.map(d => d.date.substring(5)),
          axisLabel: { color: '#666' }
        },
        yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%', color: '#666' } },
        series: [{
          type: 'bar', data: this.dailyRates.map(d => d.occupancyRate),
          itemStyle: {
            color: params => {
              const v = params.value
              if (v < 30) return '#f56c6c'
              if (v > 80) return '#e6a23c'
              return '#409eff'
            },
            borderRadius: [4, 4, 0, 0]
          }
        }]
      })
    },
    renderSalesChart() {
      const chart = echarts.init(this.$refs.salesChart)
      this.charts.push(chart)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 50, right: 20, top: 20, bottom: 60 },
        xAxis: {
          type: 'category',
          data: this.categorySales.map(c => c.categoryName),
          axisLabel: { rotate: 30, color: '#666' }
        },
        yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#666' } },
        series: [{
          name: '订单量', type: 'bar', barWidth: '35%',
          data: this.categorySales.map(c => c.orderCount),
          itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] }
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
.intelligence-page { padding: 0; }

.rate-card {
  text-align: center;
  .rate-circle { padding: 20px 0 10px; }
  .rate-label { font-size: 15px; color: #606266; font-weight: 600; padding-bottom: 10px; }
}

.warning-item { margin-bottom: 12px; }

.suggestion {
  padding: 8px 12px;
  background: #f0f9eb;
  border-radius: 4px;
  color: #67c23a;
  font-size: 13px;
  margin-bottom: 12px;
  i { margin-right: 4px; }
}

.chart-box { height: 300px; }

.mt-16 { margin-top: 16px; }
.mb-10 { margin-bottom: 10px; }

.suggestion-text { color: #909399; font-size: 13px; }
</style>
