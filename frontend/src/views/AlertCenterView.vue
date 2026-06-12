<template>
  <div style="padding: 20px">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px">
          <span style="font-weight: 600">알림 센터</span>
          <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap">
            <el-select v-model="filterRead" size="small" style="width: 100px">
              <el-option label="전체" value="" />
              <el-option label="안읽음" value="unread" />
              <el-option label="읽음" value="read" />
            </el-select>
            <el-select v-model="filterSeverity" size="small" style="width: 90px" clearable placeholder="중요도">
              <el-option label="전체" value="" />
              <el-option v-for="(v, k) in ALERT_SEVERITY_LABEL" :key="k" :label="v" :value="k" />
            </el-select>
            <el-select v-model="filterType" size="small" style="width: 120px" clearable placeholder="유형">
              <el-option label="전체" value="" />
              <el-option v-for="(v, k) in ALERT_TYPE_LABEL" :key="k" :label="v" :value="k" />
            </el-select>
            <el-button-group size="small">
              <el-button :type="sortOrder === 'desc' ? 'primary' : 'default'" @click="sortOrder = 'desc'">최신순</el-button>
              <el-button :type="sortOrder === 'asc' ? 'primary' : 'default'" @click="sortOrder = 'asc'">오래된순</el-button>
            </el-button-group>
            <el-button size="small" @click="resetFilters">필터 초기화</el-button>
            <el-button size="small" @click="alertStore.markAllRead()">모두 읽음 처리</el-button>
          </div>
        </div>
      </template>

      <div style="margin-bottom: 10px; font-size: 13px; color: #909399">
        전체 {{ alertStore.alerts.length }}건 · 안읽음
        <strong style="color: #e6a23c">{{ alertStore.unreadCount }}</strong>건
        <span v-if="isFiltered"> · 필터 결과 {{ filteredAlerts.length }}건 표시</span>
      </div>

      <el-table :data="filteredAlerts" stripe @row-click="openDetail"
        :row-style="{ cursor: 'pointer' }" style="width: 100%"
        :row-class-name="alertRowClass">
        <el-table-column type="index" label="번호" width="60" align="center" />
        <el-table-column prop="severity" label="중요도" min-width="80">
          <template #default="{ row }">
            <el-tag :type="ALERT_SEVERITY_TYPE[row.severity]" size="small">
              {{ label(ALERT_SEVERITY_LABEL, row.severity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertType" label="유형" min-width="130">
          <template #default="{ row }">{{ label(ALERT_TYPE_LABEL, row.alertType) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="제목" min-width="180" show-overflow-tooltip />
        <el-table-column prop="stationName" label="충전소" min-width="130">
          <template #default="{ row }">{{ row.stationName ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="발생 시각" min-width="145">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="읽음 상태" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'success' : 'warning'" size="small">
              {{ row.isRead ? '읽음' : '안읽음' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 상세 모달 -->
    <el-dialog v-model="detailVisible" title="알림 상세" width="500px">
      <div v-if="selected">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="유형">{{ label(ALERT_TYPE_LABEL, selected.alertType) }}</el-descriptions-item>
          <el-descriptions-item label="중요도">
            <el-tag :type="ALERT_SEVERITY_TYPE[selected.severity]" size="small">
              {{ label(ALERT_SEVERITY_LABEL, selected.severity) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="제목">{{ selected.title }}</el-descriptions-item>
          <el-descriptions-item label="내용">
            <span style="white-space: pre-wrap">{{ selected.message }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="selected.stationName" label="충전소">{{ selected.stationName }}</el-descriptions-item>
          <el-descriptions-item label="발생 시각">{{ formatTimeFull(selected.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="읽음 상태">
            <el-tag :type="selected.isRead ? 'success' : 'warning'" size="small">
              {{ selected.isRead ? '읽음' : '안읽음' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button v-if="selected?.isRead" :loading="readLoading" @click="toggleUnread">
          안읽음으로 변경
        </el-button>
        <el-button v-if="selected && !selected.isRead" type="primary" :loading="readLoading" @click="toggleRead">
          읽음 처리
        </el-button>
        <el-button @click="detailVisible = false">닫기</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAlertStore } from '@/stores/alert'
import type { AlertResponse } from '@/types'
import {
  ALERT_SEVERITY_LABEL, ALERT_SEVERITY_TYPE,
  ALERT_TYPE_LABEL, label,
} from '@/utils/labels'
import dayjs from 'dayjs'

const alertStore = useAlertStore()
const detailVisible = ref(false)
const selected = ref<AlertResponse | null>(null)
const readLoading = ref(false)

const filterRead = ref('')
const filterSeverity = ref('')
const filterType = ref('')
const sortOrder = ref<'desc' | 'asc'>('desc')

const isFiltered = computed(() =>
  filterRead.value !== '' || filterSeverity.value !== '' || filterType.value !== ''
)

const filteredAlerts = computed(() => {
  let list = [...alertStore.alerts]
  if (filterRead.value === 'unread') list = list.filter((a) => !a.isRead)
  else if (filterRead.value === 'read') list = list.filter((a) => a.isRead)
  if (filterSeverity.value) list = list.filter((a) => a.severity === filterSeverity.value)
  if (filterType.value) list = list.filter((a) => a.alertType === filterType.value)
  list.sort((a, b) => {
    const diff = dayjs(a.createdAt).valueOf() - dayjs(b.createdAt).valueOf()
    return sortOrder.value === 'desc' ? -diff : diff
  })
  return list
})

function alertRowClass({ row }: { row: AlertResponse }) {
  return !row.isRead ? 'row-unread' : ''
}

function resetFilters() {
  filterRead.value = ''
  filterSeverity.value = ''
  filterType.value = ''
  sortOrder.value = 'desc'
}

function openDetail(row: AlertResponse) {
  selected.value = { ...row }
  detailVisible.value = true
}

async function toggleRead() {
  if (!selected.value) return
  readLoading.value = true
  try {
    await alertStore.markRead(selected.value.id)
    selected.value = { ...selected.value, isRead: true }
  } catch {
    ElMessage.error('읽음 처리에 실패했습니다.')
  } finally {
    readLoading.value = false
  }
}

function toggleUnread() {
  if (!selected.value) return
  alertStore.markUnread(selected.value.id)
  selected.value = { ...selected.value, isRead: false }
}

function formatTime(dt: string) { return dayjs(dt).format('YYYY-MM-DD HH:mm') }
function formatTimeFull(dt: string) { return dayjs(dt).format('YYYY-MM-DD HH:mm:ss') }

onMounted(() => alertStore.fetchAlerts())
</script>

<style scoped>
:deep(.row-unread td) { background-color: #e6f4ff !important; }
</style>
