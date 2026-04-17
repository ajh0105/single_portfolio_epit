<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">통합 관제 대시보드</h1>

    <!-- KPI 카드 -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <KpiCard
        title="전체 충전소"
        :value="kpi?.totalStations ?? '-'"
        :sub="`활성 ${kpi?.activeStations ?? 0}개`"
        color="blue"
        icon="station"
      />
      <KpiCard
        title="가동률"
        :value="kpi ? `${kpi.stationAvailabilityRate.toFixed(1)}%` : '-'"
        color="green"
        icon="chart"
      />
      <KpiCard
        title="미해결 알림"
        :value="kpi?.pendingAlerts ?? '-'"
        :sub="`긴급 ${kpi?.criticalAlerts ?? 0}건`"
        :color="kpi?.criticalAlerts > 0 ? 'red' : 'yellow'"
        icon="bell"
      />
      <KpiCard
        title="불법주차 (24h)"
        :value="kpi?.pendingViolationsLast24h ?? '-'"
        color="orange"
        icon="car"
      />
    </div>

    <!-- 알림 패널 + 충전소 상태 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2">
        <StationStatusTable :stations="stationStore.stations" />
      </div>
      <div>
        <AlertPanel :alerts="alertStore.alerts.slice(0, 10)" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useStationStore } from '@/stores/stationStore'
import { useAlertStore } from '@/stores/alertStore'
import KpiCard from '@/components/KpiCard.vue'
import AlertPanel from '@/components/AlertPanel.vue'
import StationStatusTable from '@/components/StationStatusTable.vue'

const stationStore = useStationStore()
const alertStore = useAlertStore()
const kpi = stationStore.kpi

onMounted(async () => {
  await Promise.all([
    stationStore.fetchStations(),
    stationStore.fetchKpi(),
    alertStore.fetchActive(),
  ])
})
</script>
