<template>
  <div class="detail-wrap" v-if="station">
    <!-- 헤더 -->
    <div class="detail-header">
      <el-button :icon="ArrowLeft" @click="$router.back()" text>목록으로</el-button>
      <h2 style="margin: 0 12px; font-size: 18px; font-weight: 700">{{ station.name }}</h2>
      <el-tag :type="OPERATION_STATUS_TYPE[station.operationStatus]">
        {{ label(OPERATION_STATUS_LABEL, station.operationStatus) }}
      </el-tag>
      <div style="margin-left: auto; display: flex; gap: 8px">
        <el-button size="small" type="success"
          :disabled="station.operationStatus === 'ACTIVE'" :loading="statusLoading"
          @click="changeStatus('ACTIVE')">운영중으로 전환</el-button>
        <el-button size="small" type="warning"
          :disabled="station.operationStatus === 'MAINTENANCE'" :loading="statusLoading"
          @click="changeStatus('MAINTENANCE')">점검중으로 전환</el-button>
      </div>
    </div>

    <div style="padding: 0 20px 20px">
      <el-row :gutter="16" style="align-items: stretch">
        <!-- 기본 정보 -->
        <el-col :span="8" style="display: flex; flex-direction: column">
          <el-card shadow="never" class="info-card">
            <template #header><span class="card-title">기본 정보</span></template>
            <el-descriptions class="info-desc" :column="1" border size="small">
              <el-descriptions-item label="충전소 코드">{{ station.stationCode }}</el-descriptions-item>
              <el-descriptions-item label="주소">{{ station.address }}</el-descriptions-item>
              <el-descriptions-item label="전체 충전기">{{ station.totalChargers }}기</el-descriptions-item>
              <el-descriptions-item label="위도">{{ station.latitude }}</el-descriptions-item>
              <el-descriptions-item label="경도">{{ station.longitude }}</el-descriptions-item>
              <el-descriptions-item label="카메라 ID">{{ station.cameraDeviceId ?? '미설정' }}</el-descriptions-item>
              <el-descriptions-item label="등록일">{{ formatDate(station.createdAt) }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <!-- 지도 -->
        <el-col :span="10">
          <el-card shadow="never" style="height: 100%">
            <template #header><span class="card-title">위치</span></template>
            <div id="station-map" style="height: 280px; border-radius: 4px; background: #e8e8e8"></div>
          </el-card>
        </el-col>

        <!-- 충전기 상태 파이차트 -->
        <el-col :span="6">
          <el-card shadow="never" style="height: 100%">
            <template #header><span class="card-title">충전기 상태</span></template>
            <div ref="pieChartEl" style="height: 280px"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 충전기 목록 -->
      <el-card shadow="never" style="margin-top: 16px">
        <template #header><span class="card-title">충전기 목록</span></template>
        <el-table :data="chargers" v-loading="chargersLoading" stripe size="small" style="width:100%"
          @row-click="openChargerDetail" :row-style="{ cursor: 'pointer' }">
          <el-table-column prop="chargerCode" label="충전기 코드" min-width="140" />
          <el-table-column prop="connectorType" label="커넥터" min-width="90">
            <template #default="{ row }">{{ label(CONNECTOR_TYPE_LABEL, row.connectorType) }}</template>
          </el-table-column>
          <el-table-column prop="maxPowerKw" label="최대 전력(kW)" min-width="110" align="center" />
          <el-table-column prop="status" label="상태" min-width="90">
            <template #default="{ row }">
              <el-tag :type="CHARGER_STATUS_TYPE[row.status]" size="small">
                {{ label(CHARGER_STATUS_LABEL, row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="healthScore" label="건강도" min-width="120">
            <template #default="{ row }">
              <el-progress v-if="row.healthScore != null"
                :percentage="Number(row.healthScore)"
                :status="Number(row.healthScore) < 30 ? 'exception' : Number(row.healthScore) < 60 ? 'warning' : ''"
                :stroke-width="8" />
              <span v-else style="color: #c0c4cc">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="lastMaintenanceAt" label="최근 점검" min-width="140">
            <template #default="{ row }">{{ row.lastMaintenanceAt ? formatDate(row.lastMaintenanceAt) : '-' }}</template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 최근 위반 내역 -->
      <el-card shadow="never" style="margin-top: 16px">
        <template #header><span class="card-title">최근 불법 주차 내역</span></template>
        <el-table :data="violations" v-loading="violationsLoading" stripe size="small" style="width:100%"
          @row-click="openViolationDetail" :row-style="{ cursor: 'pointer' }">
          <el-table-column prop="occurredAt" label="감지 시각" min-width="145">
            <template #default="{ row }">{{ formatDateTime(row.occurredAt) }}</template>
          </el-table-column>
          <el-table-column prop="plateNumber" label="번호판" min-width="110" />
          <el-table-column prop="violationType" label="위반 유형" min-width="130">
            <template #default="{ row }">
              <el-tag type="danger" size="small">{{ label(VIOLATION_TYPE_LABEL, row.violationType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="처리 상태" min-width="100">
            <template #default="{ row }">
              <el-tag :type="VIOLATION_STATUS_TYPE[row.status]" size="small">
                {{ label(VIOLATION_STATUS_LABEL, row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
  <el-skeleton v-else :rows="8" animated style="padding: 20px" />

  <!-- ── 충전기 PHM 상세 모달 ── -->
  <el-dialog v-model="chargerDetailVisible" title="PHM 장비 진단 상세" width="720px" destroy-on-close>
    <div v-if="chargerDetailLoading" style="text-align:center; padding: 60px 0">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>
    <div v-else-if="selectedCharger">
      <!-- 행 1: 고장 확률 게이지 -->
      <div v-if="chargerPrediction" style="display: flex; justify-content: center; align-items: center; gap: 24px; padding: 8px 0 16px; border-bottom: 1px solid #ebeef5; margin-bottom: 16px">
        <div ref="gaugeEl" style="width: 280px; height: 160px; flex-shrink: 0"></div>
        <div>
          <el-tag :type="PHM_RISK_TYPE[chargerPrediction.riskLevel]" size="large" effect="dark" style="margin-bottom: 8px">
            {{ label(PHM_RISK_LABEL, chargerPrediction.riskLevel) }} &nbsp;
            {{ (Number(chargerPrediction.failureProbability) * 100).toFixed(1) }}%
          </el-tag>
          <div style="font-size: 12px; color: #909399; margin-top: 6px">예상 고장 부품</div>
          <div style="font-size: 15px; font-weight: 700; color: #F56C6C; margin-top: 2px">
            {{ chargerPrediction.predictedComponent ?? '미확인' }}
          </div>
          <div style="font-size: 12px; color: #909399; margin-top: 8px">잔여 수명</div>
          <div style="font-size: 14px; font-weight: 600; margin-top: 2px">
            {{ chargerPrediction.remainingUsefulLifeHours ? `약 ${chargerPrediction.remainingUsefulLifeHours}h` : '미산출' }}
          </div>
        </div>
      </div>
      <el-alert v-else type="info" :closable="false" style="margin-bottom: 16px"
        title="이 충전기에 대한 AI 고장 예측 데이터가 없습니다. 센서 데이터를 기반으로 현황을 표시합니다." />

      <!-- 행 2: 표 | 센서 이상 감지 현황 -->
      <el-row :gutter="16" style="align-items: stretch; min-height: 240px">
        <el-col :span="11" style="display: flex; flex-direction: column">
          <div class="section-title">충전기 정보</div>
          <el-descriptions class="fill-desc" :column="1" border size="small">
            <el-descriptions-item label="충전기">{{ selectedCharger.chargerCode }}</el-descriptions-item>
            <el-descriptions-item label="커넥터">{{ label(CONNECTOR_TYPE_LABEL, selectedCharger.connectorType) }}</el-descriptions-item>
            <el-descriptions-item label="최대 전력">{{ selectedCharger.maxPowerKw }} kW</el-descriptions-item>
            <el-descriptions-item label="현재 상태">
              <el-tag :type="CHARGER_STATUS_TYPE[selectedCharger.status]" size="small">
                {{ label(CHARGER_STATUS_LABEL, selectedCharger.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="건강도">
              <el-progress
                :percentage="Number(selectedCharger.healthScore ?? 0)"
                :status="Number(selectedCharger.healthScore ?? 0) < 30 ? 'exception' : Number(selectedCharger.healthScore ?? 0) < 60 ? 'warning' : ''"
                :stroke-width="8" style="width: 120px; display: inline-flex; align-items: center" />
            </el-descriptions-item>
            <el-descriptions-item label="최근 점검">
              {{ selectedCharger.lastMaintenanceAt ? formatDate(selectedCharger.lastMaintenanceAt) : '미점검' }}
            </el-descriptions-item>
            <el-descriptions-item v-if="chargerPrediction" label="AI 모델">{{ chargerPrediction.modelVersion }}</el-descriptions-item>
          </el-descriptions>
        </el-col>

        <el-col :span="13">
          <div class="section-title">센서 이상 감지 현황 <span class="section-sub">(예측 근거)</span></div>
          <div v-if="chargerHealth">
            <div v-for="s in sensorItems" :key="s.key" class="sensor-row">
              <div class="sensor-header">
                <span class="sensor-name">{{ s.label }}</span>
                <el-tag :type="s.status" size="small" effect="plain">{{ s.statusLabel }}</el-tag>
              </div>
              <div class="sensor-value-row">
                <span class="sensor-val" :style="{ color: s.color }">{{ s.value }}</span>
                <span class="sensor-range">정상 {{ s.range }}</span>
              </div>
              <el-progress :percentage="s.pct"
                :status="s.status === 'danger' ? 'exception' : s.status === 'warning' ? 'warning' : ''"
                :stroke-width="6" :show-text="false" style="margin-top: 4px" />
            </div>
          </div>
          <el-empty v-else description="센서 데이터 없음" :image-size="60" style="margin-top: 20px" />
        </el-col>
      </el-row>
        <!-- 행 3: 선정 근거 리포트 -->
        <div v-if="chargerReportData" style="border-top: 1px solid #ebeef5; margin-top: 16px; padding-top: 12px">
          <div style="display: flex; justify-content: space-between; align-items: center">
            <div style="font-weight: 600; font-size: 13px; color: #303133; display: flex; align-items: center; gap: 6px">
              <el-icon><Document /></el-icon>
              예상 고장 부품 선정 근거
            </div>
            <el-button size="small" @click="chargerReportExpanded = !chargerReportExpanded">
              {{ chargerReportExpanded ? '접기 ▲' : '근거 리포트 보기 ▼' }}
            </el-button>
          </div>
          <div v-if="chargerReportExpanded" class="report-card">
            <div class="report-head">
              <span>고장 부품 선정 근거 리포트</span>
              <span class="report-meta-inline">{{ chargerReportData.chargerCode }} | {{ chargerReportData.predictedAt }} | {{ chargerReportData.modelVersion }}</span>
              <el-button size="small" link @click="copyChargerReport">
                <el-icon><CopyDocument /></el-icon> 복사
              </el-button>
            </div>
            <div class="rs-section">
              <div class="rs-title">① 판정 결과</div>
              <div class="rs-row"><span class="rs-key">위험 등급</span>
                <el-tag :type="chargerReportData.riskType" size="small">{{ chargerReportData.riskLabel }}</el-tag>
                <span class="rs-sub">고장 확률 {{ chargerReportData.prob }}%</span>
              </div>
              <div class="rs-row"><span class="rs-key">예상 고장 부품</span>
                <strong style="color: #F56C6C">{{ chargerReportData.component }}</strong>
              </div>
              <div class="rs-row"><span class="rs-key">잔여 수명(RUL)</span>
                <span>{{ chargerReportData.rul }}</span>
              </div>
            </div>
            <div class="rs-section">
              <div class="rs-title">② 이상 감지 항목
                <el-tag v-if="chargerReportData.anomalies.length" type="danger" size="small" style="margin-left:4px">{{ chargerReportData.anomalies.length }}건</el-tag>
                <el-tag v-else type="success" size="small" style="margin-left:4px">이상 없음</el-tag>
              </div>
              <div v-if="!chargerReportData.anomalies.length" class="rs-ok">
                <el-icon><Check /></el-icon> 모든 센서 정상 범위 내
              </div>
              <div v-else v-for="a in chargerReportData.anomalies" :key="a.key" class="rs-anomaly-row">
                <el-tag :type="a.status" size="small" effect="plain" class="rs-status-tag">{{ a.statusLabel }}</el-tag>
                <span class="rs-anomaly-text">
                  {{ a.label }} — <strong>{{ a.value }}</strong>
                  <span class="rs-sub"> (정상 {{ a.range }})</span>
                </span>
              </div>
            </div>
            <div class="rs-section">
              <div class="rs-title">③ 선정 근거 서술</div>
              <div class="rs-narrative">{{ chargerNarrative }}</div>
            </div>
            <div class="rs-section" style="border-bottom: none">
              <div class="rs-title">④ 권고 조치</div>
              <el-tag :type="chargerReportData.riskType" effect="dark">{{ chargerReportData.levelWord }}</el-tag>
            </div>
          </div>
        </div>
    </div>
    <template #footer>
      <el-button @click="chargerDetailVisible = false">닫기</el-button>
    </template>
  </el-dialog>

  <!-- ── 불법 주차 상세 모달 ── -->
  <el-dialog v-model="violationDetailVisible" title="위반 상세 정보" width="660px">
    <div v-if="selectedViolation">
      <el-steps :active="statusStep(selectedViolation.status)" finish-status="success" size="small"
        style="margin-bottom: 16px">
        <el-step title="감지" description="AI 자동" />
        <el-step title="통보" description="AI 자동" />
        <el-step title="해결" description="수동 처리" />
      </el-steps>
      <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
        <el-descriptions-item label="감지 시각" :span="2">{{ formatTimeFull(selectedViolation.occurredAt) }}</el-descriptions-item>
        <el-descriptions-item label="충전소">{{ selectedViolation.stationName }}</el-descriptions-item>
        <el-descriptions-item label="충전기 위치">{{ selectedViolation.chargerId ? `${selectedViolation.chargerId}번` : '-' }}</el-descriptions-item>
        <el-descriptions-item label="번호판">{{ selectedViolation.plateNumber }}</el-descriptions-item>
        <el-descriptions-item label="위반 유형">
          <el-tag type="danger" size="small">{{ label(VIOLATION_TYPE_LABEL, selectedViolation.violationType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="처리 상태">
          <el-tag :type="VIOLATION_STATUS_TYPE[selectedViolation.status]" size="small">
            {{ label(VIOLATION_STATUS_LABEL, selectedViolation.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="처리 완료" v-if="selectedViolation.resolvedAt">
          {{ formatTimeFull(selectedViolation.resolvedAt) }}
        </el-descriptions-item>
      </el-descriptions>
      <el-row :gutter="12">
        <el-col :span="12">
          <div class="img-label">차량 전체 이미지</div>
          <el-image v-if="selectedViolation.evidenceImagePath"
            :src="selectedViolation.evidenceImagePath" fit="contain"
            style="width: 100%; height: 180px; border: 1px solid #e8e8e8; border-radius: 4px" />
          <div v-else class="img-placeholder">이미지 없음</div>
        </el-col>
        <el-col :span="12">
          <div class="img-label">번호판 크롭 이미지</div>
          <div class="img-placeholder">번호판 이미지 없음</div>
        </el-col>
      </el-row>
    </div>
    <template #footer>
      <el-button
        v-if="selectedViolation && (selectedViolation.status === 'DETECTED' || selectedViolation.status === 'NOTIFIED')"
        type="primary" :loading="resolveLoading" @click="resolveViolation">
        해결 처리
      </el-button>
      <el-button @click="violationDetailVisible = false">닫기</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useStationStore } from '@/stores/station'
import http from '@/api/http'
import type { StationResponse, ChargerResponse, ViolationResponse, FailurePredictionResponse, EquipmentHealthResponse } from '@/types'
import {
  OPERATION_STATUS_LABEL, OPERATION_STATUS_TYPE,
  CHARGER_STATUS_LABEL, CHARGER_STATUS_TYPE,
  CONNECTOR_TYPE_LABEL,
  VIOLATION_TYPE_LABEL, VIOLATION_STATUS_LABEL, VIOLATION_STATUS_TYPE,
  PHM_RISK_LABEL, PHM_RISK_TYPE,
  label,
} from '@/utils/labels'
import dayjs from 'dayjs'
import * as echarts from 'echarts/core'
import { PieChart, GaugeChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([PieChart, GaugeChart, TooltipComponent, LegendComponent, CanvasRenderer])

const COMPONENT_DESCRIPTION: Record<string, string> = {
  '전력 변환 모듈': '전력 변환 모듈은 고전류·고온 환경에 장기간 노출 시 내부 IGBT 소자 및 전해 커패시터 열화가 급격히 진행됩니다.',
  '냉각 시스템': '냉각 시스템 이상은 온도 센서 이상과 높은 상관관계가 있으며, 냉각팬 마모 또는 냉매 순환 불량이 연쇄적 과열을 유발합니다.',
  '커넥터/케이블': '커넥터 및 케이블은 전류 과부하와 반복적 연결에 의해 절연 피복 손상 및 접촉 저항 증가가 발생할 수 있습니다.',
  'IGBT 모듈': 'IGBT 모듈은 전류 과부하 및 온도 이상이 복합 발생 시 게이트 산화막 파괴 위험이 급격히 증가합니다.',
  '충전 케이블': '충전 케이블은 반복 사용과 전류 과부하에 의해 절연 피복 손상 및 단자 접촉 불량이 진행될 수 있습니다.',
  '전압 조정 회로': '전압 조정 회로는 입력 전압 불안정 시 리플 전류 증가로 인한 내부 소자 열화가 진행됩니다.',
  '배터리 관리 시스템': '배터리 관리 시스템은 전압 불안정 및 전류 이상 패턴에 민감하며, 셀 불균형이 시스템 전반에 영향을 줍니다.',
}

const SENSOR_FAILURE_HINT: Record<string, string> = {
  temp: '과열로 인한 내부 소자 열화',
  vib: '기계적 진동으로 인한 부품 피로 누적',
  cur: '전류 과부하로 인한 전기적 스트레스',
  volt: '전압 불안정으로 인한 회로 오류',
}

const route = useRoute()
const stationStore = useStationStore()
const station = ref<StationResponse | null>(null)
const chargers = ref<ChargerResponse[]>([])
const violations = ref<ViolationResponse[]>([])
const chargersLoading = ref(false)
const violationsLoading = ref(false)
const statusLoading = ref(false)
const pieChartEl = ref<HTMLElement>()

// 충전기 PHM 모달
const chargerDetailVisible = ref(false)
const chargerDetailLoading = ref(false)
const selectedCharger = ref<ChargerResponse | null>(null)
const chargerPrediction = ref<FailurePredictionResponse | null>(null)
const chargerHealth = ref<EquipmentHealthResponse | null>(null)
const chargerReportExpanded = ref(false)
const gaugeEl = ref<HTMLElement>()
let gaugeChart: echarts.ECharts | null = null

// 위반 모달
const violationDetailVisible = ref(false)
const selectedViolation = ref<ViolationResponse | null>(null)
const resolveLoading = ref(false)

function formatDate(dt: string) { return dayjs(dt).format('YYYY-MM-DD') }
function formatDateTime(dt: string) { return dayjs(dt).format('YYYY-MM-DD HH:mm') }
function formatTimeFull(dt: string) { return dayjs(dt).format('YYYY-MM-DD HH:mm:ss') }

async function changeStatus(status: 'ACTIVE' | 'MAINTENANCE') {
  if (!station.value) return
  statusLoading.value = true
  try {
    const res = await http.patch(`/stations/${station.value.id}/status`, null, { params: { status } })
    station.value = { ...station.value, operationStatus: res.data.data.operationStatus }
    ElMessage.success(`상태가 '${status === 'ACTIVE' ? '운영중' : '점검중'}'으로 변경되었습니다.`)
  } catch {
    ElMessage.error('상태 변경에 실패했습니다.')
  } finally {
    statusLoading.value = false
  }
}

async function openChargerDetail(row: ChargerResponse) {
  selectedCharger.value = row
  chargerPrediction.value = null
  chargerHealth.value = null
  chargerDetailVisible.value = true
  chargerDetailLoading.value = true
  try {
    const [predRes, healthRes] = await Promise.allSettled([
      http.get(`/predictions/charger/${row.id}`),
      http.get(`/chargers/${row.id}/health/latest`),
    ])
    if (predRes.status === 'fulfilled') chargerPrediction.value = predRes.value.data.data
    if (healthRes.status === 'fulfilled') chargerHealth.value = healthRes.value.data.data
  } finally {
    chargerDetailLoading.value = false
  }
}

function openViolationDetail(row: ViolationResponse) {
  selectedViolation.value = { ...row }
  violationDetailVisible.value = true
}

function statusStep(status: string) {
  if (status === 'DETECTED') return 1
  if (status === 'NOTIFIED') return 2
  return 3
}

async function resolveViolation() {
  if (!selectedViolation.value) return
  resolveLoading.value = true
  try {
    const res = await http.patch(`/violations/${selectedViolation.value.id}/resolve`)
    const updated = res.data.data
    selectedViolation.value = { ...selectedViolation.value, status: updated.status, resolvedAt: updated.resolvedAt }
    const idx = violations.value.findIndex((v) => v.id === selectedViolation.value!.id)
    if (idx !== -1) violations.value[idx] = { ...violations.value[idx], status: updated.status, resolvedAt: updated.resolvedAt }
    ElMessage.success('해결 처리되었습니다.')
  } catch {
    ElMessage.error('처리에 실패했습니다.')
  } finally {
    resolveLoading.value = false
  }
}

// 센서 임계값 기반 지표 계산
const sensorItems = computed(() => {
  const h = chargerHealth.value
  if (!h) return []
  const temp = Number(h.temperature), vib = Number(h.vibration)
  const cur = Number(h.current), volt = Number(h.voltage)
  const tempS = temp > 80 ? 'danger' : temp > 60 ? 'warning' : 'success'
  const vibS  = vib  > 0.30 ? 'danger' : vib  > 0.10 ? 'warning' : 'success'
  const curS  = cur  > 95 ? 'danger' : cur > 80 ? 'warning' : 'success'
  const voltOk = volt >= 390 && volt <= 420
  const voltS = (!voltOk && (volt < 380 || volt > 430)) ? 'danger' : !voltOk ? 'warning' : 'success'
  const sl = (s: string) => s === 'danger' ? '위험' : s === 'warning' ? '주의' : '정상'
  const sc = (s: string) => s === 'danger' ? '#F56C6C' : s === 'warning' ? '#E6A23C' : '#67C23A'
  return [
    { key: 'temp', label: '온도 (Temperature)', value: `${temp.toFixed(1)} °C`, range: '< 60°C', pct: Math.min(Math.round(temp / 100 * 100), 100), status: tempS, statusLabel: sl(tempS), color: sc(tempS) },
    { key: 'vib',  label: '진동 (Vibration)',   value: `${vib.toFixed(4)} g`,   range: '< 0.10 g', pct: Math.min(Math.round(vib / 0.5 * 100), 100), status: vibS, statusLabel: sl(vibS), color: sc(vibS) },
    { key: 'cur',  label: '전류 (Current)',     value: `${cur.toFixed(1)} A`,   range: '< 80 A', pct: Math.min(Math.round(cur / 110 * 100), 100), status: curS, statusLabel: sl(curS), color: sc(curS) },
    { key: 'volt', label: '전압 (Voltage)',     value: `${volt.toFixed(1)} V`,  range: '390 ~ 420 V', pct: Math.min(Math.abs(volt - 405) / 30 * 100, 100), status: voltS, statusLabel: sl(voltS), color: sc(voltS) },
  ]
})

// 선정 근거 리포트
const chargerReportData = computed(() => {
  const pred = chargerPrediction.value
  if (!pred) return null
  const prob = (Number(pred.failureProbability) * 100).toFixed(1)
  const anomalies = sensorItems.value.filter((s) => s.status !== 'success')
  const levelWord = pred.riskLevel === 'CRITICAL' ? '긴급 점검 필요' : pred.riskLevel === 'WARNING' ? '조기 점검 권고' : '지속 모니터링 권고'
  return {
    chargerCode: pred.chargerCode,
    predictedAt: formatDate(pred.predictedAt) + ' ' + pred.predictedAt.slice(11, 16),
    modelVersion: pred.modelVersion,
    riskLabel: label(PHM_RISK_LABEL, pred.riskLevel),
    riskType: PHM_RISK_TYPE[pred.riskLevel],
    prob,
    component: pred.predictedComponent ?? '미확인',
    rul: pred.remainingUsefulLifeHours ? `약 ${pred.remainingUsefulLifeHours}시간` : '미산출',
    levelWord,
    anomalies,
    hasHealth: !!chargerHealth.value,
  }
})

const chargerNarrative = computed(() => {
  const d = chargerReportData.value
  if (!d) return ''
  if (!d.hasHealth) {
    return `센서 데이터가 없어 실측 기반 근거를 제시하기 어렵습니다. AI 예측 모델(${d.modelVersion})의 시계열 패턴 분석 결과 고장 확률 ${d.prob}%로 산정되었습니다.`
  }
  if (!d.anomalies.length) {
    return `현재 모든 센서값이 정상 범위 내에 있습니다. 단, LSTM 시계열 모델(${d.modelVersion})은 최근 24시간의 센서값 추세를 분석한 결과, '${d.component}' 부품에서 임계값 미만의 미세한 열화 패턴을 감지하였습니다. 예방적 차원의 점검을 권고합니다.`
  }
  const names = d.anomalies.map((a) => `${a.label.split(' ')[0]}(${a.value})`).join(', ')
  const hints = d.anomalies.map((a) => SENSOR_FAILURE_HINT[a.key] ?? '센서 이상').join(' / ')
  const compDesc = COMPONENT_DESCRIPTION[d.component] ?? `'${d.component}'은(는) 해당 센서 이상 패턴과 높은 상관관계를 보이는 부품입니다.`
  return `${d.anomalies.length}개 센서(${names})에서 정상 임계값 초과가 확인되었습니다. 이는 ${hints}와(과) 관련됩니다.\n\n${compDesc}\n\nAI 예측 모델(${d.modelVersion})은 이러한 복합 이상 패턴을 분석하여 고장 확률 ${d.prob}%로 산정하였으며, '${d.component}' 부품의 ${d.rul} 내 교체 또는 정밀 점검을 권고합니다.`
})

function copyChargerReport() {
  const d = chargerReportData.value
  if (!d) return
  const anomalyText = !d.anomalies.length
    ? '  - 모든 센서 정상 범위 내'
    : d.anomalies.map((a) => `  - ${a.label}: ${a.value} (정상 ${a.range}) [${a.statusLabel}]`).join('\n')
  const text = `【 예상 고장 부품 선정 근거 리포트 】\n충전기: ${d.chargerCode} | 예측: ${d.predictedAt} | 모델: ${d.modelVersion}\n\n① 판정 결과\n  위험 등급: ${d.riskLabel} | 고장 확률: ${d.prob}%\n  예상 고장 부품: ${d.component}\n  잔여 수명(RUL): ${d.rul}\n\n② 이상 감지 항목 (${d.anomalies.length}개)\n${anomalyText}\n\n③ 선정 근거 서술\n${chargerNarrative.value}\n\n④ 권고 조치: ${d.levelWord}`
  navigator.clipboard.writeText(text)
    .then(() => ElMessage.success('리포트가 클립보드에 복사되었습니다.'))
    .catch(() => ElMessage.warning('복사 기능을 지원하지 않는 환경입니다.'))
}

// 게이지 차트 — 예측 데이터 로드 후 렌더링
watch([chargerDetailLoading, chargerDetailVisible], async ([loading, visible]) => {
  if (visible && !loading && chargerPrediction.value) {
    await nextTick()
    if (!gaugeEl.value) return
    if (gaugeChart) gaugeChart.dispose()
    gaugeChart = echarts.init(gaugeEl.value)
    const prob = Math.round(Number(chargerPrediction.value.failureProbability) * 100)
    gaugeChart.setOption({
      series: [{
        type: 'gauge', startAngle: 180, endAngle: 0,
        min: 0, max: 100, splitNumber: 5,
        progress: { show: true, width: 14 },
        axisLine: { lineStyle: { width: 14, color: [[0.5, '#67C23A'], [0.8, '#E6A23C'], [1, '#F56C6C']] } },
        pointer: { show: false }, axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false },
        detail: { valueAnimation: true, formatter: '{value}%', fontSize: 20, fontWeight: 'bold', offsetCenter: [0, '-10%'], color: prob > 80 ? '#F56C6C' : prob > 50 ? '#E6A23C' : '#67C23A' },
        data: [{ value: prob, name: '고장 확률' }],
        title: { offsetCenter: [0, '20%'], fontSize: 12 },
      }],
    })
  }
  if (!visible && gaugeChart) { gaugeChart.dispose(); gaugeChart = null }
  if (!visible) chargerReportExpanded.value = false
})

async function loadChargers(id: number) {
  chargersLoading.value = true
  try {
    const res = await http.get(`/chargers/station/${id}`)
    chargers.value = res.data.data ?? []
  } finally { chargersLoading.value = false }
}

async function loadViolations(id: number) {
  violationsLoading.value = true
  try {
    const res = await http.get('/violations', { params: { stationId: id, size: 10 } })
    violations.value = res.data.data?.content ?? []
  } finally { violationsLoading.value = false }
}

function initPieChart() {
  if (!pieChartEl.value || chargers.value.length === 0) return
  const chart = echarts.init(pieChartEl.value)
  const statusCount: Record<string, number> = {}
  chargers.value.forEach((c) => { statusCount[c.status] = (statusCount[c.status] ?? 0) + 1 })
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie', radius: ['40%', '70%'],
      center: ['50%', '45%'],
      data: Object.entries(statusCount).map(([name, value]) => ({ name: label(CHARGER_STATUS_LABEL, name), value })),
      label: { show: false },
    }],
  })
}

function initMap() {
  if (!station.value) return
  const mapEl = document.getElementById('station-map')
  if (!mapEl) return
  import('leaflet').then((L) => {
    const map = L.default.map(mapEl).setView([station.value!.latitude, station.value!.longitude], 15)
    L.default.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(map)
    const icon = L.default.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      iconSize: [25, 41], iconAnchor: [12, 41],
    })
    L.default.marker([station.value!.latitude, station.value!.longitude], { icon })
      .addTo(map).bindPopup(station.value!.name).openPopup()
  }).catch(() => {
    if (mapEl) mapEl.innerHTML = `<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#909399;font-size:13px">위도: ${station.value?.latitude} / 경도: ${station.value?.longitude}</div>`
  })
}

onMounted(async () => {
  const id = Number(route.params.id)
  station.value = await stationStore.fetchOne(id)
  await Promise.all([loadChargers(id), loadViolations(id)])
  await nextTick()
  initMap()
  initPieChart()
})
</script>

<style scoped>
.detail-wrap { min-height: 100%; background: #f0f2f5; }
.detail-header {
  display: flex; align-items: center; padding: 16px 20px;
  background: #fff; border-bottom: 1px solid #e8e8e8; margin-bottom: 0;
}
.card-title { font-weight: 600; font-size: 14px; }
.info-card { flex: 1; display: flex; flex-direction: column; }
:deep(.info-card .el-card__body) { flex: 1; display: flex; flex-direction: column; }
:deep(.info-desc) { flex: 1; display: flex; flex-direction: column; }
:deep(.info-desc .el-descriptions__body) { flex: 1; }
:deep(.info-desc .el-descriptions__body table) { height: 100%; }
.section-title { font-weight: 600; font-size: 13px; color: #303133; border-bottom: 1px solid #ebeef5; padding-bottom: 6px; margin-bottom: 10px; }
.section-sub { font-weight: 400; font-size: 11px; color: #909399; }
.sensor-row { margin-bottom: 12px; }
.sensor-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2px; }
.sensor-name { font-size: 12px; color: #606266; }
.sensor-value-row { display: flex; justify-content: space-between; align-items: baseline; }
.sensor-val { font-size: 16px; font-weight: 700; }
.sensor-range { font-size: 11px; color: #909399; }
.img-label { font-size: 12px; color: #606266; margin-bottom: 6px; font-weight: 500; }
.img-placeholder {
  height: 180px; border: 1px dashed #dcdfe6; border-radius: 4px;
  display: flex; align-items: center; justify-content: center;
  color: #c0c4cc; font-size: 13px;
}
:deep(.fill-desc) { flex: 1; display: flex; flex-direction: column; }
:deep(.fill-desc .el-descriptions__body) { flex: 1; }
:deep(.fill-desc .el-descriptions__body table) { height: 100%; }

/* 선정 근거 리포트 */
.report-card { margin-top: 10px; border: 1px solid #e4e7ed; border-radius: 6px; overflow: hidden; }
.report-head {
  background: #f5f7fa; border-bottom: 1px solid #e4e7ed;
  padding: 8px 12px; display: flex; align-items: center; gap: 8px;
  font-weight: 600; font-size: 12px; color: #303133;
}
.report-meta-inline { font-weight: 400; font-size: 11px; color: #909399; flex: 1; }
.rs-section { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; }
.rs-title {
  font-weight: 600; font-size: 11px; color: #606266;
  margin-bottom: 6px; display: flex; align-items: center;
}
.rs-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; font-size: 12px; }
.rs-key { color: #909399; width: 110px; flex-shrink: 0; font-size: 11px; }
.rs-sub { font-size: 11px; color: #909399; }
.rs-ok { font-size: 12px; color: #67C23A; display: flex; align-items: center; gap: 4px; }
.rs-anomaly-row { display: flex; align-items: center; margin-bottom: 4px; }
.rs-status-tag { min-width: 34px; text-align: center; flex-shrink: 0; }
.rs-anomaly-text { font-size: 12px; margin-left: 6px; }
.rs-narrative { font-size: 12px; line-height: 1.8; color: #606266; white-space: pre-wrap; }
</style>
