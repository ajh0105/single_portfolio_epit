"""Raspberry Pi 카메라 캡처 및 AI 서버 전송 스크립트.

실행 방법:
    python capture.py --station-id 1 --device-id CAM-001 --ai-server http://ai-server:8000 --interval 5

환경 변수:
    AI_SERVER_URL   AI 서버 주소 (기본: http://ai-server:8000)
    STATION_ID      충전소 ID
    DEVICE_ID       카메라 장치 ID
    CAPTURE_INTERVAL  촬영 주기(초, 기본: 5)
"""

import argparse
import io
import logging
import os
import time
import schedule
import requests
from PIL import Image

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

# 실제 Pi 환경에서는 picamera2 사용
PICAMERA_AVAILABLE = False
try:
    from picamera2 import Picamera2
    PICAMERA_AVAILABLE = True
except ImportError:
    logger.warning("picamera2 없음. 테스트 이미지로 대체합니다.")


def capture_frame() -> bytes:
    """카메라에서 JPEG 프레임 캡처."""
    if PICAMERA_AVAILABLE:
        cam = Picamera2()
        cam.configure(cam.create_still_configuration(main={"size": (1280, 720)}))
        cam.start()
        time.sleep(0.5)
        stream = io.BytesIO()
        cam.capture_file(stream, format="jpeg")
        cam.stop()
        cam.close()
        return stream.getvalue()
    else:
        # 테스트용 더미 이미지
        img = Image.new("RGB", (640, 480), color=(100, 100, 100))
        buf = io.BytesIO()
        img.save(buf, format="JPEG")
        return buf.getvalue()


def send_to_ai_server(frame: bytes, station_id: int, device_id: str, ai_server_url: str) -> bool:
    """AI 서버로 이미지 전송."""
    url = f"{ai_server_url}/ai/v1/vision/detect"
    try:
        response = requests.post(
            url,
            files={"file": ("frame.jpg", frame, "image/jpeg")},
            data={"station_id": station_id, "device_id": device_id},
            timeout=15,
        )
        if response.status_code == 200:
            result = response.json()
            detections = result.get("detections", [])
            logger.info("감지 완료: %d대, inference=%dms", len(detections), result.get("inference_ms", 0))
            for det in detections:
                logger.info("  → type=%s, ev=%s, plate=%s, conf=%.2f",
                            det["vehicle_type"], det["is_electric"],
                            det.get("plate_number", "-"), det["confidence"])
            return True
        else:
            logger.error("AI 서버 응답 오류: %d %s", response.status_code, response.text[:200])
            return False
    except requests.exceptions.ConnectionError:
        logger.error("AI 서버 연결 실패: %s", url)
        return False
    except requests.exceptions.Timeout:
        logger.error("AI 서버 타임아웃: %s", url)
        return False


def run_capture(station_id: int, device_id: str, ai_server_url: str):
    """단일 캡처 + 전송 사이클."""
    logger.info("캡처 시작 - 충전소 %d, 장치 %s", station_id, device_id)
    frame = capture_frame()
    send_to_ai_server(frame, station_id, device_id, ai_server_url)


def main():
    parser = argparse.ArgumentParser(description="E-pit 카메라 캡처 에이전트")
    parser.add_argument("--station-id", type=int, default=int(os.getenv("STATION_ID", "1")))
    parser.add_argument("--device-id", default=os.getenv("DEVICE_ID", "CAM-001"))
    parser.add_argument("--ai-server", default=os.getenv("AI_SERVER_URL", "http://ai-server:8000"))
    parser.add_argument("--interval", type=int, default=int(os.getenv("CAPTURE_INTERVAL", "5")),
                        help="촬영 주기(초)")
    args = parser.parse_args()

    logger.info("E-pit 카메라 에이전트 시작 - 충전소 ID: %d, 주기: %ds", args.station_id, args.interval)

    schedule.every(args.interval).seconds.do(
        run_capture, station_id=args.station_id, device_id=args.device_id, ai_server_url=args.ai_server
    )

    # 즉시 1회 실행
    run_capture(args.station_id, args.device_id, args.ai_server)

    while True:
        schedule.run_pending()
        time.sleep(1)


if __name__ == "__main__":
    main()
