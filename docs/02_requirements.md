# 요구사항 정의서 (Requirements Specification)

> **E-pit AI 기반 스마트 충전소 통합 관제 시스템**

| 항목 | 내용 |
|------|------|
| 문서 버전 | v1.0 |
| 작성일 | 2026-06-11 |
| 작성자 | 수석 아키텍트 |
| 문서 상태 | 확정 (Baseline) |

---

## 1. 개요

본 문서는 E-pit AI 기반 스마트 충전소 통합 관제 시스템의 **기능 요구사항(FR)** 및 **비기능 요구사항(NFR)**, **제약 사항**을 정의한다.

### 1.1 요구사항 식별 체계

- **FR-{도메인}-{번호}**: 기능 요구사항 (Functional Requirement)
- **NFR-{분류}-{번호}**: 비기능 요구사항 (Non-Functional Requirement)
- **CON-{번호}**: 제약 사항 (Constraint)

### 1.2 우선순위 등급

| 등급 | 의미 |
|------|------|
| **M** (Must) | 반드시 구현 (필수) |
| **S** (Should) | 구현 권장 (중요) |
| **C** (Could) | 가능하면 구현 (선택) |

---

## 2. 기능 요구사항 (Functional Requirements)

### 2.1 인증 및 권한 (Authentication & Authorization)

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-AUTH-01 | 사용자는 username/password로 로그인하면 JWT 액세스 토큰을 발급받아야 한다. (`POST /api/v1/auth/login`) | M |
| FR-AUTH-02 | 시스템은 ADMIN/OPERATOR/VIEWER 3개 역할에 대해 역할 기반 접근 제어(RBAC)를 적용해야 한다. | M |
| FR-AUTH-03 | VIEWER 역할은 조회만 가능하며, 생성/수정/삭제 API 호출 시 403을 반환해야 한다. | M |
| FR-AUTH-04 | OPERATOR 역할은 위반 처리·알림 읽음 처리는 가능하나 회원·자산 삭제는 불가해야 한다. | S |
| FR-AUTH-05 | ADMIN 역할은 회원 관리 및 모든 자산 CRUD가 가능해야 한다. | M |
| FR-AUTH-06 | 비활성화(`enabled=false`)된 회원은 로그인할 수 없어야 한다. | M |
| FR-AUTH-07 | 만료되거나 위변조된 JWT로 보호 API 호출 시 401을 반환해야 한다. | M |

### 2.2 충전소/충전기 관리 (Asset Management)

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-STN-01 | 사용자는 충전소를 등록(`POST /api/v1/stations`)할 수 있어야 하며, `stationCode`는 유일해야 한다. | M |
| FR-STN-02 | 사용자는 충전소 목록을 조회(`GET /api/v1/stations`)하고 운영상태로 필터링할 수 있어야 한다. | M |
| FR-STN-03 | 사용자는 충전소 정보를 수정(`PUT`) 및 삭제(`DELETE`)할 수 있어야 한다. | M |
| FR-STN-04 | 충전소는 위경도(latitude/longitude)를 보유하여 지도에 표시 가능해야 한다. | S |
| FR-STN-05 | 충전소는 운영상태(ACTIVE/MAINTENANCE/CLOSED)를 관리해야 한다. | M |
| FR-STN-06 | 충전소는 카메라 장치 ID(`cameraDeviceId`)를 등록하여 비전 감지와 연계해야 한다. | M |
| FR-CHG-01 | 사용자는 충전기를 등록·조회(`GET/POST /api/v1/chargers`)할 수 있어야 한다. | M |
| FR-CHG-02 | 충전기는 커넥터 타입(CCS1/CCS2/CHADEMO/AC3)과 최대출력(maxPowerKw)을 보유해야 한다. | M |
| FR-CHG-03 | 충전기 상태(AVAILABLE/CHARGING/FAULT/OFFLINE/RESERVED)를 관리해야 한다. | M |
| FR-CHG-04 | 충전기는 헬스스코어(healthScore)를 보유하며 PHM 예측 결과로 갱신되어야 한다. | S |

### 2.3 AI 불법주차 감지 (Illegal Parking Detection)

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-VIS-01 | AI 서버는 비전 추론 결과를 `POST /api/v1/internal/vision/result`로 콜백해야 하며, `X-Internal-Token` 헤더 검증을 통과해야 한다. | M |
| FR-VIS-02 | Backend는 비전 콜백 수신 시 `vehicle_detection` 레코드를 저장해야 한다. (vehicleType, isElectric, plateNumber, confidence, boundingBox, imagePath) | M |
| FR-VIS-03 | Backend는 `isElectric=false`인 감지에 대해 `NON_EV_OCCUPANCY` 위반(`parking_violation`)을 생성해야 한다. | M |
| FR-VIS-04 | EV/ICE 판별은 AI 서버 책임이며 Backend는 `isElectric` 값을 신뢰해야 한다. | M |
| FR-VIS-05 | 위반 생성 시 증거 이미지 경로(`evidenceImagePath`)를 함께 저장해야 한다. | M |
| FR-VIS-06 | 위반 발생 시 `AlertService.create()`를 통해 PARKING_VIOLATION 알림을 자동 생성해야 한다. | M |
| FR-VIS-07 | 사용자는 위반 목록을 조회(`GET /api/v1/violations`)하고 상태(DETECTED/NOTIFIED/RESOLVED)로 필터링할 수 있어야 한다. | M |
| FR-VIS-08 | 운영자는 위반을 처리 완료(RESOLVED)로 변경하고 `resolvedAt`을 기록할 수 있어야 한다. | S |

### 2.4 PHM 예지보전 (Prognostics & Health Management)

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-PHM-01 | Edge는 충전기 센서 데이터(전압/전류/온도/진동)를 주기적으로 수집해야 한다. | M |
| FR-PHM-02 | Backend는 센서 데이터를 `equipment_health`에 저장해야 한다. | M |
| FR-PHM-03 | AI 서버는 LSTM 예측 결과를 `POST /api/v1/internal/phm/result`로 콜백해야 한다. | M |
| FR-PHM-04 | Backend는 `failure_prediction`을 저장해야 한다. (failureProbability, remainingUsefulLifeHours, riskLevel, predictedComponent, modelVersion) | M |
| FR-PHM-05 | riskLevel이 CRITICAL인 예측에 대해 EQUIPMENT_FAILURE 알림을 자동 생성해야 한다. | M |
| FR-PHM-06 | 사용자는 위험 예측 목록을 조회(`GET /api/v1/predictions/critical`)할 수 있어야 한다. | M |
| FR-PHM-07 | PHM 예측 결과는 해당 충전기의 `healthScore`를 갱신해야 한다. | S |

### 2.5 교통 위험도 예측 (Traffic Risk Prediction)

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-TRF-01 | 시스템은 충전소 인근 교통 데이터(vehicleCount/avgSpeedKmh/congestionLevel/weather/roadSurface)를 수집·저장해야 한다. | M |
| FR-TRF-02 | AI 서버는 XGBoost 위험도 예측 결과를 `POST /api/v1/internal/traffic/result`로 콜백해야 한다. | M |
| FR-TRF-03 | Backend는 `risk_prediction`을 저장해야 한다. (riskLevel, riskScore, contributingFactors, modelVersion) | M |
| FR-TRF-04 | 사용자는 특정 충전소의 최신 교통 위험도를 조회(`GET /api/v1/stations/{id}/risk/latest`)할 수 있어야 한다. | M |
| FR-TRF-05 | riskLevel이 HIGH/CRITICAL인 예측에 대해 TRAFFIC_RISK 알림을 생성해야 한다. | S |

### 2.6 알림 (Alert)

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-ALT-01 | 모든 알림은 `AlertService.create(AlertCreateCommand)` 단일 경로로만 생성되어야 한다. | M |
| FR-ALT-02 | 알림은 타입(PARKING_VIOLATION/EQUIPMENT_FAILURE/TRAFFIC_RISK/SYSTEM)과 심각도(INFO/WARNING/CRITICAL)를 보유해야 한다. | M |
| FR-ALT-03 | 알림 생성 시 DB 저장 + WebSocket 푸시 + Redis Pub/Sub이 캡슐화되어 동작해야 한다. | M |
| FR-ALT-04 | 알림은 참조 대상(referenceType, referenceId)을 보유하여 원본 이벤트로 추적 가능해야 한다. | S |
| FR-ALT-05 | 사용자는 알림 목록을 조회(`GET /api/v1/alerts`)하고 읽음/안읽음으로 필터링할 수 있어야 한다. | M |
| FR-ALT-06 | 사용자는 알림을 읽음(`isRead=true`) 처리할 수 있어야 한다. | M |

### 2.7 실시간 관제 대시보드 (Real-time Dashboard)

| ID | 요구사항 | 우선순위 |
|----|----------|----------|
| FR-DSH-01 | 사용자는 대시보드 현황(`GET /api/v1/dashboard/overview`)에서 충전소·충전기·위반·알림 요약을 조회할 수 있어야 한다. | M |
| FR-DSH-02 | 신규 이벤트(위반/고장/교통/알림)는 WebSocket STOMP로 클라이언트에 실시간 푸시되어야 한다. | M |
| FR-DSH-03 | 대시보드는 ECharts 기반 시각화(상태 분포, 시계열 추이)를 제공해야 한다. | S |
| FR-DSH-04 | 대시보드는 충전소를 지도에 위경도 기반으로 표시해야 한다. | C |

> **기능 요구사항 합계: 41개** (FR-AUTH 7, FR-STN 6, FR-CHG 4, FR-VIS 8, FR-PHM 7, FR-TRF 5, FR-ALT 6, FR-DSH 4 — 도메인별 분류)

---

## 3. 비기능 요구사항 (Non-Functional Requirements)

### 3.1 성능 (Performance)

| ID | 요구사항 | 목표치 |
|----|----------|--------|
| NFR-PERF-01 | REST API 응답 시간(P95)은 임계치 이하여야 한다. | < 300ms |
| NFR-PERF-02 | AI 비전 콜백 처리 후 대시보드 반영 지연 시간은 최소화되어야 한다. | < 2초 (end-to-end) |
| NFR-PERF-03 | WebSocket 이벤트 푸시 지연은 최소화되어야 한다. | < 500ms |
| NFR-PERF-04 | 시스템은 동시 접속 운영자 수를 수용해야 한다. | 동시 50명 이상 |
| NFR-PERF-05 | 조회 API에는 적절한 인덱스가 적용되어 대용량 데이터에서도 성능을 유지해야 한다. | - |

### 3.2 보안 (Security)

| ID | 요구사항 |
|----|----------|
| NFR-SEC-01 | 모든 비밀번호는 BCrypt 등 단방향 해시로 저장되어야 한다. |
| NFR-SEC-02 | 인증은 JWT 기반이며, 토큰은 만료 시간을 가져야 한다. |
| NFR-SEC-03 | 내부 콜백(`/api/v1/internal/**`)은 `X-Internal-Token` 헤더 검증을 통과한 요청만 허용해야 한다. |
| NFR-SEC-04 | RBAC(ADMIN/OPERATOR/VIEWER)를 통해 최소 권한 원칙을 적용해야 한다. |
| NFR-SEC-05 | API는 CORS 정책을 적용하여 허용된 오리진만 접근 가능해야 한다. |
| NFR-SEC-06 | 민감 정보(번호판 등)는 접근 권한이 있는 사용자에게만 노출되어야 한다. |
| NFR-SEC-07 | 모든 통신은 HTTPS/WSS(운영)로 암호화되어야 한다. |

### 3.3 가용성 및 신뢰성 (Availability & Reliability)

| ID | 요구사항 |
|----|----------|
| NFR-AVL-01 | 시스템 가동률은 99.5% 이상이어야 한다. |
| NFR-AVL-02 | AI 서버 장애 시에도 Backend는 정상 동작해야 하며 콜백 미수신은 재시도 또는 큐잉으로 처리한다. |
| NFR-AVL-03 | AI 모델 파일이 없으면 Mock 추론으로 자동 폴백하여 서비스 연속성을 보장해야 한다. |
| NFR-AVL-04 | Redis 장애 시에도 알림은 DB에 저장되어 유실되지 않아야 한다. |
| NFR-AVL-05 | DB 스키마 변경은 Flyway 마이그레이션으로 버전 관리되어야 한다. |

### 3.4 확장성 (Scalability)

| ID | 요구사항 |
|----|----------|
| NFR-SCL-01 | 신규 충전소 추가 시 코드 변경 없이 Edge 디바이스 등록만으로 확장 가능해야 한다. |
| NFR-SCL-02 | Backend는 다중 인스턴스로 수평 확장 가능해야 하며, 알림은 Redis Pub/Sub로 인스턴스 간 브로드캐스트되어야 한다. |
| NFR-SCL-03 | AI 모델은 버전(`modelVersion`)을 관리하여 무중단 모델 교체가 가능해야 한다. |

### 3.5 유지보수성 (Maintainability)

| ID | 요구사항 |
|----|----------|
| NFR-MNT-01 | enum 값은 DB CHECK / JPA STRING / TS union / pydantic Literal 전 계층에서 동일(SSOT)해야 한다. |
| NFR-MNT-02 | 공통 응답은 `ApiResponse<T>` 래퍼(success/data/error/timestamp)로 통일해야 한다. |
| NFR-MNT-03 | 예외는 `BusinessException(ErrorCode)` → `GlobalExceptionHandler`로 일관 처리해야 한다. |
| NFR-MNT-04 | 패키지는 `com.epit.admin.*` 도메인별 구조를 따라야 한다. |

### 3.6 데이터 무결성 (Data Integrity)

| ID | 요구사항 |
|----|----------|
| NFR-DAT-01 | 모든 시각 데이터는 DB에 UTC(`TIMESTAMPTZ`)로 저장하고 프론트에서 KST 변환해야 한다. |
| NFR-DAT-02 | 위반(`parking_violation`)은 감지(`vehicle_detection`)를 FK로 참조하여 추적성을 보장해야 한다. |
| NFR-DAT-03 | enum 컬럼은 DB CHECK 제약으로 허용값을 강제해야 한다. |

### 3.7 사용성 (Usability)

| ID | 요구사항 |
|----|----------|
| NFR-USE-01 | 대시보드는 신규 이벤트 발생 시 별도 새로고침 없이 자동 갱신되어야 한다. |
| NFR-USE-02 | UI는 Element Plus 기반으로 일관된 디자인 언어를 유지해야 한다. |
| NFR-USE-03 | 심각도(CRITICAL/WARNING/INFO)는 색상으로 명확히 구분되어야 한다. |

---

## 4. 제약 사항 (Constraints)

| ID | 제약 사항 |
|----|-----------|
| CON-01 | Backend는 Spring Boot 3.2 / Java 17 이상 환경에서 구동되어야 한다. |
| CON-02 | AI 서버는 Python 3.11 / FastAPI 기반으로 구현되어야 한다. |
| CON-03 | DB는 PostgreSQL 16, 메시지/캐시는 Redis 7을 사용해야 한다. |
| CON-04 | Frontend는 Vue.js 3 + TypeScript + Pinia로 구현되어야 한다. |
| CON-05 | 전체 시스템은 Docker Compose로 오케스트레이션되어야 한다. |
| CON-06 | Edge 디바이스는 Raspberry Pi 환경이며 PiCamera2, pymodbus를 사용한다. |
| CON-07 | AI→Backend 통신은 내부망 콜백 + 토큰 인증 방식으로 제한된다. |
| CON-08 | 본 시스템은 운영자용 관제에 한정되며 결제·OCPP·고객앱은 범위에서 제외된다. |

---

## 5. 요구사항 추적 매트릭스 (요약)

| 도메인 | 관련 FR | 관련 NFR |
|--------|---------|----------|
| 인증/권한 | FR-AUTH-01~07 | NFR-SEC-01~07 |
| 자산관리 | FR-STN-01~06, FR-CHG-01~04 | NFR-PERF-05, NFR-DAT-03 |
| 불법주차 | FR-VIS-01~08 | NFR-SEC-03, NFR-DAT-02, NFR-PERF-02 |
| PHM | FR-PHM-01~07 | NFR-AVL-03, NFR-SCL-03 |
| 교통예측 | FR-TRF-01~05 | NFR-AVL-03 |
| 알림 | FR-ALT-01~06 | NFR-AVL-04, NFR-SCL-02 |
| 대시보드 | FR-DSH-01~04 | NFR-PERF-03, NFR-USE-01 |
