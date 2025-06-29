<template>
  <div class="exchange-records-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>{{ userStore.isAdmin ? '兑换记录管理' : '我的兑换记录' }}</h2>
        <div class="total-info" v-if="statistics">
          <span class="total-exchanges">总兑换次数：{{ statistics.totalExchanges || 0 }}</span>
          <span class="total-points">累计消耗：{{ statistics.totalPointsUsed || 0 }} 积分</span>
        </div>
      </div>
      
      <div class="header-right">
        <el-button 
          v-if="userStore.isAdmin" 
          type="primary" 
          @click="loadStatistics"
        >
          <el-icon><Refresh /></el-icon>
          刷新统计
        </el-button>
        
        <el-button 
          type="success" 
          @click="exportRecords"
          :loading="exportLoading"
        >
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="商品名称" v-if="userStore.isAdmin">
          <el-input 
            v-model="queryForm.productName" 
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="用户ID" v-if="userStore.isAdmin">
          <el-input-number 
            v-model="queryForm.userId" 
            placeholder="用户ID"
            :min="0"
            style="width: 150px"
          />
        </el-form-item>
        
        <el-form-item label="兑换状态">
          <el-select 
            v-model="queryForm.status" 
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
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
            style="width: 350px"
          />
        </el-form-item>
        
        <el-form-item label="积分范围">
          <el-input-number 
            v-model="queryForm.minPoints" 
            placeholder="最小积分"
            :min="0"
            style="width: 120px"
          />
          <span style="margin: 0 10px">-</span>
          <el-input-number 
            v-model="queryForm.maxPoints" 
            placeholder="最大积分"
            :min="0"
            style="width: 120px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadRecords">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stats-cards" v-if="userStore.isAdmin && statistics">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.totalExchanges || 0 }}</div>
              <div class="stat-label">总兑换数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.successExchanges || 0 }}</div>
              <div class="stat-label">成功兑换</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.todayExchanges || 0 }}</div>
              <div class="stat-label">今日兑换</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ formatPoints(statistics.totalPointsUsed) }}</div>
              <div class="stat-label">累计积分</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 兑换记录表格 -->
    <el-card class="table-card">
      <el-table 
        :data="recordsList" 
        style="width: 100%"
        row-key="id"
        v-loading="loading"
      >
        <el-table-column prop="id" label="记录ID" width="100" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" v-if="userStore.isAdmin" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="quantity" label="兑换数量" width="100" align="center">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.quantity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pointsUsed" label="消耗积分" width="120" align="center">
          <template #default="scope">
            <el-tag type="warning">{{ scope.row.pointsUsed }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="兑换状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exchangeTime" label="兑换时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.exchangeTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" min-width="120">
          <template #default="scope">
            <span v-if="scope.row.remarks">{{ scope.row.remarks }}</span>
            <span v-else class="no-remarks">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" v-if="userStore.isAdmin">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetails(scope.row)">
              详情
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="deleteRecord(scope.row)"
              v-if="scope.row.status !== 'SUCCESS'"
            >
              删除
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
          @size-change="loadRecords"
          @current-change="loadRecords"
        />
      </div>
    </el-card>

    <!-- 热门商品统计 -->
    <el-card class="popular-products" v-if="userStore.isAdmin && popularProducts.length > 0">
      <template #header>
        <div class="card-header">
          <span>热门商品统计</span>
          <el-button type="text" @click="loadPopularProducts">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="popularProducts" style="width: 100%">
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="totalQuantity" label="兑换总量" width="120" align="center">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.totalQuantity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exchangeCount" label="兑换次数" width="120" align="center">
          <template #default="scope">
            <el-tag type="primary">{{ scope.row.exchangeCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalPoints" label="消耗积分" width="120" align="center">
          <template #default="scope">
            <el-tag type="warning">{{ scope.row.totalPoints }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailsDialog" title="兑换详情" width="600px">
      <div class="record-details" v-if="selectedRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录ID">{{ selectedRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ selectedRecord.userId }}</el-descriptions-item>
          <el-descriptions-item label="商品ID">{{ selectedRecord.productId }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ selectedRecord.productName }}</el-descriptions-item>
          <el-descriptions-item label="兑换数量">{{ selectedRecord.quantity }}</el-descriptions-item>
          <el-descriptions-item label="消耗积分">{{ selectedRecord.pointsUsed }}</el-descriptions-item>
          <el-descriptions-item label="兑换状态">
            <el-tag :type="getStatusType(selectedRecord.status)">
              {{ getStatusText(selectedRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="兑换时间">{{ formatDate(selectedRecord.exchangeTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ selectedRecord.remarks || '无备注' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailsDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Download 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

interface ExchangeRecord {
  id: number
  userId: number
  productId: number
  productName: string
  quantity: number
  pointsUsed: number
  status: string
  exchangeTime: string
  remarks?: string
}

const userStore = useUserStore()

// 响应式数据
const recordsList = ref<ExchangeRecord[]>([])
const statistics = ref<any>(null)
const popularProducts = ref([])
const selectedRecord = ref<ExchangeRecord | null>(null)

// 查询表单
const queryForm = ref({
  userId: null as number | null,
  productName: '',
  status: '',
  minPoints: null as number | null,
  maxPoints: null as number | null,
  startTime: null as string | null,
  endTime: null as string | null
})

const dateRange = ref<[string, string] | null>(null)

// 分页
const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

// 对话框状态
const showDetailsDialog = ref(false)

// 加载状态
const loading = ref(false)
const exportLoading = ref(false)

// 监听时间范围变化
watch(dateRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    queryForm.value.startTime = newVal[0]
    queryForm.value.endTime = newVal[1]
  } else {
    queryForm.value.startTime = null
    queryForm.value.endTime = null
  }
})

// 方法
const loadRecords = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.value.current,
      size: pagination.value.size,
      ...queryForm.value
    }
    
    // 如果不是管理员，只查询当前用户的记录
    if (!userStore.isAdmin && userStore.userInfo?.id) {
      params.userId = userStore.userInfo.id
    }
    
    const response = await request.get('/exchange-record/list', { params })
    recordsList.value = response.data.records || []
    pagination.value.total = response.data.total || 0
  } catch (error) {
    ElMessage.error('加载兑换记录失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  if (!userStore.isAdmin) return
  
  try {
    const response = await request.get('/exchange-record/statistics')
    statistics.value = response.data
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const loadPopularProducts = async () => {
  if (!userStore.isAdmin) return
  
  try {
    const response = await request.get('/exchange-record/popular-products?limit=10')
    popularProducts.value = response.data || []
  } catch (error) {
    ElMessage.error('加载热门商品失败')
  }
}

const resetQuery = () => {
  queryForm.value = {
    userId: null,
    productName: '',
    status: '',
    minPoints: null,
    maxPoints: null,
    startTime: null,
    endTime: null
  }
  dateRange.value = null
  pagination.value.current = 1
  loadRecords()
}

const viewDetails = (record: ExchangeRecord) => {
  selectedRecord.value = record
  showDetailsDialog.value = true
}

const deleteRecord = async (record: ExchangeRecord) => {
  try {
    await ElMessageBox.confirm(`确定要删除记录ID为"${record.id}"的兑换记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/exchange-record/delete/${record.id}`)
    ElMessage.success('删除成功')
    loadRecords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const exportRecords = async () => {
  try {
    loading.value = true;
    
    // 构建查询参数
    const params = new URLSearchParams();
    if (queryForm.value.userId) params.append('userId', queryForm.value.userId.toString());
    if (queryForm.value.productName) params.append('productName', queryForm.value.productName);
    if (queryForm.value.status) params.append('status', queryForm.value.status);
    if (queryForm.value.minPoints) params.append('minPoints', queryForm.value.minPoints.toString());
    if (queryForm.value.maxPoints) params.append('maxPoints', queryForm.value.maxPoints.toString());
    if (queryForm.value.startTime) {
      params.append('startTime', queryForm.value.startTime);
    }
    if (queryForm.value.endTime) {
      params.append('endTime', queryForm.value.endTime);
    }
    
    // 使用window.open直接下载文件
    const exportUrl = `/api/exchange-record/export?${params.toString()}`;
    window.open(exportUrl, '_blank');
    
    ElMessage.success('导出请求已发送，请稍候下载');
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 辅助方法
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'CANCELLED': 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const formatPoints = (points: number) => {
  if (points >= 10000) {
    return (points / 10000).toFixed(1) + 'w'
  } else if (points >= 1000) {
    return (points / 1000).toFixed(1) + 'k'
  }
  return points?.toString() || '0'
}

// 生命周期
onMounted(() => {
  loadRecords()
  if (userStore.isAdmin) {
    loadStatistics()
    loadPopularProducts()
  }
})
</script>

<style lang="scss" scoped>
.exchange-records-container {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 20px;
      
      h2 {
        margin: 0;
        color: #333;
      }
      
      .total-info {
        display: flex;
        gap: 15px;
        
        span {
          color: #666;
          font-size: 14px;
        }
      }
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
    
    .el-form {
      margin-bottom: 0;
    }
  }
  
  .stats-cards {
    margin-bottom: 20px;
    
    .stat-card {
      text-align: center;
      
      .stat-item {
        padding: 10px;
        
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #409eff;
          margin-bottom: 5px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #999;
        }
      }
    }
  }
  
  .table-card {
    margin-bottom: 20px;
    
    .no-remarks {
      color: #c0c4cc;
      font-style: italic;
    }
    
    .pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
  
  .popular-products {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .record-details {
    .el-descriptions {
      margin-top: 20px;
    }
  }
}
</style> 