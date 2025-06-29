<template>
  <div class="maintenance-tasks">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <h2>定时任务管理</h2>
          <p>管理系统定时任务和手动同步操作</p>
        </div>
      </template>

      <!-- 快速操作区 -->
      <div class="quick-actions">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="action-card">
              <template #header>
                <div class="action-header">
                  <el-icon><Timer /></el-icon>
                  <span>信用分数计算</span>
                </div>
              </template>
              <div class="action-content">
                <p>手动触发信用分数计算和评分记录生成</p>
                <el-button 
                  type="primary" 
                  :loading="syncLoading.scoreGeneration"
                  @click="executeScoreGeneration"
                >
                  {{ syncLoading.scoreGeneration ? '执行中...' : '立即同步' }}
                </el-button>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card class="action-card">
              <template #header>
                <div class="action-header">
                  <el-icon><Refresh /></el-icon>
                  <span>信用等级重算</span>
                </div>
              </template>
              <div class="action-content">
                <p>重新计算所有用户的信用等级</p>
                <el-button 
                  type="success" 
                  :loading="syncLoading.recalculateCredit"
                  @click="executeRecalculateCredit"
                >
                  {{ syncLoading.recalculateCredit ? '执行中...' : '立即重算' }}
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 任务状态概览 -->
    <el-card class="status-card">
      <template #header>
        <h3>任务状态概览</h3>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="status-item">
            <div class="status-icon success">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="status-info">
              <h4>系统任务</h4>
              <p>2个定时任务正在运行</p>
            </div>
          </div>
        </el-col>
        
        <el-col :span="8">
          <div class="status-item">
            <div class="status-icon info">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="status-info">
              <h4>下次执行</h4>
              <p>{{ nextExecutionTime }}</p>
            </div>
          </div>
        </el-col>
        
        <el-col :span="8">
          <div class="status-item">
            <div class="status-icon warning">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="status-info">
              <h4>最近执行</h4>
              <p>{{ lastExecutionTime }}</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 执行日志 -->
    <el-card class="log-card">
      <template #header>
        <h3>最近执行记录</h3>
      </template>
      
      <el-table :data="executionLogs" style="width: 100%">
        <el-table-column prop="time" label="执行时间" width="180" />
        <el-table-column prop="task" label="任务名称" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ scope.row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="执行结果" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, Refresh, CircleCheck, Clock, DataLine } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const syncLoading = ref({
  scoreGeneration: false,
  recalculateCredit: false
})

const nextExecutionTime = ref('周一 02:00')
const lastExecutionTime = ref('暂无记录')

const executionLogs = ref([
  {
    time: '2024-01-15 02:00:00',
    task: '信用分数计算',
    status: 'SUCCESS',
    message: '成功处理10个用户的评分记录'
  },
  {
    time: '2024-01-14 03:00:00', 
    task: '信用等级重算',
    status: 'SUCCESS',
    message: '重新计算完成，更新5个用户等级'
  }
])

// 执行信用分数计算
const executeScoreGeneration = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要手动执行信用分数计算吗？这将为所有用户生成当前周期的评分记录。',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    syncLoading.value.scoreGeneration = true
    
    await request.post('/system/tasks/execute/score-generation')
    
    ElMessage.success('信用分数计算执行成功！')
    
    // 添加到执行日志
    executionLogs.value.unshift({
      time: new Date().toLocaleString(),
      task: '信用分数计算',
      status: 'SUCCESS',
      message: '手动执行成功'
    })
    
    // 更新最近执行时间
    lastExecutionTime.value = '刚刚'
    
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '执行失败')
      
      // 添加失败日志
      executionLogs.value.unshift({
        time: new Date().toLocaleString(),
        task: '信用分数计算',
        status: 'FAILED',
        message: error.message || '执行失败'
      })
    }
  } finally {
    syncLoading.value.scoreGeneration = false
  }
}

// 执行信用等级重算
const executeRecalculateCredit = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重新计算所有用户的信用等级吗？这可能需要一些时间。',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    syncLoading.value.recalculateCredit = true
    
    await request.post('/system/tasks/execute/recalculate-credit')
    
    ElMessage.success('信用等级重算执行成功！')
    
    // 添加到执行日志
    executionLogs.value.unshift({
      time: new Date().toLocaleString(),
      task: '信用等级重算',
      status: 'SUCCESS',
      message: '手动执行成功'
    })
    
    // 更新最近执行时间
    lastExecutionTime.value = '刚刚'
    
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '执行失败')
      
      // 添加失败日志
      executionLogs.value.unshift({
        time: new Date().toLocaleString(),
        task: '信用等级重算',
        status: 'FAILED',
        message: error.message || '执行失败'
      })
    }
  } finally {
    syncLoading.value.recalculateCredit = false
  }
}

// 页面加载时获取基本信息
onMounted(() => {
  // 这里可以调用接口获取实际的执行状态
  console.log('定时任务管理页面已加载')
})
</script>

<style scoped>
.maintenance-tasks {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.card-header p {
  margin: 5px 0 0 0;
  color: #909399;
  font-size: 14px;
}

.quick-actions {
  margin-top: 20px;
}

.action-card {
  height: 160px;
}

.action-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.action-content {
  padding: 10px 0;
}

.action-content p {
  margin: 0 0 15px 0;
  color: #606266;
  font-size: 14px;
}

.status-card {
  margin-bottom: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
}

.status-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.status-icon.success {
  background: #f0f9ff;
  color: #67c23a;
}

.status-icon.info {
  background: #f4f4f5;
  color: #409eff;
}

.status-icon.warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-info h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.status-info p {
  margin: 5px 0 0 0;
  font-size: 14px;
  color: #909399;
}

.log-card {
  margin-bottom: 20px;
}
</style> 