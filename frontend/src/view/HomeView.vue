<template>
  <div class="min-h-screen bg-gray-50">
    <Navigation :activeTab="activeTab" @tab-change="handleTabChange" />
    <component :is="currentView" />
  </div>
</template>

<script setup>
import { ref } from "vue";
import Navigation from "@/components/Navigation.vue";
import Dashboard from "@/components/Dashboard.vue";
import PlaceholderView from "@/components/PlaceholderView.vue";
import BehaviorView from "./BehaviorView.vue";
import LearningView from '@/view/LearningView.vue';

const activeTab = ref("home");

const views = {
  home: Dashboard,
  behavior: BehaviorView,
  points: PlaceholderView,
  learning: LearningView,
  profile: PlaceholderView,
};

const currentView = ref(views.home);

const handleTabChange = (tab) => {
  activeTab.value = tab;
  currentView.value = views[tab] || views.home;
};
</script>

<style scoped>
/* 全局布局样式 */
.min-h-screen {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 导航栏样式（蓝色渐变，类似登录界面的左侧风格） */
.Navigation {
  background: linear-gradient(135deg, #409eff 0%, #3375b9 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  color: white;
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all 0.3s ease;
}

/* 导航栏内容容器 */
.Navigation-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 导航标题样式 */
.Navigation-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

/* 导航菜单样式 */
.Navigation-menu {
  display: flex;
  gap: 24px;
}

/* 导航菜单项样式 */
.Navigation-item {
  position: relative;
  padding: 8px 0;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

/* 活动菜单项样式（蓝色高亮） */
.Navigation-item.active {
  color: #e6f7ff;
}

/* 活动菜单项下划线 */
.Navigation-item.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: #e6f7ff;
  border-radius: 2px;
}

/* 菜单项悬停效果 */
.Navigation-item:hover:not(.active) {
  color: #e6f7ff;
  opacity: 0.9;
}

/* 内容区域样式（类似登录界面的右侧面板） */
.current-view {
  flex: 1;
  padding: 32px 24px;
  background: #f5f7fa;
}

/* 响应式设计（小屏幕适配） */
@media (max-width: 768px) {
  .Navigation-menu {
    gap: 16px;
  }

  .Navigation-item {
    font-size: 12px;
  }

  .current-view {
    padding: 24px 16px;
  }
}

@media (max-width: 480px) {
  .Navigation-container {
    padding: 0 16px;
  }

  .Navigation-menu {
    gap: 12px;
  }

  .Navigation-title {
    font-size: 16px;
  }
}

/* 平滑过渡动画 */
.Navigation {
  transition: all 0.3s ease;
}

.Navigation-item {
  transition: all 0.2s ease;
}
</style>
