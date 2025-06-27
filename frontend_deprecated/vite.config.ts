const { fileURLToPath, URL } = require("url");
import { defineConfig } from "vite";
const vue = require("@vitejs/plugin-vue");
const vueDevTools = require("vite-plugin-vue-devtools");

// 导入Tailwind CSS的PostCSS插件
const tailwindcss = require("@tailwindcss/postcss");
const autoprefixer = require("autoprefixer");

// https://vite.dev/config/
module.exports = defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  css: {
    postcss: {
      plugins: [
        tailwindcss("./tailwind.config.js"), // 指定配置文件路径
        autoprefixer,
      ],
    },
  },
});
