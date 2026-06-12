<template>
  <div style="padding: 20px">
    <el-card shadow="never">
      <template #header><span style="font-weight: 600">교통 위험도 분석</span></template>

      <!-- 위험도 범례 -->
      <div class="risk-legend">
        <span class="legend-label">위험도 지표 (0 ~ 100%)</span>
        <div class="legend-items">
          <el-tag type="success" size="small" effect="plain">안전 0~40%</el-tag>
          <el-tag type="warning" size="small" effect="plain">주의 40~70%</el-tag>
          <el-tag type="danger"  size="small" effect="plain">위험 70~85%</el-tag>
          <el-tag type="danger"  size="small" effect="dark">긴급 85~100%</el-tag>
        </div>
      </div>

      <el-row :gutter="12">
        <el-col v-for="station in stationStore.stations.slice(0, 6)" :key="station.id" :span="8" style="margin-bottom: 12px">
          <el-card shadow="hover" :body-style="{ padding: '12px 14px' }" class="risk-card"
            style="cursor: pointer" @click="openDetail(station.id)">
            <div class="risk-header">
              <span class="station-name">{{ station.name }}</span>
              <el-tag :type="TRAFFIC_RISK_TYPE[latestRisk(station.id)?.riskLevel ?? 'LOW']" size="small">
                {{ label(TRAFFIC_RISK_LABEL, latestRisk(station.id)?.riskLevel ?? '미집계') }}
              </el-tag>
            </div>
            <div style="display:flex; align-items:center; gap:8px; margin-top:8px">
              <el-progress
                style="flex:1"
                :percentage="Math.round((latestRisk(station.id)?.riskScore ?? 0) * 100)"
                :status="(latestRisk(station.id)?.riskScore ?? 0) > 0.85 ? 'exception' : (latestRisk(station.id)?.riskScore ?? 0) > 0.4 ? 'warning' : ''"
                :stroke-width="8" :show-text="false" />
              <span class="risk-pct">{{ ((latestRisk(station.id)?.riskScore ?? 0) * 100).toFixed(0) }}%</span>
            </div>
            <div class="risk-time" v-if="latestRisk(station.id)">
              분석: {{ formatTime(latestRisk(station.id)!.predictedAt) }}
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 상세 모달 -->
    <el-dialog v-model="detailVisible" title="교통 위험도 상세" width="500px">
      <div v-if="selectedRisk && selectedStation">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="충전소">{{ selectedStation.name }}</el-descriptions-item>
          <el-descriptions-item label="위험 수준">
            <el-tag :type="TRAFFIC_RISK_TYPE[selectedRisk.riskLevel]">
              {{ label(TRAFFIC_RISK_LABEL, selectedRisk.riskLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="위험도 점수">{{ (selectedRisk.riskScore * 100).toFixed(1) }}%</el-descriptions-item>
          <el-descriptions-item label="분석 시각">{{ formatTimeFull(selectedRisk.predictedAt) }}</el-descriptions-item>
          <el-descriptions-item label="모델 버전">{{ selectedRisk.modelVersion }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="selectedRisk.contributingFactors && Object.keys(selectedRisk.contributingFactors).length > 0"
          style="margin-top: 16px">
          <div style="font-weight: 600; font-size: 13px; margin-bottom: 2px">요인별 위험 지수</div>
          <div style="font-size: 11px; color: #909399; margin-bottom: 10px">
            각 요인의 독립적 위험 수준 (0% = 안전 · 100% = 최대 위험), 종합 점수와 별개로 산출됩니다.
          </div>
          <div v-for="(val, key) in selectedRisk.contributingFactors" :key="key" style="margin-bottom: 10px">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 3px">
              <span style="font-size: 13px">{{ FACTOR_LABEL[key] ?? key }}</span>
              <div style="display: flex; align-items: center; gap: 6px">
                <el-tag
                  :type="val > 0.7 ? 'danger' : val > 0.4 ? 'warning' : 'success'"
                  size="small" effect="plain">
                  {{ val > 0.7 ? '위험' : val > 0.4 ? '주의' : '안전' }}
                </el-tag>
                <span style="font-size: 13px; font-weight: 600; min-width: 42px; text-align: right">
                  {{ (val * 100).toFixed(0) }}%
                </span>
              </div>
            </div>
            <el-progress
              :percentage="Math.round(val * 100)"
              :status="val > 0.7 ? 'exception' : val > 0.4 ? 'warning' : ''"
              :stroke-width="7" :show-text="false" />
          </div>
        </div>
        <el-empty v-else description="기여 요인 데이터 없음" :image-size="60" style="margin-top: 16px" />
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">닫기</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useStationStore } from '@/stores/station'
import http from '@/api/http'
import type { RiskPredictionResponse, StationResponse } from '@/types'
import { TRAFFIC_RISK_LABEL, TRAFFIC_RISK_TYPE, label } from '@/utils/labels'
import dayjs from 'dayjs'

const FACTOR_LABEL: Record<string, string> = {
  '교통혼잡도': '교통 혼잡도',
  '위반빈도': '위반 발생 빈도',
  '시간대가중치': '시간대 위험 가중치',
  '기상조건': '기상 악조건',
}

const stationStore = useStationStore()
const riskMap = ref<Record<number, RiskPredictionResponse>>({})
const detailVisible = ref(false)
const selectedRisk = ref<RiskPredictionResponse | null>(null)
const selectedStation = ref<StationResponse | null>(null)

function latestRisk(stationId: number) { return riskMap.value[stationId] }

function openDetail(stationId: number) {
  const risk = riskMap.value[stationId]
  const station = stationStore.stations.find((s) => s.id === stationId)
  if (!risk || !station) return
  selectedRisk.value = risk
  selectedStation.value = station
  detailVisible.value = true
}

function formatTime(dt: string) { return dayjs(dt).format('MM/DD HH:mm') }
function formatTimeFull(dt: string) { return dayjs(dt).format('YYYY-MM-DD HH:mm:ss') }

async function loadRisks() {
  for (const station of stationStore.stations) {
    try {
      const res = await http.get(`/stations/${station.id}/risk/latest`)
      if (res.data.data) riskMap.value[station.id] = res.data.data
    } catch { /* 데이터 없음 */ }
  }
}

onMounted(async () => {
  await stationStore.fetchAll()
  await loadRisks()
})
</script>

<style scoped>
.risk-legend {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 14px; flex-wrap: wrap;
}
.legend-label { font-size: 12px; color: #606266; font-weight: 600; }
.legend-items { display: flex; gap: 6px; flex-wrap: wrap; }
.risk-card { border-radius: 8px; transition: box-shadow 0.2s; }
.risk-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.risk-header { display: flex; justify-content: space-between; align-items: center; }
.station-name { font-weight: 600; font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 130px; }
.risk-pct { font-size: 13px; font-weight: 700; color: #303133; min-width: 36px; text-align: right; }
.risk-time { font-size: 11px; color: #909399; margin-top: 5px; }
</style>
