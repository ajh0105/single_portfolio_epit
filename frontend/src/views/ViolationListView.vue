<template>
  <div style="padding: 20px">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: 600">불법 주차 위반 현황</span>
          <div style="display: flex; gap: 8px">
            <el-button size="small" @click="exportImage"><el-icon><Picture /></el-icon> 이미지</el-button>
            <el-button size="small" @click="exportPdf"><el-icon><Document /></el-icon> PDF</el-button>
            <el-button size="small" type="primary" @click="openCsvDialog"><el-icon><Download /></el-icon> CSV</el-button>
          </div>
        </div>
      </template>

      <!-- 필터 바 -->
      <div style="display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; align-items: center">
        <el-select v-model="filterType" size="small" style="width: 140px" clearable placeholder="위반 유형">
          <el-option label="전체" value="" />
          <el-option v-for="(v, k) in VIOLATION_TYPE_LABEL" :key="k" :label="v" :value="k" />
        </el-select>
        <el-select v-model="filterStatus" size="small" style="width: 110px" clearable placeholder="처리 상태">
          <el-option label="전체" value="" />
          <el-option v-for="(v, k) in VIOLATION_STATUS_LABEL" :key="k" :label="v" :value="k" />
        </el-select>
        <el-button-group size="small">
          <el-button :type="sortOrder === 'desc' ? 'primary' : 'default'" @click="sortOrder = 'desc'">최신순</el-button>
          <el-button :type="sortOrder === 'asc' ? 'primary' : 'default'" @click="sortOrder = 'asc'">오래된순</el-button>
        </el-button-group>
        <el-button size="small" @click="resetFilters">초기화</el-button>
        <span style="font-size: 13px; color: #909399; margin-left: 4px">
          전체 {{ violations.length }}건
          <span v-if="isFiltered"> · 필터 결과 {{ filteredViolations.length }}건 표시</span>
        </span>
      </div>

      <div id="violation-table-area">
        <el-table :data="filteredViolations" v-loading="loading" stripe @row-click="openDetail" style="width:100%"
          :row-style="{ cursor: 'pointer' }">
          <el-table-column type="index" label="번호" width="60" align="center" />
          <el-table-column prop="occurredAt" label="감지 시각" min-width="150">
            <template #default="{ row }">{{ formatTime(row.occurredAt) }}</template>
          </el-table-column>
          <el-table-column prop="stationName" label="충전소" min-width="160" />
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
      </div>
    </el-card>

    <!-- 상세 모달 -->
    <el-dialog v-model="detailVisible" title="위반 상세 정보" width="660px">
      <div v-if="selected">
        <!-- 처리 단계 표시 -->
        <el-steps :active="statusStep(selected.status)" finish-status="success" size="small"
          style="margin-bottom: 16px">
          <el-step title="감지" description="AI 자동" />
          <el-step title="통보" description="AI 자동" />
          <el-step title="해결" description="수동 처리" />
        </el-steps>
        <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">
          <el-descriptions-item label="감지 시각" :span="2">{{ formatTimeFull(selected.occurredAt) }}</el-descriptions-item>
          <el-descriptions-item label="충전소">{{ selected.stationName }}</el-descriptions-item>
          <el-descriptions-item label="충전기 위치">{{ selected.chargerId ? `${selected.chargerId}번` : '-' }}</el-descriptions-item>
          <el-descriptions-item label="번호판">{{ selected.plateNumber }}</el-descriptions-item>
          <el-descriptions-item label="위반 유형">
            <el-tag type="danger" size="small">{{ label(VIOLATION_TYPE_LABEL, selected.violationType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="처리 상태">
            <el-tag :type="VIOLATION_STATUS_TYPE[selected.status]" size="small">
              {{ label(VIOLATION_STATUS_LABEL, selected.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="처리 완료" v-if="selected.resolvedAt">{{ formatTimeFull(selected.resolvedAt) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 이미지 영역 -->
        <el-row :gutter="12">
          <el-col :span="12">
            <div class="img-label">차량 전체 이미지</div>
            <el-image v-if="selected.evidenceImagePath"
              :src="selected.evidenceImagePath" fit="contain"
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
          v-if="selected && (selected.status === 'DETECTED' || selected.status === 'NOTIFIED')"
          type="primary" :loading="resolveLoading" @click="resolveViolation">
          해결 처리
        </el-button>
        <el-button @click="detailVisible = false">닫기</el-button>
      </template>
    </el-dialog>

    <!-- CSV 기간 선택 다이얼로그 -->
    <el-dialog v-model="csvDialogVisible" title="CSV 내보내기 — 기간 선택" width="420px">
      <el-form label-width="80px">
        <el-form-item label="기간">
          <el-date-picker
            v-model="csvDateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="시작일"
            end-placeholder="종료일"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="csvDialogVisible = false">취소</el-button>
        <el-button type="primary" @click="downloadCsv">내보내기</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import type { ViolationResponse } from '@/types'
import {
  VIOLATION_TYPE_LABEL, VIOLATION_STATUS_LABEL, VIOLATION_STATUS_TYPE, label,
} from '@/utils/labels'
import dayjs from 'dayjs'

const violations = ref<ViolationResponse[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const selected = ref<ViolationResponse | null>(null)
const resolveLoading = ref(false)
const csvDialogVisible = ref(false)
const csvDateRange = ref<[string, string] | null>(null)

const filterType = ref('')
const filterStatus = ref('')
const sortOrder = ref<'desc' | 'asc'>('desc')

const isFiltered = computed(() => filterType.value !== '' || filterStatus.value !== '')

const filteredViolations = computed(() => {
  let list = [...violations.value]
  if (filterType.value) list = list.filter((v) => v.violationType === filterType.value)
  if (filterStatus.value) list = list.filter((v) => v.status === filterStatus.value)
  list.sort((a, b) => {
    const diff = dayjs(a.occurredAt).valueOf() - dayjs(b.occurredAt).valueOf()
    return sortOrder.value === 'desc' ? -diff : diff
  })
  return list
})

function resetFilters() {
  filterType.value = ''
  filterStatus.value = ''
  sortOrder.value = 'desc'
}

async function load() {
  loading.value = true
  try {
    const res = await http.get('/violations', { params: { size: 50 } })
    violations.value = res.data.data?.content ?? []
  } finally { loading.value = false }
}

function openDetail(row: ViolationResponse) {
  selected.value = { ...row }
  detailVisible.value = true
}

function statusStep(status: string) {
  if (status === 'DETECTED') return 1
  if (status === 'NOTIFIED') return 2
  return 3
}

async function resolveViolation() {
  if (!selected.value) return
  resolveLoading.value = true
  try {
    const res = await http.patch(`/violations/${selected.value.id}/resolve`)
    const updated = res.data.data
    selected.value = { ...selected.value, status: updated.status, resolvedAt: updated.resolvedAt }
    const idx = violations.value.findIndex((v) => v.id === selected.value!.id)
    if (idx !== -1) violations.value[idx] = { ...violations.value[idx], status: updated.status, resolvedAt: updated.resolvedAt }
    ElMessage.success('해결 처리되었습니다.')
  } catch {
    ElMessage.error('처리에 실패했습니다.')
  } finally {
    resolveLoading.value = false
  }
}

function formatTime(dt: string) { return dayjs(dt).format('YYYY-MM-DD HH:mm') }
function formatTimeFull(dt: string) { return dayjs(dt).format('YYYY-MM-DD HH:mm:ss') }

// 이미지 내보내기 (html2canvas)
async function exportImage() {
  const el = document.getElementById('violation-table-area')
  if (!el) return
  const html2canvas = (await import('html2canvas')).default
  const canvas = await html2canvas(el)
  const a = document.createElement('a')
  a.download = `위반현황_${dayjs().format('YYYYMMDD')}.png`
  a.href = canvas.toDataURL()
  a.click()
}

// PDF 내보내기 (jsPDF)
async function exportPdf() {
  const el = document.getElementById('violation-table-area')
  if (!el) return
  const html2canvas = (await import('html2canvas')).default
  const { jsPDF } = await import('jspdf')
  const canvas = await html2canvas(el)
  const imgData = canvas.toDataURL('image/png')
  const pdf = new jsPDF({ orientation: 'landscape' })
  const w = pdf.internal.pageSize.getWidth()
  const h = (canvas.height * w) / canvas.width
  pdf.addImage(imgData, 'PNG', 0, 0, w, h)
  pdf.save(`위반현황_${dayjs().format('YYYYMMDD')}.pdf`)
}

function openCsvDialog() {
  csvDateRange.value = null
  csvDialogVisible.value = true
}

function downloadCsv() {
  const [start, end] = csvDateRange.value ?? ['', '']
  const filtered = violations.value.filter((v) => {
    if (!start && !end) return true
    const d = dayjs(v.occurredAt)
    if (start && d.isBefore(dayjs(start))) return false
    if (end && d.isAfter(dayjs(end).endOf('day'))) return false
    return true
  })
  const header = ['감지시각', '충전소', '번호판', '위반유형', '처리상태']
  const rows = filtered.map((v) => [
    formatTimeFull(v.occurredAt),
    v.stationName,
    v.plateNumber,
    VIOLATION_TYPE_LABEL[v.violationType] ?? v.violationType,
    VIOLATION_STATUS_LABEL[v.status] ?? v.status,
  ])
  // BOM 포함 (Excel 한글 깨짐 방지)
  const csv = '﻿' + [header, ...rows].map((r) => r.join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `위반현황_${start || 'all'}_${end || 'all'}.csv`
  a.click()
  URL.revokeObjectURL(url)
  csvDialogVisible.value = false
}

onMounted(load)
</script>

<style scoped>
.img-label { font-size: 12px; color: #606266; margin-bottom: 6px; font-weight: 500; }
.img-placeholder {
  height: 180px; border: 1px dashed #dcdfe6; border-radius: 4px;
  display: flex; align-items: center; justify-content: center;
  color: #c0c4cc; font-size: 13px;
}
</style>
