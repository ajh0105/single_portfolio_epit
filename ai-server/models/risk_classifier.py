"""
교통사고 위험도 분류 모델 (XGBoost + 딥러닝 앙상블).

입력 피처:
  - traffic_volume (교통량)
  - avg_speed_kmh (평균 속도)
  - congestion_level (혼잡도: 0~3)
  - weather_condition (날씨: 0~4)
  - visibility_m (가시거리)
  - road_surface_condition (노면 상태: 0~3)

출력: 위험 레벨 (LOW/MEDIUM/HIGH/CRITICAL) + 점수 (0~1)
"""

import json
import time
from pathlib import Path
from typing import Optional

import numpy as np

from core.config import settings


RISK_LEVELS = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]

CONGESTION_MAP = {"SMOOTH": 0, "SLOW": 1, "CONGESTED": 2, "BLOCKED": 3}
WEATHER_MAP = {"CLEAR": 0, "RAIN": 1, "FOG": 2, "SNOW": 3, "STORM": 4}
SURFACE_MAP = {"DRY": 0, "WET": 1, "SNOWY": 2, "ICY": 3}

RISK_FACTORS_WEIGHT = {
    "weather_condition": 0.30,
    "road_surface_condition": 0.25,
    "congestion_level": 0.20,
    "visibility_m": 0.15,
    "avg_speed_kmh": 0.10,
}


class AccidentRiskClassifier:

    def __init__(self):
        self.model = None
        self._load_model()

    def _load_model(self):
        model_path = Path(settings.risk_model_path)
        if model_path.exists():
            import pickle
            with open(str(model_path), "rb") as f:
                self.model = pickle.load(f)

    def predict(self, station_id: int, traffic_data: dict) -> dict:
        start = time.time()

        if self.model is None:
            return self._mock_prediction(station_id, traffic_data, time.time() - start)

        features = self._extract_features(traffic_data)
        risk_score, risk_class = self._infer(features)
        primary_factor = self._identify_primary_factor(traffic_data)
        risk_factors = self._build_risk_factors(traffic_data)

        return {
            "station_id": station_id,
            "risk_level": RISK_LEVELS[risk_class],
            "risk_score": round(risk_score, 4),
            "primary_risk_factor": primary_factor,
            "risk_factors_json": json.dumps(risk_factors, ensure_ascii=False),
            "model_version": settings.model_version,
            "processing_time_ms": round((time.time() - start) * 1000, 2),
        }

    def _extract_features(self, data: dict) -> np.ndarray:
        return np.array([[
            data.get("traffic_volume") or 0,
            data.get("avg_speed_kmh") or 60.0,
            CONGESTION_MAP.get(str(data.get("congestion_level", "SMOOTH")), 0),
            WEATHER_MAP.get(str(data.get("weather_condition", "CLEAR")), 0),
            data.get("visibility_m") or 1000,
            SURFACE_MAP.get(str(data.get("road_surface_condition", "DRY")), 0),
        ]], dtype=np.float32)

    def _infer(self, features: np.ndarray):
        probs = self.model.predict_proba(features)[0]
        risk_class = int(np.argmax(probs))
        risk_score = float(probs[risk_class])
        return risk_score, risk_class

    def _identify_primary_factor(self, data: dict) -> Optional[str]:
        factor_scores = {}

        weather = str(data.get("weather_condition", "CLEAR"))
        if weather in ("SNOW", "STORM", "FOG"):
            factor_scores["기상 악화"] = 0.9
        elif weather == "RAIN":
            factor_scores["우천"] = 0.6

        surface = str(data.get("road_surface_condition", "DRY"))
        if surface == "ICY":
            factor_scores["결빙 노면"] = 0.95
        elif surface == "SNOWY":
            factor_scores["적설 노면"] = 0.85

        congestion = str(data.get("congestion_level", "SMOOTH"))
        if congestion == "BLOCKED":
            factor_scores["극심한 정체"] = 0.8
        elif congestion == "CONGESTED":
            factor_scores["심한 정체"] = 0.6

        visibility = data.get("visibility_m") or 1000
        if visibility < 100:
            factor_scores["극도의 저시정"] = 0.9
        elif visibility < 500:
            factor_scores["저시정"] = 0.7

        if not factor_scores:
            return None
        return max(factor_scores, key=factor_scores.get)

    def _build_risk_factors(self, data: dict) -> dict:
        return {
            "weather": str(data.get("weather_condition", "CLEAR")),
            "road_surface": str(data.get("road_surface_condition", "DRY")),
            "congestion": str(data.get("congestion_level", "SMOOTH")),
            "visibility_m": data.get("visibility_m") or 1000,
            "avg_speed_kmh": data.get("avg_speed_kmh") or 60,
        }

    def _mock_prediction(self, station_id: int, data: dict, elapsed: float) -> dict:
        weather = str(data.get("weather_condition", "CLEAR"))
        risk_score = 0.35 if weather == "CLEAR" else 0.78

        return {
            "station_id": station_id,
            "risk_level": "LOW" if risk_score < 0.4 else "HIGH",
            "risk_score": risk_score,
            "primary_risk_factor": "기상 악화" if weather != "CLEAR" else None,
            "risk_factors_json": json.dumps(self._build_risk_factors(data), ensure_ascii=False),
            "model_version": settings.model_version,
            "processing_time_ms": round(elapsed * 1000, 2),
        }
