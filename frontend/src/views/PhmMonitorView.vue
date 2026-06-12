<template>
  <div style="padding: 20px">
    <el-card shadow="never">
      <template #header><span style="font-weight: 600">장비 상태 모니터링 (PHM)</span></template>

      <el-table :data="predictions" v-loading="loading" stripe @row-click="openDetail"
        style="width: 100%" :row-style="{ cursor: 'pointer' }">
        <el-table-column prop="chargerCode" label="충전기 코드" min-width="170" />
        <el-table-column prop="riskLevel" label="위험 등급" min-width="100">
          <template #default="{ row }">
            <el-tag :type="PHM_RISK_TYPE[row.riskLevel]" size="small">
              {{ label(PHM_RISK_LABEL, row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failureProbability" label="고장 확률" min-width="180">
          <template #default="{ row }">
            <el-progress :percentage="Math.round(Number(row.failureProbability) * 100)"
              :status="Number(row.failureProbability) > 0.8 ? 'exception' : Number(row.failureProbability) > 0.5 ? 'warning' : ''"
              :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column prop="remainingUsefulLifeHours" label="잔여 수명(h)" min-width="110" align="center">
          <template #default="{ row }">{{ row.remainingUsefulLifeHours ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="predictedComponent" label="예상 고장 부품" min-width="140">
          <template #default="{ row }">{{ row.predictedComponent ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="predictedAt" label="예측 시각" min-width="145">
          <template #default="{ row }">{{ formatTime(row.predictedAt) }}</template>
        </el-table-column>
        <el-table-column label="" min-width="80" align="center">
          <template #default="{ row }">
            <el-button size="small" @click.stop="openLog(row)">
              <el-icon><List /></el-icon> 로그
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ── 상세 모달 ── -->
    <el-dialog v-model="detailVisible" title="PHM 장비 진단 상세" width="720px" destroy-on-close>
      <div v-if="selected">
        <!-- 행 1: 고장 확률 게이지 차트 (전체 너비) -->
        <div style="display: flex; justify-content: center; align-items: center; gap: 24px; padding: 8px 0 16px; border-bottom: 1px solid #ebeef5; margin-bottom: 16px">
          <div ref="gaugeEl" style="width: 280px; height: 160px; flex-shrink: 0"></div>
          <div>
            <el-tag :type="PHM_RISK_TYPE[selected.riskLevel]" size="large" effect="dark" style="margin-bottom: 8px">
              {{ label(PHM_RISK_LABEL, selected.riskLevel) }} &nbsp;
              {{ (Number(selected.failureProbability) * 100).toFixed(1) }}%
            </el-tag>
            <div style="font-size: 12px; color: #909399; margin-top: 6px">예상 고장 부품</div>
            <div style="font-size: 15px; font-weight: 700; color: #F56C6C; margin-top: 2px">
              {{ selected.predictedComponent ?? '미확인' }}
            </div>
            <div style="font-size: 12px; color: #909399; margin-top: 8px">잔여 수명</div>
            <div style="font-size: 14px; font-weight: 600; margin-top: 2px">
              {{ selected.remainingUsefulLifeHours ? `약 ${selected.remainingUsefulLifeHours}h` : '미산출' }}
            </div>
          </div>
        </div>

        <!-- 행 2: 표 | 센서 이상 감지 현황 -->
        <el-row :gutter="16" style="align-items: stretch; min-height: 240px">
          <el-col :span="11" style="display: flex; flex-direction: column">
            <div class="section-title">충전기 정보</div>
            <el-descriptions class="fill-desc" :column="1" border size="small">
              <el-descriptions-item label="충전기">{{ selected.chargerCode }}</el-descriptions-item>
              <el-descriptions-item label="최대 전력">{{ selected.chargerMaxPowerKw }} kW</el-descriptions-item>
              <el-descriptions-item label="현재 상태">
                <el-tag :type="CHARGER_STATUS_TYPE[selected.chargerStatus]" size="small">
                  {{ label(CHARGER_STATUS_LABEL, selected.chargerStatus) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="건강도">
                <el-progress
                  :percentage="Number(selected.chargerHealthScore ?? 0)"
                  :status="Number(selected.chargerHealthScore ?? 0) < 30 ? 'exception' : Number(selected.chargerHealthScore ?? 0) < 60 ? 'warning' : ''"
                  :stroke-width="8" style="width: 120px; display: inline-flex; align-items: center" />
              </el-descriptions-item>
              <el-descriptions-item label="최근 점검">
                {{ selected.chargerLastMaintenanceAt ? formatDate(selected.chargerLastMaintenanceAt) : '미점검' }}
              </el-descriptions-item>
              <el-descriptions-item label="AI 모델">{{ selected.modelVersion }}</el-descriptions-item>
            </el-descriptions>
          </el-col>

          <el-col :span="13">
            <div class="section-title">센서 이상 감지 현황 <span class="section-sub">(예측 근거)</span></div>
            <div v-if="healthLoading" style="text-align:center; padding: 40px 0">
              <el-icon class="is-loading" :size="24"><Loading /></el-icon>
            </div>
            <div v-else-if="latestHealth">
              <div v-for="s in sensorItems" :key="s.key" class="sensor-row">
                <div class="sensor-header">
                  <span class="sensor-name">{{ s.label }}</span>
                  <el-tag :type="s.status" size="small" effect="plain">{{ s.statusLabel }}</el-tag>
                </div>
                <div class="sensor-value-row">
                  <span class="sensor-val" :style="{ color: s.color }">{{ s.value }}</span>
                  <span class="sensor-range">정상 {{ s.range }}</span>
                </div>
                <el-progress
                  :percentage="s.pct"
                  :status="s.status === 'danger' ? 'exception' : s.status === 'warning' ? 'warning' : ''"
                  :stroke-width="6" :show-text="false" style="margin-top: 4px" />
              </div>
            </div>
            <el-empty v-else description="센서 데이터 없음" :image-size="60" style="margin-top: 20px" />
          </el-col>
        </el-row>

        <!-- 행 3: 선정 근거 리포트 -->
        <div v-if="reportData" style="border-top: 1px solid #ebeef5; margin-top: 16px; padding-top: 12px">
          <div style="display: flex; justify-content: space-between; align-items: center">
            <div style="font-weight: 600; font-size: 13px; color: #303133; display: flex; align-items: center; gap: 6px">
              <el-icon><Document /></el-icon>
              예상 고장 부품 선정 근거
            </div>
            <el-button size="small" @click="reportExpanded = !reportExpanded">
              {{ reportExpanded ? '접기 ▲' : '근거 리포트 보기 ▼' }}
            </el-button>
          </div>
          <div v-if="reportExpanded" class="report-card">
            <div class="report-head">
              <span>고장 부품 선정 근거 리포트</span>
              <span class="report-meta-inline">{{ reportData.chargerCode }} | {{ reportData.predictedAt }} | {{ reportData.modelVersion }}</span>
              <el-button size="small" link @click="copyReport">
                <el-icon><CopyDocument /></el-icon> 복사
              </el-button>
            </div>
            <!-- ① 판정 결과 -->
            <div class="rs-section">
              <div class="rs-title">① 판정 결과</div>
              <div class="rs-row"><span class="rs-key">위험 등급</span>
                <el-tag :type="reportData.riskType" size="small">{{ reportData.riskLabel }}</el-tag>
                <span class="rs-sub">고장 확률 {{ reportData.prob }}%</span>
              </div>
              <div class="rs-row"><span class="rs-key">예상 고장 부품</span>
                <strong style="color: #F56C6C">{{ reportData.component }}</strong>
              </div>
              <div class="rs-row"><span class="rs-key">잔여 수명(RUL)</span>
                <span>{{ reportData.rul }}</span>
              </div>
            </div>
            <!-- ② 이상 감지 항목 -->
            <div class="rs-section">
              <div class="rs-title">② 이상 감지 항목
                <el-tag v-if="reportData.anomalies.length" type="danger" size="small" style="margin-left:4px">{{ reportData.anomalies.length }}건</el-tag>
                <el-tag v-else type="success" size="small" style="margin-left:4px">이상 없음</el-tag>
              </div>
              <div v-if="!reportData.anomalies.length" class="rs-ok">
                <el-icon><Check /></el-icon> 모든 센서 정상 범위 내
              </div>
              <div v-else v-for="a in reportData.anomalies" :key="a.key" class="rs-anomaly-row">
                <el-tag :type="a.status" size="small" effect="plain" class="rs-status-tag">{{ a.statusLabel }}</el-tag>
                <span class="rs-anomaly-text">
                  {{ a.label }} — <strong>{{ a.value }}</strong>
                  <span class="rs-sub"> (정상 {{ a.range }})</span>
                </span>
              </div>
            </div>
            <!-- ③ 선정 근거 서술 -->
            <div class="rs-section">
              <div class="rs-title">③ 선정 근거 서술</div>
              <div class="rs-narrative">{{ narrative }}</div>
            </div>
            <!-- ④ 권고 조치 -->
            <div class="rs-section" style="border-bottom: none">
              <div class="rs-title">④ 권고 조치</div>
              <el-tag :type="reportData.riskType" effect="dark">{{ reportData.levelWord }}</el-tag>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">닫기</el-button>
      </template>
    </el-dialog>

    <!-- ── LSTM 수집 로그 모달 ── -->
    <el-dialog v-model="logVisible" :title="`${logChargerCode} — LSTM 수집 로그`"
      width="900px" destroy-on-close>
      <!-- 기간 선택 -->
      <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 14px; flex-wrap: wrap">
        <el-button-group size="small">
          <el-button :type="logPeriod === '1d' ? 'primary' : 'default'" @click="setPeriod('1d')">일간</el-button>
          <el-button :type="logPeriod === '7d' ? 'primary' : 'default'" @click="setPeriod('7d')">주간</el-button>
          <el-button :type="logPeriod === '30d' ? 'primary' : 'default'" @click="setPeriod('30d')">월간</el-button>
          <el-button :type="logPeriod === 'all' ? 'primary' : 'default'" @click="setPeriod('all')">전체</el-button>
        </el-button-group>
        <el-date-picker
          v-model="logDateRange"
          type="daterange"
          size="small"
          style="width: 240px"
          range-separator="~"
          start-placeholder="시작일"
          end-placeholder="종료일"
          value-format="YYYY-MM-DD"
          @change="onDateRangeChange"
        />
        <span style="font-size: 12px; color: #909399">{{ logPeriodLabel }}</span>
        <span style="font-size: 12px; color: #409EFF; margin-left: 4px">{{ logData.length }}건</span>
      </div>

      <div v-if="logLoading" style="text-align:center; padding: 40px 0">
        <el-icon class="is-loading" :size="28"><Loading /></el-icon>
      </div>
      <div v-else-if="logData.length">
        <!-- 트렌드 미니차트 -->
        <div ref="logChartEl" style="height: 180px; margin-bottom: 12px"></div>

        <!-- 로그 테이블 -->
        <el-table :data="logData" size="small" stripe style="width:100%" max-height="320">
          <el-table-column prop="measuredAt" label="측정 시각" min-width="150">
            <template #default="{ row }">{{ formatTimeFull(row.measuredAt) }}</template>
          </el-table-column>
          <el-table-column prop="voltage" label="전압 (V)" min-width="100" align="right">
            <template #default="{ row }">
              <span :class="voltClass(row.voltage)">{{ Number(row.voltage).toFixed(1) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="current" label="전류 (A)" min-width="100" align="right">
            <template #default="{ row }">
              <span :class="curClass(row.current)">{{ Number(row.current).toFixed(1) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="temperature" label="온도 (°C)" min-width="100" align="right">
            <template #default="{ row }">
              <span :class="tempClass(row.temperature)">{{ Number(row.temperature).toFixed(1) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="vibration" label="진동 (g)" min-width="100" align="right">
            <template #default="{ row }">
              <span :class="vibClass(row.vibration)">{{ Number(row.vibration).toFixed(4) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-else description="수집된 로그 없음" :image-size="80" />
      <template #footer>
        <el-button @click="exportLogCsv" :disabled="!logData.length">
          <el-icon><Download /></el-icon> CSV 내보내기
        </el-button>
        <el-button @click="logVisible = false">닫기</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import type { FailurePredictionResponse, EquipmentHealthResponse } from '@/types'
import { PHM_RISK_LABEL, PHM_RISK_TYPE, CHARGER_STATUS_LABEL, CHARGER_STATUS_TYPE, label } from '@/utils/labels'
import dayjs from 'dayjs'
import * as echarts from 'echarts/core'
import { GaugeChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([GaugeChart, LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

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

const predictions = ref<FailurePredictionResponse[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const selected = ref<FailurePredictionResponse | null>(null)
const latestHealth = ref<EquipmentHealthResponse | null>(null)
const healthLoading = ref(false)
const reportExpanded = ref(false)
const gaugeEl = ref<HTMLElement>()
let gaugeChart: echarts.ECharts | null = null

// 로그 모달
const logVisible = ref(false)
const logChargerCode = ref('')
const logChargerId = ref<number | null>(null)
const logData = ref<EquipmentHealthResponse[]>([])
const logLoading = ref(false)
const logChartEl = ref<HTMLElement>()
let logChart: echarts.ECharts | null = null
const logPeriod = ref<'1d' | '7d' | '30d' | 'all' | 'custom'>('1d')
const logDateRange = ref<[string, string] | null>(null)

const logPeriodLabel = computed(() => {
  if (logPeriod.value === '1d') return '최근 24시간'
  if (logPeriod.value === '7d') return '최근 7일'
  if (logPeriod.value === '30d') return '최근 30일'
  if (logPeriod.value === 'all') return '전체 기간'
  if (logDateRange.value) return `${logDateRange.value[0]} ~ ${logDateRange.value[1]}`
  return '기간 선택'
})

async function load() {
  loading.value = true
  try {
    const res = await http.get('/predictions/critical')
    predictions.value = res.data.data ?? []
  } finally { loading.value = false }
}

async function openDetail(row: FailurePredictionResponse) {
  selected.value = row
  latestHealth.value = null
  detailVisible.value = true
  healthLoading.value = true
  try {
    const res = await http.get(`/chargers/${row.chargerId}/health/latest`)
    latestHealth.value = res.data.data
  } catch { latestHealth.value = null }
  finally { healthLoading.value = false }
}

async function fetchLog() {
  if (!logChargerId.value) return
  logLoading.value = true
  logData.value = []
  try {
    let params: Record<string, unknown> = {}
    if (logPeriod.value === 'custom' && logDateRange.value) {
      params.startDate = dayjs(logDateRange.value[0]).startOf('day').format('YYYY-MM-DDTHH:mm:ss')
      params.endDate = dayjs(logDateRange.value[1]).endOf('day').format('YYYY-MM-DDTHH:mm:ss')
    } else {
      const hoursMap: Record<string, number> = { '1d': 24, '7d': 168, '30d': 720, 'all': 87600 }
      params.hours = hoursMap[logPeriod.value]
    }
    const res = await http.get(`/chargers/${logChargerId.value}/health`, { params })
    logData.value = (res.data.data ?? []).slice().reverse()
  } catch { logData.value = [] }
  finally { logLoading.value = false }
}

function setPeriod(p: '1d' | '7d' | '30d' | 'all' | 'custom') {
  logPeriod.value = p
  if (p !== 'custom') {
    logDateRange.value = null
    fetchLog()
  }
}

function onDateRangeChange(val: [string, string] | null) {
  if (val) {
    logPeriod.value = 'custom'
    fetchLog()
  }
}

async function openLog(row: FailurePredictionResponse) {
  logChargerCode.value = row.chargerCode
  logChargerId.value = row.chargerId
  logPeriod.value = '1d'
  logDateRange.value = null
  logData.value = []
  logVisible.value = true
  fetchLog()
}

// CSV 내보내기
function exportLogCsv() {
  const header = ['측정시각', '전압(V)', '전류(A)', '온도(°C)', '진동(g)']
  const rows = logData.value.map(r => [
    formatTimeFull(r.measuredAt),
    Number(r.voltage).toFixed(1),
    Number(r.current).toFixed(1),
    Number(r.temperature).toFixed(1),
    Number(r.vibration).toFixed(4),
  ])
  const csv = '﻿' + [header, ...rows].map(r => r.join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `LSTM_log_${logChargerCode.value}_${dayjs().format('YYYYMMDD_HHmm')}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
}

// 센서 임계값 분류 헬퍼
const tempClass = (v: number) => Number(v) > 80 ? 'val-danger' : Number(v) > 60 ? 'val-warning' : ''
const vibClass  = (v: number) => Number(v) > 0.30 ? 'val-danger' : Number(v) > 0.10 ? 'val-warning' : ''
const curClass  = (v: number) => Number(v) > 95 ? 'val-danger' : Number(v) > 80 ? 'val-warning' : ''
const voltClass = (v: number) => {
  const n = Number(v)
  return (n < 380 || n > 430) ? 'val-danger' : (n < 390 || n > 420) ? 'val-warning' : ''
}

// 상세 모달용 센서 지표
const sensorItems = computed(() => {
  const h = latestHealth.value
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

function formatTime(dt: string) { return dayjs(dt).format('YYYY-MM-DD HH:mm') }
function formatTimeFull(dt: string) { return dayjs(dt).format('YYYY-MM-DD HH:mm:ss') }
function formatDate(dt: string) { return dayjs(dt).format('YYYY-MM-DD') }

// 선정 근거 리포트
const reportData = computed(() => {
  const sel = selected.value
  if (!sel) return null
  const prob = (Number(sel.failureProbability) * 100).toFixed(1)
  const anomalies = sensorItems.value.filter((s) => s.status !== 'success')
  const levelWord = sel.riskLevel === 'CRITICAL' ? '긴급 점검 필요' : sel.riskLevel === 'WARNING' ? '조기 점검 권고' : '지속 모니터링 권고'
  return {
    chargerCode: sel.chargerCode,
    predictedAt: formatTime(sel.predictedAt),
    modelVersion: sel.modelVersion,
    riskLabel: label(PHM_RISK_LABEL, sel.riskLevel),
    riskType: PHM_RISK_TYPE[sel.riskLevel],
    prob,
    component: sel.predictedComponent ?? '미확인',
    rul: sel.remainingUsefulLifeHours ? `약 ${sel.remainingUsefulLifeHours}시간` : '미산출',
    levelWord,
    anomalies,
    hasHealth: !!latestHealth.value,
  }
})

const narrative = computed(() => {
  const d = reportData.value
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

function copyReport() {
  const d = reportData.value
  if (!d) return
  const anomalyText = !d.anomalies.length
    ? '  - 모든 센서 정상 범위 내'
    : d.anomalies.map((a) => `  - ${a.label}: ${a.value} (정상 ${a.range}) [${a.statusLabel}]`).join('\n')
  const text = `【 예상 고장 부품 선정 근거 리포트 】\n충전기: ${d.chargerCode} | 예측: ${d.predictedAt} | 모델: ${d.modelVersion}\n\n① 판정 결과\n  위험 등급: ${d.riskLabel} | 고장 확률: ${d.prob}%\n  예상 고장 부품: ${d.component}\n  잔여 수명(RUL): ${d.rul}\n\n② 이상 감지 항목 (${d.anomalies.length}개)\n${anomalyText}\n\n③ 선정 근거 서술\n${narrative.value}\n\n④ 권고 조치: ${d.levelWord}`
  navigator.clipboard.writeText(text)
    .then(() => ElMessage.success('리포트가 클립보드에 복사되었습니다.'))
    .catch(() => ElMessage.warning('복사 기능을 지원하지 않는 환경입니다.'))
}

// 로그 모달 열릴 때 트렌드 차트 렌더링
watch(logVisible, async (v) => {
  if (v) return
  if (logChart) { logChart.dispose(); logChart = null }
  logPeriod.value = '1d'
  logDateRange.value = null
})

watch(logData, async (data) => {
  if (!logVisible.value || !data.length) return
  await nextTick()
  if (!logChartEl.value) return
  if (logChart) logChart.dispose()
  logChart = echarts.init(logChartEl.value)
  const fmt = logPeriod.value === '1d' ? 'HH:mm' : 'MM/DD HH:mm'
  const times = data.map(r => dayjs(r.measuredAt).format(fmt))
  logChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['온도(°C)', '진동×100(g)'], top: 0, right: 0, textStyle: { fontSize: 11 } },
    grid: { left: 40, right: 10, top: 28, bottom: 24 },
    xAxis: { type: 'category', data: times, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', axisLabel: { fontSize: 10 } },
    series: [
      {
        name: '온도(°C)', type: 'line', smooth: true, symbol: 'none',
        data: data.map(r => Number(r.temperature).toFixed(1)),
        lineStyle: { color: '#F56C6C', width: 2 },
        areaStyle: { color: 'rgba(245,108,108,0.08)' },
        markLine: { silent: true, lineStyle: { color: '#F56C6C', type: 'dashed', width: 1 }, data: [{ yAxis: 60, name: '주의' }, { yAxis: 80, name: '위험' }] },
      },
      {
        name: '진동×100(g)', type: 'line', smooth: true, symbol: 'none',
        data: data.map(r => (Number(r.vibration) * 100).toFixed(2)),
        lineStyle: { color: '#E6A23C', width: 2 },
        areaStyle: { color: 'rgba(230,162,60,0.08)' },
      },
    ],
  })
})

// 상세 모달 게이지
watch(detailVisible, async (v) => {
  if (v) {
    await nextTick()
    if (gaugeEl.value && selected.value) {
      if (gaugeChart) gaugeChart.dispose()
      gaugeChart = echarts.init(gaugeEl.value)
      const prob = Math.round(Number(selected.value.failureProbability) * 100)
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
  } else {
    if (gaugeChart) { gaugeChart.dispose(); gaugeChart = null }
    reportExpanded.value = false
  }
})

onMounted(load)
</script>

<style scoped>
.section-title { font-weight: 600; font-size: 13px; color: #303133; border-bottom: 1px solid #ebeef5; padding-bottom: 6px; margin-bottom: 10px; }
.section-sub { font-weight: 400; font-size: 11px; color: #909399; }
.sensor-row { margin-bottom: 12px; }
.sensor-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2px; }
.sensor-name { font-size: 12px; color: #606266; }
.sensor-value-row { display: flex; justify-content: space-between; align-items: baseline; }
.sensor-val { font-size: 16px; font-weight: 700; }
.sensor-range { font-size: 11px; color: #909399; }
.val-danger { color: #F56C6C; font-weight: 600; }
.val-warning { color: #E6A23C; font-weight: 600; }
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
