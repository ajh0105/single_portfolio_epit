UPDATE risk_prediction SET contributing_factors = '{"교통혼잡도": 0.28, "위반빈도": 0.15, "시간대가중치": 0.22, "기상조건": 0.12}'::jsonb WHERE station_id = 1;
UPDATE risk_prediction SET contributing_factors = '{"교통혼잡도": 0.85, "위반빈도": 0.78, "시간대가중치": 0.72, "기상조건": 0.45}'::jsonb WHERE station_id = 2;
UPDATE risk_prediction SET contributing_factors = '{"교통혼잡도": 0.62, "위반빈도": 0.55, "시간대가중치": 0.41, "기상조건": 0.28}'::jsonb WHERE station_id = 3;
UPDATE risk_prediction SET contributing_factors = '{"교통혼잡도": 0.95, "위반빈도": 0.91, "시간대가중치": 0.88, "기상조건": 0.76}'::jsonb WHERE station_id = 4;
UPDATE risk_prediction SET contributing_factors = '{"교통혼잡도": 0.52, "위반빈도": 0.44, "시간대가중치": 0.35, "기상조건": 0.38}'::jsonb WHERE station_id = 5;
UPDATE risk_prediction SET contributing_factors = '{"교통혼잡도": 0.35, "위반빈도": 0.22, "시간대가중치": 0.18, "기상조건": 0.15}'::jsonb WHERE station_id = 6;
