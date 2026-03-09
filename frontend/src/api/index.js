import axios from 'axios'

const request = axios.create({
  baseURL: '/',
  timeout: 60000
})

// 每日内容API
export const dailyApi = {
  getToday: () => request.get('/app-api/cpa/daily/today'),
  getByDate: (date) => request.get('/app-api/cpa/daily/get', { params: { date } }),
  getDetail: (id) => request.get('/app-api/cpa/daily/detail', { params: { id } }),
  getCoverage: () => request.get('/app-api/cpa/daily/coverage'),
  generate: () => request.post('/app-api/cpa/daily/generate')
}

// AI对话API
export const chatApi = {
  createConversation: (data) => request.post('/app-api/cpa/chat/conversation/create', data),
  listConversations: () => request.get('/app-api/cpa/chat/conversation/list'),
  deleteConversation: (id) => request.delete('/app-api/cpa/chat/conversation/delete', { params: { id } }),
  sendMessage: (data) => request.post('/app-api/cpa/chat/message/send', data),
  listMessages: (conversationId) => request.get('/app-api/cpa/chat/message/list', { params: { conversationId } })
}

// 笔记API
export const noteApi = {
  create: (data) => request.post('/app-api/cpa/note/create', data),
  update: (data) => request.put('/app-api/cpa/note/update', data),
  delete: (id) => request.delete('/app-api/cpa/note/delete', { params: { id } }),
  page: (params) => request.get('/app-api/cpa/note/page', { params }),
  toggleStar: (id) => request.put('/app-api/cpa/note/star', null, { params: { id } })
}

export default request
