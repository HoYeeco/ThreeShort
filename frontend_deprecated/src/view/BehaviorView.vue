<template>
  <div class="behavior-view">
    <BehaviorReport />
    <BehaviorProcessing />
    <AppealWindow />
  </div>
</template>

<script setup>
import BehaviorReport from "../behavior/BehaviorReport.vue";
import BehaviorProcessing from "../behavior/BehaviorProcessing.vue";
import AppealWindow from "../behavior/AppealWindow.vue";
</script>

<style scoped>
.behavior-view {
  padding: 32px;
}
</style>
