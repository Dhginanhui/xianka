import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 9091,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:9091',
        changeOrigin: true
      }
    }
  }
})
