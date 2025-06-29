<template>
  <div class="credit-profiles-container">
    <!-- 个人信用档案卡片 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>我的信用档案</span>
              <el-button 
                v-if="userStore.isAdmin" 
                type="primary" 
                @click="showUserSelectDialog = true"
              >
                查看其他用户
              </el-button>
            </div>
          </template>
          
          <div class="profile-content" v-if="currentProfile">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="score-circle">
                  <el-progress 
                    type="circle" 
                    :percentage="getScorePercentage(currentProfile.currentScore)"
                    :width="120"
                    :stroke-width="8"
                    :color="getScoreColor(currentProfile.currentScore)"
                  >
                    <div class="score-content">
                      <div class="score-number">{{ currentProfile.currentScore }}</div>
                      <div class="score-label">信用分</div>
                    </div>
                  </el-progress>
                </div>
              </el-col>
              
              <el-col :span="8">
                <div class="level-info">
                  <div class="level-badge">
                    <el-tag 
                      :type="getLevelType(currentProfile.creditLevel)" 
                      size="large"
                      effect="dark"
                    >
                      {{ currentProfile.creditLevel }}级
                    </el-tag>
                  </div>
                  <div class="level-desc">{{ getLevelDescription(currentProfile.creditLevel) }}</div>
                  <div class="reward-points">
                    <el-icon><Star /></el-icon>
                    奖励积分：{{ currentProfile.rewardPoints }}
                  </div>
                </div>
              </el-col>
              
              <el-col :span="8">
                <div class="stats-info">
                  <div class="stat-item">
                    <div class="stat-number">{{ currentProfile.totalReports }}</div>
                    <div class="stat-label">总上报次数</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-number">{{ currentProfile.approvedReports }}</div>
                    <div class="stat-label">通过次数</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-number">{{ getApprovalRate() }}%</div>
                    <div class="stat-label">通过率</div>
                  </div>
                </div>
              </el-col>
            </el-row>
            
            <!-- 进度提示 -->
            <div class="progress-info" v-if="stats.nextLevel && stats.nextLevel !== currentProfile.creditLevel">
              <div class="progress-text">
                距离下一等级 {{ stats.nextLevel }} 还需 {{ stats.scoreToNext }} 分
              </div>
              <el-progress 
                :percentage="getProgressPercentage()" 
                :show-text="false"
                :stroke-width="6"
                :color="getLevelColor(stats.nextLevel)"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据统计区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>评分历史趋势</span>
          </template>
          <div id="scoreChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>信用等级分布</span>
          </template>
          <div id="levelChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 排行榜和管理功能 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>信用排行榜</span>
          </template>
          <el-table :data="rankingData" style="width: 100%">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="currentScore" label="信用分" width="80" />
            <el-table-column prop="creditLevel" label="等级" width="80">
              <template #default="scope">
                <el-tag :type="getLevelType(scope.row.creditLevel)">
                  {{ scope.row.creditLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rewardPoints" label="奖励积分" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>管理功能</span>
          </template>
          <div class="management-actions">
            <el-button 
              type="primary" 
              @click="recalculateCurrentUserCredit"
              :loading="recalculateLoading"
            >
              重新计算信用
            </el-button>
            
            <!-- <el-button 
              v-if="userStore.isAdmin"
              type="success" 
              @click="generateScoreRecord"
              :loading="generateLoading"
            >
              生成周期评分
            </el-button> -->
            
            <el-button 
              v-if="userStore.isAdmin"
              type="warning" 
              @click="batchGenerateScore"
              :loading="batchLoading"
            >
              批量生成评分
              <el-tooltip content="为所有用户生成当前周期的信用评分记录，用于周期性评估和排名更新" placement="top">
                <el-icon style="margin-left: 5px;"><QuestionFilled /></el-icon>
              </el-tooltip>
            </el-button>
          </div>
          
          <!-- 信用等级说明 -->
          <div class="level-guide">
            <h4>信用等级说明</h4>
            <div class="level-item" v-for="level in levelGuide" :key="level.name">
              <el-tag :type="level.type" size="small">{{ level.name }}</el-tag>
              <span class="level-range">{{ level.range }}</span>
              <span class="level-desc">{{ level.description }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户选择对话框 -->
    <el-dialog v-model="showUserSelectDialog" title="选择用户" width="500px">
      <el-form>
        <el-form-item label="选择用户">
          <el-select v-model="selectedUserId" placeholder="请选择用户" style="width: 100%">
            <el-option 
              v-for="user in userOptions" 
              :key="user.id" 
              :label="user.realName" 
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUserSelectDialog = false">取消</el-button>
          <el-button type="primary" @click="loadUserProfile">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 用户信用档案查询（管理员功能） -->
    <el-card v-if="userStore.isAdmin" style="margin-top: 20px;">
      <template #header>
        <span>用户信用档案查询</span>
      </template>
      
      <div class="query-section">
        <el-form :model="queryForm" inline>
          <el-form-item label="搜索用户">
            <el-input 
              v-model="queryForm.keyword" 
              placeholder="输入用户名或真实姓名"
              style="width: 200px;"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="信用等级">
            <el-select v-model="queryForm.creditLevel" placeholder="请选择" style="width: 120px;">
              <el-option label="全部" value="" />
              <el-option v-for="level in levelGuide" :key="level.name" :label="level.name" :value="level.name" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table 
        :data="userProfiles" 
        v-loading="queryLoading"
        style="width: 100%"
      >
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="currentScore" label="当前信用分" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getScoreTagType(row.currentScore)">
              {{ row.currentScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creditLevel" label="信用等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.creditLevel)">
              {{ row.creditLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rewardPoints" label="奖励积分" width="120" align="center" />
        <el-table-column prop="totalReports" label="总上报次数" width="120" align="center" />
        <el-table-column prop="approvedReports" label="通过次数" width="120" align="center" />
        <el-table-column prop="lastScoreUpdate" label="最后更新" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.lastScoreUpdate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewUserProfile(row.userId)">查看详情</el-button>
            <el-button size="small" type="warning" @click="recalculateCredit(row.userId)">重算分数</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="showUserDetailDialog" title="用户详情" width="800px">
      <div class="user-detail-content" v-if="selectedUserProfile">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="score-circle">
              <el-progress 
                type="circle" 
                :percentage="getScorePercentage(selectedUserProfile.currentScore)"
                :width="120"
                :stroke-width="8"
                :color="getScoreColor(selectedUserProfile.currentScore)"
              >
                <div class="score-content">
                  <div class="score-number">{{ selectedUserProfile.currentScore }}</div>
                  <div class="score-label">信用分</div>
                </div>
              </el-progress>
            </div>
          </el-col>
          
          <el-col :span="12">
            <div class="level-info">
              <div class="level-badge">
                <el-tag 
                  :type="getLevelType(selectedUserProfile.creditLevel)" 
                  size="large"
                  effect="dark"
                >
                  {{ selectedUserProfile.creditLevel }}级
                </el-tag>
              </div>
              <div class="level-desc">{{ getLevelDescription(selectedUserProfile.creditLevel) }}</div>
              <div class="reward-points">
                <el-icon><Star /></el-icon>
                奖励积分：{{ selectedUserProfile.rewardPoints }}
              </div>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <div class="stats-info">
              <div class="stat-item">
                <div class="stat-number">{{ selectedUserProfile.totalReports }}</div>
                <div class="stat-label">总上报次数</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ selectedUserProfile.approvedReports }}</div>
                <div class="stat-label">通过次数</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ getSelectedUserApprovalRate() }}%</div>
                <div class="stat-label">通过率</div>
              </div>
            </div>
          </el-col>
          
          <el-col :span="12">
            <div class="progress-info" v-if="selectedUserStats.nextLevel && selectedUserStats.nextLevel !== selectedUserProfile.creditLevel">
              <div class="progress-text">
                距离下一等级 {{ selectedUserStats.nextLevel }} 还需 {{ selectedUserStats.scoreToNext }} 分
              </div>
              <el-progress 
                :percentage="getSelectedUserProgressPercentage()" 
                :show-text="false"
                :stroke-width="6"
                :color="getLevelColor(selectedUserStats.nextLevel)"
              />
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="24">
            <div class="score-history">
              <span class="score-history-title">信用分数历史趋势</span>
              <div id="userDetailChart" style="height: 300px;"></div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, QuestionFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import * as echarts from 'echarts'

interface UserCreditProfile {
  id: number
  userId: number
  currentScore: number
  rewardPoints: number
  creditLevel: string
  totalReports: number
  approvedReports: number
  realName?: string
}

interface CreditStats {
  currentScore: number
  creditLevel: string
  rewardPoints: number
  totalReports: number
  approvedReports: number
  approvalRate: number
  nextLevel: string
  nextLevelScore: number
  scoreToNext: number
}

const userStore = useUserStore()

// 响应式数据
const currentProfile = ref<UserCreditProfile | null>(null)
const stats = ref<CreditStats>({} as CreditStats)
const rankingData = ref<UserCreditProfile[]>([])
const scoreHistory = ref([])
const levelDistribution = ref<{ levelCount?: Record<string, number> }>({})
const showUserSelectDialog = ref(false)
const showUserDetailDialog = ref(false)
const selectedUserId = ref<number | null>(null)
const selectedUserProfile = ref<UserCreditProfile | null>(null)
const selectedUserStats = ref<CreditStats>({} as CreditStats)
const selectedUserScoreHistory = ref([])
const userList = ref([])
const userOptions = ref<any[]>([])

// 分页查询相关
const userProfiles = ref<any[]>([])
const queryLoading = ref(false)
const total = ref(0)

const queryForm = reactive({
  page: 1,
  size: 10,
  keyword: '',
  creditLevel: ''
})

// 加载状态
const recalculateLoading = ref(false)

const batchLoading = ref(false)

// 等级指南
const levelGuide = [
  { name: 'AAA', type: 'success', range: '120+分', description: '信用优秀' },
  { name: 'AA', type: 'success', range: '100-119分', description: '信用良好' },
  { name: 'A', type: 'primary', range: '80-99分', description: '信用合格' },
  { name: 'B', type: 'warning', range: '60-79分', description: '信用一般' },
  { name: 'C', type: 'danger', range: '40-59分', description: '信用较差' },
  { name: 'D', type: 'danger', range: '0-39分', description: '信用很差' }
]

// 方法
const loadCurrentUserProfile = async () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return
    
    const response = await request.get(`/user-credit-profile/current?userId=${userId}`)
    currentProfile.value = response.data
    
    // 加载统计数据
    const statsResponse = await request.get(`/user-credit-profile/${userId}/stats`)
    stats.value = statsResponse.data
  } catch (error) {
    ElMessage.error('加载用户档案失败')
  }
}

const loadUserProfile = async () => {
  if (!selectedUserId.value) return
  
  try {
    const response = await request.get(`/user-credit-profile/${selectedUserId.value}`)
    currentProfile.value = response.data
    
    const statsResponse = await request.get(`/user-credit-profile/${selectedUserId.value}/stats`)
    stats.value = statsResponse.data
    
    showUserSelectDialog.value = false
  } catch (error) {
    ElMessage.error('加载用户档案失败')
  }
}

const loadRanking = async () => {
  try {
    const response = await request.get('/user-credit-profile/ranking?limit=10')
    rankingData.value = response.data || []
  } catch (error) {
    ElMessage.error('加载排行榜失败')
  }
}

const loadLevelDistribution = async () => {
  try {
    const response = await request.get('/user-credit-profile/level-distribution')
    levelDistribution.value = response.data
    
    nextTick(() => {
      initLevelChart()
    })
  } catch (error) {
    ElMessage.error('加载等级分布失败')
  }
}

const loadScoreHistory = async () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return
    
    const response = await request.get(`/user-credit-profile/${userId}/score-history`)
    scoreHistory.value = response.data || []
    
    nextTick(() => {
      initScoreChart()
    })
  } catch (error) {
    ElMessage.error('加载评分历史失败')
  }
}

const loadUserList = async () => {
  if (!userStore.isAdmin) return
  
  try {
    const response = await request.get('/user/list', {
      params: { size: 1000 } // 获取所有用户
    })
    userList.value = response.data.records || []
    // 为用户选择下拉框准备数据
    userOptions.value = userList.value.filter((user: any) => user.role !== 'ADMIN')
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

const recalculateCredit = async (userId: number) => {
  try {
    recalculateLoading.value = true
    await request.post(`/user-credit-profile/${userId}/recalculate`)
    ElMessage.success('重新计算完成')
    await loadCurrentUserProfile()
  } catch (error) {
    ElMessage.error('重新计算失败')
  } finally {
    recalculateLoading.value = false
  }
}

const recalculateCurrentUserCredit = async () => {
  const userId = currentProfile.value?.userId || userStore.userInfo?.id
  if (!userId) {
    ElMessage.error('无法获取用户ID')
    return
  }
  await recalculateCredit(userId)
}



const batchGenerateScore = async () => {
  batchLoading.value = true
  try {
    const period = getCurrentPeriod()
    await request.post(`/user-credit-profile/batch-generate-score?period=${period}`)
    ElMessage.success('批量生成评分记录完成')
    // 重新加载数据以显示更新后的结果
    await loadCurrentUserProfile()
    await loadRanking()
    await loadUserProfiles()
  } catch (error) {
    ElMessage.error('批量生成评分记录失败')
  } finally {
    batchLoading.value = false
  }
}

// 辅助方法
const getScorePercentage = (score: number) => {
  return Math.min(score / 120 * 100, 100)
}

const getScoreColor = (score: number) => {
  if (score >= 100) return '#67C23A'
  if (score >= 80) return '#409EFF'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

const getLevelType = (level: string) => {
  const typeMap: { [key: string]: string } = {
    'AAA': 'success',
    'AA': 'success',
    'A': 'primary',
    'B': 'warning',
    'C': 'danger',
    'D': 'danger'
  }
  return typeMap[level] || 'info'
}

const getLevelColor = (level: string) => {
  const colorMap: { [key: string]: string } = {
    'AAA': '#67C23A',
    'AA': '#67C23A',
    'A': '#409EFF',
    'B': '#E6A23C',
    'C': '#F56C6C',
    'D': '#F56C6C'
  }
  return colorMap[level] || '#909399'
}

const getLevelDescription = (level: string) => {
  const descMap: { [key: string]: string } = {
    'AAA': '信用优秀，享受最高优惠',
    'AA': '信用良好，享受多项优惠',
    'A': '信用合格，享受部分优惠',
    'B': '信用一般，享受基础服务',
    'C': '信用较差，需要改进',
    'D': '信用很差，需要重点改进'
  }
  return descMap[level] || '未知等级'
}

const getApprovalRate = () => {
  if (!currentProfile.value || currentProfile.value.totalReports === 0) return 0
  return Math.round(currentProfile.value.approvedReports / currentProfile.value.totalReports * 100)
}

const getSelectedUserApprovalRate = () => {
  if (!selectedUserProfile.value || selectedUserProfile.value.totalReports === 0) return 0
  return Math.round(selectedUserProfile.value.approvedReports / selectedUserProfile.value.totalReports * 100)
}

const getProgressPercentage = () => {
  if (!stats.value.nextLevelScore || !stats.value.scoreToNext) return 0
  const current = stats.value.nextLevelScore - stats.value.scoreToNext
  const total = stats.value.nextLevelScore
  return Math.round(current / total * 100)
}

const getSelectedUserProgressPercentage = () => {
  if (!selectedUserStats.value.nextLevelScore || !selectedUserStats.value.scoreToNext) return 0
  const current = selectedUserStats.value.nextLevelScore - selectedUserStats.value.scoreToNext
  const total = selectedUserStats.value.nextLevelScore
  return Math.round(current / total * 100)
}

const getCurrentPeriod = () => {
  const now = new Date()
  const weekStart = new Date(now.setDate(now.getDate() - now.getDay() + 1))
  return weekStart.toISOString().split('T')[0]
}

const initScoreChart = () => {
  const chartElement = document.getElementById('scoreChart')
  if (!chartElement || !scoreHistory.value.length) return
  
  const chart = echarts.init(chartElement)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: scoreHistory.value.map((item: any) => item.period)
    },
    yAxis: {
      type: 'value',
      name: '分数'
    },
    series: [{
      name: '信用分数',
      type: 'line',
      data: scoreHistory.value.map((item: any) => item.score),
      smooth: true,
      itemStyle: {
        color: '#409EFF'
      }
    }]
  }
  
  chart.setOption(option)
}

const initLevelChart = () => {
  const chartElement = document.getElementById('levelChart')
  if (!chartElement || !levelDistribution.value.levelCount) return
  
  const chart = echarts.init(chartElement)
  
  const data = Object.entries(levelDistribution.value.levelCount).map(([key, value]) => ({
    name: key + '级',
    value: value
  }))
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    series: [{
      name: '信用等级',
      type: 'pie',
      radius: '50%',
      data: data
    }]
  }
  
  chart.setOption(option)
}

// 分页查询方法
const handleQuery = async () => {
  queryForm.page = 1
  await loadUserProfiles()
}

const resetQuery = () => {
  Object.assign(queryForm, {
    page: 1,
    size: 10,
    keyword: '',
    creditLevel: ''
  })
  loadUserProfiles()
}

const handleSizeChange = (newSize: number) => {
  queryForm.size = newSize
  queryForm.page = 1
  loadUserProfiles()
}

const handleCurrentChange = (newPage: number) => {
  queryForm.page = newPage
  loadUserProfiles()
}

const loadUserProfiles = async () => {
  try {
    queryLoading.value = true
    const response = await request.get('/user-credit-profile/list', {
      params: queryForm
    })
    userProfiles.value = response.data.records || []
    total.value = response.data.total || 0
  } catch (error) {
    console.error('加载用户档案失败:', error)
    ElMessage.error('加载用户档案失败')
    userProfiles.value = []
    total.value = 0
  } finally {
    queryLoading.value = false
  }
}

const viewUserProfile = async (userId: number) => {
  try {
    selectedUserId.value = userId
    
    // 获取用户档案
    const profileResponse = await request.get(`/user-credit-profile/current?userId=${userId}`)
    selectedUserProfile.value = profileResponse.data
    
    // 获取用户统计数据
    const statsResponse = await request.get(`/user-credit-profile/${userId}/stats`)
    selectedUserStats.value = statsResponse.data
    
    // 获取用户积分历史
    const historyResponse = await request.get(`/user-credit-profile/${userId}/score-history`)
    selectedUserScoreHistory.value = historyResponse.data || []
    
    // 显示详情对话框
    showUserDetailDialog.value = true
    
    // 等待DOM更新后初始化图表
    nextTick(() => {
      initUserDetailChart()
    })
  } catch (error) {
    ElMessage.error('获取用户详情失败')
  }
}

const initUserDetailChart = () => {
  const chartElement = document.getElementById('userDetailChart')
  if (!chartElement || !selectedUserScoreHistory.value.length) return
  
  const chart = echarts.init(chartElement)
  
  const option = {
    title: {
      text: '积分历史趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params: any) {
        const data = params[0]
        return `${data.name}<br/>积分: ${data.value}`
      }
    },
    xAxis: {
      type: 'category',
      data: selectedUserScoreHistory.value.map((item: any) => item.period)
    },
    yAxis: {
      type: 'value',
      name: '积分'
    },
    series: [{
      name: '积分',
      type: 'line',
      data: selectedUserScoreHistory.value.map((item: any) => item.score),
      smooth: true,
      itemStyle: {
        color: '#409EFF'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0, color: 'rgba(64, 158, 255, 0.3)'
          }, {
            offset: 1, color: 'rgba(64, 158, 255, 0.1)'
          }]
        }
      }
    }]
  }
  
  chart.setOption(option)
}

const getScoreTagType = (score: number) => {
  if (score >= 100) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 60) return 'warning'
  return 'danger'
}

const getLevelTagType = (level: string) => {
  const typeMap: Record<string, string> = {
    'AAA': 'success',
    'AA': 'success',
    'A': 'primary',
    'B': 'warning',
    'C': 'danger',
    'D': 'danger'
  }
  return typeMap[level] || 'info'
}

const formatDateTime = (datetime: string) => {
  if (!datetime) return '-'
  return new Date(datetime).toLocaleString()
}

// 生命周期
onMounted(() => {
  loadCurrentUserProfile()
  loadRanking()
  loadLevelDistribution()
  loadScoreHistory()
  loadUserList()
  loadUserProfiles()
})
</script>

<style lang="scss" scoped>
.credit-profiles-container {
  .profile-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .profile-content {
      .score-circle {
        text-align: center;
        
        .score-content {
          .score-number {
            font-size: 24px;
            font-weight: bold;
            color: #409EFF;
          }
          
          .score-label {
            font-size: 12px;
            color: #999;
          }
        }
      }
      
      .level-info {
        text-align: center;
        
        .level-badge {
          margin-bottom: 10px;
          
          .el-tag {
            font-size: 16px;
            padding: 8px 16px;
          }
        }
        
        .level-desc {
          color: #666;
          margin-bottom: 15px;
        }
        
        .reward-points {
          color: #E6A23C;
          font-weight: bold;
          
          .el-icon {
            margin-right: 5px;
          }
        }
      }
      
      .stats-info {
        .stat-item {
          text-align: center;
          margin-bottom: 15px;
          
          .stat-number {
            font-size: 20px;
            font-weight: bold;
            color: #409EFF;
          }
          
          .stat-label {
            font-size: 12px;
            color: #999;
          }
        }
      }
      
      .progress-info {
        margin-top: 20px;
        padding: 15px;
        background-color: #f5f7fa;
        border-radius: 4px;
        
        .progress-text {
          margin-bottom: 10px;
          color: #666;
          text-align: center;
        }
      }
    }
  }
  
  .management-actions {
    margin-bottom: 20px;
    
    .el-button {
      margin-right: 10px;
      margin-bottom: 10px;
    }
  }
  
  .level-guide {
    h4 {
      margin-bottom: 15px;
      color: #333;
    }
    
    .level-item {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      
      .el-tag {
        margin-right: 10px;
        min-width: 50px;
        text-align: center;
      }
      
      .level-range {
        margin-right: 15px;
        color: #666;
        min-width: 80px;
      }
      
      .level-desc {
        color: #999;
      }
    }
  }
}
</style> 