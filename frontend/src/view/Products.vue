<template>
  <div class="products-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>{{ userStore.isAdmin ? '商品管理' : '积分商城' }}</h2>
        <div class="user-points" v-if="!userStore.isAdmin && userProfile">
          <el-icon><Star /></el-icon>
          <span>我的积分：{{ userProfile.rewardPoints || 0 }}</span>
        </div>
      </div>
      
      <div class="header-right">
        <el-button 
          v-if="userStore.isAdmin" 
          type="primary" 
          @click="showCreateDialog = true"
        >
          <el-icon><Plus /></el-icon>
          添加商品
        </el-button>
        
        <el-button 
          v-if="!userStore.isAdmin" 
          type="success" 
          @click="showExchangeRecords = true"
        >
          <el-icon><List /></el-icon>
          兑换记录
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="商品名称">
          <el-input 
            v-model="queryForm.name" 
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="商品分类">
          <el-select 
            v-model="queryForm.category" 
            placeholder="请选择分类"
            clearable
            style="width: 150px"
          >
            <el-option label="生活用品" value="生活用品" />
            <el-option label="服务券" value="服务券" />
            <el-option label="荣誉奖品" value="荣誉奖品" />
          </el-select>
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
        
        <el-form-item v-if="userStore.isAdmin" label="状态">
          <el-select 
            v-model="queryForm.isActive" 
            placeholder="商品状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadProducts">
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

    <!-- 商品列表 -->
    <div class="products-grid" v-if="!userStore.isAdmin">
      <el-row :gutter="20">
        <el-col 
          v-for="product in productList" 
          :key="product.id" 
          :xs="24" :sm="12" :md="8" :lg="6"
        >
          <el-card class="product-card" shadow="hover">
            <div class="product-image">
              <img :src="product.imageUrl || defaultImage" alt="商品图片" />
            </div>
            
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              
              <div class="product-price">
                <el-icon><Star /></el-icon>
                <span class="points">{{ product.pointsRequired }}</span>
                <span class="unit">积分</span>
              </div>
              
              <div class="product-meta">
                <el-tag :type="getLevelColor(product.eligibleLevels)" size="small">
                  {{ getLevelText(product.eligibleLevels) }}
                </el-tag>
                <span class="stock">库存：{{ product.stockQuantity }}</span>
              </div>
              
              <div class="product-actions">
                <el-button 
                  type="primary" 
                  @click="showExchangeDialog(product)"
                  :disabled="!canExchange(product)"
                  style="width: 100%"
                >
                  {{ getExchangeText(product) }}
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 管理员表格视图 -->
    <el-card v-if="userStore.isAdmin" class="table-card">
      <el-table 
        :data="productList" 
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="pointsRequired" label="所需积分" width="100" align="center">
          <template #default="scope">
            <el-tag type="warning">{{ scope.row.pointsRequired }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="库存" width="80" align="center" />
        <el-table-column prop="eligibleLevels" label="可兑换等级" width="120">
          <template #default="scope">
            <el-tag 
              v-for="level in scope.row.eligibleLevels" 
              :key="level" 
              size="small" 
              style="margin-right: 5px"
            >
              {{ level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="scope">
            <el-switch 
              v-model="scope.row.isActive" 
              @change="toggleStatus(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editProduct(scope.row)">
              编辑
            </el-button>
            <el-button type="warning" size="small" @click="updateStock(scope.row)">
              库存
            </el-button>
            <el-button type="danger" size="small" @click="deleteProduct(scope.row)">
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
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadProducts"
          @current-change="loadProducts"
        />
      </div>
    </el-card>

    <!-- 创建/编辑商品对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="editingProduct ? '编辑商品' : '添加商品'"
      width="600px"
    >
      <el-form :model="productForm" :rules="productRules" ref="productFormRef" label-width="120px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        
        <el-form-item label="商品描述" prop="description">
          <el-input 
            v-model="productForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        
        <el-form-item label="所需积分" prop="pointsRequired">
          <el-input-number 
            v-model="productForm.pointsRequired" 
            :min="1"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="库存数量" prop="stockQuantity">
          <el-input-number 
            v-model="productForm.stockQuantity" 
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="productForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="生活用品" value="生活用品" />
            <el-option label="服务券" value="服务券" />
            <el-option label="荣誉奖品" value="荣誉奖品" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="可兑换等级" prop="eligibleLevels">
          <el-checkbox-group v-model="productForm.eligibleLevels">
            <el-checkbox label="D">D级</el-checkbox>
            <el-checkbox label="C">C级</el-checkbox>
            <el-checkbox label="B">B级</el-checkbox>
            <el-checkbox label="A">A级</el-checkbox>
            <el-checkbox label="AA">AA级</el-checkbox>
            <el-checkbox label="AAA">AAA级</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="商品图片">
          <el-input v-model="productForm.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="saveProduct" :loading="saveLoading">
            {{ editingProduct ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 兑换对话框 -->
    <el-dialog v-model="showExchangeDialogVisible" title="积分兑换" width="500px">
      <div class="exchange-dialog" v-if="selectedProduct">
        <div class="product-summary">
          <div class="product-image-small">
            <img :src="selectedProduct.imageUrl || defaultImage" alt="商品图片" />
          </div>
          <div class="product-details">
            <h3>{{ selectedProduct.name }}</h3>
            <p>{{ selectedProduct.description }}</p>
            <div class="price-info">
              <span class="points">{{ selectedProduct.pointsRequired }}</span>
              <span class="unit">积分/个</span>
            </div>
          </div>
        </div>
        
        <el-form :model="exchangeForm" label-width="80px">
          <el-form-item label="兑换数量">
            <el-input-number 
              v-model="exchangeForm.quantity" 
              :min="1" 
              :max="maxExchangeQuantity"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="总积分">
            <span class="total-points">{{ totalPoints }} 积分</span>
          </el-form-item>
          
          <el-form-item label="备注">
            <el-input 
              v-model="exchangeForm.remarks" 
              placeholder="请输入备注信息（可选）"
            />
          </el-form-item>
        </el-form>
        
        <div class="exchange-info">
          <p>您当前积分：{{ userProfile?.rewardPoints || 0 }}</p>
          <p>兑换后余额：{{ userProfile?.rewardPoints - totalPoints || 0 }}</p>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showExchangeDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmExchange" 
            :loading="exchangeLoading"
            :disabled="!canConfirmExchange"
          >
            确认兑换
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 兑换记录对话框 -->
    <el-dialog v-model="showExchangeRecords" title="我的兑换记录" width="800px">
      <el-table :data="exchangeRecords" style="width: 100%">
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column prop="pointsUsed" label="消耗积分" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
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
      </el-table>
    </el-dialog>

    <!-- 库存更新对话框 -->
    <el-dialog v-model="showStockDialog" title="更新库存" width="400px">
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="商品名称">
          <span>{{ stockForm.productName }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ stockForm.currentStock }}</span>
        </el-form-item>
        <el-form-item label="新库存" required>
          <el-input-number 
            v-model="stockForm.newStock" 
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showStockDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmUpdateStock" :loading="stockLoading">
            更新
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Star, Plus, List, Search, Refresh 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

interface Product {
  id: number
  name: string
  description: string
  pointsRequired: number
  stockQuantity: number
  category: string
  imageUrl?: string
  eligibleLevels: string[]
  isActive: boolean
  createdTime: string
}

interface UserProfile {
  rewardPoints: number
  creditLevel: string
}

const userStore = useUserStore()

// 响应式数据
const productList = ref<Product[]>([])
const userProfile = ref<UserProfile | null>(null)
const exchangeRecords = ref([])

// 查询表单
const queryForm = ref({
  name: '',
  category: '',
  minPoints: null,
  maxPoints: null,
  isActive: null
})

// 分页
const pagination = ref({
  current: 1,
  size: 12,
  total: 0
})

// 对话框状态
const showCreateDialog = ref(false)
const showExchangeDialogVisible = ref(false)
const showExchangeRecords = ref(false)
const showStockDialog = ref(false)

// 加载状态
const saveLoading = ref(false)
const exchangeLoading = ref(false)
const stockLoading = ref(false)

// 表单数据
const editingProduct = ref<Product | null>(null)
const selectedProduct = ref<Product | null>(null)

const productForm = ref({
  name: '',
  description: '',
  pointsRequired: 1,
  stockQuantity: 0,
  category: '',
  eligibleLevels: [],
  imageUrl: ''
})

const exchangeForm = ref({
  quantity: 1,
  remarks: ''
})

const stockForm = ref({
  productId: null,
  productName: '',
  currentStock: 0,
  newStock: 0
})

// 表单验证规则
const productRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  pointsRequired: [{ required: true, message: '请输入所需积分', trigger: 'blur' }],
  stockQuantity: [{ required: true, message: '请输入库存数量', trigger: 'blur' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }]
}

// 默认图片
const defaultImage = '/images/default-product.png'

// 计算属性
const totalPoints = computed(() => {
  if (!selectedProduct.value) return 0
  return selectedProduct.value.pointsRequired * exchangeForm.value.quantity
})

const maxExchangeQuantity = computed(() => {
  if (!selectedProduct.value || !userProfile.value) return 1
  const stockLimit = selectedProduct.value.stockQuantity
  const pointsLimit = Math.floor(userProfile.value.rewardPoints / selectedProduct.value.pointsRequired)
  return Math.min(stockLimit, pointsLimit, 99)
})

const canConfirmExchange = computed(() => {
  return userProfile.value && 
         userProfile.value.rewardPoints >= totalPoints.value &&
         exchangeForm.value.quantity > 0
})

// 方法
const loadProducts = async () => {
  try {
    const params = {
      current: pagination.value.current,
      size: pagination.value.size,
      ...queryForm.value
    }
    
    const response = await request.get('/product/list', { params })
    productList.value = response.data.records || []
    pagination.value.total = response.data.total || 0
  } catch (error) {
    ElMessage.error('加载商品列表失败')
  }
}

const loadUserProfile = async () => {
  if (!userStore.userInfo?.id) return
  
  try {
    const response = await request.get(`/user-credit-profile/current?userId=${userStore.userInfo.id}`)
    userProfile.value = response.data
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

const loadExchangeRecords = async () => {
  if (!userStore.userInfo?.id) return
  
  try {
    const response = await request.get(`/exchange-record/current?userId=${userStore.userInfo.id}&limit=20`)
    exchangeRecords.value = response.data || []
  } catch (error) {
    ElMessage.error('加载兑换记录失败')
  }
}

const resetQuery = () => {
  queryForm.value = {
    name: '',
    category: '',
    minPoints: null,
    maxPoints: null,
    isActive: null
  }
  pagination.value.current = 1
  loadProducts()
}

const editProduct = (product: Product) => {
  editingProduct.value = product
  productForm.value = { ...product }
  showCreateDialog.value = true
}

const saveProduct = async () => {
  saveLoading.value = true
  try {
    if (editingProduct.value) {
      await request.put(`/product/update/${editingProduct.value.id}`, productForm.value)
      ElMessage.success('更新商品成功')
    } else {
      await request.post('/product/create', productForm.value)
      ElMessage.success('创建商品成功')
    }
    
    showCreateDialog.value = false
    editingProduct.value = null
    loadProducts()
  } catch (error) {
    ElMessage.error(editingProduct.value ? '更新商品失败' : '创建商品失败')
  } finally {
    saveLoading.value = false
  }
}

const deleteProduct = async (product: Product) => {
  try {
    await ElMessageBox.confirm(`确定要删除商品"${product.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/product/delete/${product.id}`)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const toggleStatus = async (product: Product) => {
  try {
    await request.put(`/product/toggle-status/${product.id}`)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    product.isActive = !product.isActive // 回滚状态
  }
}

const updateStock = (product: Product) => {
  stockForm.value = {
    productId: product.id,
    productName: product.name,
    currentStock: product.stockQuantity,
    newStock: product.stockQuantity
  }
  showStockDialog.value = true
}

const confirmUpdateStock = async () => {
  stockLoading.value = true
  try {
    await request.put(`/product/update-stock/${stockForm.value.productId}?quantity=${stockForm.value.newStock}`)
    ElMessage.success('库存更新成功')
    showStockDialog.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error('库存更新失败')
  } finally {
    stockLoading.value = false
  }
}

const showExchangeDialog = (product: Product) => {
  selectedProduct.value = product
  exchangeForm.value = {
    quantity: 1,
    remarks: ''
  }
  showExchangeDialogVisible.value = true
}

const confirmExchange = async () => {
  if (!selectedProduct.value || !userStore.userInfo?.id) return
  
  exchangeLoading.value = true
  try {
    const exchangeData = {
      userId: userStore.userInfo.id,
      productId: selectedProduct.value.id,
      quantity: exchangeForm.value.quantity,
      remarks: exchangeForm.value.remarks
    }
    
    await request.post('/product/exchange', exchangeData)
    ElMessage.success('兑换成功！')
    
    showExchangeDialogVisible.value = false
    loadProducts()
    loadUserProfile()
  } catch (error) {
    ElMessage.error('兑换失败')
  } finally {
    exchangeLoading.value = false
  }
}

// 辅助方法
const canExchange = (product: Product) => {
  if (!userProfile.value) return false
  
  return product.isActive && 
         product.stockQuantity > 0 && 
         userProfile.value.rewardPoints >= product.pointsRequired &&
         (product.eligibleLevels.length === 0 || product.eligibleLevels.includes(userProfile.value.creditLevel))
}

const getExchangeText = (product: Product) => {
  if (!product.isActive) return '已下架'
  if (product.stockQuantity <= 0) return '已售罄'
  if (!userProfile.value) return '立即兑换'
  if (userProfile.value.rewardPoints < product.pointsRequired) return '积分不足'
  if (product.eligibleLevels.length > 0 && !product.eligibleLevels.includes(userProfile.value.creditLevel)) {
    return '等级不足'
  }
  return '立即兑换'
}

const getLevelColor = (levels: string[]) => {
  if (!levels || levels.length === 0) return 'info'
  if (levels.includes('AAA') || levels.includes('AA')) return 'success'
  if (levels.includes('A') || levels.includes('B')) return 'primary'
  return 'warning'
}

const getLevelText = (levels: string[]) => {
  if (!levels || levels.length === 0) return '全员可兑换'
  return levels.join('、') + '级可兑换'
}

const getStatusType = (status: string) => {
  const statusMap = {
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'CANCELLED': 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadProducts()
  if (!userStore.isAdmin) {
    loadUserProfile()
  }
})
</script>

<style lang="scss" scoped>
.products-container {
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
      
      .user-points {
        display: flex;
        align-items: center;
        gap: 5px;
        color: #E6A23C;
        font-weight: bold;
        
        .el-icon {
          font-size: 18px;
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
  
  .products-grid {
    .product-card {
      margin-bottom: 20px;
      height: 100%;
      
      .product-image {
        height: 200px;
        overflow: hidden;
        border-radius: 4px;
        margin-bottom: 15px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .product-info {
        .product-name {
          font-size: 16px;
          font-weight: bold;
          margin: 0 0 8px 0;
          color: #333;
        }
        
        .product-desc {
          color: #666;
          font-size: 14px;
          margin: 0 0 15px 0;
          line-height: 1.4;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
        
        .product-price {
          display: flex;
          align-items: center;
          gap: 5px;
          margin-bottom: 15px;
          
          .el-icon {
            color: #E6A23C;
            font-size: 18px;
          }
          
          .points {
            font-size: 20px;
            font-weight: bold;
            color: #E6A23C;
          }
          
          .unit {
            color: #999;
            font-size: 14px;
          }
        }
        
        .product-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 15px;
          
          .stock {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }
  }
  
  .table-card {
    .pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
  
  .exchange-dialog {
    .product-summary {
      display: flex;
      gap: 15px;
      margin-bottom: 20px;
      padding: 15px;
      background-color: #f5f7fa;
      border-radius: 8px;
      
      .product-image-small {
        width: 80px;
        height: 80px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 4px;
        }
      }
      
      .product-details {
        flex: 1;
        
        h3 {
          margin: 0 0 8px 0;
          color: #333;
        }
        
        p {
          margin: 0 0 10px 0;
          color: #666;
          font-size: 14px;
        }
        
        .price-info {
          .points {
            font-size: 18px;
            font-weight: bold;
            color: #E6A23C;
          }
          
          .unit {
            color: #999;
            margin-left: 5px;
          }
        }
      }
    }
    
    .total-points {
      font-size: 18px;
      font-weight: bold;
      color: #E6A23C;
    }
    
    .exchange-info {
      margin-top: 20px;
      padding: 15px;
      background-color: #f0f9ff;
      border-radius: 8px;
      
      p {
        margin: 5px 0;
        color: #666;
      }
    }
  }
}
</style> 