import io
from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from PIL import Image
from app.services.vision_service import process_detection
from app.schemas.vision import VisionDetectResponse

router = APIRouter(prefix="/vision", tags=["Vision"])


@router.post("/detect", response_model=VisionDetectResponse)
async def detect_vehicle(
    file: UploadFile = File(..., description="차량 이미지 (JPEG/PNG)"),
    station_id: int = Form(..., description="충전소 ID"),
    device_id: str = Form(None, description="Edge 카메라 장치 ID"),
):
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="이미지 파일만 허용됩니다.")

    contents = await file.read()
    try:
        image = Image.open(io.BytesIO(contents)).convert("RGB")
    except Exception:
        raise HTTPException(status_code=400, detail="이미지 파일을 읽을 수 없습니다.")

    return await process_detection(image, station_id)
