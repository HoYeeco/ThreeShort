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
      <!-- 管理员统计数据 -->
      <template v-if="userStore.isAdmin">
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
              <div class="stat-number">{{ reportStats.pendingReports }}</div>
              <div class="stat-label">待审核上报</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#E6A23C"><Document /></el-icon>
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
              <el-icon size="40" color="#67C23A"><Medal /></el-icon>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ exchangeStats.todayExchanges }}</div>
              <div class="stat-label">今日兑换</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#F56C6C"><ShoppingBag /></el-icon>
            </div>
          </el-card>
        </el-col>
      </template>

      <!-- 系统维护人员统计数据 -->
      <template v-else-if="userStore.isMaintainer">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ systemStats.totalOperations }}</div>
              <div class="stat-label">总操作数</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#409EFF"><Setting /></el-icon>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ systemStats.todayOperations }}</div>
              <div class="stat-label">今日操作</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#67C23A"><Monitor /></el-icon>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ systemStats.onlineUsers }}</div>
              <div class="stat-label">在线用户</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#E6A23C"><Avatar /></el-icon>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ systemStats.systemUptime }}</div>
              <div class="stat-label">系统运行时间</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#F56C6C"><Clock /></el-icon>
            </div>
          </el-card>
        </el-col>
      </template>

      <!-- 普通居民统计数据 -->
      <template v-else>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ userProfile.currentScore || 0 }}</div>
              <div class="stat-label">当前信用分</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#409EFF"><Star /></el-icon>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ userProfile.creditLevel || 'D' }}</div>
              <div class="stat-label">信用等级</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#67C23A"><Medal /></el-icon>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ userProfile.rewardPoints || 0 }}</div>
              <div class="stat-label">奖励积分</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#E6A23C"><Coin /></el-icon>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ userProfile.totalReports || 0 }}</div>
              <div class="stat-label">我的上报</div>
            </div>
            <div class="stat-icon">
              <el-icon size="40" color="#F56C6C"><Document /></el-icon>
            </div>
          </el-card>
        </el-col>
      </template>
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
            <!-- 所有角色都可以访问的功能 -->
            <el-button type="primary" @click="$router.push('/profile')">
              <el-icon><User /></el-icon>
              个人信息
            </el-button>

            <!-- 管理员专用功能 -->
            <template v-if="userStore.isAdmin">
              <el-button type="success" @click="$router.push('/users')">
                <el-icon><UserFilled /></el-icon>
                用户管理
              </el-button>
              <el-button type="warning" @click="$router.push('/admin/reports')">
                <el-icon><Document /></el-icon>
                审核上报
              </el-button>
              <el-button type="info" @click="$router.push('/credit-profiles')">
                <el-icon><Star /></el-icon>
                信用档案
              </el-button>
              <el-button type="danger" @click="$router.push('/admin/products')">
                <el-icon><ShoppingBag /></el-icon>
                商品管理
              </el-button>
              <el-button type="default" @click="$router.push('/admin/agreements')">
                <el-icon><Document /></el-icon>
                公约管理
              </el-button>
            </template>

            <!-- 系统维护人员专用功能 -->
            <template v-else-if="userStore.isMaintainer">
              <el-button type="warning" @click="$router.push('/maintenance/logs')">
                <el-icon><Document /></el-icon>
                操作日志
              </el-button>
              <el-button type="info" @click="$router.push('/maintenance/tasks')">
                <el-icon><Setting /></el-icon>
                定时任务
              </el-button>
              <el-button type="danger" @click="$router.push('/maintenance/system')">
                <el-icon><Monitor /></el-icon>
                系统监控
              </el-button>
            </template>

            <!-- 普通居民专用功能 -->
            <template v-else>
              <el-button type="warning" @click="$router.push('/credit-reports')">
                <el-icon><Document /></el-icon>
                信用上报
              </el-button>
              <el-button type="info" @click="$router.push('/credit-profiles')">
                <el-icon><Star /></el-icon>
                我的信用
              </el-button>
              <el-button type="danger" @click="$router.push('/products')">
                <el-icon><ShoppingBag /></el-icon>
                积分商城
              </el-button>
              <el-button type="default" @click="$router.push('/exchange-records')">
                <el-icon><List /></el-icon>
                兑换记录
              </el-button>
              <el-button type="success" @click="$router.push('/feedback')">
                <el-icon><ChatLineSquare /></el-icon>
                意见反馈
              </el-button>
              <el-button type="primary" @click="$router.push('/agreements')">
                <el-icon><Document /></el-icon>
                学习公约
              </el-button>
            </template>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { UserFilled, Avatar, Medal, User, Document, Star, ShoppingBag, List, Setting, Monitor, Clock, Coin, ChatLineSquare } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userStore = useUserStore()

// 用户统计数据
const userStats = ref({
  total: 0,
  active: 0,
  today: 0,
  creditHigh: 0
})

// 积分兑换统计数据
const exchangeStats = ref({
  totalExchanges: 0,
  totalPoints: 0,
  todayExchanges: 0,
  successRate: 0
})

// 信用报告统计数据
const reportStats = ref({
  totalReports: 0,
  pendingReports: 0,
  approvedReports: 0,
  rejectedReports: 0
})

// 系统统计数据
const systemStats = ref({
  totalOperations: 0,
  todayOperations: 0,
  onlineUsers: 0,
  systemUptime: '0天'
})

// 用户个人档案数据（普通居民使用）
const userProfile = ref({
  currentScore: 0,
  creditLevel: 'D',
  rewardPoints: 0,
  totalReports: 0
})

const currentDate = ref('')

const loadStats = async () => {
  try {
    if (userStore.isAdmin) {
      // 管理员加载用户统计、信用报告统计、积分兑换统计
      const [userStatsResponse, reportStatsResponse, exchangeStatsResponse] = await Promise.all([
        request.get('/user/statistics'),
        request.get('/credit-report/statistics'),
        request.get('/exchange-record/statistics')
      ]);

      userStats.value = {
        total: userStatsResponse.data.total || 0,
        active: userStatsResponse.data.active || 0,
        today: userStatsResponse.data.today || 0,
        creditHigh: userStatsResponse.data.creditHigh || 0
      };

      reportStats.value = {
        totalReports: reportStatsResponse.data.totalReports || 0,
        pendingReports: reportStatsResponse.data.pendingReports || 0,
        approvedReports: reportStatsResponse.data.approvedReports || 0,
        rejectedReports: reportStatsResponse.data.rejectedReports || 0
      };

      exchangeStats.value = {
        totalExchanges: exchangeStatsResponse.data.totalExchanges || 0,
        totalPoints: exchangeStatsResponse.data.totalPoints || 0,
        todayExchanges: exchangeStatsResponse.data.todayExchanges || 0,
        successRate: exchangeStatsResponse.data.successRate || 0
      };
    } else if (userStore.isMaintainer) {
      // 系统维护人员加载系统统计数据
      const systemStatsResponse = await request.get('/system/statistics');
      systemStats.value = {
        totalOperations: systemStatsResponse.data.totalOperations || 0,
        todayOperations: systemStatsResponse.data.todayOperations || 0,
        onlineUsers: systemStatsResponse.data.onlineUsers || 0,
        systemUptime: systemStatsResponse.data.systemUptime || '0天'
      };
    } else {
      // 普通居民加载个人档案数据
      const userProfileResponse = await request.get(`/user-credit-profile/${userStore.userInfo?.id}`);
      userProfile.value = {
        currentScore: userProfileResponse.data.currentScore || 0,
        creditLevel: userProfileResponse.data.creditLevel || 'D',
        rewardPoints: userProfileResponse.data.rewardPoints || 0,
        totalReports: userProfileResponse.data.totalReports || 0
      };
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
    // 如果API调用失败，使用默认值
    userStats.value = { total: 0, active: 0, today: 0, creditHigh: 0 };
    exchangeStats.value = { totalExchanges: 0, totalPoints: 0, todayExchanges: 0, successRate: 0 };
    reportStats.value = { totalReports: 0, pendingReports: 0, approvedReports: 0, rejectedReports: 0 };
    systemStats.value = { totalOperations: 0, todayOperations: 0, onlineUsers: 0, systemUptime: '0天' };
    userProfile.value = { currentScore: 0, creditLevel: 'D', rewardPoints: 0, totalReports: 0 };
  }
};

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