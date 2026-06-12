from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class SensorPoint(BaseModel):
    measured_at: datetime
    voltage: float
    current: float
    temperature: float
    vibration: float


class PhmIngestRequest(BaseModel):
    charger_id: int
    series: list[SensorPoint]


class PhmPredictResponse(BaseModel):
    charger_id: int
    failure_probability: float
    remaining_useful_life_hours: Optional[int] = None
    risk_level: str
    predicted_component: Optional[str] = None
    model_version: str = "lstm_v1"
    callback_sent: bool
