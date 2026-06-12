-- ============================================
-- 충전기 추가 (강남 CH02, CH03)
-- ============================================
INSERT INTO charger (charger_code, station_id, connector_type, max_power_kw, status, health_score, last_maintenance_at)
VALUES
  ('EPIT-GN-001-CH02', 1, 'CCS2',    100, 'AVAILABLE', 87.50, NOW() - INTERVAL '15 days'),
  ('EPIT-GN-001-CH03', 1, 'AC3',      22, 'CHARGING',  92.00, NOW() - INTERVAL '5 days')
ON CONFLICT (charger_code) DO NOTHING;

-- 서초 (station_id=2)
INSERT INTO charger (charger_code, station_id, connector_type, max_power_kw, status, health_score, last_maintenance_at)
VALUES
  ('EPIT-SC-001-CH01', 2, 'CCS2',    200, 'AVAILABLE', 95.00, NOW() - INTERVAL '3 days'),
  ('EPIT-SC-001-CH02', 2, 'CCS2',    200, 'AVAILABLE', 91.00, NOW() - INTERVAL '10 days'),
  ('EPIT-SC-001-CH03', 2, 'CHADEMO',  50, 'FAULT',     34.20, NOW() - INTERVAL '30 days'),
  ('EPIT-SC-001-CH04', 2, 'AC3',      22, 'CHARGING',  88.50, NOW() - INTERVAL '7 days')
ON CONFLICT (charger_code) DO NOTHING;

-- 판교 (station_id=3)
INSERT INTO charger (charger_code, station_id, connector_type, max_power_kw, status, health_score, last_maintenance_at)
VALUES
  ('EPIT-PG-001-CH01', 3, 'CCS2',    350, 'AVAILABLE', 97.00, NOW() - INTERVAL '2 days'),
  ('EPIT-PG-001-CH02', 3, 'CCS2',    350, 'CHARGING',  89.00, NOW() - INTERVAL '8 days'),
  ('EPIT-PG-001-CH03', 3, 'CCS1',    200, 'AVAILABLE', 76.50, NOW() - INTERVAL '25 days'),
  ('EPIT-PG-001-CH04', 3, 'CHADEMO',  50, 'AVAILABLE', 82.00, NOW() - INTERVAL '14 days'),
  ('EPIT-PG-001-CH05', 3, 'AC3',      22, 'CHARGING',  94.00, NOW() - INTERVAL '4 days'),
  ('EPIT-PG-001-CH06', 3, 'AC3',      22, 'AVAILABLE', 90.00, NOW() - INTERVAL '6 days')
ON CONFLICT (charger_code) DO NOTHING;

-- 잠실 (station_id=4)
INSERT INTO charger (charger_code, station_id, connector_type, max_power_kw, status, health_score, last_maintenance_at)
VALUES
  ('EPIT-JS-001-CH01', 4, 'CCS2',   100, 'OFFLINE',   45.00, NOW() - INTERVAL '40 days'),
  ('EPIT-JS-001-CH02', 4, 'CCS2',   100, 'OFFLINE',   52.00, NOW() - INTERVAL '35 days'),
  ('EPIT-JS-001-CH03', 4, 'AC3',     22, 'AVAILABLE', 78.00, NOW() - INTERVAL '10 days')
ON CONFLICT (charger_code) DO NOTHING;

-- 마포 (station_id=5)
INSERT INTO charger (charger_code, station_id, connector_type, max_power_kw, status, health_score, last_maintenance_at)
VALUES
  ('EPIT-MP-001-CH01', 5, 'CCS2',   200, 'CHARGING',  91.50, NOW() - INTERVAL '5 days'),
  ('EPIT-MP-001-CH02', 5, 'CCS2',   200, 'AVAILABLE', 86.00, NOW() - INTERVAL '12 days'),
  ('EPIT-MP-001-CH03', 5, 'CHADEMO', 50, 'AVAILABLE', 79.00, NOW() - INTERVAL '18 days'),
  ('EPIT-MP-001-CH04', 5, 'AC3',     22, 'FAULT',     28.50, NOW() - INTERVAL '45 days'),
  ('EPIT-MP-001-CH05', 5, 'AC3',     22, 'CHARGING',  93.00, NOW() - INTERVAL '3 days')
ON CONFLICT (charger_code) DO NOTHING;

-- 영등포 (station_id=6)
INSERT INTO charger (charger_code, station_id, connector_type, max_power_kw, status, health_score, last_maintenance_at)
VALUES
  ('EPIT-YS-001-CH01', 6, 'CCS2',   150, 'AVAILABLE', 88.00, NOW() - INTERVAL '9 days'),
  ('EPIT-YS-001-CH02', 6, 'CCS2',   150, 'CHARGING',  84.50, NOW() - INTERVAL '16 days'),
  ('EPIT-YS-001-CH03', 6, 'AC3',     22, 'AVAILABLE', 96.00, NOW() - INTERVAL '1 day'),
  ('EPIT-YS-001-CH04', 6, 'AC3',     22, 'AVAILABLE', 90.50, NOW() - INTERVAL '11 days')
ON CONFLICT (charger_code) DO NOTHING;

-- ============================================
-- 차량 감지 → 위반 내역 (CTE 방식)
-- ============================================
WITH det AS (
  INSERT INTO vehicle_detection (station_id, detected_at, vehicle_type, is_electric, plate_number, confidence)
  VALUES
    (1, NOW() - INTERVAL '2 hours',         'ICE', false, '서울12가3456', 0.97),
    (1, NOW() - INTERVAL '4 hours',         'ICE', false, '경기34나5678', 0.93),
    (2, NOW() - INTERVAL '1 day',           'ICE', false, '서울56다7890', 0.88),
    (2, NOW() - INTERVAL '1 day 3 hours',   'ICE', false, '인천78라9012', 0.91),
    (3, NOW() - INTERVAL '2 days',          'ICE', false, '부산90마1234', 0.95),
    (4, NOW() - INTERVAL '30 minutes',      'ICE', false, '대구12바3456', 0.86),
    (5, NOW() - INTERVAL '5 hours',         'ICE', false, '광주34사5678', 0.92),
    (5, NOW() - INTERVAL '3 days',          'ICE', false, '대전56아7890', 0.98),
    (6, NOW() - INTERVAL '6 hours',         'ICE', false, '울산78자9012', 0.90),
    (6, NOW() - INTERVAL '15 minutes',      'ICE', false, '세종90차1234', 0.87)
  RETURNING id, station_id, plate_number, detected_at
)
INSERT INTO parking_violation (detection_id, station_id, plate_number, violation_type, status, occurred_at)
SELECT
  d.id,
  d.station_id,
  d.plate_number,
  'NON_EV_OCCUPANCY',
  CASE
    WHEN d.plate_number IN ('부산90마1234','대전56아7890') THEN 'RESOLVED'
    WHEN d.plate_number IN ('서울56다7890','인천78라9012','광주34사5678','울산78자9012') THEN 'NOTIFIED'
    ELSE 'DETECTED'
  END,
  d.detected_at
FROM det d;

-- ============================================
-- PHM 장비 예측 (NORMAL/WARNING/CRITICAL)
-- ============================================
INSERT INTO failure_prediction (charger_id, predicted_at, failure_probability, remaining_useful_life_hours, risk_level, predicted_component, model_version)
SELECT id, NOW() - INTERVAL '1 hour', 0.9120, 48,  'CRITICAL', 'IGBT 모듈',      'lstm-v2.1' FROM charger WHERE charger_code = 'EPIT-SC-001-CH03'
ON CONFLICT DO NOTHING;
INSERT INTO failure_prediction (charger_id, predicted_at, failure_probability, remaining_useful_life_hours, risk_level, predicted_component, model_version)
SELECT id, NOW() - INTERVAL '2 hours', 0.7850, 120, 'WARNING', '냉각 팬',         'lstm-v2.1' FROM charger WHERE charger_code = 'EPIT-PG-001-CH03'
ON CONFLICT DO NOTHING;
INSERT INTO failure_prediction (charger_id, predicted_at, failure_probability, remaining_useful_life_hours, risk_level, predicted_component, model_version)
SELECT id, NOW() - INTERVAL '1 hour 30 minutes', 0.8340, 72, 'CRITICAL', '전력 변환기', 'lstm-v2.1' FROM charger WHERE charger_code = 'EPIT-MP-001-CH04'
ON CONFLICT DO NOTHING;
INSERT INTO failure_prediction (charger_id, predicted_at, failure_probability, remaining_useful_life_hours, risk_level, predicted_component, model_version)
SELECT id, NOW() - INTERVAL '3 hours', 0.5640, 336, 'WARNING', '커넥터 접촉부',   'lstm-v2.1' FROM charger WHERE charger_code = 'EPIT-JS-001-CH01'
ON CONFLICT DO NOTHING;
INSERT INTO failure_prediction (charger_id, predicted_at, failure_probability, remaining_useful_life_hours, risk_level, predicted_component, model_version)
SELECT id, NOW() - INTERVAL '45 minutes', 0.9450, 24, 'CRITICAL', '전력 변환기',  'lstm-v2.1' FROM charger WHERE charger_code = 'EPIT-JS-001-CH02'
ON CONFLICT DO NOTHING;
INSERT INTO failure_prediction (charger_id, predicted_at, failure_probability, remaining_useful_life_hours, risk_level, predicted_component, model_version)
SELECT id, NOW() - INTERVAL '4 hours', 0.4920, 480, 'WARNING', '통신 모듈',       'lstm-v2.1' FROM charger WHERE charger_code = 'EPIT-GN-001-CH02'
ON CONFLICT DO NOTHING;
INSERT INTO failure_prediction (charger_id, predicted_at, failure_probability, remaining_useful_life_hours, risk_level, predicted_component, model_version)
SELECT id, NOW() - INTERVAL '5 hours', 0.4510, 720, 'WARNING', '냉각수 펌프',     'lstm-v2.1' FROM charger WHERE charger_code = 'EPIT-YS-001-CH02'
ON CONFLICT DO NOTHING;
INSERT INTO failure_prediction (charger_id, predicted_at, failure_probability, remaining_useful_life_hours, risk_level, predicted_component, model_version)
SELECT id, NOW() - INTERVAL '2 hours 30 minutes', 0.7680, 144, 'WARNING', '냉각 팬', 'lstm-v2.1' FROM charger WHERE charger_code = 'EPIT-SC-001-CH01'
ON CONFLICT DO NOTHING;

-- ============================================
-- 교통 데이터 → 위험도 예측 (CTE 방식)
-- ============================================
WITH td AS (
  INSERT INTO traffic_data (station_id, collected_at, vehicle_count, avg_speed_kmh, congestion_level, weather, road_surface)
  VALUES
    (1, NOW() - INTERVAL '30 minutes', 12, 35.50, 'SLOW',      'CLEAR', 'DRY'),
    (2, NOW() - INTERVAL '25 minutes', 45, 8.20,  'CONGESTED', 'RAIN',  'WET'),
    (3, NOW() - INTERVAL '20 minutes', 28, 18.40, 'SLOW',      'CLEAR', 'DRY'),
    (4, NOW() - INTERVAL '15 minutes', 62, 4.10,  'CONGESTED', 'RAIN',  'WET'),
    (5, NOW() - INTERVAL '35 minutes', 22, 22.00, 'SLOW',      'CLEAR', 'DRY'),
    (6, NOW() - INTERVAL '40 minutes', 8,  42.30, 'SMOOTH',    'CLEAR', 'DRY')
  RETURNING id, station_id
)
INSERT INTO risk_prediction (traffic_data_id, station_id, predicted_at, risk_level, risk_score, contributing_factors, model_version)
SELECT
  t.id,
  t.station_id,
  NOW() - (CASE t.station_id WHEN 1 THEN INTERVAL '30 minutes' WHEN 2 THEN INTERVAL '25 minutes' WHEN 3 THEN INTERVAL '20 minutes' WHEN 4 THEN INTERVAL '15 minutes' WHEN 5 THEN INTERVAL '35 minutes' ELSE INTERVAL '40 minutes' END),
  CASE t.station_id WHEN 1 THEN 'LOW' WHEN 2 THEN 'HIGH' WHEN 3 THEN 'MEDIUM' WHEN 4 THEN 'CRITICAL' WHEN 5 THEN 'MEDIUM' ELSE 'LOW' END,
  CASE t.station_id WHEN 1 THEN 0.2345 WHEN 2 THEN 0.8120 WHEN 3 THEN 0.5670 WHEN 4 THEN 0.9230 WHEN 5 THEN 0.4890 ELSE 0.3120 END,
  CASE t.station_id
    WHEN 1 THEN '{"혼잡도": 0.28, "위반빈도": 0.15, "기상조건": 0.12}'::jsonb
    WHEN 2 THEN '{"혼잡도": 0.85, "위반빈도": 0.78, "시간대": 0.72, "기상조건": 0.45}'::jsonb
    WHEN 3 THEN '{"혼잡도": 0.62, "위반빈도": 0.55, "시간대": 0.41}'::jsonb
    WHEN 4 THEN '{"혼잡도": 0.95, "위반빈도": 0.91, "시간대": 0.88, "기상조건": 0.76}'::jsonb
    WHEN 5 THEN '{"혼잡도": 0.52, "위반빈도": 0.44, "기상조건": 0.38}'::jsonb
    ELSE     '{"혼잡도": 0.35, "위반빈도": 0.22, "시간대": 0.18}'::jsonb
  END,
  'xgb-v1.3'
FROM td t;

-- ============================================
-- 알림 추가 (alert_type/severity 정확한 값)
-- ============================================
INSERT INTO alert (alert_type, severity, title, message, station_id, is_read, created_at)
VALUES
  ('PARKING_VIOLATION', 'WARNING',  '불법 주차 감지 — 잠실 E-pit',     '잠실 E-pit 대구12바3456 비전기차 불법 주차 감지', 4, false, NOW() - INTERVAL '30 minutes'),
  ('EQUIPMENT_FAILURE', 'CRITICAL', '장비 위험 — 서초 E-pit CH03',      '서초 EPIT-SC-001-CH03 고장 확률 91.2%. 즉시 점검 요망', 2, false, NOW() - INTERVAL '45 minutes'),
  ('TRAFFIC_RISK',      'CRITICAL', '교통 혼잡 위험 — 잠실 E-pit',      '잠실 E-pit 교통 위험도 92.3% 매우 위험', 4, false, NOW() - INTERVAL '15 minutes'),
  ('EQUIPMENT_FAILURE', 'WARNING',  '장비 고위험 — 판교 E-pit CH03',    '판교 EPIT-PG-001-CH03 잔여 수명 120h, 냉각 팬 이상', 3, false, NOW() - INTERVAL '1 hour'),
  ('PARKING_VIOLATION', 'INFO',     '불법 주차 해소 — 마포 E-pit',      '마포 광주34사5678 차량 이동 확인', 5, true, NOW() - INTERVAL '5 hours'),
  ('TRAFFIC_RISK',      'WARNING',  '교통 혼잡 경고 — 서초 E-pit',      '서초 E-pit 교통 위험도 81.2%', 2, true, NOW() - INTERVAL '25 minutes'),
  ('EQUIPMENT_FAILURE', 'WARNING',  '장비 경고 — 마포 E-pit CH04',      '마포 통신 모듈 점검 권고. 고장 확률 49.2%', 5, true, NOW() - INTERVAL '4 hours'),
  ('PARKING_VIOLATION', 'INFO',     '위반 해소 — 판교 E-pit',           '판교 E-pit 부산90마1234 차량 이동 완료', 3, true, NOW() - INTERVAL '2 days'),
  ('SYSTEM',            'INFO',     '시스템 정기 점검 완료',             '전체 시스템 정기 점검이 완료되었습니다.', NULL, true, NOW() - INTERVAL '1 day')
ON CONFLICT DO NOTHING;

-- ============================================
-- 충전소 total_chargers 최신화
-- ============================================
UPDATE charging_station cs
SET total_chargers = (SELECT COUNT(*) FROM charger c WHERE c.station_id = cs.id);
