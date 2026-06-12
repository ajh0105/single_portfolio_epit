import json
import logging
from app.ml.lstm_model import lstm_model
from app.core.backend_client import backend_client
from app.schemas.phm import PhmIngestRequest, PhmPredictResponse
from app.core.config import settings

logger = logging.getLogger(__name__)

MODEL_VERSION = "lstm_v1"


def _to_risk_level(prob: float) -> str:
    if prob >= settings.phm_critical_threshold:
        return "CRITICAL"
    elif prob >= settings.phm_warning_threshold:
        return "WARNING"
    return "NORMAL"


async def process_phm(request: PhmIngestRequest) -> PhmPredictResponse:
    series = [
        {"voltage": p.voltage, "current": p.current,
         "temperature": p.temperature, "vibration": p.vibration}
        for p in request.series
    ]

    prediction = lstm_model.predict(series)
    prob = prediction["failure_probability"]
    risk_level = _to_risk_level(prob)

    callback_payload = {
        "chargerId": request.charger_id,
        "predictedAt": request.series[-1].measured_at.isoformat() if request.series else None,
        "failureProbability": prob,
        "remainingUsefulLifeHours": prediction.get("remaining_useful_life_hours"),
        "riskLevel": risk_level,
        "predictedComponent": prediction.get("predicted_component"),
        "modelVersion": MODEL_VERSION,
        "rawSeries": [
            {
                "measuredAt": p.measured_at.isoformat(),
                "voltage": p.voltage,
                "current": p.current,
                "temperature": p.temperature,
                "vibration": p.vibration,
            }
            for p in request.series
        ],
    }

    callback_sent = await backend_client.send_phm_result(callback_payload)

    return PhmPredictResponse(
        charger_id=request.charger_id,
        failure_probability=prob,
        remaining_useful_life_hours=prediction.get("remaining_useful_life_hours"),
        risk_level=risk_level,
        predicted_component=prediction.get("predicted_component"),
        model_version=MODEL_VERSION,
        callback_sent=callback_sent,
    )
