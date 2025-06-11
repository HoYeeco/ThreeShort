<template>
  <div class="behavior-processing">
    <h3 class="behavior-processing-title">行为处理</h3>
    <table>
      <thead>
        <tr>
          <th>被举报居民</th>
          <th>行为描述</th>
          <th>处理状态</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in behaviorList" :key="item.id">
          <td>{{ item.reportedUser }}</td>
          <td>{{ item.behaviorDescription }}</td>
          <td>{{ item.processingStatus }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref } from "vue";

const behaviorList = ref([
  {
    id: 1,
    reportedUser: "张三",
    behaviorDescription: "参与社区志愿服务",
    processingStatus: "AI已处理",
  },
  {
    id: 2,
    reportedUser: "李四",
    behaviorDescription: "违反社区公约",
    processingStatus: "待居委会审查",
  },
]);
</script>

<style scoped>
.behavior-processing {
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 24px;
  margin-bottom: 24px;
}

.behavior-processing-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

th {
  font-weight: 600;
  color: #6b7280;
}
</style>
