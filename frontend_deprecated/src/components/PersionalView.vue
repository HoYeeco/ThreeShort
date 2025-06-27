<template>
  <div class="personal-center">
    <div class="user-header">
      <div class="user-avatar">
        <img src="https://picsum.photos/120/120?random=1" alt="用户头像" class="avatar-img" />
      </div>
      <div class="user-title">
        <h1 class="page-title">个人中心</h1>
        <p class="user-status">社区成员 · 信用良好</p>
      </div>
    </div>

    <div class="user-info">
      <div class="info-header">
        <h2>基本信息</h2>
        <button class="edit-btn">
          <EditIcon class="edit-icon" />
          <span>编辑</span>
        </button>
      </div>

      <div class="info-fields">
        <div class="info-row">
          <span class="info-label">用户名：</span>
          <span class="info-value">张三</span>
        </div>
        <div class="info-row">
          <span class="info-label">联系方式：</span>
          <span class="info-value">13800138000</span>
        </div>
        <div class="info-row">
          <span class="info-label">居住地址：</span>
          <span class="info-value">城南社区 XX 栋 XX 号</span>
        </div>
        <div class="info-row">
          <span class="info-label">注册时间：</span>
          <span class="info-value">2023-05-15</span>
        </div>
      </div>
    </div>

    <div class="security-settings">
      <h2>安全设置</h2>
      <div class="settings-list">
        <div class="setting-item">
          <div class="item-icon">
            <LockIcon class="icon" />
          </div>
          <div class="item-content">
            <h3>修改密码</h3>
            <p>每3个月建议更换一次登录密码</p>
          </div>
          <button class="item-btn" @click="openPasswordModal">修改</button>
        </div>

        <div class="setting-item">
          <div class="item-icon">
            <PhoneIcon class="icon" />
          </div>
          <div class="item-content">
            <h3>绑定手机</h3>
            <p>已绑定：138****8000</p>
          </div>
          <button class="item-btn">更换</button>
        </div>
      </div>
    </div>

    <div class="system-messages">
      <h2>系统通知</h2>
      <div class="message-item">
        <div class="message-icon">
          <BellIcon class="icon" />
        </div>
        <div class="message-content">
          <h3>您的诚信分已更新</h3>
          <p>您的诚信分已更新为78分，感谢您积极参与社区活动</p>
          <span class="message-time">2024-05-10 15:30</span>
        </div>
      </div>
    </div>
  </div>

  <!-- 修改密码模态框 -->
  <div v-if="showPasswordModal" class="modal-overlay" @click.self="showPasswordModal = false">
    <div class="modal-content">
      <div class="modal-header">
        <h3>修改密码</h3>
        <button class="close-btn" @click="showPasswordModal = false">
          <XIcon class="close-icon" />
        </button>
      </div>

      <div class="modal-body">
        <div class="form-group">
          <label>当前密码</label>
          <input type="password" placeholder="请输入当前密码" />
        </div>
        <div class="form-group">
          <label>新密码</label>
          <input type="password" placeholder="请输入新密码" />
        </div>
        <div class="form-group">
          <label>确认新密码</label>
          <input type="password" placeholder="请再次输入新密码" />
        </div>
      </div>

      <div class="modal-footer">
        <button class="cancel-btn" @click="showPasswordModal = false">取消</button>
        <button class="confirm-btn" @click="showPasswordModal = false">确认修改</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { Edit, Lock, Phone, Bell, X } from "lucide-vue-next";

// 导入图标组件
const EditIcon = Edit;
const LockIcon = Lock;
const PhoneIcon = Phone;
const BellIcon = Bell;
const XIcon = X;

// 状态管理
const showPasswordModal = ref(false);

// 打开修改密码模态框
const openPasswordModal = () => {
  showPasswordModal.value = true;
};
</script>

<style scoped>
.personal-center {
  padding: 24px 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
  padding: 16px;
  background-color: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.avatar-img:hover {
  transform: scale(1.1);
}

.user-title {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.user-status {
  font-size: 14px;
  color: #6b7280;
}

.user-info,
.security-settings,
.system-messages {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 24px;
  margin-bottom: 24px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.user-info:hover,
.security-settings:hover,
.system-messages:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.info-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.edit-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: #f3f4f6;
  color: #6b7280;
  border: none;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.edit-btn:hover {
  background-color: #e5e7eb;
  color: #374151;
}

.edit-icon {
  width: 16px;
  height: 16px;
}

.info-fields {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 600;
  color: #4b5563;
  width: 80px;
  flex-shrink: 0;
}

.info-value {
  color: #1f2937;
  flex-grow: 1;
}

.security-settings h2,
.system-messages h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-radius: 12px;
  background-color: #f9fafb;
  margin-bottom: 16px;
  transition: background-color 0.2s ease;
}

.setting-item:hover {
  background-color: #f3f4f6;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.item-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(59, 130, 246, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.icon {
  width: 20px;
  height: 20px;
  color: #3b82f6;
}

.item-content {
  flex-grow: 1;
}

.item-content h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.item-content p {
  font-size: 14px;
  color: #6b7280;
}

.item-btn {
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.item-btn:hover {
  background-color: #2563eb;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border-radius: 12px;
  background-color: #f9fafb;
  transition: background-color 0.2s ease;
}

.message-item:hover {
  background-color: #f3f4f6;
}

.message-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(59, 130, 246, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.message-content {
  flex-grow: 1;
}

.message-content h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.message-content p {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.message-time {
  font-size: 12px;
  color: #9ca3af;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.modal-overlay.active {
  opacity: 1;
  pointer-events: auto;
}

.modal-content {
  background-color: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 400px;
  transform: translateY(20px);
  transition: transform 0.3s ease;
}

.modal-overlay.active .modal-content {
  transform: translateY(0);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  background-color: transparent;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.close-btn:hover {
  background-color: #f3f4f6;
}

.close-icon {
  width: 16px;
  height: 16px;
  color: #6b7280;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #4b5563;
  margin-bottom: 8px;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  color: #1f2937;
  transition: border-color 0.2s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #3b82f6;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f3f4f6;
}

.cancel-btn,
.confirm-btn {
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.cancel-btn {
  background-color: #f3f4f6;
  color: #6b7280;
  border: none;
}

.cancel-btn:hover {
  background-color: #e5e7eb;
}

.confirm-btn {
  background-color: #3b82f6;
  color: white;
  border: none;
}

.confirm-btn:hover {
  background-color: #2563eb;
}

/* 响应式设计 */
@media (min-width: 768px) {
  .personal-center {
    padding: 32px;
  }

  .info-fields {
    grid-template-columns: 1fr 1fr;
  }

  .info-row {
    border-bottom: none;
    padding: 8px 0;
  }
}
</style>
