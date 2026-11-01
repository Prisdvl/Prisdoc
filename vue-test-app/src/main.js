import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
// Space Grotesk：数字与拉丁字形走本地打包字体（不引外链，离线可用）
import '@fontsource/space-grotesk/400.css'
import '@fontsource/space-grotesk/500.css'
import '@fontsource/space-grotesk/600.css'
import '@fontsource/space-grotesk/700.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './style.css'
// 设计令牌 + Element Plus 暗色映射（必须排在 Element Plus 样式之后）
import './styles/prisdoc-tokens.css'
import './collaboration/autoInit'

// Prisdoc 只有深色一套主题：根节点常驻 dark
document.documentElement.classList.add('dark')

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.use(router)
app.use(ElementPlus)
app.mount('#app')
