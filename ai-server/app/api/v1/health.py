from fastapi import APIRouter
from app.ml.yolo_model import yolo_model
from app.ml.lstm_model import lstm_model
from app.ml.traffic_model import traffic_model

router = APIRouter(tags=["Health"])


@router.get("/health")
async def health():
    return {
        "status": "ok",
        "models": {
            "vision": "mock" if yolo_model._mock else "loaded",
            "phm": "mock" if lstm_model._mock else "loaded",
            "traffic": "mock" if traffic_model._mock else "loaded",
        },
    }
