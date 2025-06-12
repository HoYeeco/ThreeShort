<template>
  <div class="login-container">
    <!-- 左侧系统展示区 -->
    <div class="left-panel">
      <div class="system-info">
        <div class="system-logo">
          <img src="/public/system-logo.png" alt="Logo" />
          <Home class="w-16 h-16 text-white" />
        </div>
        <h1 class="system-name">社区居民诚信管理系统</h1>
        <p class="system-desc">高效 · 安全 · 智能</p>
        <div class="features">
          <div class="feature-item">
            <Users class="w-5 h-5" />
            <span>居民信息管理</span>
          </div>
          <div class="feature-item">
            <Award class="w-5 h-5" />
            <span>诚信积分系统</span>
          </div>
          <div class="feature-item">
            <BookOpen class="w-5 h-5" />
            <span>公约学习平台</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="right-panel">
      <div class="login-form">
        <h2 class="login-title">用户登录</h2>
        <form @submit.prevent="onSubmit">
          <div class="form-group">
            <label for="username">用户名</label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              required
            />
          </div>
          <div class="form-group">
            <label for="password">密码</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              required
            />
          </div>
          <div class="form-options">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.remember" />
              <span class="checkmark"></span>
              记住密码
            </label>
            <a href="#" class="forgot-password">忘记密码？</a>
          </div>
          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="loading">登录中...</span>
            <span v-else>登录</span>
          </button>
        </form>
        <div class="login-footer">
          <p>还没有账号？<a href="#" class="register-link">立即注册</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { Home, Users, Award, BookOpen } from "lucide-vue-next";

const router = useRouter();
const loading = ref(false);

const form = reactive({
  username: "",
  password: "",
  remember: false,
});

const onSubmit = async () => {
  if (!form.username || !form.password) {
    alert("请输入用户名和密码");
    return;
  }

  loading.value = true;

  try {
    // 模拟登录API调用
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // 模拟登录成功
    if (form.username && form.password) {
      // 这里可以添加实际的登录验证逻辑
      localStorage.setItem("isLoggedIn", "true");
      localStorage.setItem("username", form.username);

      // 跳转到主页
      router.push("/home");
    }
  } catch (error) {
    alert("登录失败，请重试");
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* 整体容器样式 */
.login-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 左侧面板样式 */
.left-panel {
  flex: 1;
  background: linear-gradient(135deg, #409eff 0%, #3375b9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  overflow: hidden;
}

.left-panel::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.1"/><circle cx="10" cy="60" r="0.5" fill="white" opacity="0.1"/><circle cx="90" cy="40" r="0.5" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  pointer-events: none;
}

.system-info {
  text-align: center;
  padding: 0 40px;
  max-width: 500px;
  position: relative;
  z-index: 1;
}

.system-logo {
  width: 120px;
  height: 120px;
  margin: 0 auto 30px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden; /* 确保图片不超出圆形框 */
}

.system-logo img {
  max-width: 100%; /* 图片最大宽度为容器的80% */
  max-height: 100%; /* 图片最大高度为容器的80% */
  object-fit: contain; /* 保持图片比例，不拉伸 */
}

.system-name {
  font-size: 36px;
  margin-bottom: 15px;
  letter-spacing: 2px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.system-desc {
  font-size: 18px;
  opacity: 0.9;
  letter-spacing: 1px;
  margin-bottom: 40px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(5px);
}

/* 右侧面板样式 */
.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px;
}

.login-form {
  width: 100%;
  max-width: 420px;
  padding: 48px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-title {
  text-align: center;
  margin-bottom: 40px;
  color: #333;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 600;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 16px 20px;
  border: 2px solid #e1e5e9;
  border-radius: 12px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: #fafbfc;
}

.form-group input:focus {
  outline: none;
  border-color: #409eff;
  background: white;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.checkbox-label input[type="checkbox"] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid #ddd;
  border-radius: 4px;
  margin-right: 8px;
  position: relative;
  transition: all 0.3s ease;
}

.checkbox-label input[type="checkbox"]:checked + .checkmark {
  background: #409eff;
  border-color: #409eff;
}

.checkbox-label input[type="checkbox"]:checked + .checkmark::after {
  content: "✓";
  position: absolute;
  top: -2px;
  left: 2px;
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.forgot-password {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #3375b9;
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #409eff 0%, #3375b9 100%);
  border: none;
  border-radius: 12px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.login-footer p {
  color: #666;
  font-size: 14px;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #3375b9;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .left-panel {
    padding: 40px 20px;
    flex: none;
    height: auto;
    min-height: 40vh;
  }

  .right-panel {
    padding: 20px;
    flex: 1;
  }

  .system-logo {
    width: 80px;
    height: 80px;
  }

  .system-name {
    font-size: 24px;
  }

  .features {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 8px;
  }

  .feature-item {
    flex: 1;
    min-width: 120px;
    padding: 8px 12px;
    font-size: 12px;
  }

  .login-form {
    padding: 32px 24px;
  }
}

@media (max-width: 480px) {
  .features {
    display: none;
  }

  .login-form {
    padding: 24px 20px;
  }

  .login-title {
    font-size: 24px;
  }
}
</style>
