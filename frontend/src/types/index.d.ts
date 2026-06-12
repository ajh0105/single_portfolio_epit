// ===== 공통 =====
export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: { code: string; message: string } | null
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// ===== 인증 =====
export interface LoginRequest { username: string; password: string }
export interface TokenResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  member: MemberSummary
}
export interface MemberSummary { id: number; username: string; name: string; role: string }

// ===== 충전소 =====
export type OperationStatus = 'ACTIVE' | 'MAINTENANCE' | 'CLOSED'
export interface StationResponse {
  id: number
  stationCode: string
  name: string
  address: string
  latitude: number
  longitude: number
  totalChargers: number
  operationStatus: OperationStatus
  cameraDeviceId: string | null
  createdAt: string
}

// ===== 충전기 =====
export type ChargerStatus = 'AVAILABLE' | 'CHARGING' | 'FAULT' | 'OFFLINE' | 'RESERVED'
export type ConnectorType = 'CCS1' | 'CCS2' | 'CHADEMO' | 'AC3'
export interface ChargerResponse {
  id: number
  stationId: number
  chargerCode: string
  connectorType: ConnectorType
  maxPowerKw: number
  status: ChargerStatus
  healthScore: number | null
  lastMaintenanceAt: string | null
}

// ===== 위반 =====
export type ViolationType = 'NON_EV_OCCUPANCY' | 'OVERSTAY'
export type ViolationStatus = 'DETECTED' | 'NOTIFIED' | 'RESOLVED' | 'FALSE_POSITIVE'
export interface ViolationResponse {
  id: number
  stationId: number
  stationName: string
  chargerId: number | null
  plateNumber: string
  violationType: ViolationType
  status: ViolationStatus
  occurredAt: string
  resolvedAt: string | null
  evidenceImagePath: string | null
}

// ===== 사용자 =====
export interface MemberResponse {
  id: number
  username: string
  name: string
  email: string
  role: string
  enabled: boolean
  lastLoginAt: string | null
  createdAt: string
}

// ===== PHM =====
export interface EquipmentHealthResponse {
  id: number
  chargerId: number
  measuredAt: string
  voltage: number
  current: number
  temperature: number
  vibration: number
}
export type PhmRiskLevel = 'NORMAL' | 'WARNING' | 'CRITICAL'
export interface FailurePredictionResponse {
  id: number
  chargerId: number
  chargerCode: string
  chargerStatus: string
  chargerMaxPowerKw: number
  chargerHealthScore: number | null
  chargerLastMaintenanceAt: string | null
  predictedAt: string
  failureProbability: number
  remainingUsefulLifeHours: number | null
  riskLevel: PhmRiskLevel
  predictedComponent: string | null
  modelVersion: string
}

// ===== 교통/위험도 =====
export type TrafficRiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export interface RiskPredictionResponse {
  id: number
  stationId: number
  trafficDataId: number
  predictedAt: string
  riskLevel: TrafficRiskLevel
  riskScore: number
  contributingFactors: Record<string, number> | null
  modelVersion: string
}

// ===== 알림 =====
export type AlertType = 'PARKING_VIOLATION' | 'EQUIPMENT_FAILURE' | 'TRAFFIC_RISK' | 'SYSTEM'
export type AlertSeverity = 'INFO' | 'WARNING' | 'CRITICAL'
export interface AlertResponse {
  id: number
  stationId: number | null
  stationName: string | null
  alertType: AlertType
  severity: AlertSeverity
  title: string
  message: string
  referenceType: string | null
  referenceId: number | null
  isRead: boolean
  createdAt: string
}

// ===== 대시보드 =====
export interface DashboardOverviewResponse {
  totalStations: number
  activeChargers: number
  faultChargers: number
  todayViolations: number
  criticalPredictions: number
  highRiskStations: number
  unreadAlerts: number
}
