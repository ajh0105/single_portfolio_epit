<template>
  <div class="dashboard">
    <!-- KPI 카드 -->
    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="6" v-for="card in kpiCards" :key="card.label">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="kpi-card">
            <el-icon :size="36" :color="card.color"><component :is="card.icon" /></el-icon>
            <div class="kpi-info">
              <div class="kpi-value">{{ card.value }}</div>
              <div class="kpi-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 충전소 현황 테이블 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span style="font-weight: 600">충전소 현황</span></template>
          <el-table :data="stationStore.stations" size="small" stripe style="width:100%">
            <el-table-column prop="stationCode" label="코드" min-width="120" />
            <el-table-column prop="name" label="충전소명" min-width="160" />
            <el-table-column prop="operationStatus" label="상태" min-width="90">
              <template #default="{ row }">
                <el-tag :type="OPERATION_STATUS_TYPE[row.operationStatus]" size="small">
                  {{ label(OPERATION_STATUS_LABEL, row.operationStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalChargers" label="충전기 수" min-width="80" align="center" />
          </el-table>
        </el-card>
      </el-col>

      <!-- 최신 알림 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: 600">최신 알림</span>
              <el-button size="small" link @click="alertStore.markAllRead()">모두 읽음</el-button>
            </div>
          </template>
          <div v-if="alertStore.alerts.length === 0" style="text-align: center; color: #909399; padding: 20px">
            알림이 없습니다.
          </div>
          <div v-for="a in alertStore.alerts.slice(0, 8)" :key="a.id" class="alert-item"
            :class="{ unread: !a.isRead }" @click="openAlertDetail(a)">
            <el-tag :type="ALERT_SEVERITY_TYPE[a.severity]" size="small" style="margin-right: 8px">
              {{ label(ALERT_SEVERITY_LABEL, a.severity) }}
            </el-tag>
            <span class="alert-title">{{ a.title }}</span>
            <span class="alert-time">{{ formatTime(a.createdAt) }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 알림 상세 모달 -->
    <el-dialog v-model="alertDialogVisible" title="알림 상세" width="500px" :close-on-click-modal="true">
      <div v-if="selectedAlert">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="유형">{{ label(ALERT_TYPE_LABEL, selectedAlert.alertType) }}</el-descriptions-item>
          <el-descriptions-item label="중요도">
            <el-tag :type="ALERT_SEVERITY_TYPE[selectedAlert.severity]" size="small">
              {{ label(ALERT_SEVERITY_LABEL, selectedAlert.severity) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="제목">{{ selectedAlert.title }}</el-descriptions-item>
          <el-descriptions-item label="내용" :span="1">
            <span style="white-space: pre-wrap">{{ selectedAlert.message }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedAlert.stationName" label="충전소">{{ selectedAlert.stationName }}</el-descriptions-item>
          <el-descriptions-item label="발생 시각">{{ formatTimeFull(selectedAlert.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="읽음 상태">
            <el-tag :type="selectedAlert.isRead ? 'success' : 'warning'" size="small">
              {{ selectedAlert.isRead ? '읽음' : '안읽음' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button v-if="selectedAlert && !selectedAlert.isRead" type="primary" @click="markReadAndClose">읽음 처리</el-button>
        <el-button @click="alertDialogVisible = false">닫기</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useDashboardStore } from '@/stores/dashboard'
import { useStationStore } from '@/stores/station'
import { useAlertStore } from '@/stores/alert'
import type { AlertResponse } from '@/types'
import {
  OPERATION_STATUS_LABEL, OPERATION_STATUS_TYPE,
  ALERT_SEVERITY_LABEL, ALERT_SEVERITY_TYPE,
  ALERT_TYPE_LABEL, label,
} from '@/utils/labels'
import dayjs from 'dayjs'

const dashboardStore = useDashboardStore()
const stationStore = useStationStore()
const alertStore = useAlertStore()

const alertDialogVisible = ref(false)
const selectedAlert = ref<AlertResponse | null>(null)

const kpiCards = computed(() => {
  const ov = dashboardStore.overview
  return [
    { label: '전체 충전소', value: ov?.totalStations ?? 0, icon: 'Location', color: '#1890ff' },
    { label: '오늘 위반 건수', value: ov?.todayViolations ?? 0, icon: 'Warning', color: '#fa8c16' },
    { label: '위험 장비', value: ov?.criticalPredictions ?? 0, icon: 'Cpu', color: '#f5222d' },
    { label: '미읽은 알림', value: ov?.unreadAlerts ?? 0, icon: 'Bell', color: '#722ed1' },
  ]
})

function openAlertDetail(a: AlertResponse) {
  selectedAlert.value = { ...a }
  alertDialogVisible.value = true
}

async function markReadAndClose() {
  if (selectedAlert.value && !selectedAlert.value.isRead) {
    try {
      await alertStore.markRead(selectedAlert.value.id)
      selectedAlert.value = { ...selectedAlert.value, isRead: true }
      ElMessage.success('읽음 처리되었습니다.')
    } catch {
      ElMessage.error('읽음 처리에 실패했습니다.')
    }
  }
  alertDialogVisible.value = false
}

function formatTime(dt: string) { return dayjs(dt).format('MM/DD HH:mm') }
function formatTimeFull(dt: string) { return dayjs(dt).format('YYYY-MM-DD HH:mm:ss') }

onMounted(async () => {
  await Promise.all([
    dashboardStore.fetchOverview(),
    stationStore.fetchAll(),
    alertStore.fetchAlerts(),
  ])
})
</script>

<style scoped>
.dashboard { padding: 20px; }
.kpi-card { display: flex; align-items: center; gap: 16px; }
.kpi-value { font-size: 28px; font-weight: 700; line-height: 1; }
.kpi-label { font-size: 13px; color: #909399; margin-top: 4px; }
.alert-item {
  display: flex; align-items: center; padding: 8px 4px;
  border-bottom: 1px solid #f0f0f0; cursor: pointer; border-radius: 4px;
  transition: background 0.15s;
}
.alert-item:hover { background: #f5f7fa; }
.alert-item.unread { background: #e6f4ff; }
.alert-title { flex: 1; font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.alert-time { font-size: 12px; color: #909399; white-space: nowrap; margin-left: 8px; }
</style>
