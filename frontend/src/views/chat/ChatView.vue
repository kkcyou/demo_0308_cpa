<template>
  <div class="chat-page">
    <div class="header">
      <el-button text @click="$router.push('/chat')" style="color:#fff">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2>{{ conversationTitle }}</h2>
      <div style="width:32px"></div>
    </div>

    <div class="messages" ref="messagesRef">
      <div v-for="msg in messages" :key="msg.id"
           class="message" :class="msg.role">
        <div class="bubble">
          <MarkdownRenderer v-if="msg.role === 'assistant'" :content="msg.content" />
          <p v-else>{{ msg.content }}</p>
        </div>
      </div>
      <div v-if="thinking" class="message assistant">
        <div class="bubble thinking">AI正在思考中...</div>
      </div>
    </div>

    <div class="input-bar">
      <el-input
        v-model="inputMessage"
        placeholder="输入你的问题..."
        @keyup.enter="send"
        :disabled="thinking"
      />
      <el-button type="primary" @click="send" :loading="thinking" :disabled="!inputMessage.trim()">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { chatApi } from '../../api'
import MarkdownRenderer from '../../components/MarkdownRenderer.vue'

const route = useRoute()
const conversationId = Number(route.params.id)
const conversationTitle = ref('AI对话')
const messages = ref([])
const inputMessage = ref('')
const thinking = ref(false)
const messagesRef = ref(null)

onMounted(async () => {
  try {
    const [convRes, msgRes] = await Promise.all([
      chatApi.listConversations(),
      chatApi.listMessages(conversationId)
    ])
    const conv = convRes.data.find(c => c.id === conversationId)
    if (conv) conversationTitle.value = conv.title
    messages.value = msgRes.data
    scrollToBottom()
  } catch (e) {
    ElMessage.error('加载失败')
  }
})

const send = async () => {
  const msg = inputMessage.value.trim()
  if (!msg || thinking.value) return

  // 显示用户消息
  messages.value.push({ id: Date.now(), role: 'user', content: msg })
  inputMessage.value = ''
  thinking.value = true
  scrollToBottom()

  try {
    const res = await chatApi.sendMessage({ conversationId, message: msg })
    messages.value.push({ id: Date.now() + 1, role: 'assistant', content: res.data })
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    thinking.value = false
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.chat-page { display: flex; flex-direction: column; height: 100vh; max-width: 768px; margin: 0 auto; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: linear-gradient(135deg, #67c23a, #85ce61); color: #fff;
  flex-shrink: 0;
}
.header h2 { font-size: 16px; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.messages { flex: 1; overflow-y: auto; padding: 16px; background: #f5f5f5; }
.message { display: flex; margin-bottom: 16px; }
.message.user { justify-content: flex-end; }
.message.assistant { justify-content: flex-start; }
.bubble {
  max-width: 85%; padding: 12px 16px; border-radius: 12px;
  font-size: 15px; line-height: 1.6;
}
.message.user .bubble { background: #409eff; color: #fff; border-bottom-right-radius: 4px; }
.message.assistant .bubble { background: #fff; color: #333; border-bottom-left-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.thinking { color: #999; font-style: italic; }
.input-bar {
  display: flex; gap: 8px; padding: 12px 16px;
  background: #fff; border-top: 1px solid #eee; flex-shrink: 0;
}
.input-bar .el-input { flex: 1; }
</style>
