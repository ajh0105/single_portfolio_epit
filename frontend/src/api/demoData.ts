import dayjs from 'dayjs'

const now = () => dayjs().toISOString()
const ago = (h: number) => dayjs().subtract(h, 'hour').toISOString()

// ── 공통 래퍼 ──────────────────────────────────────────────
export function ok<T>(data: T) {
  return { success: true, data, error: null, timestamp: now() }
}
export function page<T>(content: T[], total = content.length) {
  return { content, page: 0, size: 50, totalElements: total, totalPages: 1 }
}

// ── 충전소 ─────────────────────────────────────────────────
export const STATIONS = [
  { id: 1, stationCode: 'EP-GN-001', name: 'E-pit 강남', address: '서울시 강남구 테헤란로 521', latitude: 37.5065, longitude: 127.0536, totalChargers: 3, operationStatus: 'ACTIVE', cameraDeviceId: 'CAM-GN-01', createdAt: ago(720) },
  { id: 2, stationCode: 'EP-JL-001', name: 'E-pit 잠실', address: '서울시 송파구 올림픽로 300', latitude: 37.5140, longitude: 127.1001, totalChargers: 3, operationStatus: 'ACTIVE', cameraDeviceId: 'CAM-JL-01', createdAt: ago(700) },
  { id: 3, stationCode: 'EP-IC-001', name: 'E-pit 인천공항', address: '인천시 중구 공항로 272', latitude: 37.4602, longitude: 126.4407, totalChargers: 2, operationStatus: 'MAINTENANCE', cameraDeviceId: null, createdAt: ago(680) },
  { id: 4, stationCode: 'EP-PG-001', name: 'E-pit 판교', address: '경기도 성남시 분당구 대왕판교로 660', latitude: 37.3947, longitude: 127.1112, totalChargers: 3, operationStatus: 'ACTIVE', cameraDeviceId: 'CAM-PG-01', createdAt: ago(660) },
]

// ── 충전기 ─────────────────────────────────────────────────
export const CHARGERS = [
  { id: 1, stationId: 1, chargerCode: 'EP-GN-001-C01', connectorType: 'CCS2', maxPowerKw: 350, status: 'AVAILABLE', healthScore: 92, lastMaintenanceAt: ago(720) },
  { id: 2, stationId: 1, chargerCode: 'EP-GN-001-C02', connectorType: 'CCS2', maxPowerKw: 350, status: 'CHARGING',   healthScore: 45, lastMaintenanceAt: ago(1440) },
  { id: 3, stationId: 1, chargerCode: 'EP-GN-001-C03', connectorType: 'CHADEMO', maxPowerKw: 100, status: 'FAULT',  healthScore: 18, lastMaintenanceAt: ago(2160) },
  { id: 4, stationId: 2, chargerCode: 'EP-JL-001-C01', connectorType: 'CCS2', maxPowerKw: 350, status: 'AVAILABLE', healthScore: 87, lastMaintenanceAt: ago(480) },
  { id: 5, stationId: 2, chargerCode: 'EP-JL-001-C02', connectorType: 'CCS1', maxPowerKw: 150, status: 'CHARGING',  healthScore: 71, lastMaintenanceAt: ago(960) },
  { id: 6, stationId: 2, chargerCode: 'EP-JL-001-C03', connectorType: 'CCS2', maxPowerKw: 350, status: 'OFFLINE',   healthScore: 63, lastMaintenanceAt: ago(1200) },
  { id: 7, stationId: 3, chargerCode: 'EP-IC-001-C01', connectorType: 'CCS2', maxPowerKw: 350, status: 'OFFLINE',   healthScore: 55, lastMaintenanceAt: ago(2400) },
  { id: 8, stationId: 3, chargerCode: 'EP-IC-001-C02', connectorType: 'CCS2', maxPowerKw: 350, status: 'OFFLINE',   healthScore: 60, lastMaintenanceAt: ago(2400) },
  { id: 9, stationId: 4, chargerCode: 'EP-PG-001-C01', connectorType: 'CCS2', maxPowerKw: 350, status: 'AVAILABLE', healthScore: 95, lastMaintenanceAt: ago(240) },
  { id: 10, stationId: 4, chargerCode: 'EP-PG-001-C02', connectorType: 'CCS1', maxPowerKw: 150, status: 'CHARGING', healthScore: 82, lastMaintenanceAt: ago(480) },
  { id: 11, stationId: 4, chargerCode: 'EP-PG-001-C03', connectorType: 'CCS2', maxPowerKw: 350, status: 'RESERVED', healthScore: 78, lastMaintenanceAt: ago(720) },
]

// ── 위반 ───────────────────────────────────────────────────
export const VIOLATIONS = [
  { id: 1, stationId: 1, stationName: 'E-pit 강남', chargerId: 1, plateNumber: '12가 3456', violationType: 'NON_EV_OCCUPANCY', status: 'RESOLVED',  occurredAt: ago(48),  resolvedAt: ago(46), evidenceImagePath: null },
  { id: 2, stationId: 1, stationName: 'E-pit 강남', chargerId: 2, plateNumber: '서울 78나 9012', violationType: 'OVERSTAY',         status: 'NOTIFIED',  occurredAt: ago(6),   resolvedAt: null, evidenceImagePath: null },
  { id: 3, stationId: 2, stationName: 'E-pit 잠실', chargerId: 4, plateNumber: '34다 5678', violationType: 'NON_EV_OCCUPANCY', status: 'DETECTED',  occurredAt: ago(1),   resolvedAt: null, evidenceImagePath: null },
  { id: 4, stationId: 2, stationName: 'E-pit 잠실', chargerId: 5, plateNumber: '경기 56라 7890', violationType: 'OVERSTAY',      status: 'RESOLVED',  occurredAt: ago(72),  resolvedAt: ago(70), evidenceImagePath: null },
  { id: 5, stationId: 4, stationName: 'E-pit 판교', chargerId: 9, plateNumber: '78마 9012', violationType: 'NON_EV_OCCUPANCY', status: 'NOTIFIED',  occurredAt: ago(12),  resolvedAt: null, evidenceImagePath: null },
  { id: 6, stationId: 4, stationName: 'E-pit 판교', chargerId: 10, plateNumber: '부산 90바 1234', violationType: 'OVERSTAY',    status: 'DETECTED',  occurredAt: ago(2),   resolvedAt: null, evidenceImagePath: null },
  { id: 7, stationId: 1, stationName: 'E-pit 강남', chargerId: 3, plateNumber: '12사 3456', violationType: 'NON_EV_OCCUPANCY', status: 'RESOLVED',  occurredAt: ago(120), resolvedAt: ago(118), evidenceImagePath: null },
  { id: 8, stationId: 2, stationName: 'E-pit 잠실', chargerId: 6, plateNumber: '인천 34아 5678', violationType: 'OVERSTAY',    status: 'FALSE_POSITIVE', occurredAt: ago(96), resolvedAt: ago(94), evidenceImagePath: null },
]

// ── PHM 예측 ───────────────────────────────────────────────
export const PREDICTIONS = [
  { id: 1, chargerId: 3, chargerCode: 'EP-GN-001-C03', chargerStatus: 'FAULT', chargerMaxPowerKw: 100, chargerHealthScore: 18, chargerLastMaintenanceAt: ago(2160), predictedAt: ago(2), failureProbability: 0.93, remainingUsefulLifeHours: 12, riskLevel: 'CRITICAL', predictedComponent: '전력 변환 모듈', modelVersion: 'LSTM-v2.1.0' },
  { id: 2, chargerId: 2, chargerCode: 'EP-GN-001-C02', chargerStatus: 'CHARGING', chargerMaxPowerKw: 350, chargerHealthScore: 45, chargerLastMaintenanceAt: ago(1440), predictedAt: ago(3), failureProbability: 0.82, remainingUsefulLifeHours: 36, riskLevel: 'CRITICAL', predictedComponent: 'IGBT 모듈', modelVersion: 'LSTM-v2.1.0' },
  { id: 3, chargerId: 6, chargerCode: 'EP-JL-001-C03', chargerStatus: 'OFFLINE', chargerMaxPowerKw: 350, chargerHealthScore: 63, chargerLastMaintenanceAt: ago(1200), predictedAt: ago(5), failureProbability: 0.61, remainingUsefulLifeHours: 96, riskLevel: 'WARNING', predictedComponent: '냉각 시스템', modelVersion: 'LSTM-v2.1.0' },
  { id: 4, chargerId: 5, chargerCode: 'EP-JL-001-C02', chargerStatus: 'CHARGING', chargerMaxPowerKw: 150, chargerHealthScore: 71, chargerLastMaintenanceAt: ago(960), predictedAt: ago(8), failureProbability: 0.34, remainingUsefulLifeHours: 240, riskLevel: 'WARNING', predictedComponent: '커넥터/케이블', modelVersion: 'LSTM-v2.1.0' },
]

// ── 최신 센서 ──────────────────────────────────────────────
function latestHealth(chargerId: number): object {
  const map: Record<number, object> = {
    2: { id: 20, chargerId: 2, measuredAt: ago(0.1), voltage: 385.2, current: 98.5, temperature: 82.3, vibration: 0.38 },
    3: { id: 30, chargerId: 3, measuredAt: ago(0.2), voltage: 371.0, current: 103.1, temperature: 91.7, vibration: 0.52 },
    5: { id: 50, chargerId: 5, measuredAt: ago(0.1), voltage: 408.5, current: 72.3, temperature: 55.4, vibration: 0.08 },
    6: { id: 60, chargerId: 6, measuredAt: ago(0.3), voltage: 395.8, current: 81.2, temperature: 68.9, vibration: 0.22 },
  }
  const def = { id: chargerId * 10, chargerId, measuredAt: ago(0.1), voltage: 405.0, current: 65.0, temperature: 48.0, vibration: 0.05 }
  return (map[chargerId] as object) ?? def
}

// ── 센서 히스토리 (24시간 기준 생성) ────────────────────────
export function healthHistory(chargerId: number, hours: number): object[] {
  const isFault = [2, 3].includes(chargerId)
  return Array.from({ length: Math.min(hours, 72) }, (_, i) => {
    const t = isFault ? (i / hours) : 0
    return {
      id: chargerId * 1000 + i,
      chargerId,
      measuredAt: dayjs().subtract(hours - i, 'hour').toISOString(),
      voltage:     +(400 + (Math.random() - 0.5) * 30 - (isFault ? t * 30 : 0)).toFixed(1),
      current:     +(70  + (Math.random() - 0.5) * 20 + (isFault ? t * 35 : 0)).toFixed(1),
      temperature: +(50  + (Math.random() - 0.5) * 10 + (isFault ? t * 45 : 0)).toFixed(1),
      vibration:   +( 0.05 + Math.random() * 0.05 + (isFault ? t * 0.5 : 0)).toFixed(4),
    }
  })
}

// ── 알림 ───────────────────────────────────────────────────
export const ALERTS = [
  { id: 1,  stationId: 1, stationName: 'E-pit 강남', alertType: 'EQUIPMENT_FAILURE', severity: 'CRITICAL', title: '[긴급] EP-GN-001-C03 고장 확률 93%', message: '전력 변환 모듈 이상 감지. 즉시 점검이 필요합니다.', referenceType: 'CHARGER', referenceId: 3, isRead: false, createdAt: ago(2) },
  { id: 2,  stationId: 1, stationName: 'E-pit 강남', alertType: 'EQUIPMENT_FAILURE', severity: 'CRITICAL', title: '[긴급] EP-GN-001-C02 IGBT 모듈 위험', message: 'IGBT 모듈 열화 패턴 감지. 잔여 수명 36시간 예측.', referenceType: 'CHARGER', referenceId: 2, isRead: false, createdAt: ago(3) },
  { id: 3,  stationId: 2, stationName: 'E-pit 잠실', alertType: 'PARKING_VIOLATION', severity: 'WARNING',  title: '불법 주차 감지 — EP-JL-001-C01', message: '비전기차 점유 확인. 차량번호: 34다 5678', referenceType: 'VIOLATION', referenceId: 3, isRead: false, createdAt: ago(1) },
  { id: 4,  stationId: 4, stationName: 'E-pit 판교', alertType: 'PARKING_VIOLATION', severity: 'WARNING',  title: '초과 주차 감지 — EP-PG-001-C01', message: '충전 완료 후 30분 이상 점유 확인. 차량번호: 78마 9012', referenceType: 'VIOLATION', referenceId: 5, isRead: false, createdAt: ago(12) },
  { id: 5,  stationId: 2, stationName: 'E-pit 잠실', alertType: 'EQUIPMENT_FAILURE', severity: 'WARNING',  title: '[주의] EP-JL-001-C03 냉각 시스템 이상', message: '냉각 시스템 열화 패턴 감지. 잔여 수명 96시간 예측.', referenceType: 'CHARGER', referenceId: 6, isRead: false, createdAt: ago(5) },
  { id: 6,  stationId: 3, stationName: 'E-pit 인천공항', alertType: 'SYSTEM', severity: 'INFO', title: 'E-pit 인천공항 점검 모드 전환', message: '정기 점검을 위해 운영 상태가 MAINTENANCE로 변경되었습니다.', referenceType: null, referenceId: null, isRead: true, createdAt: ago(24) },
  { id: 7,  stationId: 1, stationName: 'E-pit 강남', alertType: 'PARKING_VIOLATION', severity: 'INFO',    title: '불법 주차 해결 — EP-GN-001-C01', message: '차량 이동 확인. 주차 위반 건 해결 처리됨.', referenceType: 'VIOLATION', referenceId: 1, isRead: true, createdAt: ago(46) },
  { id: 8,  stationId: 4, stationName: 'E-pit 판교', alertType: 'TRAFFIC_RISK',  severity: 'WARNING',  title: '교통 위험도 상승 — E-pit 판교', message: '주변 교통량 급증으로 위험도 HIGH 수준 도달.', referenceType: null, referenceId: null, isRead: true, createdAt: ago(36) },
  { id: 9,  stationId: 2, stationName: 'E-pit 잠실', alertType: 'TRAFFIC_RISK',  severity: 'CRITICAL', title: '[위험] 교통 위험도 CRITICAL — E-pit 잠실', message: '사고 위험도가 임계값을 초과했습니다. 모니터링 강화 필요.', referenceType: null, referenceId: null, isRead: true, createdAt: ago(72) },
  { id: 10, stationId: 1, stationName: 'E-pit 강남', alertType: 'SYSTEM', severity: 'INFO', title: '시스템 정상 가동 확인', message: 'AI 추론 서버 및 엣지 디바이스 연결 상태 정상.', referenceType: null, referenceId: null, isRead: true, createdAt: ago(96) },
  { id: 11, stationId: 4, stationName: 'E-pit 판교', alertType: 'PARKING_VIOLATION', severity: 'WARNING', title: '초과 주차 감지 — EP-PG-001-C02', message: '충전 완료 후 45분 이상 점유. 차량번호: 부산 90바 1234', referenceType: 'VIOLATION', referenceId: 6, isRead: true, createdAt: ago(2) },
  { id: 12, stationId: 1, stationName: 'E-pit 강남', alertType: 'EQUIPMENT_FAILURE', severity: 'INFO', title: 'EP-GN-001-C01 정기 점검 완료', message: '정기 점검 및 부품 교체 완료. 건강 점수 92점으로 정상화.', referenceType: 'CHARGER', referenceId: 1, isRead: true, createdAt: ago(720) },
]

// ── 교통 위험도 ─────────────────────────────────────────────
export const RISK_MAP: Record<number, object> = {
  1: { id: 1, stationId: 1, trafficDataId: 10, predictedAt: ago(1), riskLevel: 'MEDIUM', riskScore: 0.52, contributingFactors: { 교통밀도: 0.65, 사고이력: 0.40, 기상: 0.20, 시간대: 0.55 }, modelVersion: 'XGB-v1.3.0' },
  2: { id: 2, stationId: 2, trafficDataId: 11, predictedAt: ago(1), riskLevel: 'HIGH',   riskScore: 0.74, contributingFactors: { 교통밀도: 0.88, 사고이력: 0.62, 기상: 0.35, 시간대: 0.70 }, modelVersion: 'XGB-v1.3.0' },
  3: { id: 3, stationId: 3, trafficDataId: 12, predictedAt: ago(2), riskLevel: 'LOW',    riskScore: 0.22, contributingFactors: { 교통밀도: 0.20, 사고이력: 0.15, 기상: 0.10, 시간대: 0.25 }, modelVersion: 'XGB-v1.3.0' },
  4: { id: 4, stationId: 4, trafficDataId: 13, predictedAt: ago(1), riskLevel: 'CRITICAL', riskScore: 0.91, contributingFactors: { 교통밀도: 0.95, 사고이력: 0.88, 기상: 0.72, 시간대: 0.85 }, modelVersion: 'XGB-v1.3.0' },
}

// ── 사용자 ─────────────────────────────────────────────────
export const MEMBERS = [
  { id: 1, username: 'admin', name: '관리자', email: 'admin@epit.co.kr', role: 'ADMIN', enabled: true, lastLoginAt: ago(0.5), createdAt: ago(720) },
  { id: 2, username: 'operator1', name: '김운영', email: 'op1@epit.co.kr', role: 'OPERATOR', enabled: true, lastLoginAt: ago(2), createdAt: ago(600) },
  { id: 3, username: 'viewer1', name: '이감시', email: 'view1@epit.co.kr', role: 'VIEWER', enabled: true, lastLoginAt: ago(8), createdAt: ago(500) },
]

// ── 대시보드 KPI ────────────────────────────────────────────
export const OVERVIEW = {
  totalStations: 4,
  activeChargers: 6,
  faultChargers: 2,
  todayViolations: 3,
  criticalPredictions: 2,
  highRiskStations: 2,
  unreadAlerts: 5,
}

// ── 충전기별 예측 ───────────────────────────────────────────
export function predictionByCharger(chargerId: number): object | null {
  return PREDICTIONS.find(p => p.chargerId === chargerId) ?? null
}

export { latestHealth }
