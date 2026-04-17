from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class ParkingDetectionResponse(BaseModel):
    violation_detected: bool
    license_plate: Optional[str] = None
    vehicle_type: str = "UNKNOWN"    # EV | NON_EV | UNKNOWN
    confidence: float = 0.0
    image_url: Optional[str] = None
    bounding_box: Optional[dict] = None
    processing_time_ms: float = 0.0


class EquipmentPredictionResponse(BaseModel):
    charger_id: int
    failure_probability: float
    failure_type: Optional[str] = None
    predicted_failure_at: Optional[datetime] = None
    confidence_score: float
    recommendation: str
    model_version: str
    feature_importances: Optional[dict] = None


class AccidentRiskResponse(BaseModel):
    station_id: int
    risk_level: str              # LOW | MEDIUM | HIGH | CRITICAL
    risk_score: float
    primary_risk_factor: Optional[str] = None
    risk_factors_json: Optional[str] = None
    model_version: str
    processing_time_ms: float = 0.0
