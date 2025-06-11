<template>
  <div class="stat-card">
    <div class="stat-card-header">
      <div class="stat-card-info">
        <p class="stat-card-title">{{ title }}</p>
        <p class="stat-card-value">{{ value }}</p>
        <div v-if="trend" class="stat-card-trend">
          <span>{{ trend.isPositive ? "↗" : "↘" }}</span>
          <span>{{ Math.abs(trend.value) }}%</span>
          <span>相比上月</span>
        </div>
      </div>
      <div :class="['stat-card-icon-container', 'bg-' + color]">
        <div :class="['stat-card-icon', 'color-' + color]">
          <component :is="icon" class="stat-card-icon-svg" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number],
    required: true,
  },
  icon: {
    required: true,
  },
  trend: {
    type: Object,
    default: null,
  },
  color: {
    type: String,
    required: true,
    validator: (value) => ["blue", "green", "purple", "orange"].includes(value),
  },
});
</script>

<style scoped>
.stat-card {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
  transform: scale(1.05);
}

.stat-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card-info {
  flex: 1;
}

.stat-card-title {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  margin-bottom: 4px;
}

.stat-card-value {
  font-size: 36px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.stat-card-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.stat-card-trend.text-green-600 {
  color: #16a34a;
}

.stat-card-trend.text-red-600 {
  color: #dc2626;
}

.stat-card-icon-container {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bg-blue {
  background-color: #eff6ff;
}

.bg-green {
  background-color: #f0fdf4;
}

.bg-purple {
  background-color: #faf5ff;
}

.bg-orange {
  background-color: #fff7ed;
}

.stat-card-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-blue {
  background: linear-gradient(to bottom right, #3b82f6, #2563eb);
}

.color-green {
  background: linear-gradient(to bottom right, #22c55e, #16a34a);
}

.color-purple {
  background: linear-gradient(to bottom right, #8b5cf6, #7c3aed);
}

.color-orange {
  background: linear-gradient(to bottom right, #f97316, #ea580c);
}

.stat-card-icon-svg {
  width: 16px;
  height: 16px;
  color: white;
}
</style>
