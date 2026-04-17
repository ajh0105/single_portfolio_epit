from fastapi import APIRouter, HTTPException, status
from schemas.requests import EquipmentPredictionRequest
from schemas.responses import EquipmentPredictionResponse
from models.lstm_predictor import LstmEquipmentPredictor

router = APIRouter(prefix="/predict", tags=["Equipment Monitoring"])

_predictor = LstmEquipmentPredictor()


@router.post("/equipment", response_model=EquipmentPredictionResponse)
async def predict_equipment_failure(request: EquipmentPredictionRequest):
    """
    LSTM 기반 충전기 장비 고장 예측.

    - Spring Boot @Async 서비스에서 비동기 호출
    - 최근 100개 센서 데이터(시계열)를 입력으로 받아 고장 확률 예측
    - failure_probability >= 0.7 이면 Spring Boot에서 Alert 생성
    """
    try:
        readings = [r.model_dump() for r in request.sensor_readings]
        result = _predictor.predict(request.charger_id, readings)
        return EquipmentPredictionResponse(**result)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Equipment prediction failed: {str(e)}"
        )
