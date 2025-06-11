<template>
  <div class="dashboard">
    <div class="dashboard-container">
      <!-- Welcome Section -->
      <div class="dashboard-welcome">
        <div class="dashboard-welcome-content">
          <div class="dashboard-welcome-text">
            <h1 class="dashboard-welcome-title">欢迎回来，管理员</h1>
            <p class="dashboard-welcome-subtitle">今天是 {{ currentDate }} | 系统运行正常</p>
          </div>
          <div class="dashboard-welcome-icon">
            <Home class="dashboard-welcome-icon-svg" />
          </div>
        </div>
      </div>

      <!-- Statistics Cards -->
      <div class="dashboard-stats">
        <StatCard
          title="总居民数"
          value="1,247"
          :icon="Users"
          :trend="{ value: 12, isPositive: true }"
          color="blue"
        />
        <StatCard
          title="平均诚信积分"
          value="82.5"
          :icon="Award"
          :trend="{ value: 5.2, isPositive: true }"
          color="green"
        />
        <StatCard
          title="学习完成率"
          value="78%"
          :icon="BookOpen"
          :trend="{ value: 8.1, isPositive: true }"
          color="purple"
        />
        <StatCard
          title="活跃用户"
          value="456"
          :icon="TrendingUp"
          :trend="{ value: 3.2, isPositive: false }"
          color="orange"
        />
      </div>

      <!-- Charts and Analytics -->
      <div class="dashboard-charts">
        <ProgressChart title="公约学习进度统计" :data="learningProgressData" />
        <ProgressChart title="居民信用分布" :data="creditDistributionData" />
      </div>

      <!-- Activity and Quick Actions -->
      <div class="dashboard-activity">
        <div class="dashboard-activity-feed">
          <ActivityFeed />
        </div>
        <div class="dashboard-quick-actions">
          <QuickActions />
        </div>
      </div>

      <!-- Summary Cards -->
      <div class="dashboard-summary">
        <div class="dashboard-summary-card">
          <div class="dashboard-summary-card-content">
            <div class="dashboard-summary-card-icon">
              <CheckCircle class="dashboard-summary-card-icon-svg" />
            </div>
            <div class="dashboard-summary-card-text">
              <h4 class="dashboard-summary-card-title">本月新增认证</h4>
              <p class="dashboard-summary-card-value">124</p>
              <p class="dashboard-summary-card-subtitle">比上月增长 18%</p>
            </div>
          </div>
        </div>
        <div class="dashboard-summary-card">
          <div class="dashboard-summary-card-content">
            <div class="dashboard-summary-card-icon">
              <BookOpen class="dashboard-summary-card-icon-svg" />
            </div>
            <div class="dashboard-summary-card-text">
              <h4 class="dashboard-summary-card-title">学习时长</h4>
              <p class="dashboard-summary-card-value">2,145h</p>
              <p class="dashboard-summary-card-subtitle">本月累计学习时长</p>
            </div>
          </div>
        </div>
        <div class="dashboard-summary-card">
          <div class="dashboard-summary-card-content">
            <div class="dashboard-summary-card-icon">
              <Award class="dashboard-summary-card-icon-svg" />
            </div>
            <div class="dashboard-summary-card-text">
              <h4 class="dashboard-summary-card-title">积分兑换</h4>
              <p class="dashboard-summary-card-value">3,267</p>
              <p class="dashboard-summary-card-subtitle">本月积分兑换次数</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { Users, Award, BookOpen, TrendingUp, Home, CheckCircle } from "lucide-vue-next";
import StatCard from "./StatCard.vue";
import ProgressChart from "./ProgressChart.vue";
import ActivityFeed from "./ActivityFeed.vue";
import QuickActions from "./QuickActions.vue";

const currentDate = computed(() => {
  return new Date().toLocaleDateString("zh-CN");
});

const learningProgressData = [
  { label: "居民公约", value: 85, color: "#3B82F6" },
  { label: "文明行为规范", value: 72, color: "#10B981" },
  { label: "环境保护条例", value: 68, color: "#8B5CF6" },
  { label: "安全管理制度", value: 91, color: "#F59E0B" },
];

const creditDistributionData = [
  { label: "优秀 (90-100分)", value: 25, color: "#10B981" },
  { label: "良好 (80-89分)", value: 45, color: "#3B82F6" },
  { label: "一般 (70-79分)", value: 20, color: "#F59E0B" },
  { label: "待改进 (<70分)", value: 10, color: "#EF4444" },
];
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(to bottom right, #f9fafb, #eff6ff);
}

.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
}

.dashboard-welcome {
  background: linear-gradient(to right, #3b82f6, #2563eb);
  border-radius: 20px;
  padding: 32px;
  color: white;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
  margin-bottom: 32px;
}

.dashboard-welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard-welcome-text {
  flex: 1;
}

.dashboard-welcome-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
}

.dashboard-welcome-subtitle {
  font-size: 20px;
  color: #bfdbfe;
}

.dashboard-welcome-icon {
  display: none;
}

@media (min-width: 768px) {
  .dashboard-welcome-icon {
    display: block;
  }
}

.dashboard-welcome-icon-svg {
  width: 64px;
  height: 64px;
  color: #bfdbfe;
}

.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

@media (min-width: 768px) {
  .dashboard-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .dashboard-stats {
    grid-template-columns: repeat(4, 1fr);
  }
}

.dashboard-charts {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 32px;
  margin-bottom: 32px;
}

@media (min-width: 1024px) {
  .dashboard-charts {
    grid-template-columns: repeat(2, 1fr);
  }
}

.dashboard-activity {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 32px;
}

@media (min-width: 1024px) {
  .dashboard-activity {
    grid-template-columns: 2fr 1fr;
  }
}

.dashboard-activity-feed {
  grid-column: span 2;
}

.dashboard-summary {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 24px;
  margin-top: 32px;
}

@media (min-width: 768px) {
  .dashboard-summary {
    grid-template-columns: repeat(3, 1fr);
  }
}

.dashboard-summary-card {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.dashboard-summary-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dashboard-summary-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dashboard-summary-card-icon.bg-green-100 {
  background-color: #dcfce7;
}

.dashboard-summary-card-icon.bg-blue-100 {
  background-color: #dbeafe;
}

.dashboard-summary-card-icon.bg-purple-100 {
  background-color: #f3e8ff;
}

.dashboard-summary-card-icon-svg {
  width: 24px;
  height: 24px;
}

.dashboard-summary-card-icon-svg.text-green-600 {
  color: #16a34a;
}

.dashboard-summary-card-icon-svg.text-blue-600 {
  color: #2563eb;
}

.dashboard-summary-card-icon-svg.text-purple-600 {
  color: #7c3aed;
}

.dashboard-summary-card-text {
  flex: 1;
}

.dashboard-summary-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.dashboard-summary-card-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.dashboard-summary-card-subtitle {
  font-size: 12px;
  color: #6b7280;
}
</style>
