from fastapi import APIRouter
from app.api.v1 import vision, phm, traffic, health

router = APIRouter(prefix="/ai/v1")
router.include_router(health.router)
router.include_router(vision.router)
router.include_router(phm.router)
router.include_router(traffic.router)
