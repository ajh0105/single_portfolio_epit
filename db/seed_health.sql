-- CRITICAL/WARNING 충전기의 센서 이상값 (최근 측정치)
-- EPIT-SC-001-CH03: CRITICAL (고장확률 91.2%, IGBT 모듈)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id, NOW() - INTERVAL '5 minutes', 391.50, 98.20, 84.30, 0.3820
FROM charger c WHERE c.charger_code = 'EPIT-SC-001-CH03';

-- EPIT-JS-001-CH02: CRITICAL (고장확률 94.5%, 전력 변환기)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id, NOW() - INTERVAL '3 minutes', 388.00, 99.50, 87.10, 0.4150
FROM charger c WHERE c.charger_code = 'EPIT-JS-001-CH02';

-- EPIT-MP-001-CH04: CRITICAL (고장확률 83.4%, 전력 변환기)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id, NOW() - INTERVAL '8 minutes', 384.20, 94.80, 78.60, 0.3210
FROM charger c WHERE c.charger_code = 'EPIT-MP-001-CH04';

-- EPIT-PG-001-CH03: WARNING (고장확률 78.5%, 냉각 팬)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id, NOW() - INTERVAL '12 minutes', 398.70, 86.40, 69.20, 0.2340
FROM charger c WHERE c.charger_code = 'EPIT-PG-001-CH03';

-- EPIT-SC-001-CH01: WARNING (고장확률 76.8%, 냉각 팬)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id, NOW() - INTERVAL '10 minutes', 402.10, 84.90, 66.80, 0.2180
FROM charger c WHERE c.charger_code = 'EPIT-SC-001-CH01';

-- EPIT-JS-001-CH01: WARNING (고장확률 56.4%, 커넥터 접촉부)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id, NOW() - INTERVAL '15 minutes', 395.30, 82.10, 63.40, 0.1760
FROM charger c WHERE c.charger_code = 'EPIT-JS-001-CH01';

-- EPIT-GN-001-CH02: WARNING (고장확률 49.2%, 통신 모듈)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id, NOW() - INTERVAL '20 minutes', 406.80, 79.30, 58.90, 0.1420
FROM charger c WHERE c.charger_code = 'EPIT-GN-001-CH02';

-- EPIT-GN-001-CH01: NORMAL (고장확률 12.3%)
INSERT INTO equipment_health (charger_id, measured_at, voltage, current, temperature, vibration)
SELECT c.id, NOW() - INTERVAL '6 minutes', 410.20, 62.40, 42.10, 0.0480
FROM charger c WHERE c.charger_code = 'EPIT-GN-001-CH01';
