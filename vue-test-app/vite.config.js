/**
 * Vite 构建与开发服务器配置。
 *
 * 开发期 /api 代理到网关、/api/ai 直连 ai-service；mock 模式下改由本地中间件响应。
 */
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { prisdocMockPlugin } from './mock/index.js'

const apiTarget = process.env.VITE_API_TARGET || 'http://localhost:8080'
const aiTarget = process.env.VITE_AI_TARGET || 'http://localhost:8083'

export default defineConfig(({ mode }) => {
  // Mock 模式：`npm run dev:mock`（即 vite --mode mock），或手动 VITE_MOCK=1
  const mockEnabled = mode === 'mock' || process.env.VITE_MOCK === '1'

  return {
    plugins: [
      vue(),
      // 仅开发服务器生效：命中 /api/** 时用本地假数据响应，不启动任何后端
      prisdocMockPlugin({ enabled: mockEnabled })
    ],
    server: {
      port: 5173,
      proxy: {
        // 拦截 AI 核心接口
        '/api/ai': {
          target: aiTarget,
          changeOrigin: true, // 建议设为 true，方便本地和服务器间的主机头切换
        },

        // 拦截 AI 技能接口
        '/api/skills': {
          target: aiTarget,
          changeOrigin: true,
        },

        '/api': {
          target: apiTarget,
          changeOrigin: false,
          configure: (proxy) => {
            proxy.on('proxyReq', (proxyReq) => {
              proxyReq.removeHeader('Origin')
              proxyReq.setHeader('Origin', 'http://localhost:5173')
              proxyReq.setHeader('Connection', 'keep-alive')
            })
            proxy.on('error', (err) => {
              // 代理失败只影响开发期联调，记录后交给 Vite 继续处理
              console.error('[proxy error]', err.message)
            })
          }
        }
      }
    }
  }
})
