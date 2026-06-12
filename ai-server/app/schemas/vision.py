from pydantic import BaseModel
from typing import Optional
import json


class BoundingBox(BaseModel):
    x: float
    y: float
    w: float
    h: float


class VehicleDetectionResult(BaseModel):
    vehicle_type: str
    is_electric: bool
    plate_number: Optional[str] = None
    confidence: float
    bounding_box: Optional[BoundingBox] = None


class VisionDetectResponse(BaseModel):
    station_id: int
    detections: list[VehicleDetectionResult]
    inference_ms: int
    callback_sent: bool
