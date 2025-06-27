<template>
  <div class="quick-actions">
    <h3 class="quick-actions-title">快捷操作</h3>
    <div class="quick-actions-buttons">
      <button
        v-for="action in actions"
        :key="action.id"
        :class="['quick-actions-button', action.color]"
        @mouseover="hoverAction(action.id)"
        @mouseout="unhoverAction(action.id)"
      >
        <div class="quick-actions-button-content">
          <component :is="action.icon" class="quick-actions-button-icon" />
          <div class="quick-actions-button-text">
            <p class="quick-actions-button-title">{{ action.title }}</p>
            <p class="quick-actions-button-description">{{ action.description }}</p>
          </div>
        </div>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { Plus, FileText, Users, Settings } from "lucide-vue-next";

const hovered = ref(null);

const actions = [
  {
    id: 1,
    title: "添加居民",
    description: "录入新居民信息",
    icon: Plus,
    color: "bg-blue",
    hoverColor: "hover-bg-blue",
  },
  {
    id: 2,
    title: "发布公告",
    description: "发布社区公告",
    icon: FileText,
    color: "bg-green",
    hoverColor: "hover-bg-green",
  },
  {
    id: 3,
    title: "居民管理",
    description: "管理居民档案",
    icon: Users,
    color: "bg-purple",
    hoverColor: "hover-bg-purple",
  },
  {
    id: 4,
    title: "系统设置",
    description: "配置系统参数",
    icon: Settings,
    color: "bg-orange",
    hoverColor: "hover-bg-orange",
  },
];

const hoverAction = (id) => {
  hovered.value = id;
};

const unhoverAction = (id) => {
  if (hovered.value === id) {
    hovered.value = null;
  }
};
</script>

<style scoped>
.quick-actions {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.quick-actions-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24px;
}

.quick-actions-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.quick-actions-button {
  padding: 16px;
  border-radius: 10px;
  color: white;
  transition: all 0.2s ease;
  border: none;
  cursor: pointer;
}

.quick-actions-button:hover {
  transform: scale(1.05);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
}

.quick-actions-button-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quick-actions-button-icon {
  width: 20px;
  height: 20px;
}

.quick-actions-button-text {
  text-align: left;
}

.quick-actions-button-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.quick-actions-button-description {
  font-size: 12px;
  opacity: 0.9;
}

.bg-blue {
  background-color: #3b82f6;
}

.hover-bg-blue {
  background-color: #2563eb;
}

.bg-green {
  background-color: #22c55e;
}

.hover-bg-green {
  background-color: #16a34a;
}

.bg-purple {
  background-color: #8b5cf6;
}

.hover-bg-purple {
  background-color: #7c3aed;
}

.bg-orange {
  background-color: #f97316;
}

.hover-bg-orange {
  background-color: #ea580c;
}
</style>
