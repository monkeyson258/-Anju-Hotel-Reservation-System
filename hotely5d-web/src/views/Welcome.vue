<template>
  <div class="welcome-page">
    <div class="welcome-banner">
      <div class="banner-text">
        <h1>欢迎回来，{{ user.name || '管理员' }}</h1>
        <p>{{ $sysTitle }} · 数据驾驶舱</p>
      </div>
      <div class="banner-stats" v-if="overview.totalRooms">
        <div class="mini-stat">
          <span class="val">{{ overview.todayOccupancyRate }}%</span>
          <span class="lbl">今日入住率</span>
        </div>
        <div class="mini-stat">
          <span class="val">{{ overview.availableRooms }}</span>
          <span class="lbl">可用房间</span>
        </div>
        <div class="mini-stat">
          <span class="val">{{ overview.pendingAppointments }}</span>
          <span class="lbl">待处理预约</span>
        </div>
      </div>
    </div>

    <el-row :gutter="16" class="quick-links">
      <el-col :span="8">
        <el-card shadow="hover" class="link-card" @click.native="$router.push('/admin/statistics')">
          <i class="el-icon-data-analysis link-icon" style="color:#c9a962"></i>
          <h3>数据统计</h3>
          <p>入住趋势、房型占比、营收分析</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="link-card" @click.native="$router.push('/admin/intelligence')">
          <i class="el-icon-cpu link-icon" style="color:#67c23a"></i>
          <h3>数智决策</h3>
          <p>入住率预警、房型销量智能建议</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="link-card" @click.native="$router.push('/admin/orders')">
          <i class="el-icon-s-order link-icon" style="color:#e6a23c"></i>
          <h3>入住管理</h3>
          <p>处理入住、退房等日常运营</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="hover">
          <div slot="header"><i class="el-icon-data-line"></i> 近七天入住趋势预览</div>
          <div ref="miniChart" class="mini-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="alert-card">
          <div slot="header"><i class="el-icon-warning-outline"></i> 智能预警</div>
          <div v-if="topWarning" class="alert-content">
            <el-tag :type="topWarning.level === 'danger' ? 'danger' : topWarning.level" size="small">
              {{ topWarning.title }}
            </el-tag>
            <p>{{ topWarning.message }}</p>
          </div>
          <el-empty v-else description="加载中..." :image-size="60"></el-empty>
          <el-button type="text" @click="$router.push('/admin/intelligence')">查看详细分析 →</el-button>
        </el-card>
      </el-col>
    </el-row>

    <AiChat greeting="您好！我是斌安居酒店管理助手，可以帮您分析运营数据、解答管理问题。" />
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { mapState } from 'vuex'
import { getOverviewAPI, getCheckInTrendAPI, getIntelligenceAPI } from '@/api/stats'
import AiChat from '@/components/AiChat.vue'

export default {
  name: 'Welcome',
  components: { AiChat },
  data() {
    return {
      overview: {},
      topWarning: null,
      chart: null
    }
  },
  computed: {
    ...mapState(['user'])
  },
  mounted() {
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chart) this.chart.dispose()
  },
  methods: {
    async loadData() {
      try {
        const [overview, trend, intel] = await Promise.all([
          getOverviewAPI(), getCheckInTrendAPI(7), getIntelligenceAPI()
        ])
        this.overview = overview.data
        if (intel.data.warnings && intel.data.warnings.length) {
          this.topWarning = intel.data.warnings[0]
        }
        this.$nextTick(() => this.renderChart(trend.data))
      } catch (e) { /* handled by interceptor */ }
    },
    renderChart(data) {
      if (!this.$refs.miniChart) return
      this.chart = echarts.init(this.$refs.miniChart)
      this.chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 16, top: 16, bottom: 24 },
        xAxis: { type: 'category', data: data.dates, axisLabel: { fontSize: 11 } },
        yAxis: { type: 'value', minInterval: 1 },
        series: [{
          type: 'line', smooth: true, data: data.counts,
          areaStyle: { color: 'rgba(201,169,98,0.18)' },
          lineStyle: { color: '#c9a962', width: 2 },
          itemStyle: { color: '#c9a962' }
        }]
      })
    },
    handleResize() {
      if (this.chart) this.chart.resize()
    }
  }
}
</script>

<style lang="less" scoped>
.welcome-page { padding: 0; }

.welcome-banner {
  background: linear-gradient(135deg, #141820 0%, #1c2333 60%, #2a3142 100%);
  border-radius: 14px;
  padding: 36px 40px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border: 1px solid rgba(201, 169, 98, 0.15);
  box-shadow: 0 8px 32px rgba(20, 24, 32, 0.12);

  h1 { font-size: 26px; margin: 0 0 8px; font-weight: 600; }
  p { margin: 0; color: #c9a962; font-size: 14px; letter-spacing: 1px; }
}

.banner-stats {
  display: flex;
  gap: 32px;

  .mini-stat {
    text-align: center;
    .val { display: block; font-size: 28px; font-weight: 700; }
    .lbl { font-size: 12px; opacity: 0.8; }
  }
}

.quick-links { margin-bottom: 16px; }

.link-card {
  text-align: center;
  cursor: pointer;
  border-radius: 12px;
  border: none;
  transition: transform 0.25s, box-shadow 0.25s;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  }

  .link-icon { font-size: 36px; margin-bottom: 8px; }
  h3 { margin: 0 0 6px; font-size: 16px; color: #303133; }
  p { margin: 0; font-size: 13px; color: #909399; }
}

.mini-chart { height: 240px; }

.alert-card {
  height: 100%;
  .alert-content {
    p { font-size: 13px; color: #606266; line-height: 1.6; margin: 12px 0; }
  }
}
</style>
