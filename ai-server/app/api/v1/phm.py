from fastapi import APIRouter
from app.schemas.phm import PhmIngestRequest, PhmPredictResponse
from app.services.phm_service import process_phm

router = APIRouter(prefix="/phm", tags=["PHM"])


@router.post("/ingest", response_model=PhmPredictResponse)
async def ingest_sensor_data(request: PhmIngestRequest):
    """센서 시계열 데이터를 수신하여 LSTM 고장 예측을 수행합니다."""
    return await process_phm(request)
