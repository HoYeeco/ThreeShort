<template>
  <div class="credit-reports-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>信用行为上报管理</span>
          <el-button v-if="!userStore.isAdmin" type="primary" @click="showSubmitDialog = true">
            <el-icon><Plus /></el-icon>
            提交上报
          </el-button>
        </div>
      </template>

      <!-- 搜索条件 -->
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 200px">
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
              <el-option label="已撤回" value="WITHDRAWN" />
            </el-select>
          </el-form-item>
          <el-form-item label="行为类型">
            <el-select v-model="searchForm.behaviorTypeId" placeholder="请选择行为类型" clearable style="width: 200px">
              <el-option
                v-for="type in behaviorTypes"
                :key="type.id"
                :label="type.name"
                :value="type.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="请输入标题或描述" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 上报列表 -->
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="behaviorType" label="行为类型" width="120" />
        <el-table-column prop="reportTime" label="上报时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.reportTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scoreAwarded" label="得分" width="80" />
        <el-table-column prop="reviewComment" label="审核意见" show-overflow-tooltip />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">查看</el-button>
            <el-button 
              v-if="userStore.isAdmin && scope.row.status === 'PENDING'"
              size="small" 
              type="primary" 
              @click="showReview(scope.row)"
            >
              审核
            </el-button>
            <el-button 
              v-if="!userStore.isAdmin && scope.row.status === 'PENDING'"
              size="small" 
              type="danger" 
              @click="withdrawReport(scope.row)"
            >
              撤回
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 提交上报对话框 -->
    <el-dialog v-model="showSubmitDialog" title="提交信用行为上报" width="600px">
      <el-form :model="submitForm" label-width="100px">
        <el-form-item label="行为类型">
          <el-select v-model="submitForm.behaviorTypeId" placeholder="请选择行为类型" style="width: 100%">
            <el-option
              v-for="type in behaviorTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="submitForm.title" placeholder="请输入上报标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="submitForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述行为情况"
          />
        </el-form-item>
        <el-form-item label="发生地点">
          <el-input v-model="submitForm.location" placeholder="请输入发生地点" />
        </el-form-item>
        <el-form-item label="涉及人员">
          <el-select 
            v-model="submitForm.involvedPersons" 
            multiple 
            placeholder="请选择涉及人员"
            style="width: 100%"
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option 
              v-for="user in availableUsers" 
              :key="user.id" 
              :label="user.realName" 
              :value="user.realName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="证据图片">
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
            :limit="5"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持jpg、png、gif格式，最多上传5张图片，每张不超过10MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showSubmitDialog = false">取消</el-button>
          <el-button type="primary" @click="submitReport">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="showReviewDialog" title="审核上报" width="800px">
      <!-- 上报内容展示 -->
      <div v-if="currentReport" class="report-content">
        <el-card class="review-report-card">
          <template #header>
            <div class="card-header">
              <span>上报内容</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="标题">{{ currentReport.title }}</el-descriptions-item>
            <el-descriptions-item label="行为类型">{{ currentReport.behaviorType }}</el-descriptions-item>
            <el-descriptions-item label="上报时间">{{ formatDate(currentReport.reportTime) }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(currentReport.status)">
                {{ getStatusText(currentReport.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发生地点" v-if="currentReport.location">{{ currentReport.location }}</el-descriptions-item>
            <el-descriptions-item label="涉及人员" v-if="currentReport.involvedPersons">{{ currentReport.involvedPersons }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ currentReport.description }}</el-descriptions-item>
            <el-descriptions-item label="证据图片" :span="2" v-if="currentReport.evidenceFiles && currentReport.evidenceFiles.length > 0">
              <div class="evidence-images">
                <el-image
                  v-for="(image, index) in currentReport.evidenceFiles"
                  :key="index"
                  :src="image"
                  :preview-src-list="currentReport.evidenceFiles"
                  :initial-index="index"
                  fit="cover"
                  class="evidence-image"
                />
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>

      <!-- 审核表单 -->
      <div class="review-form">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>审核操作</span>
            </div>
          </template>
          <el-form :model="reviewForm" label-width="100px">
            <el-form-item label="审核结果">
              <el-radio-group v-model="reviewForm.status">
                <el-radio label="APPROVED">通过</el-radio>
                <el-radio label="REJECTED">拒绝</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="得分" v-if="reviewForm.status === 'APPROVED'">
              <el-input-number v-model="reviewForm.scoreAwarded" :min="-999" :max="999" />
            </el-form-item>
            <el-form-item label="审核意见">
              <el-input
                v-model="reviewForm.reviewComment"
                type="textarea"
                :rows="3"
                placeholder="请填写审核意见"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showReviewDialog = false">取消</el-button>
          <el-button type="primary" @click="submitReview">提交审核</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="上报详情" width="600px">
      <div v-if="currentReport">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ currentReport.title }}</el-descriptions-item>
          <el-descriptions-item label="行为类型">{{ currentReport.behaviorType }}</el-descriptions-item>
          <el-descriptions-item label="上报时间">{{ formatDate(currentReport.reportTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentReport.status)">
              {{ getStatusText(currentReport.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发生地点" v-if="currentReport.location">{{ currentReport.location }}</el-descriptions-item>
          <el-descriptions-item label="涉及人员" v-if="currentReport.involvedPersons">{{ currentReport.involvedPersons }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentReport.description }}</el-descriptions-item>
          <el-descriptions-item label="证据图片" :span="2" v-if="currentReport.evidenceFiles && currentReport.evidenceFiles.length > 0">
            <div class="evidence-images">
              <el-image
                v-for="(image, index) in currentReport.evidenceFiles"
                :key="index"
                :src="image"
                :preview-src-list="currentReport.evidenceFiles"
                :initial-index="index"
                fit="cover"
                class="evidence-image"
              />
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2" v-if="currentReport.reviewComment">{{ currentReport.reviewComment }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadFile, UploadProps } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

interface BehaviorType {
  id: number
  name: string
}

interface CreditReport {
  id: number
  title: string
  description: string
  behaviorType: string
  reportTime: string
  status: string
  scoreAwarded?: number
  reviewComment?: string
  location?: string
  involvedPersons?: string
  evidenceFiles?: string[]
}

const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const tableData = ref<CreditReport[]>([])
const behaviorTypes = ref<BehaviorType[]>([])
const availableUsers = ref<any[]>([])
const showSubmitDialog = ref(false)
const showReviewDialog = ref(false)
const showDetailDialog = ref(false)
const currentReport = ref<CreditReport | null>(null)

// 文件上传相关
const fileList = ref<UploadFile[]>([])
const uploadRef = ref()

// 搜索表单
const searchForm = reactive({
  status: '',
  behaviorTypeId: null as number | null,
  keyword: '',
  current: 1,
  size: 10
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 提交表单
const submitForm = reactive({
  userId: userStore.userInfo?.id,
  behaviorTypeId: null,
  title: '',
  description: '',
  location: '',
  involvedPersons: [] as string[],
  evidenceFiles: [] as string[]
})

// 审核表单
const reviewForm = reactive({
  reviewerId: userStore.userInfo?.id,
  status: 'APPROVED',
  scoreAwarded: 0,
  reviewComment: ''
})

// 计算属性
const uploadAction = computed(() => {
  return '/api/files/upload'
})

const uploadData = computed(() => {
  return {
    businessType: 'EVIDENCE',
    userId: userStore.userInfo?.id || 1
  }
})

// 文件上传方法
const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  const isLt10M = rawFile.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response: any) => {
  if (response.code === 200) {
    submitForm.evidenceFiles.push(response.data.fileUrl)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleRemoveFile = (uploadFile: UploadFile) => {
  // 从evidenceFiles中移除对应的文件URL
  const index = fileList.value.findIndex(file => file.uid === uploadFile.uid)
  if (index > -1) {
    submitForm.evidenceFiles.splice(index, 1)
  }
}

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size,
      status: searchForm.status,
      behaviorTypeId: searchForm.behaviorTypeId,
      keyword: searchForm.keyword
    }
    
    if (!userStore.isAdmin) {
      params.userId = userStore.userInfo?.id
    }
    
    const response = await request.get('/credit-report/list', { params })
    tableData.value = response.data.records || []
    pagination.total = response.data.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadBehaviorTypes = async () => {
  try {
    const response = await request.get('/credit-behavior-type/active')
    behaviorTypes.value = response.data || []
  } catch (error) {
    ElMessage.error('加载行为类型失败')
  }
}

const loadAvailableUsers = async () => {
  try {
    const response = await request.get('/user/list', {
      params: { size: 1000 } // 获取所有用户
    })
    // 过滤掉管理员用户
    availableUsers.value = (response.data.records || []).filter((user: any) => 
      user.role !== 'ADMIN'
    )
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const resetSearch = () => {
  searchForm.status = ''
  searchForm.behaviorTypeId = null
  searchForm.keyword = ''
  pagination.current = 1
  loadData()
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  loadData()
}

const handleCurrentChange = (val: number) => {
  pagination.current = val
  loadData()
}

const submitReport = async () => {
  try {
    // 将涉及人员数组转换为字符串
    const submitData = {
      ...submitForm,
      involvedPersons: Array.isArray(submitForm.involvedPersons) 
        ? submitForm.involvedPersons.join('、') 
        : submitForm.involvedPersons
    }
    
    await request.post('/credit-report/submit', submitData)
    ElMessage.success('提交成功')
    showSubmitDialog.value = false
    // 重置表单
    submitForm.behaviorTypeId = null
    submitForm.title = ''
    submitForm.description = ''
    submitForm.location = ''
    submitForm.involvedPersons = []
    submitForm.evidenceFiles = []
    fileList.value = []
    loadData()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const showReview = (row: CreditReport) => {
  currentReport.value = row
  reviewForm.status = 'APPROVED'
  reviewForm.scoreAwarded = 0
  reviewForm.reviewComment = ''
  showReviewDialog.value = true
}

const submitReview = async () => {
  try {
    if (!currentReport.value) return
    await request.put(`/credit-report/${currentReport.value.id}/review`, reviewForm)
    ElMessage.success('审核完成')
    showReviewDialog.value = false
    loadData()
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const withdrawReport = async (row: CreditReport) => {
  try {
    await ElMessageBox.confirm('确定要撤回这个上报吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.put(`/credit-report/${row.id}/withdraw?userId=${userStore.userInfo?.id}`)
    ElMessage.success('撤回成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤回失败')
    }
  }
}

const viewDetail = (row: CreditReport) => {
  currentReport.value = row
  showDetailDialog.value = true
}

const getStatusType = (status: string) => {
  const statusMap: { [key: string]: string } = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    WITHDRAWN: 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: { [key: string]: string } = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    WITHDRAWN: '已撤回'
  }
  return statusMap[status] || '未知'
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadData()
  loadBehaviorTypes()
  loadAvailableUsers()
})
</script>

<style lang="scss" scoped>
.credit-reports-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }

  .upload-tip {
    color: #909399;
    font-size: 12px;
    margin-top: 8px;
  }

  .evidence-images {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }

  .evidence-image {
    width: 100px;
    height: 100px;
    border-radius: 4px;
    cursor: pointer;
  }

  .report-content {
    margin-bottom: 20px;
  }

  .review-report-card {
    margin-bottom: 20px;
  }

  .review-form {
    .el-card {
      border: 1px solid #e4e7ed;
    }
  }
}
</style>