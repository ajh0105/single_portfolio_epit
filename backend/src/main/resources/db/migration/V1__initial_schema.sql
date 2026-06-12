-- E-pit 통합 관제 시스템 초기 스키마
-- Flyway V1 Migration

CREATE TABLE IF NOT EXISTS member (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password      VARCHAR(255) NOT NULL,
    name          VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','OPERATOR','VIEWER')),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_member_username UNIQUE (username),
    CONSTRAINT uq_member_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS charging_station (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    station_code     VARCHAR(20)   NOT NULL,
    name             VARCHAR(100)  NOT NULL,
    address          VARCHAR(255)  NOT NULL,
    latitude         DECIMAL(10,7) NOT NULL,
    longitude        DECIMAL(10,7) NOT NULL,
    total_chargers   INT           NOT NULL DEFAULT 0,
    operation_status VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
                     CHECK (operation_status IN ('ACTIVE','MAINTENANCE','CLOSED')),
    camera_device_id VARCHAR(50),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_station_code UNIQUE (station_code)
);

CREATE TABLE IF NOT EXISTS charger (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    station_id          BIGINT      NOT NULL REFERENCES charging_station(id) ON DELETE CASCADE,
    charger_code        VARCHAR(30) NOT NULL,
    connector_type      VARCHAR(20) NOT NULL CHECK (connector_type IN ('CCS1','CCS2','CHADEMO','AC3')),
    max_power_kw        INT         NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
                        CHECK (status IN ('AVAILABLE','CHARGING','FAULT','OFFLINE','RESERVED')),
    health_score        DECIMAL(5,2),
    last_maintenance_at TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_charger_code UNIQUE (charger_code)
);
CREATE INDEX IF NOT EXISTS idx_charger_station ON charger(station_id);

CREATE TABLE IF NOT EXISTS vehicle_detection (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    station_id   BIGINT       NOT NULL REFERENCES charging_station(id) ON DELETE CASCADE,
    detected_at  TIMESTAMPTZ  NOT NULL,
    vehicle_type VARCHAR(20)  NOT NULL CHECK (vehicle_type IN ('EV','ICE','UNKNOWN')),
    is_electric  BOOLEAN      NOT NULL,
    plate_number VARCHAR(20),
    confidence   DECIMAL(5,4) NOT NULL,
    bounding_box JSONB,
    image_path   VARCHAR(255),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_detection_station_time ON vehicle_detection(station_id, detected_at DESC);

CREATE TABLE IF NOT EXISTS parking_violation (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    detection_id        BIGINT      NOT NULL REFERENCES vehicle_detection(id) ON DELETE CASCADE,
    station_id          BIGINT      NOT NULL REFERENCES charging_station(id) ON DELETE CASCADE,
    plate_number        VARCHAR(20) NOT NULL,
    violation_type      VARCHAR(30) NOT NULL CHECK (violation_type IN ('NON_EV_OCCUPANCY','OVERSTAY')),
    status              VARCHAR(20) NOT NULL DEFAULT 'DETECTED'
                        CHECK (status IN ('DETECTED','NOTIFIED','RESOLVED','FALSE_POSITIVE')),
    occurred_at         TIMESTAMPTZ NOT NULL,
    resolved_at         TIMESTAMPTZ,
    evidence_image_path VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_violation_detection UNIQUE (detection_id)
);
CREATE INDEX IF NOT EXISTS idx_violation_station_time ON parking_violation(station_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_violation_status ON parking_violation(status);

CREATE TABLE IF NOT EXISTS equipment_health (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    charger_id  BIGINT       NOT NULL REFERENCES charger(id) ON DELETE CASCADE,
    measured_at TIMESTAMPTZ  NOT NULL,
    voltage     DECIMAL(8,2) NOT NULL,
    current     DECIMAL(8,2) NOT NULL,
    temperature DECIMAL(6,2) NOT NULL,
    vibration   DECIMAL(8,4) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_health_charger_time ON equipment_health(charger_id, measured_at DESC);

CREATE TABLE IF NOT EXISTS failure_prediction (
    id                          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    charger_id                  BIGINT       NOT NULL REFERENCES charger(id) ON DELETE CASCADE,
    predicted_at                TIMESTAMPTZ  NOT NULL,
    failure_probability         DECIMAL(5,4) NOT NULL,
    remaining_useful_life_hours INT,
    risk_level                  VARCHAR(20)  NOT NULL CHECK (risk_level IN ('NORMAL','WARNING','CRITICAL')),
    predicted_component         VARCHAR(50),
    model_version               VARCHAR(30)  NOT NULL,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_prediction_charger_time ON failure_prediction(charger_id, predicted_at DESC);
CREATE INDEX IF NOT EXISTS idx_prediction_risk ON failure_prediction(risk_level);

CREATE TABLE IF NOT EXISTS traffic_data (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    station_id       BIGINT       NOT NULL REFERENCES charging_station(id) ON DELETE CASCADE,
    collected_at     TIMESTAMPTZ  NOT NULL,
    vehicle_count    INT          NOT NULL,
    avg_speed_kmh    DECIMAL(6,2) NOT NULL,
    congestion_level VARCHAR(20)  NOT NULL CHECK (congestion_level IN ('SMOOTH','SLOW','CONGESTED')),
    weather          VARCHAR(20)  CHECK (weather IN ('CLEAR','RAIN','SNOW','FOG')),
    road_surface     VARCHAR(20)  CHECK (road_surface IN ('DRY','WET','ICY')),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_traffic_station_time ON traffic_data(station_id, collected_at DESC);

CREATE TABLE IF NOT EXISTS risk_prediction (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    traffic_data_id      BIGINT       NOT NULL REFERENCES traffic_data(id) ON DELETE CASCADE,
    station_id           BIGINT       NOT NULL REFERENCES charging_station(id) ON DELETE CASCADE,
    predicted_at         TIMESTAMPTZ  NOT NULL,
    risk_level           VARCHAR(20)  NOT NULL CHECK (risk_level IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    risk_score           DECIMAL(5,4) NOT NULL,
    contributing_factors JSONB,
    model_version        VARCHAR(30)  NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_risk_traffic UNIQUE (traffic_data_id)
);
CREATE INDEX IF NOT EXISTS idx_risk_station_time ON risk_prediction(station_id, predicted_at DESC);

CREATE TABLE IF NOT EXISTS alert (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    station_id     BIGINT      REFERENCES charging_station(id) ON DELETE CASCADE,
    alert_type     VARCHAR(30) NOT NULL
                   CHECK (alert_type IN ('PARKING_VIOLATION','EQUIPMENT_FAILURE','TRAFFIC_RISK','SYSTEM')),
    severity       VARCHAR(20) NOT NULL CHECK (severity IN ('INFO','WARNING','CRITICAL')),
    title          VARCHAR(150) NOT NULL,
    message        TEXT        NOT NULL,
    reference_type VARCHAR(30),
    reference_id   BIGINT,
    is_read        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_alert_created ON alert(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_alert_unread ON alert(is_read) WHERE is_read = FALSE;

-- 초기 관리자 계정 (password: admin1234 → BCrypt $2b$10)
INSERT INTO member (username, password, name, email, role, enabled)
VALUES ('admin', '$2b$10$jQVWw2vHU0uJYUO5cH.Ub.U/HRAGhveXRFEvJn5WC7duLL1CAHFMS', '관리자', 'admin@epit.com', 'ADMIN', true)
ON CONFLICT (username) DO NOTHING;
