<template>
  <div class="today-page">
    <!-- 顶部栏 -->
    <div class="header">
      <h1>CPA Daily</h1>
      <el-button text @click="$router.push('/daily/history')">
        <el-icon><Clock /></el-icon>
      </el-button>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 无内容 -->
    <div v-else-if="!data" class="empty">
      <el-empty description="今日内容尚未生成">
        <el-button type="primary" @click="generate" :loading="generating">
          手动生成今日内容
        </el-button>
      </el-empty>
    </div>

    <!-- 内容区 -->
    <div v-else class="content">
      <!-- 日期和科目 -->
      <div class="date-bar">
        <span class="date">{{ data.pushDate }}</span>
        <el-tag :type="subjectTagType(data.subject)" size="small">{{ data.subjectName }}</el-tag>
      </div>

      <!-- 标题 -->
      <h2 class="title">{{ data.title }}</h2>

      <!-- 新闻卡片 -->
      <el-card class="section-card news-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>今日新闻</span>
          </div>
        </template>
        <p class="news-text">{{ data.newsSummary }}</p>
      </el-card>

      <!-- 考点讲解 -->
      <el-card class="section-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>考点精讲</span>
            <el-tag size="small" type="info">{{ data.chapter }}</el-tag>
          </div>
        </template>
        <MarkdownRenderer :content="data.pointExplain" />
      </el-card>

      <!-- 关联真题 -->
      <el-card v-if="data.questionExplain" class="section-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>关联真题</span>
          </div>
        </template>
        <MarkdownRenderer :content="data.questionExplain" />
      </el-card>

      <!-- 记忆辅助 -->
      <el-card v-if="data.mnemonic" class="section-card mnemonic-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>记忆辅助</span>
          </div>
        </template>
        <MarkdownRenderer :content="data.mnemonic" />
      </el-card>

      <!-- 底部操作栏 -->
      <div class="action-bar">
        <el-button @click="openNoteDialog" type="primary" plain>
          <el-icon><EditPen /></el-icon> 写笔记
        </el-button>
        <el-button @click="goChat" type="success" plain>
          <el-icon><ChatDotRound /></el-icon> 问AI
        </el-button>
      </div>
    </div>

    <!-- 写笔记对话框 -->
    <el-dialog v-model="noteDialogVisible" title="写笔记" width="90%">
      <el-input v-model="noteForm.title" placeholder="笔记标题" class="mb-10" />
      <el-input v-model="noteForm.content" type="textarea" :rows="6" placeholder="笔记内容（支持Markdown）" />
      <template #footer>
        <el-button @click="noteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNote" :loading="savingNote">保存</el-button>
      </template>
    </el-dialog>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, EditPen, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { dailyApi, noteApi, chatApi } from '../../api'
import MarkdownRenderer from '../../components/MarkdownRenderer.vue'
import BottomNav from '../../components/BottomNav.vue'

const router = useRouter()
const loading = ref(true)
const generating = ref(false)
const data = ref(null)

const noteDialogVisible = ref(false)
const savingNote = ref(false)
const noteForm = ref({ title: '', content: '' })

onMounted(async () => {
  try {
    const res = await dailyApi.getToday()
    data.value = res.data
  } catch (e) {
    if (e.response?.status !== 204) {
      ElMessage.error('加载失败')
    }
  } finally {
    loading.value = false
  }
})

const generate = async () => {
  generating.value = true
  try {
    const res = await dailyApi.generate()
    data.value = res.data
    ElMessage.success('内容生成成功！')
  } catch (e) {
    ElMessage.error('生成失败: ' + (e.response?.data?.message || e.message))
  } finally {
    generating.value = false
  }
}

const subjectTagType = (code) => {
  const types = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info', 5: 'danger', 6: '' }
  return types[code] || ''
}

const openNoteDialog = () => {
  noteForm.value = {
    title: `${data.value.knowledgePointTitle} - 学习笔记`,
    content: '',
    subject: data.value.subject,
    pushId: data.value.id
  }
  noteDialogVisible.value = true
}

const saveNote = async () => {
  savingNote.value = true
  try {
    await noteApi.create(noteForm.value)
    ElMessage.success('笔记保存成功')
    noteDialogVisible.value = false
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingNote.value = false
  }
}

const goChat = async () => {
  try {
    const res = await chatApi.createConversation({
      title: `关于 ${data.value.knowledgePointTitle}`,
      subject: data.value.subject,
      knowledgePointId: data.value.knowledgePointId
    })
    router.push(`/chat/${res.data.id}`)
  } catch (e) {
    ElMessage.error('创建对话失败')
  }
}
</script>

<style scoped>
.today-page {
  padding-bottom: 70px;
  max-width: 768px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #409eff, #53a8ff);
  color: #fff;
}
.header h1 { font-size: 20px; }
.header .el-button { color: #fff; }
.loading, .empty { padding: 40px 16px; }
.content { padding: 12px; }
.date-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.date { font-size: 14px; color: #999; }
.title { font-size: 20px; margin-bottom: 16px; color: #1a1a1a; }
.section-card { margin-bottom: 16px; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.news-card { background: linear-gradient(135deg, #f0f7ff, #e8f4fd); }
.news-text { font-size: 15px; line-height: 1.6; color: #555; }
.mnemonic-card { background: linear-gradient(135deg, #fff7e6, #fff3d0); }
.action-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 16px 0;
}
.mb-10 { margin-bottom: 10px; }
</style>
