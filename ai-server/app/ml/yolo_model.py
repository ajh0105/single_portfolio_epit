"""YOLOv8 차량 감지 모델 래퍼.
학습된 모델 파일(yolov8_vehicle.pt)이 없을 경우 Mock 결과를 반환합니다.
"""
import logging
import os
from pathlib import Path
from typing import Optional
import numpy as np
from PIL import Image
from app.core.config import settings

logger = logging.getLogger(__name__)


class YoloModel:
    def __init__(self):
        self._model = None
        self._mock = True
        model_path = Path(settings.model_dir) / "yolov8_vehicle.pt"
        if model_path.exists():
            try:
                from ultralytics import YOLO
                self._model = YOLO(str(model_path))
                self._mock = False
                logger.info("YOLOv8 모델 로드 완료: %s", model_path)
            except Exception as e:
                logger.warning("YOLOv8 로드 실패, Mock 모드: %s", e)
        else:
            logger.warning("YOLOv8 모델 파일 없음 (%s), Mock 모드 사용", model_path)

    def detect(self, image: Image.Image) -> list[dict]:
        """차량 감지 결과 반환. [{"class": str, "confidence": float, "bbox": [x,y,w,h]}]"""
        if self._mock:
            return self._mock_detect()

        results = self._model(np.array(image), conf=settings.yolo_confidence_threshold)
        detections = []
        for r in results:
            for box in r.boxes:
                detections.append({
                    "class": r.names[int(box.cls)],
                    "confidence": float(box.conf),
                    "bbox": box.xywh[0].tolist(),
                })
        return detections

    def _mock_detect(self) -> list[dict]:
        import random
        vehicle_types = ["car", "truck", "suv", "ev_vehicle"]
        return [{
            "class": random.choice(vehicle_types),
            "confidence": round(random.uniform(0.72, 0.97), 4),
            "bbox": [150.0, 100.0, 200.0, 120.0],
        }]


yolo_model = YoloModel()
