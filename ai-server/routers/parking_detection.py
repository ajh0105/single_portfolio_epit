from fastapi import APIRouter, HTTPException, status
from schemas.requests import ParkingDetectionRequest
from schemas.responses import ParkingDetectionResponse
from models.yolo_detector import YoloParkingDetector

router = APIRouter(prefix="/detect", tags=["Parking Detection"])

_detector = YoloParkingDetector()


@router.post("/parking", response_model=ParkingDetectionResponse)
async def detect_parking_violation(request: ParkingDetectionRequest):
    """
    YOLO 기반 불법주차 감지 엔드포인트.

    - Spring Boot → POST /detect/parking 으로 호출 (동기 REST)
    - Base64 인코딩된 이미지를 수신하여 차량 탐지 수행
    - 비EV 차량 감지 시 violation_detected=true 반환
    """
    try:
        result = _detector.detect(request.image_base64)
        return ParkingDetectionResponse(**result)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Detection failed: {str(e)}"
        )
