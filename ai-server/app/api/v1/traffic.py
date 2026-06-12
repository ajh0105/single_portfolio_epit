from fastapi import APIRouter
from app.schemas.traffic import TrafficAnalyzeRequest, TrafficAnalyzeResponse
from app.services.traffic_service import process_traffic

router = APIRouter(prefix="/traffic", tags=["Traffic"])


@router.post("/analyze", response_model=TrafficAnalyzeResponse)
async def analyze_traffic_risk(request: TrafficAnalyzeRequest):
    """교통 데이터를 분석하여 XGBoost 위험도를 예측합니다."""
    return await process_traffic(request)
