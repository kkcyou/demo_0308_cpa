<template>
  <div class="chat-list-page">
    <div class="header">
      <h2>AI问答</h2>
      <el-button type="primary" size="small" @click="showNewDialog = true">+ 新对话</el-button>
    </div>

    <div class="content">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="conversations.length === 0">
        <el-empty description="暂无对话，开始你的第一次AI问答吧" />
      </div>

      <div v-else class="conversation-list">
        <div v-for="conv in conversations" :key="conv.id"
             class="conversation-item" @click="$router.push(`/chat/${conv.id}`)">
          <div class="conv-info">
            <h4>{{ conv.title }}</h4>
            <p>{{ conv.messageCount }} 条消息</p>
          </div>
          <el-button text type="danger" @click.stop="deleteConv(conv.id)" size="small">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新建对话 -->
    <el-dialog v-model="showNewDialog" title="新建AI对话" width="90%">
      <el-form>
        <el-form-item label="对话标题">
          <el-input v-model="newForm.title" placeholder="例如：长期股权投资疑问" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="newForm.subject" placeholder="选择科目" clearable style="width:100%">
            <el-option v-for="s in subjects" :key="s.code" :label="s.name" :value="s.code" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNewDialog = false">取消</el-button>
        <el-button type="primary" @click="createConv">创建</el-button>
      </template>
    </el-dialog>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { chatApi } from '../../api'
import BottomNav from '../../components/BottomNav.vue'

const router = useRouter()
const loading = ref(true)
const conversations = ref([])
const showNewDialog = ref(false)
const newForm = ref({ title: '', subject: null })

const subjects = [
  { code: 1, name: '会计' }, { code: 2, name: '审计' }, { code: 3, name: '财管' },
  { code: 4, name: '经济法' }, { code: 5, name: '税法' }, { code: 6, name: '战略' }
]

onMounted(async () => {
  try {
    const res = await chatApi.listConversations()
    conversations.value = res.data
  } finally {
    loading.value = false
  }
})

const createConv = async () => {
  if (!newForm.value.title) {
    ElMessage.warning('请输入对话标题')
    return
  }
  const res = await chatApi.createConversation(newForm.value)
  showNewDialog.value = false
  router.push(`/chat/${res.data.id}`)
}

const deleteConv = async (id) => {
  await ElMessageBox.confirm('确定删除此对话？', '提示', { type: 'warning' })
  await chatApi.deleteConversation(id)
  conversations.value = conversations.value.filter(c => c.id !== id)
  ElMessage.success('已删除')
}
</script>

<style scoped>
.chat-list-page { padding-bottom: 70px; max-width: 768px; margin: 0 auto; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px; background: linear-gradient(135deg, #67c23a, #85ce61); color: #fff;
}
.header h2 { font-size: 18px; }
.content { padding: 12px; }
.conversation-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px; background: #fff; border-radius: 8px;
  margin-bottom: 10px; cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.conversation-item:hover { background: #f9f9f9; }
.conv-info h4 { font-size: 15px; margin-bottom: 4px; }
.conv-info p { font-size: 12px; color: #999; }
</style>
