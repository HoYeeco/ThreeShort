<template>
  <div class="maintenance-tasks">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <h2>定时任务管理</h2>
          <p>管理系统定时任务和手动同步操作</p>
        </div>
      </template>

          <!-- 定时任务配置区 -->
    <div class="task-config-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="config-card">
            <template #header>
              <div class="config-header">
                <el-icon><Timer /></el-icon>
                <span>信用分数计算任务</span>
                <!-- <el-button size="small" @click="showConfigDialog('scoreGeneration')">配置</el-button> -->
              </div>
            </template>
            <div class="config-content">
              <div class="config-item">
                <label>执行时间：</label>
                <span>{{ taskConfig.scoreGeneration?.cron || '0 0 2 * * MON' }}</span>
                <el-tag size="small" type="info">{{ parseCronExpression(taskConfig.scoreGeneration?.cron || '0 0 2 * * MON') }}</el-tag>
              </div>
              <div class="config-item">
                <label>状态：</label>
                <el-tag :type="taskConfig.scoreGeneration?.enabled ? 'success' : 'danger'">
                  {{ taskConfig.scoreGeneration?.enabled ? '启用' : '禁用' }}
                </el-tag>
              </div>
              <div class="config-actions">
                <el-button 
                  type="primary" 
                  size="small"
                  :loading="syncLoading.scoreGeneration"
                  @click="executeScoreGeneration"
                >
                  {{ syncLoading.scoreGeneration ? '执行中...' : '立即执行' }}
                </el-button>
                <el-button size="small" @click="showExecutionStats('scoreGeneration')">查看效果</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="config-card">
            <template #header>
              <div class="config-header">
                <el-icon><Refresh /></el-icon>
                <span>信用等级重算任务</span>
                <!-- <el-button size="small" @click="showConfigDialog('recalculateCredit')">配置</el-button> -->
              </div>
            </template>
            <div class="config-content">
              <div class="config-item">
                <label>执行时间：</label>
                <span>{{ taskConfig.recalculateCredit?.cron || '0 0 3 * * *' }}</span>
                <el-tag size="small" type="info">{{ parseCronExpression(taskConfig.recalculateCredit?.cron || '0 0 3 * * *') }}</el-tag>
              </div>
              <div class="config-item">
                <label>状态：</label>
                <el-tag :type="taskConfig.recalculateCredit?.enabled ? 'success' : 'danger'">
                  {{ taskConfig.recalculateCredit?.enabled ? '启用' : '禁用' }}
                </el-tag>
              </div>
              <div class="config-actions">
                <el-button 
                  type="success" 
                  size="small"
                  :loading="syncLoading.recalculateCredit"
                  @click="executeRecalculateCredit"
                >
                  {{ syncLoading.recalculateCredit ? '执行中...' : '立即执行' }}
                </el-button>
                <el-button size="small" @click="showExecutionStats('recalculateCredit')">查看效果</el-button>
              </div>
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

    <!-- 评分记录查看 -->
    <el-card class="records-card">
      <template #header>
        <div class="card-header">
          <h3>批量生成评分记录</h3>
          <el-button type="primary" @click="loadScoreRecords">刷新记录</el-button>
        </div>
      </template>
      
      <!-- 统计信息 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-number">{{ recordStats.totalRecords || 0 }}</div>
            <div class="stat-label">总评分记录</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-number">{{ recordStats.thisWeekRecords || 0 }}</div>
            <div class="stat-label">本周记录</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-number">{{ recordStats.totalRewardPoints || 0 }}</div>
            <div class="stat-label">总奖励积分</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-number">{{ recordStats.latestPeriod || '-' }}</div>
            <div class="stat-label">最新周期</div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 记录列表 -->
      <el-table :data="scoreRecords" v-loading="recordsLoading" style="width: 100%">
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="userName" label="用户姓名" width="120" />
        <el-table-column prop="scorePeriod" label="评分周期" width="120" />
        <el-table-column prop="periodScore" label="周期分数" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getScoreType(scope.row.periodScore)">
              {{ scope.row.periodScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rewardPointsGained" label="获得奖励积分" width="120" align="center">
          <template #default="scope">
            <span :class="scope.row.rewardPointsGained > 0 ? 'reward-positive' : 'reward-zero'">
              {{ scope.row.rewardPointsGained }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="calculationTime" label="计算时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" @click="viewRecordDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="recordQuery.page"
          v-model:page-size="recordQuery.size"
          :page-sizes="[10, 20, 50]"
          :total="recordTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleRecordSizeChange"
          @current-change="handleRecordCurrentChange"
        />
      </div>
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

    <!-- 任务配置对话框 -->
    <el-dialog 
      v-model="configDialogVisible" 
      :title="`配置${currentTaskType === 'scoreGeneration' ? taskConfig.scoreGeneration?.name : taskConfig.recalculateCredit?.name || ''}任务`"
      width="500px"
    >
      <el-form :model="tempConfig" label-width="100px">
        <el-form-item label="执行时间">
          <el-select v-model="tempConfig.cron" placeholder="选择执行时间">
            <el-option label="每周一凌晨2点" value="0 0 2 * * MON" />
            <el-option label="每周二凌晨2点" value="0 0 2 * * TUE" />
            <el-option label="每周三凌晨2点" value="0 0 2 * * WED" />
            <el-option label="每周四凌晨2点" value="0 0 2 * * THU" />
            <el-option label="每周五凌晨2点" value="0 0 2 * * FRI" />
            <el-option label="每周六凌晨2点" value="0 0 2 * * SAT" />
            <el-option label="每周日凌晨2点" value="0 0 2 * * SUN" />
            <el-option label="每天凌晨1点" value="0 0 1 * * *" />
            <el-option label="每天凌晨2点" value="0 0 2 * * *" />
            <el-option label="每天凌晨3点" value="0 0 3 * * *" />
            <el-option label="每天凌晨4点" value="0 0 4 * * *" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务状态">
          <el-switch v-model="tempConfig.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateTaskConfig">确定</el-button>
      </template>
    </el-dialog>

    <!-- 执行效果统计对话框 -->
    <el-dialog 
      v-model="statsDialogVisible" 
      :title="currentStatsTaskType === 'scoreGeneration' ? '信用分数计算任务效果' : '信用等级重算任务效果'"
      width="800px"
    >
      <!-- 信用分数计算任务效果 -->
      <div v-if="currentStatsTaskType === 'scoreGeneration' && executionStats.scoreGeneration">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">{{ executionStats.scoreGeneration.latestPeriod || '-' }}</div>
              <div class="stat-label">最新周期</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">{{ executionStats.scoreGeneration.recordsGenerated || 0 }}</div>
              <div class="stat-label">生成记录数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">{{ executionStats.scoreGeneration.totalRewardPoints || 0 }}</div>
              <div class="stat-label">总奖励积分</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">{{ executionStats.scoreGeneration.lastExecutionTime || '暂未执行' }}</div>
              <div class="stat-label">最后执行时间</div>
            </div>
          </el-col>
        </el-row>
        
        <!-- 用户信用分查询 -->
        <div style="margin-top: 30px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h5 style="margin: 0;">所有用户信用分</h5>
            <div style="display: flex; gap: 10px;">
              <el-input
                v-model="userScoreQuery.keyword"
                placeholder="搜索用户名"
                style="width: 200px;"
                clearable
                @change="loadUserCreditScores"
              />
              <el-select
                v-model="userScoreQuery.creditLevel"
                placeholder="筛选等级"
                style="width: 120px;"
                clearable
                @change="loadUserCreditScores"
              >
                <el-option label="AAA" value="AAA" />
                <el-option label="AA" value="AA" />
                <el-option label="A" value="A" />
                <el-option label="B" value="B" />
                <el-option label="C" value="C" />
                <el-option label="D" value="D" />
              </el-select>
              <el-button @click="loadUserCreditScores" :loading="userScoresLoading">刷新</el-button>
            </div>
          </div>
          
          <el-table :data="userCreditScores" v-loading="userScoresLoading" style="width: 100%;">
            <el-table-column prop="userId" label="用户ID" width="80" />
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="currentScore" label="当前信用分" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getScoreType(scope.row.currentScore)">
                  {{ scope.row.currentScore }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creditLevel" label="信用等级" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getLevelType(scope.row.creditLevel)">
                  {{ scope.row.creditLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rewardPoints" label="奖励积分" width="100" align="center" />
            <el-table-column prop="totalReports" label="总申报次数" width="100" align="center" />
            <el-table-column prop="approvedReports" label="通过次数" width="100" align="center" />
            <el-table-column prop="lastScoreUpdate" label="最后更新时间" width="160" />
          </el-table>
          
          <div style="text-align: right; margin-top: 20px;">
            <el-pagination
              v-model:current-page="userScoreQuery.page"
              v-model:page-size="userScoreQuery.size"
              :page-sizes="[10, 20, 50]"
              :total="userScoreTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleUserScoreSizeChange"
              @current-change="handleUserScoreCurrentChange"
            />
          </div>
        </div>
        
        <div style="margin-top: 20px;">
          <h5>任务说明</h5>
          <p style="color: #606266; font-size: 14px;">
            • 每周定期生成用户信用评分记录<br/>
            • 将超过70分的信用分按比例转化为奖励积分<br/>
            • 超过100分的信用分会被重置为100分上限
          </p>
        </div>
      </div>

      <!-- 信用等级重算任务效果 -->
      <div v-if="currentStatsTaskType === 'recalculateCredit' && executionStats.recalculateCredit">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-number">{{ executionStats.recalculateCredit.highCreditUserCount || 0 }}</div>
              <div class="stat-label">高信用用户数(AA/AAA)</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-number">{{ executionStats.recalculateCredit.lastExecutionTime || '暂未执行' }}</div>
              <div class="stat-label">最后执行时间</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-item">
              <div class="stat-number">已重算</div>
              <div class="stat-label">执行状态</div>
            </div>
          </el-col>
        </el-row>

        <div v-if="executionStats.recalculateCredit.levelDistribution && executionStats.recalculateCredit.levelDistribution.levelCount" style="margin-top: 20px;">
          <h5>信用等级分布</h5>
          <el-row :gutter="10">
            <template v-for="(count, level) in executionStats.recalculateCredit.levelDistribution.levelCount" :key="level">
              <el-col :span="4">
                <div class="level-stat">
                  <div class="level-name">{{ level }}</div>
                  <div class="level-count">{{ count }}人</div>
                  <div class="level-percentage">{{ executionStats.recalculateCredit.levelDistribution.levelPercentage[level] }}%</div>
                </div>
              </el-col>
            </template>
          </el-row>
          <div style="margin-top: 15px; text-align: center;">
            <el-tag type="info">总用户数：{{ executionStats.recalculateCredit.levelDistribution.totalUsers }}人</el-tag>
          </div>
        </div>
        
        <div style="margin-top: 20px;">
          <h5>任务说明</h5>
          <p style="color: #606266; font-size: 14px;">
            • 重新计算所有用户的信用等级<br/>
            • 根据当前信用分数更新用户等级(D/C/B/A/AA/AAA)<br/>
            • 确保信用等级与分数保持一致性
          </p>
        </div>
      </div>

      <template #footer>
        <el-button @click="statsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
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

// 任务配置相关数据
const taskConfig = ref<{
  scoreGeneration?: {
    name?: string
    cron?: string
    description?: string
    enabled?: boolean
  }
  recalculateCredit?: {
    name?: string
    cron?: string
    description?: string
    enabled?: boolean
  }
}>({})

// 配置对话框相关
const configDialogVisible = ref(false)
const currentTaskType = ref('')
const tempConfig = ref({
  cron: '',
  enabled: true
})

// 执行效果统计对话框
const statsDialogVisible = ref(false)
const executionStats = ref<any>({})
const currentStatsTaskType = ref('')

// 用户信用分查询相关数据
const userCreditScores = ref([])
const userScoresLoading = ref(false)
const userScoreTotal = ref(0)
const userScoreQuery = ref({
  page: 1,
  size: 20,
  keyword: '',
  creditLevel: ''
})

// 评分记录相关数据
const scoreRecords = ref([])
const recordStats = ref<{
  totalRecords?: number
  thisWeekRecords?: number
  totalRewardPoints?: number
  latestPeriod?: string
}>({})
const recordsLoading = ref(false)
const recordTotal = ref(0)
const recordQuery = ref({
  page: 1,
  size: 20,
  period: ''
})

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

// 加载评分记录
const loadScoreRecords = async () => {
  try {
    recordsLoading.value = true
    
    // 加载统计信息
    const statsResponse = await request.get('/system/tasks/score-records/statistics')
    recordStats.value = statsResponse.data || {}
    
    // 加载记录列表
    const recordsResponse = await request.get('/system/tasks/score-records', {
      params: recordQuery.value
    })
    
    scoreRecords.value = recordsResponse.data.records || []
    recordTotal.value = recordsResponse.data.total || 0
    
  } catch (error: any) {
    ElMessage.error(error.message || '加载评分记录失败')
  } finally {
    recordsLoading.value = false
  }
}

// 获取分数类型
const getScoreType = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 70) return 'primary'
  if (score >= 60) return 'warning'
  return 'danger'
}

// 查看记录详情
const viewRecordDetail = (record: any) => {
  ElMessage.info(`查看用户 ${record.userName} 的评分记录详情`)
  // 这里可以打开详情对话框
}

// 处理分页大小变化
const handleRecordSizeChange = (newSize: number) => {
  recordQuery.value.size = newSize
  recordQuery.value.page = 1
  loadScoreRecords()
}

// 处理页码变化
const handleRecordCurrentChange = (newPage: number) => {
  recordQuery.value.page = newPage
  loadScoreRecords()
}

// 解析cron表达式为可读文本
const parseCronExpression = (cron: string) => {
  const cronMap: { [key: string]: string } = {
    '0 0 2 * * MON': '每周一凌晨2点',
    '0 0 3 * * *': '每天凌晨3点',
    '0 0 1 * * *': '每天凌晨1点',
    '0 0 4 * * *': '每天凌晨4点',
    '0 0 2 * * SUN': '每周日凌晨2点',
    '0 0 2 * * TUE': '每周二凌晨2点',
    '0 0 2 * * WED': '每周三凌晨2点',
    '0 0 2 * * THU': '每周四凌晨2点',
    '0 0 2 * * FRI': '每周五凌晨2点',
    '0 0 2 * * SAT': '每周六凌晨2点'
  }
  return cronMap[cron] || cron
}

// 显示配置对话框
const showConfigDialog = (taskType: string) => {
  currentTaskType.value = taskType
  const config = taskConfig.value[taskType as keyof typeof taskConfig.value]
  tempConfig.value = {
    cron: config?.cron || '',
    enabled: config?.enabled || true
  }
  configDialogVisible.value = true
}

// 显示执行效果统计
const showExecutionStats = async (taskType: string) => {
  try {
    currentStatsTaskType.value = taskType
    const response = await request.get('/system/tasks/execution-stats')
    executionStats.value = response.data || {}
    
    // 如果是信用分数计算任务，同时加载用户信用分数据
    if (taskType === 'scoreGeneration') {
      await loadUserCreditScores()
    }
    
    statsDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取执行统计失败')
  }
}

// 加载任务配置
const loadTaskConfig = async () => {
  try {
    const response = await request.get('/system/tasks/config')
    taskConfig.value = response.data || {}
  } catch (error: any) {
    ElMessage.error(error.message || '加载任务配置失败')
  }
}

// 更新任务配置
const updateTaskConfig = async () => {
  try {
    const configData = {
      [currentTaskType.value]: {
        ...taskConfig.value[currentTaskType.value as keyof typeof taskConfig.value],
        cron: tempConfig.value.cron,
        enabled: tempConfig.value.enabled
      }
    }
    
    await request.post('/system/tasks/config', configData)
    
    // 更新本地配置
    if (taskConfig.value[currentTaskType.value as keyof typeof taskConfig.value]) {
      taskConfig.value[currentTaskType.value as keyof typeof taskConfig.value]!.cron = tempConfig.value.cron
      taskConfig.value[currentTaskType.value as keyof typeof taskConfig.value]!.enabled = tempConfig.value.enabled
    }
    
    configDialogVisible.value = false
    ElMessage.success('任务配置更新成功')
  } catch (error: any) {
    ElMessage.error(error.message || '更新任务配置失败')
  }
}

// 加载用户信用分数据
const loadUserCreditScores = async () => {
  try {
    userScoresLoading.value = true
    
    const response = await request.get('/system/tasks/user-credit-scores', {
      params: userScoreQuery.value
    })
    
    userCreditScores.value = response.data.records || []
    userScoreTotal.value = response.data.total || 0
    
  } catch (error: any) {
    ElMessage.error(error.message || '加载用户信用分失败')
  } finally {
    userScoresLoading.value = false
  }
}

// 获取信用等级标签类型
const getLevelType = (level: string) => {
  const levelMap: { [key: string]: string } = {
    'AAA': 'success',
    'AA': 'success', 
    'A': 'primary',
    'B': 'warning',
    'C': 'danger',
    'D': 'danger'
  }
  return levelMap[level] || 'info'
}

// 处理用户信用分页大小变化
const handleUserScoreSizeChange = (newSize: number) => {
  userScoreQuery.value.size = newSize
  userScoreQuery.value.page = 1
  loadUserCreditScores()
}

// 处理用户信用分页码变化
const handleUserScoreCurrentChange = (newPage: number) => {
  userScoreQuery.value.page = newPage
  loadUserCreditScores()
}

// 页面加载时获取基本信息
onMounted(() => {
  loadScoreRecords()
  loadTaskConfig()
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

.records-card {
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  color: #303133;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  margin-top: 5px;
  font-size: 14px;
  color: #909399;
}

.pagination-wrapper {
  text-align: right;
  margin-top: 20px;
}

.reward-positive {
  color: #67C23A;
  font-weight: 600;
}

.reward-zero {
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 任务配置区域样式 */
.task-config-section {
  margin-bottom: 20px;
}

.config-card {
  height: 220px;
}

.config-header {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.config-header > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.config-content {
  padding: 10px 0;
}

.config-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

.config-item label {
  width: 80px;
  color: #606266;
  margin-right: 10px;
}

.config-item span {
  margin-right: 10px;
  color: #303133;
}

.config-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

/* 统计对话框样式 */
.level-stat {
  text-align: center;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
}

.level-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.level-count {
  color: #409eff;
  font-size: 18px;
  font-weight: 600;
}

.level-percentage {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}
</style> 