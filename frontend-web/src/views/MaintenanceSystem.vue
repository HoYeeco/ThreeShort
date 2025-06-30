<template>
  <div class="maintenance-system">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>系统监控</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshAll">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 系统状态概览 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card class="status-card">
          <div class="status-content">
            <div class="status-indicator" :class="systemStatus.overall">
              <el-icon size="40"><Monitor /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">系统状态</div>
              <div class="status-text">{{ getStatusText(systemStatus.overall) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="status-card">
          <div class="status-content">
            <div class="status-indicator" :class="systemStatus.database">
              <el-icon size="40"><Coin /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">数据库</div>
              <div class="status-text">{{ getStatusText(systemStatus.database) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="status-card">
          <div class="status-content">
            <div class="status-indicator" :class="systemStatus.application">
              <el-icon size="40"><Setting /></el-icon>
            </div>
            <div class="status-info">
              <div class="status-title">应用服务</div>
              <div class="status-text">{{ getStatusText(systemStatus.application) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据统计 -->
    <!-- <el-card style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>数据统计</span>
          <span class="last-update">最后更新：{{ formatDateTime(dataStatistics.lastUpdate) }}</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ dataStatistics.userCount || 0 }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ dataStatistics.creditReportCount || 0 }}</div>
            <div class="stat-label">信用报告数</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ dataStatistics.exchangeCount || 0 }}</div>
            <div class="stat-label">积分兑换数</div>
          </div>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ dataStatistics.feedbackCount || 0 }}</div>
            <div class="stat-label">反馈建议数</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ dataStatistics.logCount || 0 }}</div>
            <div class="stat-label">系统日志数</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ getSystemUptime() }}</div>
            <div class="stat-label">系统运行时间</div>
          </div>
        </el-col>
      </el-row>
    </el-card> -->

    <!-- 最近系统事件 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>最近系统事件</span>
          <span class="event-count">共 {{ recentEvents.length }} 条记录</span>
        </div>
      </template>
      
      <el-table :data="recentEvents" style="width: 100%" max-height="400">
        <el-table-column prop="time" label="时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.time) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="level" label="级别" width="80">
          <template #default="scope">
            <el-tag :type="getEventLevelTag(scope.row.level)">
              {{ scope.row.level }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="module" label="模块" width="120" />
        
        <el-table-column prop="message" label="事件描述" min-width="300" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Monitor, 
  Coin, 
  Setting,
  Refresh
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 系统事件接口
interface SystemEvent {
  time: string
  level: string
  module: string
  message: string
}

// 响应式数据
const systemStatus = ref({
  overall: 'normal',
  database: 'normal',
  application: 'normal'
})

const dataStatistics = ref({
  userCount: 0,
  creditReportCount: 0,
  exchangeCount: 0,
  feedbackCount: 0,
  logCount: 0,
  lastUpdate: ''
})

const recentEvents = ref<SystemEvent[]>([])

// 方法
const refreshAll = () => {
  loadSystemStatus()
  loadDataStatistics()
  loadRecentEvents()
}

const loadSystemStatus = async () => {
  try {
    // 检查数据库状态
    const dbHealthResponse = await request.get('/system/database/health')
    
    // 检查系统统计
    const statsResponse = await request.get('/system/statistics')
    
    systemStatus.value = {
      overall: 'normal',
      database: dbHealthResponse.data ? 'normal' : 'error',
      application: statsResponse.data ? 'normal' : 'error'
    }
  } catch (error) {
    systemStatus.value = {
      overall: 'error',
      database: 'error',
      application: 'error'
    }
  }
}

const loadDataStatistics = async () => {
  try {
    const response = await request.get('/system/statistics')
    const data = response.data
    
    dataStatistics.value = {
      userCount: data.userCount || 0,
      creditReportCount: data.creditReportCount || 0,
      exchangeCount: data.exchangeCount || 0,
      feedbackCount: data.feedbackCount || 0,
      logCount: data.logCount || 0,
      lastUpdate: new Date().toISOString()
    }
  } catch (error) {
    ElMessage.error('加载数据统计失败')
  }
}

const loadRecentEvents = async () => {
  try {
    // 从系统日志中获取最近的事件
    const response = await request.get('/system/logs/list', {
      params: {
        page: 1,
        size: 20
      }
    })
    
    const logs = response.data?.records || []
    recentEvents.value = logs.map((log: any) => ({
      time: log.createdTime,
      level: log.status === 'SUCCESS' ? 'INFO' : (log.status === 'FAILED' ? 'ERROR' : 'WARN'),
      module: getModuleByOperationType(log.operationType),
      message: log.operationDesc
    }))
  } catch (error) {
    ElMessage.error('加载系统事件失败')
  }
}

const getModuleByOperationType = (operationType: string) => {
  const moduleMap: Record<string, string> = {
    'USER_LOGIN': '用户认证',
    'USER_REGISTER': '用户管理',
    'CREDIT_REPORT': '信用管理',
    'CREDIT_REVIEW': '信用审核',
    'POINT_EXCHANGE': '积分商城',
    'FEEDBACK_SUBMIT': '反馈系统',
    'TASK_EXECUTION': '定时任务',
    'SYSTEM_OPERATION': '系统维护'
  }
  return moduleMap[operationType] || '系统'
}

// const getSystemUptime = () => {
//   const now = new Date()
//   const startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000) // 假设24小时前启动
//   const diffMs = now.getTime() - startTime.getTime()
//   const hours = Math.floor(diffMs / (1000 * 60 * 60))
//   const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
//   return `${hours}小时${minutes}分钟`
// }

// 辅助方法
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'normal': '正常',
    'warning': '警告',
    'error': '异常'
  }
  return statusMap[status] || status
}

const getEventLevelTag = (level: string) => {
  const tagMap: Record<string, string> = {
    'INFO': 'primary',
    'WARN': 'warning',
    'ERROR': 'danger',
    'DEBUG': 'info'
  }
  return tagMap[level] || 'default'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  refreshAll()
  
  // 每30秒自动刷新一次数据
  setInterval(() => {
    refreshAll()
  }, 30000)
})
</script>

<style lang="scss" scoped>
.maintenance-system {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      color: #333;
    }
    
    .header-actions {
      display: flex;
      gap: 10px;
    }
  }
  
  .status-card {
    .el-card__body {
      padding: 20px;
    }
    
    .status-content {
      display: flex;
      align-items: center;
      gap: 15px;
      
      .status-indicator {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 60px;
        height: 60px;
        border-radius: 50%;
        
        &.normal {
          background-color: #f0f9ff;
          color: #67C23A;
        }
        
        &.warning {
          background-color: #fdf6ec;
          color: #E6A23C;
        }
        
        &.error {
          background-color: #fef0f0;
          color: #F56C6C;
        }
      }
      
      .status-info {
        .status-title {
          font-size: 14px;
          color: #909399;
          margin-bottom: 4px;
        }
        
        .status-text {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    
    .last-update, .event-count {
      font-size: 12px;
      color: #909399;
      font-weight: normal;
    }
  }
  
  .stat-item {
    text-align: center;
    padding: 20px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    background-color: #fafafa;
    
    .stat-number {
      font-size: 32px;
      font-weight: bold;
      color: #409eff;
      margin-bottom: 8px;
    }
    
    .stat-label {
      font-size: 14px;
      color: #606266;
    }
  }
}
</style> 