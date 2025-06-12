<template>
  <div class="activity-feed">
    <h3 class="activity-feed-title">最近活动</h3>
    <div class="activity-feed-items">
      <div
        v-for="activity in activities"
        :key="activity.id"
        class="activity-feed-item"
        @mouseover="activity.hovered = true"
        @mouseout="activity.hovered = false"
      >
        <div class="activity-feed-item-icon">
          <component
            :is="getActivityIcon(activity.type)"
            class="activity-feed-item-icon-svg"
            :class="getIconColor(activity.type)"
          />
        </div>
        <div class="activity-feed-item-content">
          <p class="activity-feed-item-message">{{ activity.message }}</p>
          <div class="activity-feed-item-time">
            <Clock class="activity-feed-item-time-icon" />
            <span class="activity-feed-item-time-text">{{ activity.time }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { Clock, User, Award, BookOpen } from "lucide-vue-next";

const activities = ref([
  {
    id: 1,
    type: "credit",
    message: "张三完成了社区志愿服务，获得10个诚信积分",
    time: "5分钟前",
    user: "张三",
    hovered: false,
  },
  {
    id: 2,
    type: "learning",
    message: "李四完成了《居民公约》第三章学习",
    time: "15分钟前",
    user: "李四",
    hovered: false,
  },
  {
    id: 3,
    type: "points",
    message: "王五兑换了社区停车优惠券",
    time: "1小时前",
    user: "王五",
    hovered: false,
  },
  {
    id: 4,
    type: "credit",
    message: "赵六参与了社区环境清洁活动",
    time: "2小时前",
    user: "赵六",
    hovered: false,
  },
  {
    id: 5,
    type: "learning",
    message: "钱七开始学习《文明行为规范》",
    time: "3小时前",
    user: "钱七",
    hovered: false,
  },
]);

const getActivityIcon = (type) => {
  switch (type) {
    case "credit":
      return User;
    case "learning":
      return BookOpen;
    case "points":
      return Award;
    default:
      return Clock;
  }
};

const getIconColor = (type) => {
  switch (type) {
    case "credit":
      return "text-blue-600";
    case "learning":
      return "text-green-600";
    case "points":
      return "text-orange-600";
    default:
      return "text-gray-600";
  }
};
</script>

<style scoped>
.activity-feed {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.activity-feed-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24px;
}

.activity-feed-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-feed-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  transition: background-color 0.2s ease;
}

.activity-feed-item:hover {
  background-color: #f9fafb;
}

.activity-feed-item-icon {
  width: 32px;
  height: 32px;
  background-color: #f3f4f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-feed-item-icon-svg {
  width: 16px;
  height: 16px;
}

.activity-feed-item-icon-svg.text-blue-600 {
  color: #2563eb;
}

.activity-feed-item-icon-svg.text-green-600 {
  color: #16a34a;
}

.activity-feed-item-icon-svg.text-orange-600 {
  color: #ea580c;
}

.activity-feed-item-icon-svg.text-gray-600 {
  color: #6b7280;
}

.activity-feed-item-content {
  flex: 1;
}

.activity-feed-item-message {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
}

.activity-feed-item-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.activity-feed-item-time-icon {
  width: 12px;
  height: 12px;
}
</style>
