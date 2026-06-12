import httpx
import logging
from app.core.config import settings

logger = logging.getLogger(__name__)


class BackendClient:
    """AI 서버 → Backend 내부 API 콜백 클라이언트"""

    def __init__(self):
        self._client = httpx.AsyncClient(
            base_url=settings.backend_url,
            headers={"X-Internal-Token": settings.internal_api_token},
            timeout=10.0,
        )

    async def send_vision_result(self, payload: dict) -> bool:
        return await self._post("/api/v1/internal/vision/result", payload)

    async def send_phm_result(self, payload: dict) -> bool:
        return await self._post("/api/v1/internal/phm/result", payload)

    async def send_traffic_result(self, payload: dict) -> bool:
        return await self._post("/api/v1/internal/traffic/result", payload)

    async def _post(self, path: str, payload: dict) -> bool:
        try:
            response = await self._client.post(path, json=payload)
            response.raise_for_status()
            logger.info("Callback success: %s", path)
            return True
        except httpx.HTTPStatusError as e:
            logger.error("Callback HTTP error %s: %s %s", path, e.response.status_code, e.response.text)
        except httpx.RequestError as e:
            logger.error("Callback request error %s: %s", path, e)
        return False

    async def close(self):
        await self._client.aclose()


backend_client = BackendClient()
