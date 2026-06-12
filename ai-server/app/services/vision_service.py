import time
import json
import logging
from datetime import datetime, timezone
from PIL import Image
from app.ml.yolo_model import yolo_model
from app.ml.plate_ocr import plate_ocr
from app.ml.ev_classifier import classify_ev
from app.core.backend_client import backend_client
from app.schemas.vision import VisionDetectResponse, VehicleDetectionResult, BoundingBox

logger = logging.getLogger(__name__)

MODEL_VERSION = "yolov8_v1"


async def process_detection(image: Image.Image, station_id: int) -> VisionDetectResponse:
    start = time.time()

    raw_detections = yolo_model.detect(image)
    results = []
    callbacks = []

    for det in raw_detections:
        cls = det["class"]
        confidence = det["confidence"]
        bbox_list = det.get("bbox", [0, 0, 0, 0])
        bbox = BoundingBox(x=bbox_list[0], y=bbox_list[1], w=bbox_list[2], h=bbox_list[3])

        is_electric, vehicle_type = classify_ev(cls, None)
        plate_number = None

        # 비전기차면 번호판 OCR
        if not is_electric:
            plate_number = plate_ocr.extract_plate(image)

        result = VehicleDetectionResult(
            vehicle_type=vehicle_type,
            is_electric=is_electric,
            plate_number=plate_number,
            confidence=confidence,
            bounding_box=bbox,
        )
        results.append(result)

        callbacks.append({
            "stationId": station_id,
            "detectedAt": datetime.now(timezone.utc).isoformat(),
            "vehicleType": vehicle_type,
            "isElectric": is_electric,
            "plateNumber": plate_number,
            "confidence": confidence,
            "boundingBox": json.dumps({"x": bbox.x, "y": bbox.y, "w": bbox.w, "h": bbox.h}),
            "imagePath": None,
        })

    # 백엔드 콜백 (비동기)
    callback_sent = False
    for payload in callbacks:
        callback_sent = await backend_client.send_vision_result(payload)

    inference_ms = int((time.time() - start) * 1000)
    return VisionDetectResponse(
        station_id=station_id,
        detections=results,
        inference_ms=inference_ms,
        callback_sent=callback_sent,
    )
