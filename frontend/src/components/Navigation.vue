<template>
  <nav class="navigation">
    <div class="navigation-container">
      <div class="navigation-left">
        <div class="navigation-logo">
          <div class="navigation-logo-icon">
            <Home class="navigation-logo-icon-svg" />
          </div>
          <h1 class="navigation-logo-title">社区诚信管理系统</h1>
        </div>
        <div class="navigation-menu">
          <button
            v-for="item in navItems"
            :key="item.id"
            @click="$emit('tab-change', item.id)"
            :class="[
              'navigation-menu-item',
              { 'navigation-menu-item-active': activeTab === item.id },
            ]"
          >
            <component :is="item.icon" class="navigation-menu-item-icon" />
            <span class="navigation-menu-item-label">{{ item.label }}</span>
          </button>
        </div>
      </div>
      <button class="navigation-logout">
        <LogOut class="navigation-logout-icon" />
        <span class="navigation-logout-label">退出</span>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { Home, Users, Award, BookOpen, User, LogOut } from "lucide-vue-next";
import { useRouter } from "vue-router";

defineProps({
  activeTab: {
    type: String,
    required: true,
  },
});

defineEmits(["tab-change"]);

const router = useRouter();

const navItems = [
  { id: "home", label: "主页", icon: Home },
  { id: "behavior", label: "信用行为", icon: Users },
  { id: "points", label: "积分管理", icon: Award },
  { id: "learning", label: "居民公约学习", icon: BookOpen },
  { id: "profile", label: "个人中心", icon: User },
];

const logout = () => {
  // 清除本地存储中的登录状态信息
  localStorage.removeItem("isLoggedIn");
  localStorage.removeItem("username");
  // 跳转到登录页面
  router.push("/login");
};
</script>

<style scoped>
.navigation {
  background-color: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  border-bottom: 1px solid #e5e7eb;
}

.navigation-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navigation-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.navigation-logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.navigation-logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(to bottom right, #3b82f6, #2563eb);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.navigation-logo-icon-svg {
  width: 20px;
  height: 20px;
  color: white;
}

.navigation-logo-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.navigation-menu {
  display: none;
}

@media (min-width: 768px) {
  .navigation-menu {
    display: flex;
    gap: 4px;
  }
}

.navigation-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  transition: all 0.2s ease;
  border: none;
  background-color: transparent;
  cursor: pointer;
}

.navigation-menu-item:hover {
  color: #2563eb;
  background-color: #eff6ff;
}

.navigation-menu-item-active {
  background: linear-gradient(to right, #3b82f6, #2563eb);
  color: white;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
  transform: scale(1.05);
}

.navigation-menu-item-icon {
  width: 16px;
  height: 16px;
}

.navigation-logout {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  transition: all 0.2s ease;
  border: none;
  background-color: transparent;
  cursor: pointer;
}

.navigation-logout:hover {
  color: #dc2626;
  background-color: #fef2f2;
}

.navigation-logout-icon {
  width: 16px;
  height: 16px;
}
</style>
