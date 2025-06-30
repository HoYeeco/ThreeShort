<template>
  <div class="layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px">
        <div class="logo">
          <h3>诚信管理系统</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
          router
        >
          <!-- 首页 - 所有角色都可以访问 -->
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          
          <!-- 个人信息 - 所有角色都可以访问 -->
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>
          
          <!-- 普通居民功能菜单 -->
          <template v-if="userStore.isResident">
            <el-menu-item index="/credit-reports">
              <el-icon><Document /></el-icon>
              <template #title>信用上报</template>
            </el-menu-item>
            
            <el-menu-item index="/products">
              <el-icon><ShoppingBag /></el-icon>
              <template #title>积分商城</template>
            </el-menu-item>
            
            <el-menu-item index="/exchange-records">
              <el-icon><List /></el-icon>
              <template #title>兑换记录</template>
            </el-menu-item>
            
            <el-menu-item index="/agreements">
              <el-icon><Reading /></el-icon>
              <template #title>公约学习</template>
            </el-menu-item>
            
            <el-menu-item index="/feedback">
              <el-icon><Comment /></el-icon>
              <template #title>反馈建议</template>
            </el-menu-item>
          </template>
          
          <!-- 社区管理员功能菜单 -->
          <template v-if="userStore.isAdmin">
            <el-menu-item index="/users">
              <el-icon><UserFilled /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
            
            <el-menu-item index="/credit-profiles">
              <el-icon><Star /></el-icon>
              <template #title>信用档案管理</template>
            </el-menu-item>
            
            <el-menu-item index="/admin/products">
              <el-icon><Goods /></el-icon>
              <template #title>商品配置管理</template>
            </el-menu-item>
            
            <el-menu-item index="/admin/agreements">
              <el-icon><Files /></el-icon>
              <template #title>公约管理</template>
            </el-menu-item>
            
            <el-menu-item index="/admin/feedback">
              <el-icon><MessageBox /></el-icon>
              <template #title>反馈处理</template>
            </el-menu-item>
            
            <el-menu-item index="/admin/reports">
              <el-icon><Monitor /></el-icon>
              <template #title>上报审核</template>
            </el-menu-item>
          </template>
          
          <!-- 维护员功能菜单 -->
          <template v-if="userStore.isMaintainer">
            <el-sub-menu index="maintenance">
              <template #title>
                <el-icon><Tools /></el-icon>
                <span>系统维护</span>
              </template>
              
              <el-menu-item index="/maintenance/logs">
                <el-icon><Document /></el-icon>
                <template #title>操作日志</template>
              </el-menu-item>
              
              <el-menu-item index="/maintenance/tasks">
                <el-icon><Timer /></el-icon>
                <template #title>定时任务</template>
              </el-menu-item>
              
              <el-menu-item index="/maintenance/system">
                <el-icon><Setting /></el-icon>
                <template #title>系统监控</template>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区域 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header>
          <div class="header-left">
            <el-button
                              link
              size="small"
              @click="toggleCollapse"
            >
              <el-icon><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
            </el-button>
          </div>
          
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <el-avatar :src="userStore.userInfo?.avatar" :size="32">
                  {{ userStore.userInfo?.realName?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.realName }}</span>
                <span class="role-badge" :class="getRoleClass()">{{ getRoleText() }}</span>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主内容 -->
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  House,
  User,
  UserFilled,
  Expand,
  Fold,
  ArrowDown,
  Document,
  Star,
  ShoppingBag,
  Reading,
  Comment,
  List,
  Setting,
  Tools,
  Timer,
  Monitor,
  MessageBox,
  Files,
  Goods
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const getRoleText = () => {
  switch (userStore.userInfo?.role) {
    case 'ADMIN':
      return '管理员'
    case 'MAINTAINER':
      return '维护员'
    case 'RESIDENT':
      return '居民'
    default:
      return '未知'
  }
}

const getRoleClass = () => {
  switch (userStore.userInfo?.role) {
    case 'ADMIN':
      return 'role-admin'
    case 'MAINTAINER':
      return 'role-maintainer'
    case 'RESIDENT':
      return 'role-resident'
    default:
      return 'role-unknown'
  }
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await userStore.logout()
      } catch (error) {
        // 用户取消退出
      }
      break
  }
}
</script>

<style lang="scss" scoped>
.layout {
  height: 100vh;
  
  .el-aside {
    background-color: #545c64;
    
    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-bottom: 1px solid #434a50;
      
      h3 {
        color: #fff;
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }
    }
    
    .el-menu {
      border-right: none;
    }
  }
  
  .el-header {
    background-color: #fff;
    border-bottom: 1px solid #d8dce5;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    
    .header-left {
      display: flex;
      align-items: center;
    }
    
    .header-right {
      .el-dropdown-link {
        display: flex;
        align-items: center;
        cursor: pointer;
        color: #606266;
        
        .username {
          margin-left: 8px;
          font-size: 14px;
        }
        
        .role-badge {
          margin-left: 8px;
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 12px;
          font-weight: 500;
          
          &.role-admin {
            background-color: #f56c6c;
            color: #fff;
          }
          
          &.role-maintainer {
            background-color: #e6a23c;
            color: #fff;
          }
          
          &.role-resident {
            background-color: #67c23a;
            color: #fff;
          }
          
          &.role-unknown {
            background-color: #909399;
            color: #fff;
          }
        }
      }
    }
  }
  
  .el-main {
    background-color: #f5f5f5;
    padding: 20px;
  }
}
.el-container{
  height: 100%;
}
</style> 
