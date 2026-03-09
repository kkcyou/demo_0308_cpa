<template>
  <div class="bottom-nav">
    <router-link to="/daily" class="nav-item" :class="{ active: $route.path === '/daily' }">
      <el-icon><Calendar /></el-icon>
      <span>今日学习</span>
    </router-link>
    <router-link to="/chat" class="nav-item" :class="{ active: $route.path.startsWith('/chat') }">
      <el-icon><ChatDotRound /></el-icon>
      <span>AI问答</span>
    </router-link>
    <router-link to="/note" class="nav-item" :class="{ active: $route.path === '/note' }">
      <el-icon><Notebook /></el-icon>
      <span>我的笔记</span>
    </router-link>
    <router-link to="/daily/history" class="nav-item" :class="{ active: $route.path === '/daily/history' }">
      <el-icon><Clock /></el-icon>
      <span>历史记录</span>
    </router-link>
  </div>
</template>

<script setup>
import { Calendar, ChatDotRound, Notebook, Clock } from '@element-plus/icons-vue'
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-top: 1px solid #eee;
  z-index: 100;
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #999;
  font-size: 11px;
  gap: 2px;
}
.nav-item .el-icon {
  font-size: 22px;
}
.nav-item.active {
  color: #409eff;
}
</style>
