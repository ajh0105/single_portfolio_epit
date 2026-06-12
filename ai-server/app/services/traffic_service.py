import json
import logging
from datetime import datetime, timezone
from app.ml.traffic_model import traffic_model
from app.core.backend_client import backend_client
from app.schemas.traffic import TrafficAnalyzeRequest, TrafficAnalyzeResponse

logger = logging.getLogger(__name__)

MODEL_VERSION = "xgb_v1"


async def process_traffic(request: TrafficAnalyzeRequest) -> TrafficAnalyzeResponse:
    features = {
        "vehicle_count": request.vehicle_count,
        "avg_speed_kmh": request.avg_speed_kmh,
        "congestion_level": request.congestion_level,
        "weather": request.weather or "CLEAR",
        "road_surface": request.road_surface or "DRY",
    }

    prediction = traffic_model.predict(features)

    callback_payload = {
        "stationId": request.station_id,
        "trafficData": {
            "collectedAt": (request.collected_at or datetime.now(timezone.utc)).isoformat(),
            "vehicleCount": request.vehicle_count,
            "avgSpeedKmh": request.avg_speed_kmh,
            "congestionLevel": request.congestion_level,
            "weather": request.weather,
            "roadSurface": request.road_surface,
        },
        "risk": {
            "riskLevel": prediction["risk_level"],
            "riskScore": prediction["risk_score"],
            "contributingFactors": json.dumps(prediction.get("contributing_factors", {})),
            "modelVersion": MODEL_VERSION,
        },
    }

    callback_sent = await backend_client.send_traffic_result(callback_payload)

    return TrafficAnalyzeResponse(
        station_id=request.station_id,
        risk_level=prediction["risk_level"],
        risk_score=prediction["risk_score"],
        contributing_factors=prediction.get("contributing_factors"),
        model_version=MODEL_VERSION,
        callback_sent=callback_sent,
    )
