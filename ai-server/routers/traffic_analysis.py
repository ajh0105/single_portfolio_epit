from fastapi import APIRouter, HTTPException, status
from schemas.requests import AccidentRiskRequest
from schemas.responses import AccidentRiskResponse
from models.risk_classifier import AccidentRiskClassifier

router = APIRouter(prefix="/predict", tags=["Traffic Analysis"])

_classifier = AccidentRiskClassifier()


@router.post("/accident-risk", response_model=AccidentRiskResponse)
async def predict_accident_risk(request: AccidentRiskRequest):
    """
    딥러닝 기반 교통사고 위험도 예측.

    - Spring Boot WebClient Mono 체이닝으로 호출 (비동기 반응형)
    - 교통 상황 데이터를 입력으로 받아 4단계 위험도 반환
    - HIGH/CRITICAL 시 Spring Boot에서 자동 Alert + WebSocket 브로드캐스트
    """
    try:
        data = request.model_dump()
        result = _classifier.predict(request.station_id, data)
        return AccidentRiskResponse(**result)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Risk prediction failed: {str(e)}"
        )
