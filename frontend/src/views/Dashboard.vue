<template>
  <div class="dashboard-container">
    <!-- Welcome Section -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1>Welcome Back, {{ userStore.userInfo?.realName }} 👋</h1>
        <p>这里是您的游戏卡密管理中心，今日数据一目了然。</p>
      </div>
      <div class="welcome-img">
        <!-- Abstract shape or illustration could go here -->
      </div>
    </div>

    <!-- Statistics Cards -->
    <el-row :gutter="24" class="stat-row">
      <el-col :xs="24" :sm="12" :md="4" :lg="4" :xl="4" v-for="(item, index) in statItems" :key="index">
        <div class="stat-card" :class="item.class">
          <div class="stat-icon">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">
              <span class="number">{{ formatNumber(item.value) }}</span>
            </div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
          <div class="card-bg-shape"></div>
        </div>
      </el-col>
    </el-row>

    <!-- Recent Activity / Chart Placeholder -->
     <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📊 系统状态监控</span>
            </div>
          </template>
          <div class="monitor-content">
             <div class="monitor-item">
                <span class="label">系统运行正常</span>
                <el-progress :percentage="100" status="success" :stroke-width="15" striped striped-flow />
             </div>
             <div class="monitor-item" style="margin-top: 20px">
                <span class="label">库存健康度</span>
                <el-progress :percentage="calculateHealth()" :color="customColors" :stroke-width="15" />
             </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { getStatistics } from '@/api'
import { User, Tickets, CircleCheck, SoldOut, Delete } from '@element-plus/icons-vue'

const userStore = useUserStore()
const stats = ref({
  totalCards: 0,
  usedCards: 0,
  unusedCards: 0,
  recycledCards: 0,
  totalUsers: 0
})

const statItems = computed(() => [
  { label: '用户总数', value: stats.value.totalUsers, icon: 'User', class: 'card-purple' },
  { label: '卡密库存', value: stats.value.totalCards, icon: 'Tickets', class: 'card-blue' },
  { label: '待售卡密', value: stats.value.unusedCards, icon: 'CircleCheck', class: 'card-green' },
  { label: '已核销', value: stats.value.usedCards, icon: 'SoldOut', class: 'card-orange' },
  { label: '已回收', value: stats.value.recycledCards, icon: 'Delete', class: 'card-red' },
])

const loadStats = async () => {
  try {
    const res = await getStatistics()
    stats.value = res
  } catch (error) {
    console.error(error)
  }
}

const formatNumber = (num) => {
  return num ? num.toLocaleString() : '0'
}

const calculateHealth = () => {
    if (stats.value.totalCards === 0) return 0;
    return Math.round((stats.value.unusedCards / stats.value.totalCards) * 100) || 0;
}

const customColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 80 },
]

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 30px 40px;
  color: white;
  margin-bottom: 30px;
  box-shadow: 0 10px 20px rgba(118, 75, 162, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-content h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
}

.welcome-content p {
  margin: 10px 0 0;
  opacity: 0.9;
  font-size: 16px;
}

.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  height: 140px;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  color: white;
}

.stat-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 24px;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.stat-info {
  z-index: 1;
}

.stat-value .number {
  font-size: 32px;
  font-weight: 800;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
  font-weight: 500;
}

.card-bg-shape {
  position: absolute;
  right: -20px;
  bottom: -20px;
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 100%);
  border-radius: 50%;
  pointer-events: none;
}

/* Gradients */
.card-purple {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  box-shadow: 0 8px 16px rgba(161, 140, 209, 0.3);
}

.card-blue {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 8px 16px rgba(79, 172, 254, 0.3);
}

.card-green {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  box-shadow: 0 8px 16px rgba(67, 233, 123, 0.3);
}

.card-orange {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  box-shadow: 0 8px 16px rgba(250, 112, 154, 0.3);
}

.card-red {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%);
  box-shadow: 0 8px 16px rgba(255, 154, 158, 0.3);
}

.chart-card {
  border-radius: 16px;
  border: none;
}

.monitor-item .label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #606266;
}
</style>
