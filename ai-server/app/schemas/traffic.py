from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class TrafficAnalyzeRequest(BaseModel):
    station_id: int
    collected_at: Optional[datetime] = None
    vehicle_count: int
    avg_speed_kmh: float
    congestion_level: str  # SMOOTH | SLOW | CONGESTED
    weather: Optional[str] = None  # CLEAR | RAIN | SNOW | FOG
    road_surface: Optional[str] = None  # DRY | WET | ICY


class TrafficAnalyzeResponse(BaseModel):
    station_id: int
    risk_level: str
    risk_score: float
    contributing_factors: Optional[dict] = None
    model_version: str = "xgb_v1"
    callback_sent: bool
