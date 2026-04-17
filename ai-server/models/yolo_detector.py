"""
YOLO v8 기반 불법주차 감지 모듈.

처리 파이프라인:
  1. Base64 이미지 디코딩 → OpenCV Mat
  2. YOLO 추론 → 차량 bbox + 차종 분류
  3. OCR → 번호판 텍스트 추출
  4. EV 여부 판별 (차종 + 번호판 패턴)
"""

import base64
import time
from pathlib import Path
from typing import Optional

import cv2
import numpy as np

from core.config import settings


class YoloParkingDetector:
    EV_PLATE_PATTERN = "하·허·호"  # 국내 전기차 번호판 패턴 (예시)

    def __init__(self):
        self.model = None
        self._load_model()

    def _load_model(self):
        model_path = Path(settings.yolo_model_path)
        if model_path.exists():
            from ultralytics import YOLO
            self.model = YOLO(str(model_path))
        # 모델 파일 없으면 None 유지 (개발 환경)

    def detect(self, image_base64: str) -> dict:
        start = time.time()

        image = self._decode_image(image_base64)
        if image is None:
            return self._empty_result(time.time() - start)

        if self.model is None:
            return self._mock_result(time.time() - start)

        results = self.model(image, conf=settings.detection_confidence_threshold)
        return self._parse_results(results, image, time.time() - start)

    def _decode_image(self, image_base64: str) -> Optional[np.ndarray]:
        try:
            img_bytes = base64.b64decode(image_base64)
            arr = np.frombuffer(img_bytes, dtype=np.uint8)
            return cv2.imdecode(arr, cv2.IMREAD_COLOR)
        except Exception:
            return None

    def _parse_results(self, results, image: np.ndarray, elapsed: float) -> dict:
        for result in results:
            boxes = result.boxes
            if boxes is None or len(boxes) == 0:
                continue

            for box in boxes:
                conf = float(box.conf[0])
                cls_id = int(box.cls[0])
                label = result.names[cls_id]

                if conf < settings.detection_confidence_threshold:
                    continue

                vehicle_type = self._classify_ev(label)
                license_plate = self._extract_license_plate(image, box.xyxy[0].tolist())

                return {
                    "violation_detected": vehicle_type == "NON_EV",
                    "license_plate": license_plate,
                    "vehicle_type": vehicle_type,
                    "confidence": round(conf, 4),
                    "bounding_box": {
                        "x1": box.xyxy[0][0].item(),
                        "y1": box.xyxy[0][1].item(),
                        "x2": box.xyxy[0][2].item(),
                        "y2": box.xyxy[0][3].item(),
                    },
                    "processing_time_ms": round(elapsed * 1000, 2),
                }

        return self._empty_result(elapsed)

    def _classify_ev(self, label: str) -> str:
        ev_labels = {"ev", "electric_vehicle", "ioniq", "ev6", "bz4x"}
        if label.lower() in ev_labels:
            return "EV"
        if label.lower() in {"car", "truck", "van", "suv"}:
            return "NON_EV"
        return "UNKNOWN"

    def _extract_license_plate(self, image: np.ndarray, bbox: list) -> Optional[str]:
        """
        실제 구현에서는 PaddleOCR 또는 EasyOCR을 사용.
        현재는 플레이스홀더.
        """
        return None

    def _empty_result(self, elapsed: float) -> dict:
        return {
            "violation_detected": False,
            "license_plate": None,
            "vehicle_type": "UNKNOWN",
            "confidence": 0.0,
            "bounding_box": None,
            "processing_time_ms": round(elapsed * 1000, 2),
        }

    def _mock_result(self, elapsed: float) -> dict:
        """모델 파일이 없는 개발 환경용 Mock 결과."""
        return {
            "violation_detected": True,
            "license_plate": "12가 3456",
            "vehicle_type": "NON_EV",
            "confidence": 0.92,
            "bounding_box": {"x1": 100, "y1": 200, "x2": 400, "y2": 500},
            "processing_time_ms": round(elapsed * 1000, 2),
        }
