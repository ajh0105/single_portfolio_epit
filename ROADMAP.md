# E-pit AI 스마트 충전소 통합 관제 시스템 — 7주 개발 로드맵

## 프로젝트 개요

현대자동차그룹 E-pit 초고속 전기차 충전 네트워크를 위한 AI 기반 스마트 관제 시스템.
불법주차 감지 · 장비 PHM · 교통 위험도 예측 · 통합 대시보드를 하나의 플랫폼으로 통합한다.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         시스템 전체 아키텍처                              │
│                                                                          │
│  [Raspberry Pi]──RTSP──►[FastAPI AI Server]──REST──►[Spring Boot API]   │
│  카메라/센서                YOLO | LSTM | RF             비즈니스 로직    │
│                                                              │           │
│                                                        [PostgreSQL]      │
│                                                              │           │
│                                                       [Vue.js Dashboard] │
│                                                        실시간 WebSocket  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 1주차 — 기반 설계 및 환경 구축

### 목표
- 전체 시스템 설계 확정 및 개발 환경 세팅
- PostgreSQL 스키마 설계 및 마이그레이션 스크립트 작성

### 주요 작업
| 작업 | 담당 레이어 | 우선순위 |
|------|------------|---------|
| Docker Compose 환경 구성 | Infra | P0 |
| PostgreSQL 스키마 설계 (ERD) | DB | P0 |
| Flyway 마이그레이션 V1 작성 | DB | P0 |
| Spring Boot 프로젝트 초기화 (Security, JPA, WebClient) | Backend | P0 |
| FastAPI 프로젝트 초기화 | AI Server | P0 |
| Vue.js + Pinia 프로젝트 초기화 | Frontend | P1 |
| JWT 인증 플로우 설계 | Security | P0 |

### 1주차 완료 체크리스트
- [ ] `docker-compose up` 으로 전체 스택 정상 기동
- [ ] Spring Boot `/actuator/health` 응답 200
- [ ] FastAPI `/docs` Swagger UI 정상 접근
- [ ] DB 마이그레이션 V1 적용 완료 (14개 테이블)
- [ ] JWT 로그인 API 동작 확인 (`POST /api/auth/login`)

---

## 2주차 — 핵심 도메인 API 구현

### 목표
- 충전소/충전기 CRUD API 완성
- 사용자 인증/인가 시스템 구현

### 주요 작업
| 작업 | 담당 레이어 | 우선순위 |
|------|------------|---------|
| ChargingStation CRUD REST API | Backend | P0 |
| Charger CRUD REST API | Backend | P0 |
| User 회원가입/로그인/권한 관리 | Backend/Security | P0 |
| JWT Refresh Token 구현 | Backend/Security | P0 |
| Alert 도메인 API | Backend | P1 |
| Vue.js 라우터 설계 + 기본 레이아웃 | Frontend | P1 |
| Pinia 스토어 설계 (Auth, Station, Alert) | Frontend | P1 |

### 2주차 완료 체크리스트
- [ ] Postman 컬렉션으로 전체 Station/Charger API 테스트 통과
- [ ] ROLE_ADMIN / ROLE_OPERATOR 권한 분기 동작
- [ ] Vue.js 로그인 화면 → 대시보드 라우팅 정상
- [ ] Access Token 만료 시 Refresh Token 자동 재발급

---

## 3주차 — YOLO 불법주차 감지 모듈

### 목표
- Raspberry Pi 카메라 연동 파이프라인 구축
- YOLO 기반 번호판 인식 및 전기차 판별 시스템 구현

### 주요 작업
| 작업 | 담당 레이어 | 우선순위 |
|------|------------|---------|
| YOLO v8 모델 파인튜닝 (번호판/차종) | AI | P0 |
| FastAPI `/detect/parking` 엔드포인트 | AI Server | P0 |
| 이미지 스트림 처리 파이프라인 | AI Server | P0 |
| ParkingViolation 저장 API (Spring Boot) | Backend | P0 |
| 위반 감지 시 Alert 자동 생성 | Backend | P0 |
| 라즈베리파이 → FastAPI 영상 전송 스크립트 | Edge | P1 |
| 위반 목록/이미지 뷰 컴포넌트 | Frontend | P1 |

### 3주차 완료 체크리스트
- [ ] 테스트 이미지로 번호판 인식률 > 85%
- [ ] EV/NON-EV 차종 판별 정확도 > 80%
- [ ] 위반 감지 → DB 저장 → Alert 생성 E2E 동작
- [ ] 프론트 위반 목록 페이지 렌더링 정상

---

## 4주차 — LSTM 장비 고장 예측 (PHM) 모듈

### 목표
- 충전기 센서 데이터 수집 파이프라인 구현
- LSTM 모델로 장비 고장 사전 예측

### 주요 작업
| 작업 | 담당 레이어 | 우선순위 |
|------|------------|---------|
| 센서 데이터 수집 API (시계열) | Backend | P0 |
| LSTM 모델 학습 (전압/전류/온도 이상 감지) | AI | P0 |
| FastAPI `/predict/equipment` 엔드포인트 | AI Server | P0 |
| 예측 결과 저장 및 Alert 트리거 | Backend | P0 |
| 스케줄러: 주기적 예측 실행 (@Scheduled) | Backend | P1 |
| 장비 상태 실시간 차트 컴포넌트 (Chart.js) | Frontend | P1 |

### 4주차 완료 체크리스트
- [ ] 시계열 센서 데이터 수집 API 정상 동작
- [ ] LSTM 고장 예측 F1 Score > 0.75 (테스트 데이터)
- [ ] 이상 감지 → Spring Boot → WebSocket → 프론트 알림 E2E
- [ ] 충전기별 건강 지수 대시보드 표시

---

## 5주차 — 교통 위험도 예측 모듈

### 목표
- 충전소 인근 교통 데이터 연동
- 딥러닝 기반 사고 발생 위험도 예측

### 주요 작업
| 작업 | 담당 레이어 | 우선순위 |
|------|------------|---------|
| 교통 데이터 수집 API (외부 or 시뮬레이션) | Backend | P0 |
| Risk Classification 모델 (XGBoost/RF + DL) | AI | P0 |
| FastAPI `/predict/accident-risk` 엔드포인트 | AI Server | P0 |
| 위험도 레벨별 Alert 정책 구현 | Backend | P0 |
| 지도 기반 위험도 시각화 컴포넌트 (Kakao Map) | Frontend | P1 |

### 5주차 완료 체크리스트
- [ ] 교통 데이터 수집 및 전처리 파이프라인 완성
- [ ] 위험도 예측 AUC > 0.80
- [ ] 지도에 충전소별 위험도 오버레이 표시
- [ ] CRITICAL 위험도 → 관리자 이메일 알림 발송

---

## 6주차 — 실시간 통합 대시보드 구현

### 목표
- WebSocket 기반 실시간 이벤트 스트리밍
- 통합 관제 대시보드 UI/UX 완성

### 주요 작업
| 작업 | 담당 레이어 | 우선순위 |
|------|------------|---------|
| Spring WebSocket(STOMP) 서버 구현 | Backend | P0 |
| 실시간 Alert 스트리밍 채널 설계 | Backend | P0 |
| 프론트 WebSocket 클라이언트 연동 | Frontend | P0 |
| 통합 대시보드 메인 뷰 구현 | Frontend | P0 |
| KPI 카드 (가동률/위반건수/위험도) 컴포넌트 | Frontend | P0 |
| 충전소 목록 + 상세 뷰 완성 | Frontend | P1 |
| 다크모드 + 반응형 레이아웃 | Frontend | P1 |

### 6주차 완료 체크리스트
- [ ] WebSocket 실시간 알림 지연 < 500ms
- [ ] 대시보드 KPI 지표 정확성 검증
- [ ] Lighthouse 성능 점수 > 80
- [ ] 모바일 반응형 레이아웃 정상

---

## 7주차 — 통합 테스트, 성능 최적화, 배포

### 목표
- E2E 통합 테스트 및 성능 최적화
- 프로덕션 배포 파이프라인 구축

### 주요 작업
| 작업 | 담당 레이어 | 우선순위 |
|------|------------|---------|
| Spring Boot 통합 테스트 (@SpringBootTest) | Backend | P0 |
| FastAPI pytest 작성 | AI Server | P0 |
| DB 쿼리 최적화 (N+1 해결, 인덱스 튜닝) | DB | P0 |
| Redis 캐싱 도입 (충전소 목록 등) | Backend | P1 |
| Docker 프로덕션 빌드 최적화 (multi-stage) | Infra | P0 |
| CI/CD 파이프라인 (GitHub Actions) | Infra | P1 |
| 보안 감사 (OWASP Top 10) | Security | P0 |
| 최종 포트폴리오 문서 작성 | Docs | P1 |

### 7주차 완료 체크리스트
- [ ] 핵심 API 통합 테스트 커버리지 > 70%
- [ ] JMeter 부하 테스트 100 RPS 응답시간 < 200ms
- [ ] OWASP ZAP 스캔 Critical 취약점 0건
- [ ] `docker-compose -f docker-compose.prod.yml up` 프로덕션 기동 성공
- [ ] GitHub Actions CI 파이프라인 정상 동작

---

## 기술 스택 상세

```
Backend (Spring Boot 3.2)
├── Spring Security 6 (JWT + RBAC)
├── Spring Data JPA + Hibernate
├── Spring WebFlux WebClient (FastAPI 통신)
├── Spring WebSocket (STOMP)
├── Flyway (DB 마이그레이션)
├── Spring Cache + Redis
└── Spring Scheduler

AI Server (FastAPI 0.110)
├── Ultralytics YOLO v8 (객체 탐지)
├── PyTorch (LSTM 모델)
├── scikit-learn / XGBoost (위험도 분류)
├── OpenCV (이미지 전처리)
└── Celery + Redis (비동기 작업 큐)

Database (PostgreSQL 16)
├── 14개 테이블 (ERD 참고)
├── JSONB (비정형 센서 데이터)
├── TimescaleDB 확장 (시계열 최적화, 옵션)
└── 복합 인덱스 최적화

Frontend (Vue.js 3 + Pinia)
├── Vue Router 4
├── Chart.js / ECharts (데이터 시각화)
├── Kakao Map API (지도 시각화)
├── STOMP.js (WebSocket)
└── Tailwind CSS

Edge (Raspberry Pi 4)
├── Python OpenCV (카메라 스트리밍)
├── RTSP 스트림 → FastAPI
└── MQTT (센서 데이터 전송, 옵션)
```
