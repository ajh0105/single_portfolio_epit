"""XGBoost 교통 위험도 분류 모델.
학습된 모델이 없을 경우 규칙 기반 Mock을 반환합니다.
"""
import logging
from pathlib import Path
from app.core.config import settings

logger = logging.getLogger(__name__)

RISK_LEVELS = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]


class TrafficRiskModel:
    def __init__(self):
        self._model = None
        self._encoder = None
        self._mock = True
        self._try_load()

    def _try_load(self):
        model_path = Path(settings.model_dir) / "traffic_xgb.pkl"
        if model_path.exists():
            try:
                import joblib
                self._model = joblib.load(str(model_path))
                self._mock = False
                logger.info("XGBoost 교통 모델 로드 완료")
            except Exception as e:
                logger.warning("XGBoost 로드 실패, Mock 모드: %s", e)
        else:
            logger.warning("XGBoost 모델 파일 없음, Mock 모드 사용")

    def predict(self, features: dict) -> dict:
        """교통 데이터로 위험도 예측.
        Returns: {risk_level, risk_score, contributing_factors}
        """
        if self._mock:
            return self._rule_based_predict(features)

        try:
            import pandas as pd
            df = pd.DataFrame([features])
            proba = self._model.predict_proba(df)[0]
            risk_idx = proba.argmax()
            return {
                "risk_level": RISK_LEVELS[risk_idx],
                "risk_score": round(float(proba[risk_idx]), 4),
                "contributing_factors": {"probabilities": {k: round(float(v), 4) for k, v in zip(RISK_LEVELS, proba)}},
            }
        except Exception as e:
            logger.error("XGBoost 추론 오류: %s", e)
            return self._rule_based_predict(features)

    def _rule_based_predict(self, features: dict) -> dict:
        """규칙 기반 위험도 산출 (Mock용)"""
        score = 0.0
        contributing = {}

        speed = features.get("avg_speed_kmh", 50)
        count = features.get("vehicle_count", 100)
        congestion = features.get("congestion_level", "SMOOTH")
        weather = features.get("weather", "CLEAR")
        road = features.get("road_surface", "DRY")

        # 속도 요소
        if speed > 80:
            score += 0.25
            contributing["high_speed"] = 0.25
        elif speed > 60:
            score += 0.1
            contributing["medium_speed"] = 0.1

        # 혼잡도
        if congestion == "CONGESTED":
            score += 0.3
            contributing["congestion"] = 0.3
        elif congestion == "SLOW":
            score += 0.15
            contributing["slow_traffic"] = 0.15

        # 날씨
        weather_weights = {"CLEAR": 0, "RAIN": 0.2, "SNOW": 0.35, "FOG": 0.3}
        w = weather_weights.get(weather, 0)
        score += w
        if w > 0:
            contributing["weather"] = w

        # 노면
        road_weights = {"DRY": 0, "WET": 0.15, "ICY": 0.35}
        r = road_weights.get(road, 0)
        score += r
        if r > 0:
            contributing["road_surface"] = r

        score = min(0.99, max(0.01, score))
        if score >= 0.7:
            level = "CRITICAL"
        elif score >= 0.5:
            level = "HIGH"
        elif score >= 0.3:
            level = "MEDIUM"
        else:
            level = "LOW"

        return {
            "risk_level": level,
            "risk_score": round(score, 4),
            "contributing_factors": contributing,
        }


traffic_model = TrafficRiskModel()
