-- 각 충전기에 대해 지난 20시간의 시간별 센서 로그 생성
-- CRITICAL 충전기: 시간이 지남에 따라 점진적으로 악화되는 패턴
-- WARNING 충전기: 완만하게 상승하는 패턴
-- NORMAL 충전기: 정상 범위 내 안정적 패턴

-- EPIT-SC-001-CH03 (CRITICAL, IGBT 모듈, 현재 84°C / 0.38g)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id,
  NOW() - (n || ' hours')::interval,
  ROUND((412 - n * 1.05)::numeric, 2),
  ROUND((70.0 + (20 - n) * 1.41)::numeric, 2),
  ROUND((52.0 + (20 - n) * 1.61)::numeric, 2),
  ROUND((0.08 + (20 - n) * 0.015)::numeric, 4)
FROM charger c, generate_series(1, 20) n
WHERE c.charger_code = 'EPIT-SC-001-CH03';

-- EPIT-JS-001-CH02 (CRITICAL, 전력 변환기, 현재 87°C / 0.41g)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id,
  NOW() - (n || ' hours')::interval,
  ROUND((415 - n * 1.35)::numeric, 2),
  ROUND((68.0 + (20 - n) * 1.575)::numeric, 2),
  ROUND((50.0 + (20 - n) * 1.855)::numeric, 2),
  ROUND((0.07 + (20 - n) * 0.0170)::numeric, 4)
FROM charger c, generate_series(1, 20) n
WHERE c.charger_code = 'EPIT-JS-001-CH02';

-- EPIT-MP-001-CH04 (CRITICAL, 전력 변환기, 현재 78°C / 0.32g)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id,
  NOW() - (n || ' hours')::interval,
  ROUND((410 - n * 1.29)::numeric, 2),
  ROUND((72.0 + (20 - n) * 1.14)::numeric, 2),
  ROUND((54.0 + (20 - n) * 1.20)::numeric, 2),
  ROUND((0.09 + (20 - n) * 0.0115)::numeric, 4)
FROM charger c, generate_series(1, 20) n
WHERE c.charger_code = 'EPIT-MP-001-CH04';

-- EPIT-PG-001-CH03 (WARNING, 냉각 팬, 현재 69°C / 0.23g)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id,
  NOW() - (n || ' hours')::interval,
  ROUND((408 - n * 0.47)::numeric, 2),
  ROUND((73.0 + (20 - n) * 0.67)::numeric, 2),
  ROUND((55.0 + (20 - n) * 0.71)::numeric, 2),
  ROUND((0.10 + (20 - n) * 0.0067)::numeric, 4)
FROM charger c, generate_series(1, 20) n
WHERE c.charger_code = 'EPIT-PG-001-CH03';

-- EPIT-SC-001-CH01 (WARNING, 냉각 팬, 현재 66°C / 0.21g)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id,
  NOW() - (n || ' hours')::interval,
  ROUND((406 - n * 0.19)::numeric, 2),
  ROUND((74.0 + (20 - n) * 0.545)::numeric, 2),
  ROUND((53.0 + (20 - n) * 0.65)::numeric, 2),
  ROUND((0.09 + (20 - n) * 0.0060)::numeric, 4)
FROM charger c, generate_series(1, 20) n
WHERE c.charger_code = 'EPIT-SC-001-CH01';

-- EPIT-JS-001-CH01 (WARNING, 커넥터 접촉부, 현재 63°C / 0.17g)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id,
  NOW() - (n || ' hours')::interval,
  ROUND((404 - n * 0.435)::numeric, 2),
  ROUND((72.0 + (20 - n) * 0.505)::numeric, 2),
  ROUND((52.0 + (20 - n) * 0.555)::numeric, 2),
  ROUND((0.08 + (20 - n) * 0.0045)::numeric, 4)
FROM charger c, generate_series(1, 20) n
WHERE c.charger_code = 'EPIT-JS-001-CH01';

-- EPIT-GN-001-CH02 (WARNING, 통신 모듈, 현재 58°C / 0.14g)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id,
  NOW() - (n || ' hours')::interval,
  ROUND((407 - n * 0.01)::numeric, 2),
  ROUND((76.0 + (20 - n) * 0.165)::numeric, 2),
  ROUND((50.0 + (20 - n) * 0.445)::numeric, 2),
  ROUND((0.09 + (20 - n) * 0.0025)::numeric, 4)
FROM charger c, generate_series(1, 20) n
WHERE c.charger_code = 'EPIT-GN-001-CH02';

-- EPIT-GN-001-CH01 (NORMAL, 현재 42°C / 0.04g)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id,
  NOW() - (n || ' hours')::interval,
  ROUND((410 + (random() * 4 - 2))::numeric, 2),
  ROUND((61.0 + (random() * 4 - 2))::numeric, 2),
  ROUND((41.0 + (random() * 3 - 1))::numeric, 2),
  ROUND((0.044 + (random() * 0.008 - 0.004))::numeric, 4)
FROM charger c, generate_series(1, 20) n
WHERE c.charger_code = 'EPIT-GN-001-CH01';
