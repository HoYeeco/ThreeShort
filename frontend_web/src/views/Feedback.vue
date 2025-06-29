<template>
  <div class="feedback-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>反馈建议管理</span>
          <el-button type="primary" @click="showSubmitDialog = true">
            提交反馈
          </el-button>
        </div>
      </template>
      
      <!-- 查询表单 -->
      <el-form :model="queryForm" :inline="true" class="query-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="标题/内容"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="反馈类型">
          <el-select v-model="queryForm.type" placeholder="请选择" clearable style="width: 150px">
            <el-option
              v-for="option in feedbackTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option
              v-for="option in feedbackStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 反馈列表 -->
      <el-table :data="feedbackList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="attachmentFiles" label="附件" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.attachmentFileList && row.attachmentFileList.length > 0" color="#409EFF">
              <Paperclip />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="userStore.hasRole('ADMIN') || userStore.hasRole('MAINTAINER')"
              type="warning"
              size="small"
              @click="handleEdit(row)"
              :disabled="row.status === 'CLOSED'"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>
    
    <!-- 提交反馈对话框 -->
    <el-dialog
      v-model="showSubmitDialog"
      title="提交反馈建议"
      width="600px"
      @close="handleSubmitDialogClose"
    >
      <el-form
        ref="submitFormRef"
        :model="submitForm"
        :rules="submitRules"
        label-width="100px"
      >
        <el-form-item label="反馈类型" prop="type">
          <el-select v-model="submitForm.type" placeholder="请选择反馈类型" style="width: 100%">
            <el-option
              v-for="option in feedbackTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="反馈标题" prop="title">
          <el-input v-model="submitForm.title" placeholder="请输入反馈标题" maxlength="200" />
        </el-form-item>
        
        <el-form-item label="反馈内容" prop="content">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            placeholder="请详细描述您的反馈内容"
            :rows="5"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="附件上传">
          <el-upload
            ref="uploadRef"
            :action="uploadAction"
            :data="uploadData"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemoveFile"
            :file-list="fileList"
            list-type="picture-card"
            accept="image/*"
            multiple
            :limit="5"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showSubmitDialog = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 查看反馈对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="dialogTitle"
      width="800px"
      @close="handleDetailDialogClose"
    >
      <div v-if="currentFeedback" class="feedback-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="反馈类型">
            <el-tag :type="getTypeTagType(currentFeedback.type)">
              {{ getTypeText(currentFeedback.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentFeedback.status)">
              {{ getStatusText(currentFeedback.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">
            {{ currentFeedback.title }}
          </el-descriptions-item>
          <el-descriptions-item label="反馈内容" :span="2">
            <div class="content-text">{{ currentFeedback.content }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentFeedback.attachmentFileList?.length" label="附件" :span="2">
            <div class="attachment-list">
              <el-image
                v-for="(file, index) in currentFeedback.attachmentFileList"
                :key="index"
                :src="file"
                :preview-src-list="currentFeedback.attachmentFileList"
                fit="cover"
                class="attachment-image"
              />
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ currentFeedback.createdTime }}
          </el-descriptions-item>
          <el-descriptions-item label="处理时间">
            {{ currentFeedback.handleTime || '暂未处理' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentFeedback.handlerReply" label="处理回复" :span="2">
            <div class="reply-text">{{ currentFeedback.handlerReply }}</div>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 处理表单（管理员） -->
        <div v-if="isHandleMode" class="handle-form">
          <el-divider content-position="left">处理反馈</el-divider>
          <el-form
            ref="handleFormRef"
            :model="handleForm"
            :rules="handleRules"
            label-width="100px"
          >
            <el-form-item label="处理状态" prop="status">
              <el-select v-model="handleForm.status" placeholder="请选择处理状态" style="width: 200px">
                <el-option
                  v-for="option in feedbackStatusOptions.filter(item => item.value !== 'PENDING')"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="处理回复" prop="handlerReply">
              <el-input
                v-model="handleForm.handlerReply"
                type="textarea"
                placeholder="请输入处理回复"
                :rows="4"
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailDialog = false">取消</el-button>
          <el-button
            v-if="isHandleMode"
            type="primary"
            :loading="handleLoading"
            @click="handleProcess"
          >
            确定处理
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Paperclip } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getFeedbackList,
  getMyFeedbacks,
  getFeedbackDetail,
  submitFeedback,
  handleFeedback,

} from '@/api/feedback'
import {
  FeedbackType,
  FeedbackStatus,
  FeedbackTypeOptions,
  FeedbackStatusOptions,
  type FeedbackSuggestion,
  type FeedbackSubmitForm,
  type FeedbackQueryForm,
  type FeedbackHandleForm,
  type FileUploadResponse
} from '@/types/feedback'
import type { FormInstance, FormRules, TagProps, UploadProps, UploadFile } from 'element-plus'

const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const handleLoading = ref(false)
const showSubmitDialog = ref(false)
const showDetailDialog = ref(false)
const isHandleMode = ref(false)
const feedbackList = ref<FeedbackSuggestion[]>([])
const total = ref(0)
const currentFeedback = ref<FeedbackSuggestion | null>(null)
const fileList = ref<UploadFile[]>([])
const uploadedFiles = ref<FileUploadResponse[]>([])

// 表单引用
const submitFormRef = ref<FormInstance>()
const handleFormRef = ref<FormInstance>()

// 查询表单
const queryForm = reactive<FeedbackQueryForm>({
  page: 1,
  size: 10,
  keyword: '',
  type: undefined,
  status: undefined
})

// 提交表单
const submitForm = reactive<FeedbackSubmitForm>({
  type: FeedbackType.SUGGESTION,
  title: '',
  content: '',
  attachmentFileIds: []
})

// 处理表单
const handleForm = reactive<FeedbackHandleForm>({
  status: FeedbackStatus.PROCESSING,
  handlerReply: ''
})

// 选项数据
const feedbackTypeOptions = FeedbackTypeOptions
const feedbackStatusOptions = FeedbackStatusOptions

// 表单验证规则
const submitRules: FormRules = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [
    { required: true, message: '请输入反馈标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { max: 2000, message: '内容长度不能超过2000个字符', trigger: 'blur' }
  ]
}

const handleRules: FormRules = {
  status: [{ required: true, message: '请选择处理状态', trigger: 'change' }],
  handlerReply: [
    { required: true, message: '请输入处理回复', trigger: 'blur' },
    { max: 1000, message: '回复长度不能超过1000个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  if (isHandleMode.value) {
    return '处理反馈建议'
  }
  return '查看反馈建议'
})

const uploadAction = computed(() => {
  return '/api/files/upload'
})

const uploadData = computed(() => {
  return {
    businessType: 'FEEDBACK',
    userId: 1
  }
})

// 方法
const getTypeText = (type: FeedbackType) => {
  const option = feedbackTypeOptions.find(item => item.value === type)
  return option?.label || type
}

const getStatusText = (status: FeedbackStatus) => {
  const option = feedbackStatusOptions.find(item => item.value === status)
  return option?.label || status
}

const getTypeTagType = (type: FeedbackType): TagProps['type'] => {
  const typeMap = {
    [FeedbackType.SUGGESTION]: 'success',
    [FeedbackType.COMPLAINT]: 'danger',
    [FeedbackType.LEARNING_FEEDBACK]: 'warning',
    [FeedbackType.OTHER]: ''
  }
  return typeMap[type] as TagProps['type']
}

const getStatusTagType = (status: FeedbackStatus): TagProps['type'] => {
  const statusMap = {
    [FeedbackStatus.PENDING]: 'info',
    [FeedbackStatus.PROCESSING]: 'warning',
    [FeedbackStatus.RESOLVED]: 'success',
    [FeedbackStatus.CLOSED]: ''
  }
  return statusMap[status] as TagProps['type']
}

const loadFeedbacks = async () => {
  loading.value = true
  try {
    let response
    if (userStore.hasRole('ADMIN') || userStore.hasRole('MAINTAINER')) {
      response = await getFeedbackList(queryForm)
    } else {
      response = await getMyFeedbacks(queryForm)
    }
    
    feedbackList.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    ElMessage.error('加载反馈列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryForm.page = 1
  loadFeedbacks()
}

const resetQuery = () => {
  Object.assign(queryForm, {
    page: 1,
    size: 10,
    keyword: '',
    type: undefined,
    status: undefined
  })
  loadFeedbacks()
}

const handleView = async (feedback: FeedbackSuggestion) => {
  try {
    const response = await getFeedbackDetail(feedback.id)
    currentFeedback.value = response.data
    isHandleMode.value = false
    showDetailDialog.value = true
  } catch (error) {
    ElMessage.error('获取反馈详情失败')
  }
}

const handleEdit = async (feedback: FeedbackSuggestion) => {
  try {
    const response = await getFeedbackDetail(feedback.id)
    currentFeedback.value = response.data
    isHandleMode.value = true
    
    // 初始化处理表单
    handleForm.status = FeedbackStatus.PROCESSING
    handleForm.handlerReply = feedback.handlerReply || ''
    
    showDetailDialog.value = true
  } catch (error) {
    ElMessage.error('获取反馈详情失败')
  }
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response: any, file: UploadFile) => {
  if (response.code === 200) {
    uploadedFiles.value.push(response.data)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
    // 移除失败的文件
    const index = fileList.value.findIndex(item => item.uid === file.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
  }
}

const handleRemoveFile = (file: UploadFile) => {
  // 从已上传文件列表中移除
  const index = uploadedFiles.value.findIndex(item => item.originalName === file.name)
  if (index > -1) {
    uploadedFiles.value.splice(index, 1)
  }
}

const handleSubmit = async () => {
  if (!submitFormRef.value) return
  
  try {
    await submitFormRef.value.validate()
    
    submitLoading.value = true
    
    // 设置附件文件ID
    submitForm.attachmentFileIds = uploadedFiles.value.map(file => file.fileId)
    
    await submitFeedback(submitForm)
    
    ElMessage.success('反馈提交成功')
    showSubmitDialog.value = false
    loadFeedbacks()
  } catch (error: any) {
    if (error.fields) {
      return
    }
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

const handleProcess = async () => {
  if (!handleFormRef.value || !currentFeedback.value) return
  
  try {
    await handleFormRef.value.validate()
    
    handleLoading.value = true
    
    await handleFeedback(currentFeedback.value.id, handleForm)
    
    ElMessage.success('处理成功')
    showDetailDialog.value = false
    loadFeedbacks()
  } catch (error: any) {
    if (error.fields) {
      return
    }
    ElMessage.error(error.message || '处理失败')
  } finally {
    handleLoading.value = false
  }
}

const handleSubmitDialogClose = () => {
  if (submitFormRef.value) {
    submitFormRef.value.resetFields()
  }
  fileList.value = []
  uploadedFiles.value = []
}

const handleDetailDialogClose = () => {
  currentFeedback.value = null
  isHandleMode.value = false
  if (handleFormRef.value) {
    handleFormRef.value.resetFields()
  }
}

onMounted(() => {
  loadFeedbacks()
})
</script>

<style lang="scss" scoped>
.feedback-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 18px;
    font-weight: 600;
  }
  
  .query-form {
    margin-bottom: 20px;
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 4px;
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
  
  .feedback-detail {
    .content-text {
      white-space: pre-wrap;
      word-break: break-all;
      line-height: 1.6;
    }
    
    .reply-text {
      white-space: pre-wrap;
      word-break: break-all;
      line-height: 1.6;
      background-color: #f0f9ff;
      padding: 12px;
      border-radius: 4px;
      border-left: 4px solid #409eff;
    }
    
    .attachment-list {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      
      .attachment-image {
        width: 100px;
        height: 100px;
        border-radius: 4px;
        cursor: pointer;
      }
    }
    
    .handle-form {
      margin-top: 20px;
    }
  }
}

:deep(.el-upload--picture-card) {
  width: 80px;
  height: 80px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 80px;
  height: 80px;
}
</style> 