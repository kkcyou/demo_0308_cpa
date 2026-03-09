<template>
  <div class="history-page">
    <div class="header">
      <el-button text @click="$router.back()" style="color:#fff">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2>历史记录</h2>
      <div style="width:32px"></div>
    </div>

    <div class="content">
      <el-date-picker
        v-model="selectedDate"
        type="date"
        placeholder="选择日期查看"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        @change="loadByDate"
        style="width: 100%; margin-bottom: 16px"
      />

      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="detail">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ detail.pushDate }}</span>
              <el-tag size="small">{{ detail.subjectName }}</el-tag>
            </div>
          </template>
          <h3>{{ detail.title }}</h3>
          <MarkdownRenderer :content="detail.pointExplain" />
        </el-card>
      </div>

      <el-empty v-else description="选择日期查看历史内容" />
    </div>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { dailyApi } from '../../api'
import MarkdownRenderer from '../../components/MarkdownRenderer.vue'
import BottomNav from '../../components/BottomNav.vue'

const selectedDate = ref(null)
const loading = ref(false)
const detail = ref(null)

const loadByDate = async (date) => {
  if (!date) return
  loading.value = true
  try {
    const res = await dailyApi.getByDate(date)
    detail.value = res.data
  } catch (e) {
    detail.value = null
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.history-page { padding-bottom: 70px; max-width: 768px; margin: 0 auto; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px; background: linear-gradient(135deg, #409eff, #53a8ff); color: #fff;
}
.header h2 { font-size: 18px; }
.content { padding: 12px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.loading { padding: 20px 0; }
</style>
