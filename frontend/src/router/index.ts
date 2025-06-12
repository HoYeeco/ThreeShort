import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../view/login/LoginView.vue";
import CreditManagement from "@/view/CreditManagement.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      name: "login",
      component: LoginView,
    },
    // 可以添加更多路由...
    {
      path: "/",
      redirect: "/login", // 默认重定向到登录页
    },

    {
      path: "/credit-management",
      name: "CreditManagement",
      component: () => import("@/view/CreditManagement.vue"),
      meta: { title: "积分管理" },
    },
  ],
});

export default router;
