"""충전기 센서 데이터 수집 및 AI 서버 전송 스크립트.
Modbus RTU/TCP로 충전기에서 전압·전류·온도·진동 데이터를 수집하여
LSTM 예측을 위해 AI 서버로 배치 전송합니다.

실행 방법:
    python collector.py --charger-id 1 --ai-server http://ai-server:8000

환경 변수:
    AI_SERVER_URL       AI 서버 주소
    CHARGER_ID          충전기 ID
    COLLECT_INTERVAL    수집 주기(초, 기본: 5)
    BATCH_SIZE          배치 전송 크기 (기본: 50)
    MODBUS_HOST         Modbus TCP 호스트 (실제 장비)
    MODBUS_PORT         Modbus TCP 포트 (기본: 502)
"""

import argparse
import logging
import math
import os
import random
import time
from collections import deque
from datetime import datetime, timezone

import requests
import schedule

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

MODBUS_AVAILABLE = False
try:
    from pymodbus.client import ModbusTcpClient
    MODBUS_AVAILABLE = True
except ImportError:
    logger.warning("pymodbus 없음. 시뮬레이션 센서 데이터를 사용합니다.")


class SensorSimulator:
    """실제 Modbus 장비 없을 때 사용하는 시뮬레이터."""

    def __init__(self, charger_id: int):
        self.charger_id = charger_id
        self._t = 0

    def read(self) -> dict:
        self._t += 1
        # 정상 구간 + 점진적 이상 시뮬레이션 (1000회 이후 온도 상승)
        base_temp = 40.0 + max(0, (self._t - 800) * 0.05)
        noise = random.gauss(0, 0.3)
        return {
            "voltage": round(400.0 + math.sin(self._t * 0.01) * 5 + random.gauss(0, 0.5), 2),
            "current": round(80.0 + math.sin(self._t * 0.02) * 10 + random.gauss(0, 1), 2),
            "temperature": round(base_temp + noise, 2),
            "vibration": round(0.5 + max(0, (self._t - 800) * 0.001) + abs(random.gauss(0, 0.05)), 4),
        }


class ModbusSensorReader:
    """실제 Modbus TCP 센서 리더."""

    VOLTAGE_REG = 0
    CURRENT_REG = 1
    TEMP_REG = 2
    VIBRATION_REG = 3

    def __init__(self, host: str, port: int):
        self._client = ModbusTcpClient(host, port=port)
        self._client.connect()
        logger.info("Modbus 연결: %s:%d", host, port)

    def read(self) -> dict:
        # 각 레지스터에서 16bit 값 읽기 (실제 스케일링 필요)
        voltage = self._read_register(self.VOLTAGE_REG) * 0.1   # 예: 4000 → 400.0V
        current = self._read_register(self.CURRENT_REG) * 0.1   # 예: 800 → 80.0A
        temp = self._read_register(self.TEMP_REG) * 0.1         # 예: 450 → 45.0°C
        vibration = self._read_register(self.VIBRATION_REG) * 0.001  # 예: 500 → 0.500mm/s
        return {"voltage": voltage, "current": current, "temperature": temp, "vibration": vibration}

    def _read_register(self, reg: int) -> float:
        result = self._client.read_holding_registers(reg, 1)
        if result.isError():
            return 0.0
        return float(result.registers[0])


def send_batch(charger_id: int, batch: list[dict], ai_server_url: str) -> bool:
    url = f"{ai_server_url}/ai/v1/phm/ingest"
    payload = {"charger_id": charger_id, "series": batch}
    try:
        res = requests.post(url, json=payload, timeout=15)
        if res.status_code == 200:
            data = res.json()
            logger.info("PHM 예측 완료: prob=%.3f, risk=%s, RUL=%s",
                        data.get("failure_probability", 0),
                        data.get("risk_level", "N/A"),
                        data.get("remaining_useful_life_hours", "N/A"))
            return True
        logger.error("AI 서버 오류: %d", res.status_code)
        return False
    except Exception as e:
        logger.error("전송 오류: %s", e)
        return False


def main():
    parser = argparse.ArgumentParser(description="E-pit 센서 수집 에이전트")
    parser.add_argument("--charger-id", type=int, default=int(os.getenv("CHARGER_ID", "1")))
    parser.add_argument("--ai-server", default=os.getenv("AI_SERVER_URL", "http://ai-server:8000"))
    parser.add_argument("--interval", type=int, default=int(os.getenv("COLLECT_INTERVAL", "5")))
    parser.add_argument("--batch-size", type=int, default=int(os.getenv("BATCH_SIZE", "50")))
    parser.add_argument("--modbus-host", default=os.getenv("MODBUS_HOST", ""))
    parser.add_argument("--modbus-port", type=int, default=int(os.getenv("MODBUS_PORT", "502")))
    args = parser.parse_args()

    logger.info("E-pit 센서 에이전트 시작 - 충전기 ID: %d", args.charger_id)

    if MODBUS_AVAILABLE and args.modbus_host:
        reader = ModbusSensorReader(args.modbus_host, args.modbus_port)
    else:
        reader = SensorSimulator(args.charger_id)

    buffer: deque = deque(maxlen=args.batch_size)

    def collect_and_maybe_send():
        point = reader.read()
        point["measured_at"] = datetime.now(timezone.utc).isoformat()
        buffer.append(point)
        logger.debug("센서: V=%.1f A=%.1f T=%.1f Vib=%.3f",
                     point["voltage"], point["current"], point["temperature"], point["vibration"])
        if len(buffer) >= args.batch_size:
            send_batch(args.charger_id, list(buffer), args.ai_server)
            buffer.clear()

    schedule.every(args.interval).seconds.do(collect_and_maybe_send)

    while True:
        schedule.run_pending()
        time.sleep(0.5)


if __name__ == "__main__":
    main()
