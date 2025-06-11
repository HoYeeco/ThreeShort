import { createApp } from "vue";
import { createRouter, createWebHistory } from "vue-router";
import App from "./App.vue";

// 导入页面组件
import LoginView from "./view/login/LoginView.vue";
import HomeView from "./view/HomeView.vue";
import CreditManagement from "./view/CreditManagement.vue";
import LearningView from "./view/LearningView.vue";
// 路由配置
const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", component: LoginView },
  { path: "/home", component: HomeView },
  { path: "/credit", component: CreditManagement },
  { path: "/learning", component: LearningView },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

const app = createApp(App);
app.use(router);
app.mount("#app");

