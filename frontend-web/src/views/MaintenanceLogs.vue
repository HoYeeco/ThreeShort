<template>
  <div class="maintenance-logs">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>操作日志管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="loadLogs">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="danger" @click="showCleanDialog">
          <el-icon><Delete /></el-icon>
          清理日志
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.totalLogs || 0 }}</div>
            <div class="stat-label">总日志数</div>
          </div>
          <div class="stat-icon">
            <el-icon size="40" color="#409EFF"><Document /></el-icon>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.successLogs || 0 }}</div>
            <div class="stat-label">成功操作</div>
          </div>
          <div class="stat-icon">
            <el-icon size="40" color="#67C23A"><SuccessFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.failedLogs || 0 }}</div>
            <div class="stat-label">失败操作</div>
          </div>
          <div class="stat-icon">
            <el-icon size="40" color="#F56C6C"><CircleCloseFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.recentLogs || 0 }}</div>
            <div class="stat-label">近7天日志</div>
          </div>
          <div class="stat-icon">
            <el-icon size="40" color="#E6A23C"><Clock /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询条件 -->
    <el-card style="margin-bottom: 20px;">
      <el-form :model="queryForm" inline>
        <el-form-item label="操作类型">
          <el-select v-model="queryForm.operationType" placeholder="请选择操作类型" clearable>
            <el-option label="用户登录" value="USER_LOGIN" />
            <el-option label="用户注册" value="USER_REGISTER" />
            <el-option label="信用上报" value="CREDIT_REPORT" />
            <el-option label="积分兑换" value="POINT_EXCHANGE" />
            <el-option label="任务执行" value="TASK_EXECUTION" />
            <el-option label="系统操作" value="SYSTEM_OPERATION" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="操作状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="错误" value="ERROR" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="用户ID">
          <el-input v-model="queryForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="searchLogs">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>操作日志列表</span>
          <span class="total-count">共 {{ pagination.total }} 条记录</span>
        </div>
      </template>
      
      <el-table 
        :data="logsList" 
        v-loading="loading"
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="日志ID" width="80" />
        
        <el-table-column prop="operationType" label="操作类型" width="120">
          <template #default="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)">
              {{ getOperationTypeText(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="operationDesc" label="操作描述" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="userId" label="用户ID" width="80" />
        
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        
        <el-table-column prop="executionTime" label="执行时长" width="100">
          <template #default="scope">
            <span v-if="scope.row.executionTime">{{ scope.row.executionTime }}ms</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdTime" label="操作时间" width="180" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="text" @click="viewLogDetail(scope.row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadLogs"
          @current-change="loadLogs"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="日志详情" width="800px">
      <el-descriptions v-if="selectedLog" :column="2" border>
        <el-descriptions-item label="日志ID">{{ selectedLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTag(selectedLog.operationType)">
            {{ getOperationTypeText(selectedLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ selectedLog.operationDesc }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ selectedLog.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(selectedLog.status)">
            {{ getStatusText(selectedLog.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ selectedLog.ipAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">
          {{ selectedLog.executionTime ? selectedLog.executionTime + 'ms' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">
          {{ formatDateTime(selectedLog.createdTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="selectedLog.errorMessage">
          <el-text type="danger">{{ selectedLog.errorMessage }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 清理日志对话框 -->
    <el-dialog v-model="showCleanDialog" title="清理历史日志" width="500px">
      <el-form :model="cleanForm" label-width="120px">
        <el-form-item label="保留天数">
          <el-input-number 
            v-model="cleanForm.keepDays" 
            :min="7" 
            :max="365" 
            placeholder="请输入保留天数"
          />
          <div class="form-tip">最少保留7天，最多保留365天</div>
        </el-form-item>
        
        <el-form-item>
          <el-alert
            title="注意"
            type="warning"
            description="此操作将永久删除指定天数之前的历史日志，请谨慎操作！"
            show-icon
            :closable="false"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCleanDialog = false">取消</el-button>
        <el-button type="danger" @click="cleanHistoryLogs" :loading="cleanLoading">
          确认清理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, Delete, Document, SuccessFilled, CircleCloseFilled, 
  Clock, Search, View 
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 接口类型定义
interface SystemLog {
  id: number
  operationType: string
  operationDesc: string
  userId?: number
  status: string
  ipAddress?: string
  executionTime?: number
  errorMessage?: string
  createdTime: string
}

// 响应式数据
const logsList = ref<SystemLog[]>([])
const statistics = ref({
  totalLogs: 0,
  successLogs: 0,
  failedLogs: 0,
  errorLogs: 0,
  recentLogs: 0
})
const selectedLog = ref<SystemLog | null>(null)

// 查询表单
const queryForm = ref({
  operationType: '',
  status: '',
  userId: '',
  startTime: '',
  endTime: ''
})

const dateRange = ref<[string, string] | null>(null)

// 分页
const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

// 清理表单
const cleanForm = ref({
  keepDays: 30
})

// 对话框状态
const showDetailDialog = ref(false)
const showCleanDialog = ref(false)

// 加载状态
const loading = ref(false)
const cleanLoading = ref(false)

// 监听时间范围变化
watch(dateRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    queryForm.value.startTime = newVal[0]
    queryForm.value.endTime = newVal[1]
  } else {
    queryForm.value.startTime = ''
    queryForm.value.endTime = ''
  }
})

// 方法
const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.current,
      size: pagination.value.size,
      operationType: queryForm.value.operationType || undefined,
      status: queryForm.value.status || undefined,
      userId: queryForm.value.userId || undefined,
      startTime: queryForm.value.startTime || undefined,
      endTime: queryForm.value.endTime || undefined
    }
    
    const response = await request.get('/system/logs/list', { params })
    logsList.value = response.data.records || []
    pagination.value.total = response.data.total || 0
  } catch (error) {
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await request.get('/system/logs/statistics?days=7')
    statistics.value = response.data
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const searchLogs = () => {
  pagination.value.current = 1
  loadLogs()
}

const resetQuery = () => {
  queryForm.value = {
    operationType: '',
    status: '',
    userId: '',
    startTime: '',
    endTime: ''
  }
  dateRange.value = null
  pagination.value.current = 1
  loadLogs()
}

const handleSortChange = ({ prop: _prop, order: _order }: any) => {
  // 处理排序逻辑
  loadLogs()
}

const viewLogDetail = (log: SystemLog) => {
  selectedLog.value = log
  showDetailDialog.value = true
}

const cleanHistoryLogs = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要清理 ${cleanForm.value.keepDays} 天前的历史日志吗？此操作不可恢复！`,
      '确认清理',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    cleanLoading.value = true
    await request.delete(`/system/logs/clean?keepDays=${cleanForm.value.keepDays}`)
    ElMessage.success('历史日志清理成功')
    showCleanDialog.value = false
    loadLogs()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清理失败')
    }
  } finally {
    cleanLoading.value = false
  }
}

// 辅助方法
const getOperationTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'USER_LOGIN': '用户登录',
    'USER_REGISTER': '用户注册',
    'CREDIT_REPORT': '信用上报',
    'POINT_EXCHANGE': '积分兑换',
    'TASK_EXECUTION': '任务执行',
    'SYSTEM_OPERATION': '系统操作'
  }
  return typeMap[type] || type
}

const getOperationTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    'USER_LOGIN': 'primary',
    'USER_REGISTER': 'success',
    'CREDIT_REPORT': 'warning',
    'POINT_EXCHANGE': 'info',
    'TASK_EXECUTION': 'danger',
    'SYSTEM_OPERATION': 'default'
  }
  return tagMap[type] || 'default'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'ERROR': '错误'
  }
  return statusMap[status] || status
}

const getStatusTag = (status: string) => {
  const tagMap: Record<string, string> = {
    'SUCCESS': 'success',
    'FAILED': 'warning',
    'ERROR': 'danger'
  }
  return tagMap[status] || 'default'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadLogs()
  loadStatistics()
})
</script>

<style lang="scss" scoped>
.maintenance-logs {
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
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    
    .total-count {
      font-size: 14px;
      color: #666;
      font-weight: normal;
    }
  }
  
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
  
  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
  }
}
</style> 