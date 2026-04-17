"""
E-pit AI Server — FastAPI 메인 애플리케이션

통신 아키텍처 요약:
  ┌─────────────────────────────────────────────────────────────────┐
  │  Raspberry Pi ──RTSP──► /detect/parking  (동기 REST 호출)       │
  │  Spring Boot  ──POST──► /predict/equipment (비동기 @Async 호출)  │
  │  Spring Boot  ──POST──► /predict/accident-risk (Mono 체이닝)    │
  └─────────────────────────────────────────────────────────────────┘
"""

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from core.config import settings
from routers import parking_detection, equipment_monitoring, traffic_analysis


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 애플리케이션 시작 시 AI 모델 로딩 (워밍업)
    print(f"[E-pit AI Server] Starting — version {settings.model_version}")
    yield
    print("[E-pit AI Server] Shutting down")


app = FastAPI(
    title=settings.app_name,
    description="E-pit 스마트 충전소 AI 분석 서버 (YOLO / LSTM / Risk Classifier)",
    version=settings.model_version,
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(parking_detection.router)
app.include_router(equipment_monitoring.router)
app.include_router(traffic_analysis.router)


@app.get("/health", tags=["Health"])
async def health_check():
    return JSONResponse({"status": "ok", "version": settings.model_version})
