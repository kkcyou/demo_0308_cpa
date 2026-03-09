<template>
  <div class="note-page">
    <div class="header">
      <h2>我的笔记</h2>
      <el-button type="primary" size="small" @click="showNewDialog = true">+ 新建</el-button>
    </div>

    <div class="content">
      <!-- 筛选 -->
      <div class="filter-bar">
        <el-select v-model="filterSubject" placeholder="全部科目" clearable size="small"
                   @change="loadNotes" style="width:120px">
          <el-option v-for="s in subjects" :key="s.code" :label="s.name" :value="s.code" />
        </el-select>
        <el-switch v-model="filterStarred" active-text="仅标星" @change="loadNotes" />
      </div>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="notes.length === 0">
        <el-empty description="暂无笔记" />
      </div>

      <div v-else class="note-list">
        <div v-for="note in notes" :key="note.id" class="note-item">
          <div class="note-header">
            <h4>
              <span class="star" :class="{ active: note.starred }" @click="toggleStar(note)">
                {{ note.starred ? '★' : '☆' }}
              </span>
              {{ note.title }}
            </h4>
            <el-button text type="danger" size="small" @click="deleteNote(note.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <p class="note-preview">{{ (note.content || '').substring(0, 100) }}</p>
          <div class="note-meta">
            <el-tag v-if="note.subject" size="small" type="info">
              {{ subjectName(note.subject) }}
            </el-tag>
            <span class="time">{{ formatDate(note.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建笔记 -->
    <el-dialog v-model="showNewDialog" title="新建笔记" width="90%">
      <el-form>
        <el-form-item label="标题">
          <el-input v-model="newForm.title" placeholder="笔记标题" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="newForm.subject" placeholder="选择科目" clearable style="width:100%">
            <el-option v-for="s in subjects" :key="s.code" :label="s.name" :value="s.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="newForm.content" type="textarea" :rows="8" placeholder="支持Markdown格式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNewDialog = false">取消</el-button>
        <el-button type="primary" @click="createNote">保存</el-button>
      </template>
    </el-dialog>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noteApi } from '../../api'
import BottomNav from '../../components/BottomNav.vue'

const loading = ref(true)
const notes = ref([])
const filterSubject = ref(null)
const filterStarred = ref(false)
const showNewDialog = ref(false)
const newForm = ref({ title: '', content: '', subject: null })

const subjects = [
  { code: 1, name: '会计' }, { code: 2, name: '审计' }, { code: 3, name: '财管' },
  { code: 4, name: '经济法' }, { code: 5, name: '税法' }, { code: 6, name: '战略' }
]

const subjectName = (code) => subjects.find(s => s.code === code)?.name || ''

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

onMounted(() => loadNotes())

const loadNotes = async () => {
  loading.value = true
  try {
    const params = { pageNo: 1, pageSize: 50 }
    if (filterSubject.value) params.subject = filterSubject.value
    if (filterStarred.value) params.starred = true
    const res = await noteApi.page(params)
    notes.value = res.data.records || []
  } finally {
    loading.value = false
  }
}

const createNote = async () => {
  if (!newForm.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  await noteApi.create(newForm.value)
  ElMessage.success('创建成功')
  showNewDialog.value = false
  newForm.value = { title: '', content: '', subject: null }
  loadNotes()
}

const toggleStar = async (note) => {
  await noteApi.toggleStar(note.id)
  note.starred = !note.starred
}

const deleteNote = async (id) => {
  await ElMessageBox.confirm('确定删除此笔记？', '提示', { type: 'warning' })
  await noteApi.delete(id)
  notes.value = notes.value.filter(n => n.id !== id)
  ElMessage.success('已删除')
}
</script>

<style scoped>
.note-page { padding-bottom: 70px; max-width: 768px; margin: 0 auto; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px; background: linear-gradient(135deg, #e6a23c, #f0c060); color: #fff;
}
.header h2 { font-size: 18px; }
.content { padding: 12px; }
.filter-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; }
.note-item {
  background: #fff; border-radius: 8px; padding: 14px;
  margin-bottom: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.note-header { display: flex; justify-content: space-between; align-items: center; }
.note-header h4 { font-size: 15px; }
.star { cursor: pointer; margin-right: 4px; color: #ccc; }
.star.active { color: #e6a23c; }
.note-preview { font-size: 13px; color: #999; margin: 6px 0; line-height: 1.5; }
.note-meta { display: flex; gap: 8px; align-items: center; }
.time { font-size: 12px; color: #bbb; }
</style>
