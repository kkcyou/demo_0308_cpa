import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/daily'
  },
  {
    path: '/daily',
    name: 'DailyToday',
    component: () => import('../views/daily/TodayView.vue'),
    meta: { title: '今日学习' }
  },
  {
    path: '/daily/history',
    name: 'DailyHistory',
    component: () => import('../views/daily/HistoryView.vue'),
    meta: { title: '历史记录' }
  },
  {
    path: '/chat',
    name: 'ChatList',
    component: () => import('../views/chat/ChatList.vue'),
    meta: { title: 'AI问答' }
  },
  {
    path: '/chat/:id',
    name: 'ChatView',
    component: () => import('../views/chat/ChatView.vue'),
    meta: { title: 'AI对话' }
  },
  {
    path: '/note',
    name: 'NoteList',
    component: () => import('../views/note/NoteList.vue'),
    meta: { title: '我的笔记' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = `${to.meta.title || 'CPA Daily'} - 注会每日学习助手`
})

export default router
