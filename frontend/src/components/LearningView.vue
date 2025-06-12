<template>
  <div class="learning-view">
    <!-- 公约学习标题 -->
    <div class="learning-header">
      <h1 class="learning-title">居民公约学习</h1>
      <p class="learning-subtitle">共同遵守公约，共建美好社区</p>
    </div>

    <!-- 公约学习内容卡片 -->
    <div class="learning-content-cards">
      <div class="learning-card">
        <div class="card-header">
          <div class="card-icon">
            <i class="fa fa-users"></i>
          </div>
          <h2 class="content-title">居民公约</h2>
        </div>
        <div class="card-body">
          <ul class="content-list">
            <li>尊重邻居的生活习惯，避免在休息时间（22:00 - 07:00）进行装修等产生噪音的活动。</li>
            <li>互帮互助，当邻居遇到困难时，主动伸出援手，如帮忙代收快递等。</li>
            <li>友好沟通，遇到问题时，以平和的方式解决矛盾，避免争吵和冲突。</li>
          </ul>
        </div>
      </div>
      <div class="learning-card">
        <div class="card-header">
          <div class="card-icon">
            <i class="fa fa-building"></i>
          </div>
          <h2 class="content-title">文明行为公约</h2>
        </div>
        <div class="card-body">
          <ul class="content-list">
            <li>合理使用公共设施，不恶意损坏，如电梯、健身器材等。</li>
            <li>使用后及时归还或整理好公共设施，方便他人使用，如归还社区图书馆的书籍。</li>
            <li>发现公共设施损坏时，及时向社区管理部门报告，以便及时维修。</li>
          </ul>
        </div>
      </div>
      <div class="learning-card">
        <div class="card-header">
          <div class="card-icon">
            <i class="fa fa-leaf"></i>
          </div>
          <h2 class="content-title">环境保护公约</h2>
        </div>
        <div class="card-body">
          <ul class="content-list">
            <li>保持公共区域整洁，不随意丢弃垃圾，应将垃圾分类投放至指定垃圾桶。</li>
            <li>定期清理自家门前屋后，维护周边环境美观，不得在公共通道堆放杂物。</li>
            <li>爱护社区绿化，不践踏草坪、攀折花木，不私自占用公共绿地。</li>
          </ul>
        </div>
      </div>

      <div class="learning-card">
        <div class="card-header">
          <div class="card-icon">
            <i class="fa fa-shield"></i>
          </div>
          <h2 class="content-title">安全管理公约</h2>
        </div>
        <div class="card-body">
          <ul class="content-list">
            <li>遵守社区的消防安全规定，不占用消防通道，定期检查自家电器设备。</li>
            <li>加强自我防范意识，注意个人财物安全，不随意给陌生人开门。</li>
            <li>积极参与社区组织的安全培训和演练，提高应对突发事件的能力。</li>
          </ul>
        </div>
      </div>


    </div>

    <!-- 学习进度统计 -->
    <div class="learning-progress">
      <ProgressChart title="公约学习进度统计" :data="learningProgressData" />
    </div>

    <!-- 学习资源 -->
    <div class="learning-resources">
      <h2 class="resources-title">学习资源</h2>
      <p class="resources-text">为了帮助您更好地学习居民公约，我们提供了以下学习资源：</p>
      <div class="resource-cards">
        <div class="resource-card">
          <div class="resource-icon">
            <i class="fa fa-file-text"></i>
          </div>
          <h3 class="resource-card-title">《居民公约手册》</h3>
          <a href="#" class="resource-link">在线阅读</a>
        </div>
        <div class="resource-card">
          <div class="resource-icon">
            <i class="fa fa-play-circle"></i>
          </div>
          <h3 class="resource-card-title">社区公约学习视频教程</h3>
          <a href="#" class="resource-link">观看教程</a>
        </div>
        <div class="resource-card">
          <div class="resource-icon">
            <i class="fa fa-question-circle"></i>
          </div>
          <h3 class="resource-card-title">公约知识问答活动链接</h3>
          <a href="#" class="resource-link">参与活动</a>
        </div>
      </div>
    </div>

    <!-- 互动问答区域 -->
    <div class="learning-qa">
      <h2 class="qa-title">互动问答</h2>
      <p class="qa-text">如果您在学习过程中有任何疑问，可以在下方留言，我们会及时为您解答。</p>
      <form @submit.prevent="submitQuestion" class="qa-form">
        <div class="form-group">
          <label for="question">您的问题</label>
          <textarea id="question" v-model="question" placeholder="请输入您的问题" required></textarea>
        </div>
        <button type="submit" class="submit-btn">提交问题</button>
      </form>
      <div v-if="questions.length > 0" class="qa-list">
        <h3 class="qa-list-title">常见问题及解答</h3>
        <div v-for="(q, index) in questions" :key="index" class="qa-item">
          <p class="qa-question"><strong>问题：</strong>{{ q.question }}</p>
          <p class="qa-answer"><strong>解答：</strong>{{ q.answer }}</p>
        </div>
      </div>
    </div>

    <!-- 学习小贴士 -->
    <div class="learning-tips">
      <h2 class="tips-title">学习小贴士</h2>
      <p class="tips-text">为了更好地学习和理解居民公约，您可以结合社区举办的宣传活动进行学习。同时，将公约内容融入日常生活中，养成良好的行为习惯。</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import ProgressChart from '../components/ProgressChart.vue';

const learningProgressData = ref([
  { label: '居民公约', value: 85, color: '#3B82F6' },
  { label: '文明行为规范', value: 72, color: '#10B981' },
  { label: '环境保护条例', value: 68, color: '#8B5CF6' },
  { label: '安全管理制度', value: 91, color: '#F59E0B' },
]);

const question = ref('');
const questions = ref([
  { question: '公约中的环境卫生条款具体包括哪些内容？', answer: '环境卫生条款包括保持公共区域整洁、定期清理自家周边环境、爱护社区绿化等内容。' },
  { question: '邻里之间发生矛盾该如何解决？', answer: '应友好沟通，以平和的方式解决矛盾，避免争吵和冲突。' },
]);
const submitQuestion = () => {
  if (question.value) {
    questions.value.push({ question: question.value, answer: '待解答' });
    question.value = '';
  }
};
</script>

<style scoped>
.learning-view {
  min-height: 100vh;
  background: linear-gradient(to bottom right, #f9fafb, #eff6ff);
  padding: 32px;
}

.learning-header {
  text-align: center;
  margin-bottom: 32px;
}

.learning-title {
  font-size: 36px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.learning-subtitle {
  font-size: 18px;
  color: #6b7280;
}

.learning-content-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.learning-card {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  overflow: hidden;
  transition: all 0.3s ease;
}

.learning-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background-color: #f9fafb;
}

.card-icon {
  width: 32px;
  height: 32px;
  background-color: #e2e8f0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  font-size: 18px;
}

.content-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.card-body {
  padding: 24px;
}

.content-text {
  font-size: 16px;
  line-height: 1.6;
  color: #6b7280;
  margin-bottom: 16px;
}

.content-list {
  list-style-type: disc;
  padding-left: 20px;
  margin-bottom: 16px;
}

.content-list li {
  font-size: 16px;
  line-height: 1.6;
  color: #6b7280;
  margin-bottom: 8px;
}

.learning-progress {
  margin-bottom: 32px;
}

.learning-resources {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
  margin-bottom: 32px;
}

.resources-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.resources-text {
  font-size: 16px;
  line-height: 1.6;
  color: #6b7280;
  margin-bottom: 16px;
}

.resource-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 24px;
}

.resource-card {
  background-color: #f9fafb;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
  text-align: center;
  transition: all 0.3s ease;
}

.resource-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.resource-icon {
  width: 48px;
  height: 48px;
  background-color: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  font-size: 24px;
  margin: 0 auto 16px;
}

.resource-card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.resource-link {
  color: #3B82F6;
  text-decoration: none;
  font-weight: 600;
}

.resource-link:hover {
  text-decoration: underline;
}

.learning-qa {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
  margin-bottom: 32px;
}

.qa-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.qa-text {
  font-size: 16px;
  line-height: 1.6;
  color: #6b7280;
  margin-bottom: 16px;
}

.qa-form .form-group {
  margin-bottom: 16px;
}

.qa-form label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 600;
  font-size: 14px;
}

.qa-form textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  min-height: 100px;
  resize: vertical;
  transition: border-color 0.2s ease;
}

.qa-form textarea:focus {
  outline: none;
  border-color: #3b82f6;
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

.qa-list {
  margin-top: 24px;
}

.qa-list-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.qa-item {
  margin-bottom: 16px;
}

.qa-question {
  font-size: 16px;
  line-height: 1.6;
  color: #1f2937;
  margin-bottom: 4px;
}

.qa-answer {
  font-size: 16px;
  line-height: 1.6;
  color: #6b7280;
}

.learning-tips {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.tips-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.tips-text {
  font-size: 16px;
  line-height: 1.6;
  color: #6b7280;
}
</style>