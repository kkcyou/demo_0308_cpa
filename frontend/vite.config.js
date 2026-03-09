import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/app-api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/admin-api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
