"""교통 데이터 수집 및 AI 서버 전송 스크립트.
실제 환경에서는 교통 API(국가교통정보센터, CCTV 등)에서 데이터 수신.
시뮬레이션 모드에서는 시간대별 규칙 기반 데이터 생성.

실행 방법:
    python reporter.py --station-id 1 --ai-server http://ai-server:8000 --interval 60
"""

import argparse
import logging
import os
import random
import time
from datetime import datetime, timezone

import requests
import schedule

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)


def simulate_traffic(hour: int) -> dict:
    """시간대별 교통 데이터 시뮬레이션."""
    if 7 <= hour <= 9 or 17 <= hour <= 19:  # 출퇴근 시간
        vehicle_count = random.randint(200, 400)
        avg_speed = random.uniform(20, 40)
        congestion = "CONGESTED" if avg_speed < 30 else "SLOW"
    elif 0 <= hour <= 5:  # 새벽
        vehicle_count = random.randint(10, 50)
        avg_speed = random.uniform(60, 90)
        congestion = "SMOOTH"
    else:
        vehicle_count = random.randint(80, 200)
        avg_speed = random.uniform(40, 70)
        congestion = "SLOW" if avg_speed < 50 else "SMOOTH"

    weather_options = ["CLEAR", "CLEAR", "CLEAR", "RAIN", "FOG"]  # CLEAR 가중치
    weather = random.choice(weather_options)
    road = "WET" if weather in ("RAIN", "SNOW") else ("ICY" if weather == "FOG" and hour < 6 else "DRY")

    return {
        "vehicle_count": vehicle_count,
        "avg_speed_kmh": round(avg_speed, 1),
        "congestion_level": congestion,
        "weather": weather,
        "road_surface": road,
    }


def send_traffic(station_id: int, data: dict, ai_server_url: str) -> bool:
    url = f"{ai_server_url}/ai/v1/traffic/analyze"
    payload = {
        "station_id": station_id,
        "collected_at": datetime.now(timezone.utc).isoformat(),
        **data,
    }
    try:
        res = requests.post(url, json=payload, timeout=10)
        if res.status_code == 200:
            result = res.json()
            logger.info("교통 분석 완료: risk=%s, score=%.3f",
                        result.get("risk_level"), result.get("risk_score", 0))
            return True
        logger.error("AI 서버 오류: %d", res.status_code)
        return False
    except Exception as e:
        logger.error("전송 오류: %s", e)
        return False


def main():
    parser = argparse.ArgumentParser(description="E-pit 교통 데이터 수집 에이전트")
    parser.add_argument("--station-id", type=int, default=int(os.getenv("STATION_ID", "1")))
    parser.add_argument("--ai-server", default=os.getenv("AI_SERVER_URL", "http://ai-server:8000"))
    parser.add_argument("--interval", type=int, default=int(os.getenv("TRAFFIC_INTERVAL", "60")))
    args = parser.parse_args()

    logger.info("교통 데이터 에이전트 시작 - 충전소 %d, 주기 %ds", args.station_id, args.interval)

    def job():
        hour = datetime.now().hour
        data = simulate_traffic(hour)
        send_traffic(args.station_id, data, args.ai_server)

    schedule.every(args.interval).seconds.do(job)
    job()

    while True:
        schedule.run_pending()
        time.sleep(1)


if __name__ == "__main__":
    main()
