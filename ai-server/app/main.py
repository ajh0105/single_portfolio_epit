from contextlib import asynccontextmanager
import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.v1.router import router
from app.core.backend_client import backend_client
from app.core.config import settings

logging.basicConfig(
    level=settings.log_level.upper(),
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("E-pit AI Server 시작 중... Backend: %s", settings.backend_url)
    yield
    await backend_client.close()
    logger.info("E-pit AI Server 종료")


app = FastAPI(
    title="E-pit AI Server",
    description="E-pit 스마트 충전소 AI 추론 서버 (YOLO, LSTM, XGBoost)",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 내부망 전용 — 운영 시 제한
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router)


@app.get("/")
async def root():
    return {"service": "E-pit AI Server", "status": "running"}
