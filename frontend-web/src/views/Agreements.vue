<template>
  <div class="agreements-container">
    <!-- 学习进度卡片 -->
    <el-card class="progress-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><TrendCharts /></el-icon>
          <span>学习进度</span>
        </div>
      </template>
      <div class="progress-content">
        <div class="progress-stats">
          <div class="stat-item">
            <div class="stat-value">{{ learningStats.completionRate }}%</div>
            <div class="stat-label">完成率</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ learningStats.viewedAgreements }}</div>
            <div class="stat-label">已学习</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ learningStats.totalAgreements }}</div>
            <div class="stat-label">总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ learningStats.questionCount }}</div>
            <div class="stat-label">问答次数</div>
          </div>
        </div>
        <el-progress 
          :percentage="learningStats.completionRate" 
          :color="progressColor"
          :stroke-width="8"
          class="progress-bar"
        />
      </div>
    </el-card>

    <!-- AI智能问答 -->
    <el-card class="ai-chat-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><ChatDotSquare /></el-icon>
          <span>AI智能问答</span>
        </div>
      </template>
      <div class="ai-chat-content">
        <div class="chat-messages" ref="chatMessagesRef">
          <div 
            v-for="(message, index) in chatMessages" 
            :key="index"
            :class="['message', message.type]"
          >
            <div class="message-content">
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
        </div>
        <div class="chat-input">
          <el-input
            v-model="questionInput"
            placeholder="请输入您的问题，我会根据社区公约为您解答..."
            @keyup.enter="askQuestion"
            :disabled="isAsking"
          >
            <template #append>
              <el-button 
                type="primary" 
                @click="askQuestion"
                :loading="isAsking"
                :disabled="!questionInput.trim()"
              >
                发送
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-card>

    <!-- 公约列表 -->
    <el-card class="agreements-list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>社区公约</span>
          <div class="header-actions">
            <el-button 
              v-if="userStore.hasRole('ADMIN')"
              type="info" 
              @click="initializeKnowledge"
              :loading="isInitializingKnowledge"
            >
              <el-icon><Setting /></el-icon>
              初始化AI知识库
            </el-button>
            <el-button 
              v-if="userStore.hasRole('ADMIN') || userStore.hasRole('MAINTAINER')"
              type="primary" 
              @click="showCreateDialog = true"
            >
              <el-icon><Plus /></el-icon>
              添加公约
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="agreements-list">
        <div 
          v-for="agreement in agreements" 
          :key="agreement.id"
          class="agreement-item"
          @click="viewAgreement(agreement)"
        >
          <div class="agreement-header">
            <div class="agreement-title">
              <el-icon><Document /></el-icon>
              {{ agreement.title }}
              <!-- 学习状态标识 -->
              <div class="learning-status">
                <el-tag 
                  v-if="agreement.isCompleted" 
                  type="success" 
                  size="small"
                  effect="dark"
                >
                  <el-icon><Check /></el-icon>
                  已完成学习
                </el-tag>
                <el-tag 
                  v-else-if="agreement.isViewed" 
                  type="warning" 
                  size="small"
                  effect="plain"
                >
                  已查看
                </el-tag>
                <el-tag 
                  v-else 
                  type="info" 
                  size="small"
                  effect="plain"
                >
                  未学习
                </el-tag>
              </div>
            </div>
            <div class="agreement-meta">
              <el-tag 
                :type="agreement.contentType === 'TEXT' ? 'info' : 'warning'"
                size="small"
              >
                {{ agreement.contentType === 'TEXT' ? '文本' : '视频' }}
              </el-tag>
              <span class="agreement-order">序号: {{ agreement.orderNum }}</span>
            </div>
          </div>
          <div class="agreement-summary">
            {{ getAgreementSummary(agreement.content) }}
          </div>
          <div class="agreement-actions">
            <el-button 
              type="primary" 
              size="small" 
              @click.stop="viewAgreement(agreement)"
            >
              查看详情
            </el-button>
            <el-button 
              v-if="userStore.hasRole('ADMIN') || userStore.hasRole('MAINTAINER')"
              type="warning" 
              size="small" 
              @click.stop="editAgreement(agreement)"
            >
              编辑
            </el-button>
            <el-button 
              v-if="userStore.hasRole('ADMIN') || userStore.hasRole('MAINTAINER')"
              type="danger" 
              size="small" 
              @click.stop="deleteAgreement(agreement.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 公约详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      :title="currentAgreement?.title"
      width="60%"
      class="agreement-detail-dialog"
    >
      <div v-if="currentAgreement" class="agreement-detail">
        <div class="agreement-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="内容类型">
              <el-tag :type="currentAgreement.contentType === 'TEXT' ? 'info' : 'warning'">
                {{ currentAgreement.contentType === 'TEXT' ? '文本内容' : '视频内容' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="排序号">
              {{ currentAgreement.orderNum }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentAgreement.isActive ? 'success' : 'danger'">
                {{ currentAgreement.isActive ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(currentAgreement.createdTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="agreement-content">
          <h4>公约内容：</h4>
          <div v-if="currentAgreement.contentType === 'TEXT'" class="text-content">
            <div v-html="formatContent(currentAgreement.content)"></div>
          </div>
          <div v-else-if="currentAgreement.contentType === 'VIDEO'" class="video-content">
            <video 
              v-if="currentAgreement.videoPath" 
              :src="currentAgreement.videoPath" 
              controls 
              width="100%"
            ></video>
            <div class="video-description">
              <p>{{ currentAgreement.content }}</p>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
          <el-button 
            v-if="currentAgreement && !currentAgreement.isCompleted"
            type="primary" 
            @click="markComplete(currentAgreement.id)"
            :disabled="isMarking"
          >
            标记已完成学习
          </el-button>
          <el-tag 
            v-if="currentAgreement && currentAgreement.isCompleted"
            type="success" 
            size="large"
          >
            已完成学习
          </el-tag>
        </div>
      </template>
    </el-dialog>

    <!-- 创建/编辑公约对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="isEditing ? '编辑公约' : '创建公约'"
      width="50%"
    >
      <el-form 
        ref="agreementFormRef"
        :model="agreementForm" 
        :rules="agreementRules" 
        label-width="100px"
      >
        <el-form-item label="公约标题" prop="title">
          <el-input v-model="agreementForm.title" placeholder="请输入公约标题" />
        </el-form-item>
        
        <el-form-item label="内容类型" prop="contentType">
          <el-radio-group v-model="agreementForm.contentType">
            <el-radio value="TEXT">文本内容</el-radio>
            <el-radio value="VIDEO">视频内容</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="公约内容" prop="content">
          <el-input 
            v-model="agreementForm.content" 
            type="textarea" 
            :rows="8"
            placeholder="请输入公约内容"
          />
        </el-form-item>

        <el-form-item 
          v-if="agreementForm.contentType === 'VIDEO'" 
          label="视频文件" 
          prop="videoPath"
        >
          <el-upload
            class="video-uploader"
            action="/api/files/upload"
            :data="{ businessType: 'AGREEMENT_VIDEO', userId: 1 }"
            :show-file-list="false"
            :on-success="handleVideoUploadSuccess"
            :on-error="handleVideoUploadError"
            :before-upload="beforeVideoUpload"
            accept="video/*"
          >
            <el-button type="primary" :loading="isUploading">
              <el-icon><Upload /></el-icon>
              {{ isUploading ? '上传中...' : '选择视频文件' }}
            </el-button>
            <div v-if="agreementForm.videoPath" class="upload-success">
              <el-icon><Check /></el-icon>
              文件上传成功
            </div>
          </el-upload>
          <div class="upload-tip">
            支持 mp4、mov、avi 格式，文件大小不超过 10MB
          </div>
        </el-form-item>

        <el-form-item label="排序号" prop="orderNum">
          <el-input-number 
            v-model="agreementForm.orderNum" 
            :min="1" 
            :max="999"
            placeholder="排序号"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-switch 
            v-model="agreementForm.isActive" 
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveAgreement"
            :loading="isSaving"
          >
            {{ isEditing ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, 
  ChatDotSquare, 
  TrendCharts, 
  Plus,
  Upload,
  Check,
  Setting
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

// 用户store
const userStore = useUserStore()

interface Agreement {
  id: number
  title: string
  content: string
  contentType: 'TEXT' | 'VIDEO'
  videoPath?: string
  orderNum: number
  isActive: boolean
  createdTime: string
  updatedTime: string
  isCompleted?: boolean
  isViewed?: boolean
}

// 响应式数据
const agreements = ref<Agreement[]>([])
const learningStats = ref({
  totalAgreements: 0,
  viewedAgreements: 0,
  completionRate: 0,
  questionCount: 0,
  lastStudyTime: null
})

// AI问答相关
const chatMessages = ref([
  {
    type: 'assistant',
    content: '您好！我是社区公约智能助手。请您具体描述遇到的问题，我会根据社区公约为您提供相应的解答。',
    timestamp: new Date()
  }
])
const questionInput = ref('')
const isAsking = ref(false)
const chatMessagesRef = ref()

// 公约详情
const showDetailDialog = ref(false)
const currentAgreement = ref<Agreement | null>(null)
const isMarking = ref(false)

// 创建/编辑公约
const showCreateDialog = ref(false)
const isEditing = ref(false)
const isSaving = ref(false)
const isUploading = ref(false)
const agreementFormRef = ref()

// 知识库初始化
const isInitializingKnowledge = ref(false)

interface AgreementForm {
  id?: number
  title: string
  content: string
  contentType: string
  videoPath: string
  orderNum: number
  isActive: boolean
}

const agreementForm = reactive<AgreementForm>({
  title: '',
  content: '',
  contentType: 'TEXT',
  videoPath: '',
  orderNum: 1,
  isActive: true
})

// 表单验证规则
const agreementRules = {
  title: [
    { required: true, message: '请输入公约标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公约内容', trigger: 'blur' }
  ],
  contentType: [
    { required: true, message: '请选择内容类型', trigger: 'change' }
  ]
}

// 计算属性
const progressColor = computed(() => {
  const rate = learningStats.value.completionRate
  if (rate < 30) return '#f56c6c'
  if (rate < 70) return '#e6a23c'
  return '#67c23a'
})

// 获取公约列表
const fetchAgreements = async () => {
  try {
    // 管理员获取所有公约（包括禁用的），普通用户只获取启用的
    const endpoint = userStore.hasRole('ADMIN') || userStore.hasRole('MAINTAINER') 
      ? '/agreements/admin/list' 
      : '/agreements/list'
    
    const response = await request.get(endpoint)
    agreements.value = response.data
    
    // 为每个公约获取学习状态
    await fetchAgreementsLearningStatus()
  } catch (error) {
    ElMessage.error('获取公约列表失败')
  }
}

// 获取公约学习状态
const fetchAgreementsLearningStatus = async () => {
  if (!userStore.isLoggedIn || agreements.value.length === 0) {
    return
  }
  
  try {
    // 批量获取所有公约的学习状态
    const promises = agreements.value.map(async (agreement) => {
      try {
        const response = await request.get(`/agreements/${agreement.id}/status`)
        return {
          id: agreement.id,
          isViewed: response.data.isViewed,
          isCompleted: response.data.isCompleted
        }
      } catch (error) {
        return {
          id: agreement.id,
          isViewed: false,
          isCompleted: false
        }
      }
    })
    
    const statuses = await Promise.all(promises)
    
    // 更新公约的学习状态
    agreements.value.forEach(agreement => {
      const status = statuses.find(s => s.id === agreement.id)
      if (status) {
        agreement.isViewed = status.isViewed
        agreement.isCompleted = status.isCompleted
      }
    })
  } catch (error) {
    console.error('获取学习状态失败:', error)
  }
}

// 获取学习统计
const fetchLearningStats = async () => {
  try {
    const response = await request.get('/agreements/learning/stats')
    learningStats.value = response.data
  } catch (error) {
    console.error('获取学习统计失败:', error)
  }
}

// AI问答
const askQuestion = async () => {
  if (!questionInput.value.trim()) return

  const question = questionInput.value.trim()
  
  // 添加用户问题到聊天记录
  chatMessages.value.push({
    type: 'user',
    content: question,
    timestamp: new Date()
  })

  questionInput.value = ''
  isAsking.value = true

  try {
    const response = await request.post('/agreements/ask', {
      question: question,
      sessionId: 'web-session-' + Date.now()
    })

    // 添加AI回答到聊天记录
    chatMessages.value.push({
      type: 'assistant',
      content: response.data.answer,
      timestamp: new Date(response.data.timestamp)
    })
  } catch (error) {
    chatMessages.value.push({
      type: 'assistant',
      content: '抱歉，暂时无法回答您的问题，请稍后重试。',
      timestamp: new Date()
    })
    ElMessage.error('AI问答失败')
  } finally {
    isAsking.value = false
    // 滚动到底部
    nextTick(() => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    })
  }
}

// 查看公约详情
const viewAgreement = async (agreement: Agreement) => {
  try {
    const response = await request.get(`/agreements/${agreement.id}`)
    // 后端返回的数据结构：{ agreement: Agreement, isCompleted: boolean }
    const agreementDetail = response.data.agreement
    agreementDetail.isCompleted = response.data.isCompleted
    currentAgreement.value = agreementDetail
    showDetailDialog.value = true
    
    // 更新列表中的学习状态（查看即标记为已查看）
    const listAgreement = agreements.value.find(a => a.id === agreement.id)
    if (listAgreement) {
      listAgreement.isViewed = true
      listAgreement.isCompleted = response.data.isCompleted
    }
    
    // 刷新学习统计
    await fetchLearningStats()
  } catch (error) {
    ElMessage.error('获取公约详情失败')
  }
}

// 标记完成学习
const markComplete = async (agreementId: number) => {
  isMarking.value = true
  try {
    await request.post(`/agreements/${agreementId}/complete`)
    ElMessage.success('已标记完成学习')
    
    // 更新当前公约的完成状态
    if (currentAgreement.value) {
      currentAgreement.value.isCompleted = true
    }
    
    // 更新列表中的学习状态
    const agreement = agreements.value.find(a => a.id === agreementId)
    if (agreement) {
      agreement.isCompleted = true
      agreement.isViewed = true
    }
    
    await fetchLearningStats() // 刷新学习统计
  } catch (error) {
    ElMessage.error('标记失败')
  } finally {
    isMarking.value = false
  }
}

// 编辑公约
const editAgreement = (agreement: Agreement) => {
  isEditing.value = true
  Object.assign(agreementForm, agreement)
  showCreateDialog.value = true
}

// 保存公约
const saveAgreement = async () => {
  if (!agreementFormRef.value) return
  
  await agreementFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return

    isSaving.value = true
    try {
      const url = isEditing.value 
        ? `/agreements/admin/${agreementForm.id}`
        : '/agreements/admin/create'
      
      const method = isEditing.value ? 'put' : 'post'
      await request[method](url, agreementForm)

      ElMessage.success(isEditing.value ? '更新成功' : '创建成功')
      showCreateDialog.value = false
      resetForm()
      
      // 刷新公约列表和学习统计
      await fetchAgreements()
      await fetchLearningStats()
    } catch (error) {
      ElMessage.error(isEditing.value ? '更新失败' : '创建失败')
    } finally {
      isSaving.value = false
    }
  })
}

// 删除公约
const deleteAgreement = async (agreementId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个公约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request.delete(`/agreements/admin/${agreementId}`)
    ElMessage.success('删除成功')
    
    // 刷新公约列表和学习统计
    await fetchAgreements()
    await fetchLearningStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 文件上传相关方法
const beforeVideoUpload = (file: File) => {
  const isVideo = file.type.startsWith('video/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isVideo) {
    ElMessage.error('只能上传视频文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
    return false
  }
  
  isUploading.value = true
  return true
}

const handleVideoUploadSuccess = (response: any) => {
  isUploading.value = false
  if (response.code === 200) {
    agreementForm.videoPath = response.data.fileUrl
    ElMessage.success('视频上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleVideoUploadError = () => {
  isUploading.value = false
  ElMessage.error('上传失败，请重试')
}

// 重置表单
const resetForm = () => {
  const isEditingMode = isEditing.value
  isEditing.value = false
  Object.assign(agreementForm, {
    title: isEditingMode ? '' : '邻里和睦相处公约', // 新建时设置默认标题
    content: '',
    contentType: 'TEXT',
    videoPath: '',
    orderNum: 1,
    isActive: true
  })
  if (agreementFormRef.value) {
    agreementFormRef.value.resetFields()
  }
}

// 工具函数
const getAgreementSummary = (content: string) => {
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

const formatContent = (content: string) => {
  return content.replace(/\n/g, '<br>')
}

const formatTime = (timestamp: Date | string) => {
  return new Date(timestamp).toLocaleTimeString()
}

const formatDateTime = (datetime: Date | string) => {
  return new Date(datetime).toLocaleString()
}

// 初始化知识库
const initializeKnowledge = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将重新构建AI知识库，可能需要一些时间。确定要继续吗？',
      '初始化知识库',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    isInitializingKnowledge.value = true
    await request.post('/agreements/admin/init-knowledge')
    
    ElMessage.success('知识库初始化成功！AI问答功能已更新。')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('知识库初始化失败')
    }
  } finally {
    isInitializingKnowledge.value = false
  }
}

// 初始化
onMounted(async () => {
  await fetchAgreements()
  await fetchLearningStats()
})
</script>

<style scoped>
.agreements-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.progress-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.header-actions {
  margin-left: auto;
}

.progress-content {
  padding: 20px 0;
}

.progress-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.progress-bar {
  margin-top: 10px;
}

.ai-chat-card {
  margin-bottom: 20px;
}

.ai-chat-content {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 10px;
}

.message {
  margin-bottom: 15px;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 10px;
  position: relative;
}

.message.user .message-content {
  background-color: #409eff;
  color: white;
}

.message.assistant .message-content {
  background-color: #f5f5f5;
  color: #333;
}

.message-text {
  word-wrap: break-word;
  line-height: 1.5;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 5px;
}

.video-uploader {
  margin-bottom: 10px;
}

.upload-success {
  margin-top: 10px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 5px;
}

.upload-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #999;
}

.agreements-list-card {
  margin-bottom: 20px;
}

.agreements-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.agreement-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.agreement-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.agreement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.agreement-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.learning-status {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
}

.learning-status .el-tag {
  margin-left: 8px;
}

.agreement-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agreement-order {
  font-size: 12px;
  color: #666;
}

.agreement-summary {
  color: #666;
  line-height: 1.5;
  margin-bottom: 15px;
}

.agreement-actions {
  display: flex;
  gap: 10px;
}

.agreement-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.agreement-info {
  margin-bottom: 20px;
}

.agreement-content h4 {
  margin-bottom: 10px;
  color: #333;
}

.text-content {
  line-height: 1.8;
  color: #666;
}

.video-content {
  text-align: center;
}

.video-description {
  margin-top: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
  text-align: left;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .agreements-container {
    padding: 10px;
  }
  
  .progress-stats {
    flex-wrap: wrap;
    gap: 20px;
  }
  
  .stat-item {
    flex: 1;
    min-width: 120px;
  }
  
  .agreement-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .agreement-actions {
    flex-wrap: wrap;
  }
}
</style> 