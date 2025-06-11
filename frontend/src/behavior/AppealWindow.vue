<template>
  <div class="appeal-window">
    <h3 class="appeal-window-title">申诉窗口</h3>
    <form @submit.prevent="onSubmit">
      <div class="form-group">
        <label for="appealUser">申诉居民</label>
        <input
          id="appealUser"
          v-model="form.appealUser"
          type="text"
          placeholder="请输入申诉居民姓名"
          required
        />
      </div>
      <div class="form-group">
        <label for="appealReason">申诉理由</label>
        <textarea
          id="appealReason"
          v-model="form.appealReason"
          placeholder="请详细描述申诉理由"
          required
        ></textarea>
      </div>
      <button type="submit" class="submit-btn">提交申诉</button>
    </form>
  </div>
</template>

<script setup>
import { reactive } from "vue";

const form = reactive({
  appealUser: "",
  appealReason: "",
});

const onSubmit = () => {
  // 这里可以添加提交申诉表单的逻辑
  console.log("申诉表单提交:", form);
};
</script>

<style scoped>
.appeal-window {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.appeal-window-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 600;
  font-size: 14px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 12px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: #fafbfc;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #409eff;
  background: white;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #409eff 0%, #3375b9 100%);
  border: none;
  border-radius: 12px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
}
</style>
