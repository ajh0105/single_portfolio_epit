<template>
  <div class="monitor-wrap">
    <!-- 상단 툴바 -->
    <div class="monitor-toolbar">
      <div class="toolbar-left">
        <span class="toolbar-title">
          <el-icon style="color: #f56c6c; margin-right: 6px"><VideoCameraFilled /></el-icon>
          실시간 관제 센터
        </span>
        <span class="live-time">{{ currentTime }}</span>
      </div>
      <div class="toolbar-right">
        <el-button-group>
          <el-button
            v-for="opt in layoutOptions" :key="opt.value"
            size="small"
            :type="gridLayout === opt.value ? 'primary' : 'default'"
            @click="gridLayout = opt.value">
            {{ opt.label }}
          </el-button>
        </el-button-group>
        <el-button size="small" :type="autoRotate ? 'warning' : 'default'" @click="toggleAutoRotate" style="margin-left: 8px">
          <el-icon><RefreshRight /></el-icon> {{ autoRotate ? '순환 ON' : '순환 OFF' }}
        </el-button>
      </div>
    </div>

    <!-- 채널 그리드 -->
    <div class="channel-grid" :class="`grid-${gridLayout}`">
      <div
        v-for="(ch, idx) in visibleChannels"
        :key="ch.id"
        class="channel-card"
        :class="{ 'channel-active': activeChannel === idx }"
        @click="openFullscreen(idx)">
        <!-- 영상 -->
        <video
          :ref="el => videoRefs[idx] = el as HTMLVideoElement"
          :src="ch.src"
          class="channel-video"
          autoplay muted loop playsinline
          @error="ch.offline = true"
        />
        <!-- 오프라인 오버레이 -->
        <div v-if="ch.offline" class="offline-overlay">
          <el-icon size="32"><VideoCameraFilled /></el-icon>
          <span>신호 없음</span>
        </div>
        <!-- 채널 정보 오버레이 -->
        <div class="channel-overlay">
          <div class="channel-info-top">
            <span class="rec-badge">● REC</span>
            <span class="channel-time">{{ currentTime }}</span>
          </div>
          <div class="channel-info-bottom">
            <span class="channel-name">{{ ch.name }}</span>
            <el-tag :type="ch.offline ? 'danger' : 'success'" size="small" effect="dark">
              {{ ch.offline ? '오프라인' : '정상' }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 전체 화면 모달 -->
    <el-dialog
      v-model="fullscreenVisible"
      :title="selectedChannel?.name ?? ''"
      width="80%"
      :close-on-click-modal="true"
      destroy-on-close>
      <!-- 16:9 비율 컨테이너 -->
      <div style="position: relative; background: #000; border-radius: 6px; overflow: hidden; aspect-ratio: 16/9; width: 100%">
        <video
          v-if="selectedChannel"
          :src="selectedChannel.src"
          style="position: absolute; inset: 0; width: 100%; height: 100%; object-fit: contain; display: block"
          autoplay muted loop playsinline controls
        />
        <div v-if="selectedChannel?.offline" class="offline-overlay">
          <el-icon size="48"><VideoCameraFilled /></el-icon>
          <span>신호 없음</span>
        </div>
        <div class="channel-overlay" style="pointer-events: none">
          <div class="channel-info-top">
            <span class="rec-badge">● REC</span>
            <span class="channel-time">{{ currentTime }}</span>
          </div>
          <div class="channel-info-bottom">
            <span class="channel-name" style="font-size: 15px">{{ selectedChannel?.name }}</span>
            <el-tag type="success" size="small" effect="dark">정상</el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStationStore } from '@/stores/station'

const stationStore = useStationStore()

interface Channel {
  id: number
  name: string
  src: string
  offline: boolean
}

type GridLayout = '1x1' | '2x2' | '2x3'
const gridLayout = ref<GridLayout>('2x3')
const activeChannel = ref<number | null>(null)
const fullscreenVisible = ref(false)
const selectedChannel = ref<Channel | null>(null)
const autoRotate = ref(false)
const currentTime = ref('')
const videoRefs = ref<Record<number, HTMLVideoElement | null>>({})

let timeTimer: ReturnType<typeof setInterval> | null = null
let rotateTimer: ReturnType<typeof setInterval> | null = null

const layoutOptions: { value: GridLayout; label: string }[] = [
  { value: '1x1', label: '1×1' },
  { value: '2x2', label: '2×2' },
  { value: '2x3', label: '2×3' },
]

// 충전소 목록 기반으로 채널 생성 (영상은 공통 MP4 사용)
const channels = computed<Channel[]>(() => {
  const stations = stationStore.stations.length > 0
    ? stationStore.stations
    : [{ id: 1, name: 'E-pit 강남점', stationCode: 'DEMO-1' }]
  return stations.map((s, i) => ({
    id: s.id,
    name: `${(s as any).name ?? s.stationCode} CAM ${String(i + 1).padStart(2, '0')}`,
    src: '/videos/parking.mp4',
    offline: false,
  }))
})

const visibleChannels = computed(() => {
  const max = gridLayout.value === '1x1' ? 1 : gridLayout.value === '2x2' ? 4 : 6
  return channels.value.slice(0, max)
})

function openFullscreen(idx: number) {
  selectedChannel.value = visibleChannels.value[idx]
  fullscreenVisible.value = true
}

function toggleAutoRotate() {
  autoRotate.value = !autoRotate.value
  if (rotateTimer) clearInterval(rotateTimer)
  if (autoRotate.value) {
    let cur = 0
    rotateTimer = setInterval(() => {
      cur = (cur + 1) % visibleChannels.value.length
      activeChannel.value = cur
    }, 5000)
  }
}

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('ko-KR', { hour12: false })
}

onMounted(async () => {
  await stationStore.fetchAll()
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
  if (rotateTimer) clearInterval(rotateTimer)
})
</script>

<style scoped>
.monitor-wrap {
  background: #0d0d1a;
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.monitor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: #141428;
  border-bottom: 1px solid #2a2a4a;
  flex-shrink: 0;
}

.toolbar-title {
  color: #e0e0ff;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
}

.live-time {
  color: #8888aa;
  font-size: 13px;
  margin-left: 16px;
  font-variant-numeric: tabular-nums;
}

.toolbar-right { display: flex; align-items: center; }

.channel-grid {
  flex: 1;
  display: grid;
  gap: 4px;
  padding: 4px;
}

.grid-1x1 { grid-template-columns: 1fr; grid-template-rows: 1fr; }
.grid-2x2 { grid-template-columns: repeat(2, 1fr); grid-template-rows: repeat(2, 1fr); }
.grid-2x3 { grid-template-columns: repeat(3, 1fr); grid-template-rows: repeat(2, 1fr); }

.channel-card {
  position: relative;
  background: #111122;
  border: 1px solid #2a2a4a;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s;
  aspect-ratio: 16/9;
}

.channel-card:hover { border-color: #4466ff; }
.channel-active { border-color: #1890ff !important; box-shadow: 0 0 0 2px #1890ff55; }

.channel-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.offline-overlay {
  position: absolute;
  inset: 0;
  background: #0d0d1a;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #555577;
  gap: 8px;
  font-size: 13px;
}

.channel-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 8px 10px;
  pointer-events: none;
}

.channel-info-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rec-badge {
  color: #f56c6c;
  font-size: 11px;
  font-weight: 700;
  animation: blink 1.2s step-end infinite;
}

@keyframes blink { 50% { opacity: 0; } }

.channel-time {
  color: #ffffffcc;
  font-size: 11px;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 1px 2px rgba(0,0,0,0.8);
}

.channel-info-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.channel-name {
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(0,0,0,0.9);
}
</style>
