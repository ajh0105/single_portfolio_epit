# CLAUDE.md — 프로젝트 협업 지침

## 세션 종료 시 필수 작업

세션이 끝날 때마다 아래 두 파일을 반드시 갱신한다:

1. **README.md** — 다른 환경에서 이어서 작업할 수 있도록 현재 상태를 기록
   - 마지막으로 완료한 작업
   - 현재 진행 중인 작업 (미완료 항목)
   - 다음 세션에서 해야 할 작업
   - 알려진 이슈 또는 결정 보류 사항
   - 실행 방법 (변경 사항 있을 때)

2. **CLAUDE.md** — 이 파일 자체도 갱신
   - 프로젝트 구조나 기술 스택 변경 시 반영
   - 새로 정해진 컨벤션이나 결정 사항 추가

## 모델 사용 규칙 (중요)

- **설계 작업** (아키텍처, 도메인 모델, API 설계, ERD, 기술 의사결정): **Opus 모델** 사용
  - `Agent(model="opus", ...)` 으로 서브에이전트 생성
- **코드 구현**: 현재 세션 **Sonnet 모델** 사용
- **산출물 MD 작성** (개요서, 요구사항, 유스케이스 등): **Opus 모델** 사용

## 불필요한 파일 정리 기준

세션 중 아래 유형의 파일이 생기면 즉시 또는 세션 종료 전에 삭제한다:

- 임시 테스트 파일 (`test_*.tmp`, `debug_*`, `scratch_*` 등)
- 빌드 캐시 (`.next/`, `dist/`, `build/`, `target/` 등은 `.gitignore`에 포함)
- 중복/백업 파일 (`*.bak`, `*.orig`, `copy_of_*` 등)

## 프로젝트 개요

- **프로젝트명**: E-pit AI 기반 스마트 충전소 통합 관제 시스템
- **고객사**: 현대자동차그룹 E-pit (시나리오)
- **시작일**: 2026-06-11
- **기술 스택**:
  - Backend: Spring Boot 3.2, Spring Security 6, JPA, WebSocket(STOMP), Redis, Flyway
  - AI Server: Python 3.11, FastAPI, YOLOv8, LSTM(PyTorch), XGBoost
  - Database: PostgreSQL 16, Redis 7
  - Frontend: Vue.js 3, Pinia, TypeScript, Element Plus, ECharts
  - Edge: Raspberry Pi, PiCamera2, pymodbus
  - Infra: Docker Compose, Nginx

## 패키지 컨벤션

- Backend: `com.epit.admin.*` (도메인별 패키지)
- 공통 응답: `ApiResponse<T>` 래퍼 (success/data/error/timestamp)
- 예외: `BusinessException(ErrorCode)` → `GlobalExceptionHandler`에서 처리
- 알림: `AlertService.create(AlertCreateCommand)` 단일 경로로만 생성
- AI→Backend 콜백: `/api/v1/internal/**` + `X-Internal-Token` 헤더

## 핵심 설계 원칙

1. **Alert 생성은 단일 경로**: `AlertService.create()` → 저장 + WS 푸시 + Redis Pub/Sub 캡슐화
2. **EV 판별 책임은 AI 서버**: Backend는 `isElectric` 값을 신뢰하고 위반 여부만 결정
3. **enum 값은 전 계층 SSOT**: DB CHECK / JPA STRING / TS union / pydantic Literal 동일
4. **타임존 UTC**: DB TIMESTAMPTZ/UTC 저장, 프론트에서 KST 변환
5. **AI 모델 Mock/Real 자동 전환**: 모델 파일 없으면 Mock, 있으면 실제 모델 자동 사용

## 개발 컨벤션

- 주석은 WHY가 명확하지 않을 때만 작성 (WHAT 설명 주석 금지)
- 커밋 메시지: 한국어 또는 영어 허용, 변경 이유 중심으로 작성
- 파일 경로 참조 시 상대 경로 사용

## 확인된 기술 결정사항 (세션6)

- **Jackson boolean isRead 직렬화 버그**: Lombok `@Getter` + `boolean isRead` 조합은 Jackson이 `isRead()` getter에서 "is" prefix를 제거해 JSON key가 `"read"`로 직렬화됨 → `@JsonProperty("isRead")` 어노테이션 필수
- **PHM 로그 기간 선택**: `GET /chargers/{id}/health` 엔드포인트에 optional `startDate`/`endDate` ISO datetime 파라미터 추가 (`@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)`). 미제공 시 `hours` param fallback
- **ECharts x축 포맷 기간별 전환**: 일간은 `HH:mm`, 주간/월간/전체/커스텀은 `MM/DD HH:mm` 포맷 사용 (`logPeriod` ref 값으로 분기)
- **el-date-picker daterange value-format**: `value-format="YYYY-MM-DD"` 설정 시 v-model 값이 `[string, string]`으로 바인딩됨. API 전송 전 `dayjs().startOf('day').format('YYYY-MM-DDTHH:mm:ss')` 변환 필요

## 확인된 기술 결정사항 (세션5)

- **El-Steps 처리 단계 표시**: ViolationListView 상세 모달에 `el-steps`로 감지→통보→해결 단계 시각화. `statusStep()` 함수로 active 값 계산 (DETECTED=1, NOTIFIED=2, RESOLVED=3)
- **PATCH 상태 변경 패턴**: 충전소 운영상태 변경은 `PATCH /stations/{id}/status?status=ACTIVE`. 프론트에서 `http.patch(url, null, { params: { status } })` 형식 사용 (body 없이 query param)
- **위반 해결 처리**: `PATCH /violations/{id}/resolve` — ParkingViolation.updateStatus()가 RESOLVED 시 resolvedAt 자동 설정. 프론트 local 캐시도 즉시 갱신 (목록 row 업데이트)
- **ECharts 로그 차트**: PHM 로그 모달에서 `watch(logData, ...)` 패턴으로 데이터 로드 후 차트 렌더링. `logVisible=false` 때 `logChart.dispose()` 처리

## 확인된 기술 결정사항 (세션3)

- **BigDecimal 사용 필수**: Hibernate 6.4+에서 `@Column(precision, scale)` + `Double` 조합은 오류 발생. 소수점 필드는 모두 `BigDecimal` 사용
- **RoleHierarchy + @EnableMethodSecurity**: Spring Security 6.2.x에서 `RoleHierarchy` 빈은 `@PreAuthorize`에 자동 적용되지 않음. `SecurityConfig`에 static `MethodSecurityExpressionHandler` 빈 추가 필수
- **Jackson + JPA lazy proxy**: `fail-on-empty-beans=false` — WebSocket 이벤트 발행 시 Hibernate 프록시 직렬화 방지
- **AI 서버 opencv**: `opencv-python-headless`도 `libgl1` 패키지 필요 (Debian 기반 컨테이너)
- **Flyway validate-on-migrate**: V1 SQL 수정 후 체크섬 불일치 방지를 위해 `false` 설정 (포트폴리오 환경 한정)
- **Docker Compose name**: `name: epit` 설정 필수 — 한글 디렉토리명이 이미지 태그에 포함되면 빌드 실패
- **Internal API DTO**: AI서버→Backend 콜백 DTO는 `Double` 유지 (JSON float 수신), Service 레이어에서 `BigDecimal.valueOf()` 변환

## README.md 갱신 템플릿

세션 종료 시 README.md의 "현재 상태" 섹션을 아래 형식으로 업데이트한다:

```
## 현재 상태 (Last updated: YYYY-MM-DD)

### 완료된 작업
- [x] 항목

### 진행 중
- [ ] 항목

### 다음 세션 할 일
- [ ] 항목

### 알려진 이슈 / 결정 보류
- 항목
```
