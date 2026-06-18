import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  // 加载 .env 文件
  const env = loadEnv(mode, process.cwd(), '')
  // 后端 API 基础地址（可通过环境变量覆盖）
  const apiTarget = env.VITE_API_TARGET || 'http://localhost:8088'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      port: 3000,
      host: '0.0.0.0',  // 监听所有网络接口，允许局域网/公网访问
      allowedHosts: ['salary.feiyu.rest'],
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
          // 支持 WebSocket（如未来需要）
          ws: true
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      // 移除生产环境的 console 和 debugger
      minify: 'esbuild',
      sourcemap: false,
      // 优化大文件分割
      rollupOptions: {
        output: {
          // 代码分割 - 把 echarts 等大依赖分离
          manualChunks: {
            'vendor-vue': ['vue', 'vue-router', 'pinia'],
            'vendor-axios': ['axios'],
            'vendor-icons': ['lucide-vue-next'],
            'vendor-charts': ['echarts']
          },
          // 文件名带 hash 便于缓存
          chunkFileNames: 'assets/js/[name]-[hash].js',
          entryFileNames: 'assets/js/[name]-[hash].js',
          assetFileNames: 'assets/[ext]/[name]-[hash].[ext]'
        }
      },
      // 提升块大小告警阈值（ECharts 体积较大）
      chunkSizeWarningLimit: 1500,
      // 启用 CSS 代码分割
      cssCodeSplit: true
    },
    preview: {
      port: 3000,
      host: '0.0.0.0'
    },
    esbuild: {
      // 生产环境移除 console.log/debugger
      drop: mode === 'production' ? ['console', 'debugger'] : []
    }
  }
})
