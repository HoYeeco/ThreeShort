<template>
  <div class="credit-management">
    <!-- 诚信分总览 -->
    <div class="credit-overview card">
      <h2 class="section-title">当前诚信分</h2>
      <div class="credit-status">
        <div class="credit-value" :class="{ 'low-credit': currentCredit < 60 }">
          {{ currentCredit }} 分
        </div>
        <div class="credit-info">
          <p>状态：{{ currentCredit >= 80 ? "优秀" : currentCredit >= 60 ? "正常" : "警告" }}</p>
          <p>周期结算日：2024-12-31</p>
        </div>
      </div>

      <!-- 低信用警告弹窗 -->
      <div v-if="currentCredit < 60" class="warning-modal">
        <div class="modal-content">
          <h3 class="modal-title">⚠️ 诚信分异常警告</h3>
          <p>您当前诚信分低于60分（{{ currentCredit }}分），已触发以下措施：</p>
          <ul>
            <li>社区内部通报批评</li>
            <li>暂停本月社区福利领取</li>
            <li>需参加诚信教育课程</li>
          </ul>
          <button class="modal-close" @click="closeWarning">知道了</button>
        </div>
      </div>
    </div>

    <!-- 积分明细 -->
    <div class="credit-history card">
      <h2 class="section-title">诚信分变动明细</h2>
      <div class="table-container">
        <table class="history-table">
          <thead>
            <tr>
              <th>时间</th>
              <th>类型</th>
              <th>原因</th>
              <th>分数变动</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in creditHistory" :key="item.id">
              <td>{{ item.time }}</td>
              <td :class="item.type === 'add' ? 'text-green' : 'text-red'">
                {{ item.type === "add" ? "加分" : "扣分" }}
              </td>
              <td>{{ item.reason }}</td>
              <td :class="item.type === 'add' ? 'text-green' : 'text-red'">
                {{ item.type === "add" ? "+" : "-" }}{{ item.value }}分
              </td>
              <td>
                <button v-if="item.type === 'deduct'" class="appeal-btn" @click="showDetail(item)">
                  申诉
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 奖励分与礼品兑换 -->
    <div class="reward-section card">
      <h2 class="section-title">奖励分与礼品兑换</h2>
      <div class="reward-container">
        <div class="reward-info">
          <p class="reward-value">当前奖励分：{{ rewardPoints }} 分</p>
          <p>奖励分规则：周期结算时诚信分≥80分，按超出部分的10%获得奖励分</p>
        </div>
        <div class="gift-grid">
          <div
            v-for="gift in gifts"
            :key="gift.id"
            class="gift-item"
            :class="{ unavailable: rewardPoints < gift.points }"
          >
            <div class="gift-image-container">
              <img :src="gift.cover" :alt="gift.name" class="gift-cover" />
              <div v-if="rewardPoints < gift.points" class="overlay">
                <div class="overlay-text">积分不足</div>
              </div>
            </div>
            <div class="gift-detail">
              <h4 class="gift-name">{{ gift.name }}</h4>
              <p class="gift-points">需要：{{ gift.points }} 奖励分</p>
              <button
                class="exchange-btn"
                @click="exchangeGift(gift)"
                :disabled="rewardPoints < gift.points"
              >
                {{ rewardPoints >= gift.points ? "兑换" : "积分不足" }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分数申诉 -->
    <div class="appeal-section card">
      <h2 class="section-title">分数异议申诉</h2>
      <form @submit.prevent="submitAppeal" class="appeal-form">
        <div class="form-group">
          <label for="appealReason">申诉原因</label>
          <textarea
            id="appealReason"
            v-model="appealForm.reason"
            placeholder="请详细描述申诉理由"
            required
          ></textarea>
        </div>
        <div class="form-group">
          <label>证明材料</label>
          <div class="file-upload">
            <input
              type="file"
              id="proofFile"
              @change="handleFileUpload"
              accept="image/*, application/pdf"
              ref="fileInput"
            />
            <label for="proofFile" class="file-label">
              <span class="file-icon">📎</span>
              <span class="file-text">上传证明材料</span>
            </label>
          </div>
          <div v-if="appealForm.proof" class="file-preview">
            <img
              v-if="isImage(appealForm.proof.type)"
              :src="URL.createObjectURL(appealForm.proof)"
              alt="预览"
              class="preview-image"
            />
            <div v-else class="preview-file">
              <span class="file-icon">📄</span>
              <span class="file-name">{{ appealForm.proof.name }}</span>
            </div>
            <button type="button" class="remove-file" @click="removeFile">
              <span class="remove-icon">×</span>
            </button>
          </div>
        </div>
        <button type="submit" class="submit-btn">提交申诉</button>
      </form>
    </div>

    <!-- 积分明细详情弹窗 -->
    <div v-if="showDetailModal" class="detail-modal">
      <div class="modal-content">
        <h3 class="modal-title">积分变动详情</h3>
        <div class="detail-info">
          <p><span class="detail-label">时间：</span>{{ detailItem.time }}</p>
          <p>
            <span class="detail-label">类型：</span
            >{{ detailItem.type === "add" ? "加分" : "扣分" }}
          </p>
          <p><span class="detail-label">原因：</span>{{ detailItem.reason }}</p>
          <p>
            <span class="detail-label">分数变动：</span>{{ detailItem.type === "add" ? "+" : "-"
            }}{{ detailItem.value }}分
          </p>
          <p>
            <span class="detail-label">处理人：</span>{{ detailItem.handler || "系统自动处理" }}
          </p>
          <p><span class="detail-label">处理时间：</span>{{ detailItem.handleTime || "-" }}</p>
        </div>
        <div class="modal-actions">
          <button class="close-btn" @click="closeDetail">关闭</button>
          <button
            v-if="detailItem.type === 'deduct'"
            class="appeal-btn"
            @click="openAppealForm(detailItem)"
          >
            提交申诉
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";

// 模拟数据
const currentCredit = ref(78); // 当前诚信分（可修改测试不同状态）
const rewardPoints = ref(235); // 当前奖励分
const creditHistory = ref([
  {
    id: 1,
    time: "2024-05-10 14:30",
    type: "add",
    value: 15,
    reason: "参与社区环保活动",
    handler: "王管理员",
    handleTime: "2024-05-10 15:15",
  },
  {
    id: 2,
    time: "2024-05-15 09:10",
    type: "add",
    value: 10,
    reason: "帮助独居老人",
    handler: "李管理员",
    handleTime: "2024-05-15 10:30",
  },
  {
    id: 3,
    time: "2024-05-20 16:20",
    type: "deduct",
    value: 8,
    reason: "未按规定分类垃圾",
    handler: "张管理员",
    handleTime: "2024-05-20 17:00",
  },
  {
    id: 4,
    time: "2024-05-25 11:45",
    type: "deduct",
    value: 5,
    reason: "公共区域乱停车",
    handler: "赵管理员",
    handleTime: "2024-05-25 13:20",
  },
]);

// 礼品数据 - 已添加图片路径
const gifts = ref([
  {
    id: 1,
    name: "便民服务券",
    points: 80,
    cover: "/gifts/service-coupon.png",
    description: "可兑换社区便民服务一次",
  },
  {
    id: 2,
    name: "超市代金券",
    points: 150,
    cover: "/gifts/supermarket-coupon.png",
    description: "社区合作超市50元代金券",
  },
  {
    id: 3,
    name: "文化活动票",
    points: 120,
    cover: "/gifts/culture-ticket.png",
    description: "社区文化活动入场券一张",
  },
  {
    id: 4,
    name: "绿色植物",
    points: 200,
    cover: "/gifts/plant.png",
    description: "小型盆栽绿色植物一盆",
  },
  {
    id: 5,
    name: "健康体检券",
    points: 300,
    cover: "/gifts/health-check.png",
    description: "社区健康体检服务一次",
  },
  {
    id: 6,
    name: "社区图书馆借书卡",
    points: 250,
    cover: "/gifts/library-card.png",
    description: "社区图书馆借书卡一年使用权",
  },
]);

// 申诉表单状态
const appealForm = reactive({
  reason: "",
  proof: null,
  relatedItemId: null,
});

// 弹窗状态
const showDetailModal = ref(false);
const detailItem = ref(null);
const fileInput = ref(null);

// 关闭警告弹窗
const closeWarning = () => {
  currentCredit.value = 65; // 测试用：关闭后恢复到正常状态
};

// 兑换礼品逻辑
const exchangeGift = (gift) => {
  if (rewardPoints.value >= gift.points) {
    rewardPoints.value -= gift.points;
    alert(`成功兑换${gift.name}！剩余奖励分：${rewardPoints.value}`);
  }
};

// 处理文件上传
const handleFileUpload = (e) => {
  const file = e.target.files[0];
  if (file) {
    appealForm.proof = file;
  }
};

// 移除已上传文件
const removeFile = () => {
  appealForm.proof = null;
  fileInput.value.value = "";
};

// 判断文件是否为图片
const isImage = (type) => {
  return type.startsWith("image/");
};

// 提交申诉
const submitAppeal = () => {
  if (!appealForm.reason) {
    alert("请填写申诉理由");
    return;
  }

  if (!appealForm.proof) {
    alert("请上传证明材料");
    return;
  }

  // 实际开发中这里应调用API提交
  console.log("申诉提交:", {
    reason: appealForm.reason,
    proof: appealForm.proof,
    relatedItemId: appealForm.relatedItemId,
  });

  alert("申诉已提交，3个工作日内处理");

  // 清空表单
  appealForm.reason = "";
  appealForm.proof = null;
  appealForm.relatedItemId = null;
};

// 显示积分明细详情
const showDetail = (item) => {
  detailItem.value = item;
  showDetailModal.value = true;
};

// 关闭积分明细详情
const closeDetail = () => {
  showDetailModal.value = false;
};

// 打开针对特定记录的申诉表单
const openAppealForm = (item) => {
  closeDetail();
  appealForm.relatedItemId = item.id;
  // 平滑滚动到申诉表单
  document.querySelector(".appeal-section").scrollIntoView({ behavior: "smooth" });
};
</script>

<style scoped>
.credit-management {
  padding: 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  padding: 24px;
  margin-bottom: 32px;
  transition: all 0.3s ease;
}

.card:hover {
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: "";
  width: 4px;
  height: 20px;
  background: #3b82f6;
  border-radius: 2px;
  margin-right: 8px;
}

/* 诚信分总览 */
.credit-overview {
  position: relative;
  overflow: hidden;
}

.credit-overview::after {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  width: 150px;
  height: 150px;
  background: url("/icons/credit-badge.png") no-repeat;
  background-size: contain;
  opacity: 0.1;
  transform: rotate(15deg);
}

.credit-status {
  display: flex;
  align-items: center;
  gap: 32px;
}

.credit-value {
  font-size: 48px;
  font-weight: 700;
  color: #10b981;
  position: relative;
}

.credit-value::after {
  content: "";
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 100%;
  height: 3px;
  background: #10b981;
  border-radius: 2px;
}

.credit-value.low-credit {
  color: #ef4444;
}

.credit-value.low-credit::after {
  background: #ef4444;
}

.credit-info p {
  font-size: 16px;
  color: #6b7280;
  margin: 8px 0;
}

.warning-modal {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.modal-content {
  background: white;
  padding: 24px;
  border-radius: 12px;
  min-width: 400px;
  max-width: 600px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

.modal-title {
  color: #ef4444;
  margin-bottom: 16px;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.modal-title::before {
  content: "⚠️";
  margin-right: 8px;
}

.modal-close {
  margin-top: 24px;
  padding: 8px 16px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: #dc2626;
}

/* 积分明细表格 */
.table-container {
  overflow-x: auto;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;
}

.history-table th,
.history-table td {
  padding: 16px 12px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

.history-table th {
  color: #6b7280;
  font-weight: 600;
  background: #f9fafb;
  position: sticky;
  top: 0;
  z-index: 10;
}

.history-table tbody tr {
  transition: background-color 0.2s ease;
}

.history-table tbody tr:hover {
  background: #f3f4f6;
}

.text-green {
  color: #10b981;
}
.text-red {
  color: #ef4444;
}

.appeal-btn {
  padding: 6px 12px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.appeal-btn:hover {
  background: #2563eb;
}

/* 奖励分与礼品 */
.reward-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.reward-info {
  padding: 16px;
  background: #f0fdf4;
  border-radius: 12px;
  border-left: 4px solid #16a34a;
}

.reward-value {
  font-size: 24px;
  font-weight: 700;
  color: #16a34a;
  margin-bottom: 8px;
}

.gift-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 24px;
}

.gift-item {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
}

.gift-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.gift-item.unavailable {
  opacity: 0.7;
  cursor: not-allowed;
}

.gift-image-container {
  position: relative;
  height: 160px;
  background: #f9fafb;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.gift-cover {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.5s ease;
}

.gift-item:hover .gift-cover {
  transform: scale(1.05);
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.gift-item.unavailable .overlay {
  opacity: 1;
}

.overlay-text {
  color: white;
  font-weight: 600;
}

.gift-detail {
  padding: 12px;
  text-align: center;
}

.gift-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.gift-points {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
}

.exchange-btn {
  width: 100%;
  padding: 8px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.exchange-btn:hover {
  background: #2563eb;
}

.exchange-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

/* 申诉表单 */
.appeal-form {
  max-width: 600px;
  margin: 0 auto;
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

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  min-height: 100px;
  resize: vertical;
  transition: border-color 0.2s ease;
}

.form-group textarea:focus {
  outline: none;
  border-color: #3b82f6;
}

.file-upload {
  position: relative;
  border: 2px dashed #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  transition: all 0.2s ease;
}

.file-upload:hover {
  border-color: #3b82f6;
  background: #f3f4f6;
}

.file-upload input[type="file"] {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.file-label {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.file-icon {
  font-size: 24px;
  color: #6b7280;
}

.file-text {
  color: #6b7280;
  font-size: 14px;
}

.file-preview {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.preview-image {
  max-width: 80px;
  max-height: 80px;
  object-fit: cover;
  border-radius: 6px;
}

.preview-file {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name {
  color: #374151;
  font-size: 14px;
}

.remove-file {
  background: none;
  border: none;
  cursor: pointer;
  color: #6b7280;
  margin-left: auto;
}

.remove-icon {
  font-size: 18px;
}

.remove-file:hover {
  color: #ef4444;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #409eff 0%, #3375b9 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.submit-btn::after {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.3) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: rotate(30deg);
  animation: shine 3s infinite;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
}

/* 积分明细详情弹窗 */
.detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.detail-info {
  margin-bottom: 24px;
}

.detail-info p {
  margin: 8px 0;
}

.detail-label {
  font-weight: 600;
  color: #4b5563;
  display: inline-block;
  width: 80px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.close-btn {
  padding: 8px 16px;
  background: #e5e7eb;
  color: #374151;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #d1d5db;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes shine {
  0% {
    transform: translateX(-100%) rotate(30deg);
  }
  100% {
    transform: translateX(100%) rotate(30deg);
  }
}
</style>
