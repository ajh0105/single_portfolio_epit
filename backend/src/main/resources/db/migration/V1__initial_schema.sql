-- ============================================================
--  E-pit AI Smart Charging Station Management System
--  Database Schema V1 — Initial
-- ============================================================

-- ────────────────────────────────────────────────────────────
--  1. USERS
-- ────────────────────────────────────────────────────────────
CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'OPERATOR',
                  -- ADMIN, OPERATOR, VIEWER
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ────────────────────────────────────────────────────────────
--  2. CHARGING_STATIONS
--     충전소 단위 (여러 충전기 포함)
-- ────────────────────────────────────────────────────────────
CREATE TABLE charging_stations (
    id             BIGSERIAL     PRIMARY KEY,
    name           VARCHAR(100)  NOT NULL,
    address        VARCHAR(255)  NOT NULL,
    latitude       DECIMAL(10,8) NOT NULL,
    longitude      DECIMAL(11,8) NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
                   -- ACTIVE, INACTIVE, MAINTENANCE
    total_chargers INT           NOT NULL DEFAULT 0,
    region         VARCHAR(50),
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ────────────────────────────────────────────────────────────
--  3. CHARGERS
--     개별 충전기 (station에 N:1)
-- ────────────────────────────────────────────────────────────
CREATE TABLE chargers (
    id                    BIGSERIAL    PRIMARY KEY,
    station_id            BIGINT       NOT NULL REFERENCES charging_stations(id) ON DELETE CASCADE,
    charger_number        VARCHAR(20)  NOT NULL,
    charger_type          VARCHAR(20)  NOT NULL,
                          -- AC_SLOW, DC_FAST, ULTRA_FAST
    max_power_kw          DECIMAL(8,2) NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
                          -- AVAILABLE, IN_USE, FAULT, MAINTENANCE
    firmware_version      VARCHAR(30),
    installation_date     DATE,
    last_maintenance_date DATE,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (station_id, charger_number)
);

-- ────────────────────────────────────────────────────────────
--  4. CAMERAS
--     라즈베리파이 카메라 장비
-- ────────────────────────────────────────────────────────────
CREATE TABLE cameras (
    id                   BIGSERIAL    PRIMARY KEY,
    station_id           BIGINT       NOT NULL REFERENCES charging_stations(id) ON DELETE CASCADE,
    charger_id           BIGINT       REFERENCES chargers(id) ON DELETE SET NULL,
    camera_name          VARCHAR(100) NOT NULL,
    location_description VARCHAR(255),
    ip_address           VARCHAR(50),
    stream_url           VARCHAR(500),
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
                         -- ACTIVE, INACTIVE, ERROR
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ────────────────────────────────────────────────────────────
--  5. VEHICLES
--     YOLO로 인식된 차량 (번호판 기준 중복 방지)
-- ────────────────────────────────────────────────────────────
CREATE TABLE vehicles (
    id                BIGSERIAL    PRIMARY KEY,
    license_plate     VARCHAR(20)  NOT NULL UNIQUE,
    vehicle_type      VARCHAR(20)  NOT NULL DEFAULT 'UNKNOWN',
                      -- EV, NON_EV, UNKNOWN
    make              VARCHAR(50),
    model             VARCHAR(50),
    color             VARCHAR(30),
    confidence_score  DECIMAL(5,4),
    first_detected_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_detected_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ────────────────────────────────────────────────────────────
--  6. PARKING_VIOLATIONS
--     불법주차 위반 이력
-- ────────────────────────────────────────────────────────────
CREATE TABLE parking_violations (
    id                   BIGSERIAL    PRIMARY KEY,
    charger_id           BIGINT       NOT NULL REFERENCES chargers(id),
    camera_id            BIGINT       REFERENCES cameras(id) ON DELETE SET NULL,
    vehicle_id           BIGINT       REFERENCES vehicles(id) ON DELETE SET NULL,
    license_plate        VARCHAR(20),
    detected_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    image_url            VARCHAR(500),
    thumbnail_url        VARCHAR(500),
    detection_confidence DECIMAL(5,4),
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
                         -- PENDING, CONFIRMED, RESOLVED, FALSE_POSITIVE
    resolved_at          TIMESTAMP,
    resolved_by          BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    notes                TEXT
);

-- ────────────────────────────────────────────────────────────
--  7. CHARGER_SENSOR_DATA
--     충전기 센서 시계열 데이터 (라즈베리파이 수집)
-- ────────────────────────────────────────────────────────────
CREATE TABLE charger_sensor_data (
    id                   BIGSERIAL    PRIMARY KEY,
    charger_id           BIGINT       NOT NULL REFERENCES chargers(id),
    recorded_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    voltage              DECIMAL(8,2),
    current_ampere       DECIMAL(8,2),
    temperature_celsius  DECIMAL(6,2),
    power_output_kw      DECIMAL(10,2),
    energy_delivered_kwh DECIMAL(12,4),
    error_code           VARCHAR(20),
    raw_data             JSONB
);

-- ────────────────────────────────────────────────────────────
--  8. EQUIPMENT_FAILURE_PREDICTIONS
--     LSTM 모델의 장비 고장 예측 결과
-- ────────────────────────────────────────────────────────────
CREATE TABLE equipment_failure_predictions (
    id                   BIGSERIAL    PRIMARY KEY,
    charger_id           BIGINT       NOT NULL REFERENCES chargers(id),
    predicted_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    failure_probability  DECIMAL(5,4) NOT NULL,
    failure_type         VARCHAR(50),
                         -- VOLTAGE_ANOMALY, OVERHEATING, CONNECTOR_FAULT, COOLANT_LEAK
    predicted_failure_at TIMESTAMP,
    confidence_score     DECIMAL(5,4),
    recommendation       TEXT,
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
                         -- ACTIVE, ACKNOWLEDGED, RESOLVED, EXPIRED
    model_version        VARCHAR(20),
    acknowledged_by      BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    acknowledged_at      TIMESTAMP
);

-- ────────────────────────────────────────────────────────────
--  9. TRAFFIC_DATA
--     충전소 인근 교통 데이터
-- ────────────────────────────────────────────────────────────
CREATE TABLE traffic_data (
    id                     BIGSERIAL    PRIMARY KEY,
    station_id             BIGINT       NOT NULL REFERENCES charging_stations(id),
    recorded_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    traffic_volume         INT,
    avg_speed_kmh          DECIMAL(6,2),
    congestion_level       VARCHAR(20),
                           -- SMOOTH, SLOW, CONGESTED, BLOCKED
    weather_condition      VARCHAR(30),
                           -- CLEAR, RAIN, SNOW, FOG, STORM
    visibility_m           INT,
    road_surface_condition VARCHAR(30),
                           -- DRY, WET, ICY, SNOWY
    incident_count         INT          DEFAULT 0,
    raw_data               JSONB
);

-- ────────────────────────────────────────────────────────────
--  10. ACCIDENT_RISK_PREDICTIONS
--      딥러닝 기반 사고 위험도 예측 결과
-- ────────────────────────────────────────────────────────────
CREATE TABLE accident_risk_predictions (
    id                  BIGSERIAL    PRIMARY KEY,
    station_id          BIGINT       NOT NULL REFERENCES charging_stations(id),
    predicted_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    risk_level          VARCHAR(20)  NOT NULL,
                        -- LOW, MEDIUM, HIGH, CRITICAL
    risk_score          DECIMAL(5,4) NOT NULL,
    primary_risk_factor VARCHAR(100),
    risk_factors        JSONB,
    valid_until         TIMESTAMP,
    model_version       VARCHAR(20)
);

-- ────────────────────────────────────────────────────────────
--  11. ALERTS
--      시스템 전반의 알림 (위반/고장/위험도)
-- ────────────────────────────────────────────────────────────
CREATE TABLE alerts (
    id               BIGSERIAL    PRIMARY KEY,
    alert_type       VARCHAR(50)  NOT NULL,
                     -- PARKING_VIOLATION, EQUIPMENT_FAULT, ACCIDENT_RISK, CHARGER_STATUS
    severity         VARCHAR(20)  NOT NULL,
                     -- INFO, WARNING, CRITICAL
    title            VARCHAR(200) NOT NULL,
    message          TEXT         NOT NULL,
    entity_type      VARCHAR(50),
                     -- CHARGER, STATION, VEHICLE
    entity_id        BIGINT,
    station_id       BIGINT       REFERENCES charging_stations(id) ON DELETE SET NULL,
    is_resolved      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    acknowledged_at  TIMESTAMP,
    acknowledged_by  BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    resolved_at      TIMESTAMP
);

-- ============================================================
--  INDEXES
-- ============================================================

-- 시계열 센서 데이터: 충전기별 최신 데이터 조회
CREATE INDEX idx_sensor_charger_time
    ON charger_sensor_data (charger_id, recorded_at DESC);

-- 불법주차: 충전기별 최신 위반 조회
CREATE INDEX idx_violation_charger_time
    ON parking_violations (charger_id, detected_at DESC);

-- 위반 상태별 조회
CREATE INDEX idx_violation_status
    ON parking_violations (status, detected_at DESC);

-- 교통 데이터: 충전소별 최신 데이터 조회
CREATE INDEX idx_traffic_station_time
    ON traffic_data (station_id, recorded_at DESC);

-- 장비 예측: 충전기별 최신 예측
CREATE INDEX idx_equip_pred_charger
    ON equipment_failure_predictions (charger_id, predicted_at DESC);

-- 활성 장비 예측 조회
CREATE INDEX idx_equip_pred_active
    ON equipment_failure_predictions (status, failure_probability DESC);

-- 사고 위험도: 충전소별 최신 유효 예측
CREATE INDEX idx_risk_pred_station
    ON accident_risk_predictions (station_id, predicted_at DESC);

-- 알림: 미해결 알림 조회 (대시보드 핵심 쿼리)
CREATE INDEX idx_alerts_unresolved
    ON alerts (is_resolved, created_at DESC);

-- 알림: 충전소별 알림
CREATE INDEX idx_alerts_station
    ON alerts (station_id, created_at DESC);

-- 알림: 심각도별 조회
CREATE INDEX idx_alerts_severity
    ON alerts (severity, is_resolved, created_at DESC);

-- ============================================================
--  SEED DATA — 초기 관리자 계정
--  password: Admin@1234 (BCrypt $2a$12$...)
-- ============================================================
INSERT INTO users (username, email, password_hash, role)
VALUES (
    'admin',
    'admin@epit.hyundai.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY.5AhRY7E0jGZS',
    'ADMIN'
);
