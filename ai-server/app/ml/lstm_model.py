"""LSTM 장비 고장 예측 모델.
학습된 모델이 없을 경우 Mock 추론을 반환합니다.
"""
import logging
import os
from pathlib import Path
import numpy as np
from app.core.config import settings

logger = logging.getLogger(__name__)


class LstmPhmModel:
    def __init__(self):
        self._model = None
        self._scaler = None
        self._mock = True
        self._try_load()

    def _try_load(self):
        model_path = Path(settings.model_dir) / "lstm_phm.pt"
        scaler_path = Path(settings.model_dir) / "phm_scaler.pkl"
        if model_path.exists() and scaler_path.exists():
            try:
                import torch
                import joblib
                self._model = torch.load(str(model_path), map_location="cpu")
                self._model.eval()
                self._scaler = joblib.load(str(scaler_path))
                self._mock = False
                logger.info("LSTM PHM 모델 로드 완료")
            except Exception as e:
                logger.warning("LSTM 로드 실패, Mock 모드: %s", e)
        else:
            logger.warning("LSTM 모델 파일 없음, Mock 모드 사용")

    def predict(self, series: list[dict]) -> dict:
        """시계열 센서 데이터로 고장 예측.
        Returns: {failure_probability, remaining_useful_life_hours, predicted_component}
        """
        if self._mock:
            return self._mock_predict(series)

        try:
            import torch
            features = np.array([[
                p["voltage"], p["current"], p["temperature"], p["vibration"]
            ] for p in series[-settings.lstm_sequence_length:]], dtype=np.float32)

            scaled = self._scaler.transform(features)
            x = torch.FloatTensor(scaled).unsqueeze(0)  # (1, seq_len, 4)

            with torch.no_grad():
                output = self._model(x)
                prob = float(torch.sigmoid(output).item())

            rul = max(0, int((1.0 - prob) * 2000))
            return {
                "failure_probability": round(prob, 4),
                "remaining_useful_life_hours": rul,
                "predicted_component": "COOLING_FAN" if prob > 0.7 else None,
            }
        except Exception as e:
            logger.error("LSTM 추론 오류: %s", e)
            return self._mock_predict(series)

    def _mock_predict(self, series: list[dict]) -> dict:
        import random
        # 센서 이상 징후 시뮬레이션 (온도/진동 높으면 확률 ↑)
        if series:
            last = series[-1]
            temp_factor = min(1.0, max(0.0, (last.get("temperature", 40) - 35) / 30))
            vib_factor = min(1.0, max(0.0, (last.get("vibration", 0.5) - 0.3) / 2.0))
            base_prob = 0.05 + 0.4 * temp_factor + 0.4 * vib_factor + random.uniform(-0.05, 0.05)
        else:
            base_prob = random.uniform(0.05, 0.3)

        prob = round(min(0.99, max(0.01, base_prob)), 4)
        rul = max(0, int((1.0 - prob) * 2000))
        component = "COOLING_FAN" if prob > 0.7 else ("POWER_MODULE" if prob > 0.5 else None)
        return {
            "failure_probability": prob,
            "remaining_useful_life_hours": rul,
            "predicted_component": component,
        }


lstm_model = LstmPhmModel()
