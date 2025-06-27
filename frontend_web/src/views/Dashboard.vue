<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>欢迎回来，{{ userStore.userInfo?.realName }}！</h1>
          <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
        </div>
        <div class="welcome-avatar">
          <el-avatar :src="userStore.userInfo?.avatar" :size="80">
            {{ userStore.userInfo?.realName?.charAt(0) }}
          </el-avatar>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.total }}</div>
            <div class="stat-label">总用户数</div>
          </div>
          <div class="stat-icon">
            <el-icon size="40" color="#409EFF"><UserFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.active }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
          <div class="stat-icon">
            <el-icon size="40" color="#67C23A"><Avatar /></el-icon>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.today }}</div>
            <div class="stat-label">今日新增</div>
          </div>
          <div class="stat-icon">
            <el-icon size="40" color="#E6A23C"><Plus /></el-icon>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ userStats.creditHigh }}</div>
            <div class="stat-label">高信用用户</div>
          </div>
          <div class="stat-icon">
            <el-icon size="40" color="#F56C6C"><Medal /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统公告</span>
          </template>
          <div class="notice-content">
            <el-alert
              title="系统维护通知"
              type="info"
              description="系统将于本周日凌晨2:00-4:00进行维护升级，请提前安排相关工作。"
              show-icon
              :closable="false"
            />
            <br />
            <el-alert
              title="功能更新"
              type="success"
              description="个人信息管理功能已上线，用户可以自主修改基本信息。"
              show-icon
              :closable="false"
            />
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/profile')">
              <el-icon><User /></el-icon>
              个人信息
            </el-button>
            <el-button v-if="userStore.isAdmin" type="success" @click="$router.push('/users')">
              <el-icon><UserFilled /></el-icon>
              用户管理
            </el-button>
            <el-button type="warning" @click="$router.push('/credit-reports')">
              <el-icon><Document /></el-icon>
              信用上报
            </el-button>
            <el-button type="info" @click="$router.push('/credit-profiles')">
              <el-icon><Star /></el-icon>
              信用档案
            </el-button>
            <el-button type="danger" @click="$router.push('/products')">
              <el-icon><ShoppingBag /></el-icon>
              积分商城
            </el-button>
            <el-button type="default" @click="$router.push('/exchange-records')">
              <el-icon><List /></el-icon>
              兑换记录
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { UserFilled, Avatar, Plus, Medal, User, Document, Star, ShoppingBag, List } from '@element-plus/icons-vue'

const userStore = useUserStore()

const userStats = ref({
  total: 0,
  active: 0,
  today: 0,
  creditHigh: 0
})

const currentDate = ref('')

const loadStats = async () => {
  // 模拟数据，实际应从API获取
  userStats.value = {
    total: 1250,
    active: 890,
    today: 12,
    creditHigh: 320
  }
}

onMounted(() => {
  loadStats()
  // 设置当前日期
  const now = new Date()
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  .welcome-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    
    :deep(.el-card__body) {
      padding: 30px;
    }
    
    .welcome-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .welcome-text {
        color: white;
        
        h1 {
          margin: 0 0 10px 0;
          font-size: 28px;
          font-weight: 600;
        }
        
        p {
          margin: 0;
          font-size: 16px;
          opacity: 0.9;
        }
      }
      
      .welcome-avatar {
        .el-avatar {
          border: 3px solid rgba(255, 255, 255, 0.3);
          font-size: 24px;
          font-weight: 600;
        }
      }
    }
  }
  
  .stat-card {
    .el-card__body {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20px;
    }
    
    .stat-content {
      .stat-number {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 5px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
    
    .stat-icon {
      opacity: 0.8;
    }
  }
  
  .notice-content {
    .el-alert {
      margin-bottom: 10px;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
  
  .quick-actions {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    
    .el-button {
      flex: 1;
      min-width: 120px;
    }
  }
}
</style> 