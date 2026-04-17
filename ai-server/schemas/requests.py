from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime
from enum import Enum


class ParkingDetectionRequest(BaseModel):
    camera_id: int
    image_base64: str
    timestamp: Optional[datetime] = None


class SensorReading(BaseModel):
    recorded_at: datetime
    voltage: float = 0.0
    current_ampere: float = 0.0
    temperature_celsius: float = 0.0
    power_output_kw: float = 0.0


class EquipmentPredictionRequest(BaseModel):
    charger_id: int
    sensor_readings: list[SensorReading] = Field(min_length=1)


class CongestionLevel(str, Enum):
    SMOOTH = "SMOOTH"
    SLOW = "SLOW"
    CONGESTED = "CONGESTED"
    BLOCKED = "BLOCKED"


class WeatherCondition(str, Enum):
    CLEAR = "CLEAR"
    RAIN = "RAIN"
    SNOW = "SNOW"
    FOG = "FOG"
    STORM = "STORM"


class RoadSurface(str, Enum):
    DRY = "DRY"
    WET = "WET"
    ICY = "ICY"
    SNOWY = "SNOWY"


class AccidentRiskRequest(BaseModel):
    station_id: int
    traffic_volume: Optional[int] = None
    avg_speed_kmh: Optional[float] = None
    congestion_level: Optional[CongestionLevel] = None
    weather_condition: Optional[WeatherCondition] = None
    visibility_m: Optional[int] = None
    road_surface_condition: Optional[RoadSurface] = None
