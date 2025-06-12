<template>
  <div class="progress-chart">
    <h3 class="progress-chart-title">{{ title }}</h3>
    <div class="progress-chart-items">
      <div v-for="(item, index) in data" :key="index" class="progress-chart-item">
        <div class="progress-chart-item-header">
          <span class="progress-chart-item-label">{{ item.label }}</span>
          <span class="progress-chart-item-value">{{ item.value }}%</span>
        </div>
        <div class="progress-chart-item-bar">
          <div
            class="progress-chart-item-bar-fill"
            :style="{
              width: `${item.value}%`,
              backgroundColor: item.color,
            }"
          />
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
  data: {
    type: Array,
    required: true,
  },
});
</script>

<style scoped>
.progress-chart {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.progress-chart-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24px;
}

.progress-chart-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-chart-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-chart-item-label {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
}

.progress-chart-item-value {
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.progress-chart-item-bar {
  width: 100%;
  background-color: #e5e7eb;
  border-radius: 10px;
  height: 10px;
}

.progress-chart-item-bar-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 1s ease-out;
}
</style>
