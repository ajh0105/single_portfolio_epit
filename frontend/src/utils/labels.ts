// enum 값 → 한글 레이블 / Element Plus tag type SSOT
// 모든 화면에서 이 파일을 import해서 사용 (??? 깨짐 방지)

export const OPERATION_STATUS_LABEL: Record<string, string> = {
  ACTIVE: '운영중', MAINTENANCE: '점검중', CLOSED: '폐쇄',
}
export const OPERATION_STATUS_TYPE: Record<string, string> = {
  ACTIVE: 'success', MAINTENANCE: 'warning', CLOSED: 'danger',
}

export const CHARGER_STATUS_LABEL: Record<string, string> = {
  AVAILABLE: '사용가능', CHARGING: '충전중', FAULT: '고장', OFFLINE: '오프라인', RESERVED: '예약됨',
}
export const CHARGER_STATUS_TYPE: Record<string, string> = {
  AVAILABLE: 'success', CHARGING: 'primary', FAULT: 'danger', OFFLINE: 'info', RESERVED: 'warning',
}

export const CONNECTOR_TYPE_LABEL: Record<string, string> = {
  CCS1: 'CCS1', CCS2: 'CCS2', CHADEMO: '차데모', AC3: 'AC3상',
}

export const VIOLATION_TYPE_LABEL: Record<string, string> = {
  NON_EV_OCCUPANCY: '비전기차 점유', OVERSTAY: '장시간 점유',
}

export const VIOLATION_STATUS_LABEL: Record<string, string> = {
  DETECTED: '감지', NOTIFIED: '통보됨', RESOLVED: '해결', FALSE_POSITIVE: '오감지',
}
export const VIOLATION_STATUS_TYPE: Record<string, string> = {
  DETECTED: 'danger', NOTIFIED: 'warning', RESOLVED: 'success', FALSE_POSITIVE: 'info',
}

export const PHM_RISK_LABEL: Record<string, string> = {
  NORMAL: '정상', WARNING: '주의', CRITICAL: '위험',
}
export const PHM_RISK_TYPE: Record<string, string> = {
  NORMAL: 'success', WARNING: 'warning', CRITICAL: 'danger',
}

export const TRAFFIC_RISK_LABEL: Record<string, string> = {
  LOW: '낮음', MEDIUM: '보통', HIGH: '높음', CRITICAL: '위험',
}
export const TRAFFIC_RISK_TYPE: Record<string, string> = {
  LOW: 'success', MEDIUM: 'warning', HIGH: 'danger', CRITICAL: 'danger',
}

export const ALERT_TYPE_LABEL: Record<string, string> = {
  PARKING_VIOLATION: '불법 주차', EQUIPMENT_FAILURE: '장비 고장',
  TRAFFIC_RISK: '교통 위험', SYSTEM: '시스템',
}

export const ALERT_SEVERITY_LABEL: Record<string, string> = {
  INFO: '정보', WARNING: '주의', CRITICAL: '긴급',
}
export const ALERT_SEVERITY_TYPE: Record<string, string> = {
  INFO: 'info', WARNING: 'warning', CRITICAL: 'danger',
}

export const ROLE_LABEL: Record<string, string> = {
  ADMIN: '관리자', OPERATOR: '운영자', VIEWER: '조회자',
}
export const ROLE_TYPE: Record<string, string> = {
  ADMIN: 'danger', OPERATOR: 'warning', VIEWER: 'info',
}

/** 안전 조회: 매핑 없으면 원본 반환 */
export function label(map: Record<string, string>, key: string): string {
  return map[key] ?? key
}
