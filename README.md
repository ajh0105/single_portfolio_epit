# E-pit AI 기반 스마트 충전소 통합 관제 시스템

현대자동차그룹 E-pit 초고속 전기차 충전 네트워크를 위한 AI 기반 통합 관제 플랫폼

## 프로젝트 정보

| 항목 | 내용 |
|------|------|
| 시작일 | 2026-06-11 |
| 유형 | 기업형 포트폴리오 |
| 역할 | 수석 아키텍트 / 풀스택 개발자 |
| 배포 | Docker Compose |

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| Backend | Spring Boot 3.2, Spring Security 6, JPA, WebSocket(STOMP) |
| AI Server | Python 3.11, FastAPI, YOLOv8, LSTM(PyTorch), XGBoost |
| Database | PostgreSQL 16, Redis 7 |
| Frontend | Vue.js 3, Pinia, TypeScript, Element Plus, ECharts |
| Edge | Raspberry Pi, PiCamera2, pymodbus |
| Infra | Docker Compose, Nginx |

## 핵심 기능

| 기능 | 기술 | 흐름 |
|------|------|------|
| 불법 주차 감지 | YOLOv8 + ocrv6.5 (V5OCR CRNN) | Edge카메라 → AI서버 → Backend → WebSocket 알림 |
| 장비 고장 예측 (PHM) | LSTM | 센서데이터 → AI서버 → Backend → 예방정비 알림 |
| 교통 위험도 분석 | XGBoost | 교통데이터 → AI서버 → Backend → 위험도 대시보드 |
| 통합 대시보드 | Vue.js + ECharts | STOMP 실시간 WebSocket 스트리밍 |

## 빠른 시작

```bash
# 1. 환경 변수 설정
cp .env.example .env
# .env 파일에서 DB_PASSWORD, JWT_SECRET, INTERNAL_API_TOKEN 수정

# 2. Docker Compose 실행
docker compose up -d

# 3. 서비스 확인
# 웹 대시보드: http://localhost
# API 문서:    http://localhost/swagger-ui.html
# AI 서버:     내부망 전용 (http://ai-server:8000/ai/v1/health - 도커 내부)

# 4. 초기 로그인
# ID: admin / PW: admin1234
```

## 프로젝트 구조

```
pr1_기업형/
├── backend/                    # Spring Boot 3.x 백엔드
│   ├── src/main/java/com/epit/admin/
│   │   ├── auth/               # JWT 인증/인가
│   │   ├── member/             # 사용자 관리
│   │   ├── station/            # 충전소 관리
│   │   ├── charger/            # 충전기 관리
│   │   ├── vision/             # 차량감지/불법주차
│   │   ├── phm/                # 장비 건강/고장예측
│   │   ├── traffic/            # 교통/위험도
│   │   ├── alert/              # 알림 (단일 진입점)
│   │   ├── dashboard/          # 집계 대시보드
│   │   ├── internal/           # AI서버 콜백 수신
│   │   └── global/             # 공통(Security, WS, Redis)
│   └── src/main/resources/db/migration/V1__initial_schema.sql
├── ai-server/                  # FastAPI AI 추론 서버
│   └── app/
│       ├── ml/                 # YOLO, LSTM, XGBoost 모델
│       ├── services/           # 추론 + Backend 콜백
│       └── api/v1/             # REST 엔드포인트
├── frontend/                   # Vue 3 + Pinia 대시보드
│   └── src/
│       ├── stores/             # Pinia 상태관리
│       ├── views/              # 페이지 컴포넌트
│       └── ws/stompClient.ts   # WebSocket 연결
├── edge/                       # Raspberry Pi 에이전트
│   ├── camera/capture.py       # 카메라 → AI서버
│   ├── sensor/collector.py     # Modbus 센서 → AI서버
│   └── traffic/reporter.py    # 교통 데이터 → AI서버
├── nginx/nginx.conf            # 리버스 프록시
├── db/init/01_schema.sql       # PostgreSQL 초기화
└── docker-compose.yml
```

## API 엔드포인트 요약

| 범주 | 경로 | 설명 |
|------|------|------|
| 인증 | `POST /api/v1/auth/login` | JWT 로그인 |
| 충전소 | `GET/POST /api/v1/stations` | 충전소 CRUD |
| 위반 | `GET /api/v1/violations` | 불법주차 목록 |
| PHM | `GET /api/v1/predictions/critical` | 위험 장비 목록 |
| 위험도 | `GET /api/v1/stations/{id}/risk/latest` | 최신 위험도 |
| 알림 | `GET /api/v1/alerts` | 알림 목록 |
| 대시보드 | `GET /api/v1/dashboard/overview` | KPI 집계 |
| AI Vision | `POST /ai/v1/vision/detect` | 차량 감지 |
| AI PHM | `POST /ai/v1/phm/ingest` | 센서 배치 추론 |
| AI Traffic | `POST /ai/v1/traffic/analyze` | 교통 위험도 |

## WebSocket 실시간 토픽

| 토픽 | 발행 시점 |
|------|-----------|
| `/topic/alerts` | 신규 알림 발생 |
| `/topic/stations/{id}/violations` | 불법주차 감지 |
| `/topic/chargers/{id}/prediction` | PHM 예측 완료 |
| `/topic/stations/{id}/risk` | 위험도 변경 |

---

## API 엔드포인트 추가 (세션4~5)

| 범주 | 경로 | 설명 |
|------|------|------|
| 충전소 | `PATCH /api/v1/stations/{id}/status` | 운영 상태 변경 (OPERATOR+) |
| 위반 | `PATCH /api/v1/violations/{id}/resolve` | 위반 해결 처리 (OPERATOR+) |
| 충전기 | `GET /api/v1/chargers/{id}/health/latest` | 최신 센서 데이터 1건 |
| 충전기 | `GET /api/v1/chargers/{id}/health?hours=24` | 시간별 LSTM 로그 |

## 현재 상태 (Last updated: 2026-06-12)

### 완료된 작업
- [x] Opus 아키텍처 설계, 전체 도메인 구현, Docker 통합 빌드 (세션1~3)
- [x] 산출물 MD 작성 (docs/01~05)
- [x] **프론트엔드 전면 UI 개선** (세션4)
  - [x] 전체 리스트 테이블 min-width 통일 (컬럼 간격 수정)
  - [x] 더미 데이터 DB 입력 (충전소/충전기/위반/PHM/교통/알림)
  - [x] PhmMonitorView — 게이지 차트 + 센서 이상 감지 현황 (근거 기반)
  - [x] TrafficRiskView — 위험도 범례, 요인별 위험 지수 정확 표시
  - [x] 교통 위험도 contributingFactors String→Map 타입 수정
  - [x] LSTM 시계열 로그 DB 입력 (8충전기 × 20시간 = 160 rows)
  - [x] ChargerController — `/health/latest`, `/health?hours` 엔드포인트 추가
- [x] **세션5 추가 기능**
  - [x] PHM 로그 버튼 + 모달 (ECharts 트렌드 차트 + 테이블 + CSV 내보내기)
  - [x] PhmMonitorView 상세 모달 레이아웃 재배치 (게이지 상단 → 표|센서 2열)
  - [x] StationDetailView — 운영중/점검중 상태 전환 버튼 추가
  - [x] ViolationListView — 처리 단계 스텝 표시 + 해결 처리 버튼 (수동)
  - [x] Backend — `PATCH /stations/{id}/status`, `PATCH /violations/{id}/resolve` 신규 API
  - [x] StationDetailView — 충전기·위반 행 클릭 → PHM/위반 모달 연동
  - [x] 대시보드 최신 알림 읽음 처리 버그 수정 (try/catch + copy + 항상 닫기)
  - [x] Alert 읽음 영속성 버그 수정: dirty tracking → `@Modifying @Query` 직접 UPDATE
- [x] **세션6 추가 기능**
  - [x] AlertResponse.java — `@JsonProperty("isRead")` 추가 (Jackson boolean getter "read" 직렬화 버그 수정)
  - [x] alert.ts store — `fetchAlerts` 시 `unreadCount` 자동 동기화 (배지 카운트 정확화)
  - [x] alert.ts store — `markRead`/`markAllRead`/`markUnread` 모두 `splice` 기반 교체 (Vue 반응성 보장)
  - [x] AlertCenterView — 읽음 처리 후 행 사라짐 버그 수정 + 읽음 처리 오류 핸들링
  - [x] AlertCenterView — 필터(읽음 상태/중요도/유형) + 정렬(최신/오래된순) + 인덱스 번호 열 추가
  - [x] ViolationListView — 필터(위반 유형/처리 상태) + 정렬(최신/오래된순) + 인덱스 번호 열 추가
  - [x] AppLayout 사이드바 — `el-badge` → 커스텀 `.menu-badge` span으로 교체 (수직 정렬 수정)
  - [x] PHM 장비 진단 상세 — 예상 고장 부품 선정 근거 리포트 섹션 추가 (PhmMonitorView + StationDetailView 공통)
  - [x] "미읽음" → "안읽음" 용어 전체 변경 (AlertCenterView, DashboardView)
  - [x] PHM 로그 모달 기간 선택 기능 추가 (일간/주간/월간/전체 + 커스텀 날짜 범위 선택)
    - Backend: `GET /chargers/{id}/health` — `startDate`/`endDate` optional ISO datetime 파라미터 추가
    - Frontend: 일간(24h)/주간(7일)/월간(30일)/전체 버튼 + el-date-picker daterange
    - ECharts x축 포맷 기간별 자동 전환 (1d: HH:mm / 7d+: MM/DD HH:mm)

### 진행 중
- 없음

### 다음 세션 할 일
- [ ] parking.mp4 준비 후 `docker compose build frontend && docker compose up -d frontend`
- [ ] AI 서버 health 확인 및 vocab.json 복사
- [ ] 브라우저에서 전체 화면 동선 최종 점검

### 알려진 이슈 / 결정 보류
- **admin 비밀번호**: `admin1234`
- **실시간 관제 영상**: `frontend/public/videos/parking.mp4` 파일 없음 → 영상 없이 오프라인 표시됨
- **AI 서버**: 현재 Mock 모드 동작 중 (모델 파일 없음)
- **Flyway 체크섬**: validate-on-migrate=false (포트폴리오 환경 허용)
