"""
LSTM 기반 충전기 장비 고장 예측 모듈 (PHM - Prognostics and Health Management).

입력 피처: [voltage, current, temperature, power_output] × 시퀀스 길이(100)
출력:      고장 확률 + 고장 유형 분류 (5-class)
"""

import json
import time
from datetime import datetime, timedelta
from pathlib import Path
from typing import Optional

import numpy as np

from core.config import settings


FAILURE_TYPES = [
    "VOLTAGE_ANOMALY",
    "OVERHEATING",
    "CONNECTOR_FAULT",
    "COOLANT_LEAK",
    "COMMUNICATION_ERROR",
]

RECOMMENDATIONS = {
    "VOLTAGE_ANOMALY": "전압 센서 및 내부 배선 점검이 필요합니다. 즉시 점검 스케줄을 예약하세요.",
    "OVERHEATING": "냉각 시스템 및 환기 상태를 점검하세요. 주변 온도가 높은 경우 차양 설치를 고려하세요.",
    "CONNECTOR_FAULT": "충전 커넥터 및 접촉부 마모 상태를 확인하세요. 필요시 커넥터를 교체하세요.",
    "COOLANT_LEAK": "냉각수 누수 여부를 즉시 점검하고 충전기를 정비 모드로 전환하세요.",
    "COMMUNICATION_ERROR": "통신 모듈 및 펌웨어 상태를 점검하세요. 재부팅 후 증상이 지속되면 모듈 교체가 필요합니다.",
}


class LstmEquipmentPredictor:

    def __init__(self):
        self.model = None
        self.scaler = None
        self._load_model()

    def _load_model(self):
        model_path = Path(settings.lstm_model_path)
        if model_path.exists():
            import torch
            self.model = torch.load(str(model_path), map_location="cpu")
            self.model.eval()

    def predict(self, charger_id: int, sensor_readings: list[dict]) -> dict:
        start = time.time()

        if self.model is None:
            return self._mock_prediction(charger_id, time.time() - start)

        features = self._preprocess(sensor_readings)
        failure_prob, failure_class = self._infer(features)

        return self._build_response(
            charger_id, failure_prob, failure_class, time.time() - start)

    def _preprocess(self, readings: list[dict]) -> np.ndarray:
        seq = []
        for r in readings[-100:]:  # 최대 100 timestep
            seq.append([
                r.get("voltage", 0),
                r.get("current_ampere", 0),
                r.get("temperature_celsius", 0),
                r.get("power_output_kw", 0),
            ])

        arr = np.array(seq, dtype=np.float32)
        # 패딩 (100 timestep 미만이면 zero-pad)
        if len(arr) < 100:
            pad = np.zeros((100 - len(arr), 4), dtype=np.float32)
            arr = np.vstack([pad, arr])

        return arr.reshape(1, 100, 4)  # (batch, seq, features)

    def _infer(self, features: np.ndarray):
        import torch
        with torch.no_grad():
            tensor = torch.from_numpy(features)
            output = self.model(tensor)
            probs = torch.softmax(output, dim=-1).squeeze()
            failure_prob = float(probs.max())
            failure_class = int(probs.argmax())
        return failure_prob, failure_class

    def _build_response(self, charger_id: int, failure_prob: float,
                        failure_class: int, elapsed: float) -> dict:
        failure_type = FAILURE_TYPES[failure_class] if failure_prob > settings.failure_alert_threshold else None
        predicted_failure_at = None
        if failure_prob > 0.8:
            predicted_failure_at = (datetime.now() + timedelta(hours=24)).isoformat()
        elif failure_prob > 0.6:
            predicted_failure_at = (datetime.now() + timedelta(hours=72)).isoformat()

        return {
            "charger_id": charger_id,
            "failure_probability": round(failure_prob, 4),
            "failure_type": failure_type,
            "predicted_failure_at": predicted_failure_at,
            "confidence_score": round(failure_prob, 4),
            "recommendation": RECOMMENDATIONS.get(failure_type, "정기 점검을 유지하세요."),
            "model_version": settings.model_version,
        }

    def _mock_prediction(self, charger_id: int, elapsed: float) -> dict:
        """모델 파일이 없는 개발 환경용 Mock 결과."""
        return {
            "charger_id": charger_id,
            "failure_probability": 0.82,
            "failure_type": "OVERHEATING",
            "predicted_failure_at": (datetime.now() + timedelta(hours=48)).isoformat(),
            "confidence_score": 0.82,
            "recommendation": RECOMMENDATIONS["OVERHEATING"],
            "model_version": settings.model_version,
        }
