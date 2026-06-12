"""전기차 여부 판별 규칙 모듈.
YOLO 클래스명 + 번호판 색상 기반 판별.
"""

EV_CLASSES = {"ev_vehicle", "ev_charger_plug", "electric_vehicle"}
ICE_CLASSES = {"car", "truck", "suv", "van", "bus", "motorcycle"}

# 한국 친환경차 번호판 접두어 패턴 (파란 바탕 번호판: 전기차, 수소차)
EV_PLATE_PREFIXES = {"서울가", "서울나", "서울다"}  # 실제로는 색상 기반 판별


def classify_ev(yolo_class: str, plate_number: str | None) -> tuple[bool, str]:
    """(is_electric, vehicle_type) 반환"""
    cls = yolo_class.lower()

    if cls in EV_CLASSES:
        return True, "EV"

    if cls in ICE_CLASSES:
        return False, "ICE"

    # 번호판 색상 분석이 가능하면 여기서 추가 로직
    return False, "UNKNOWN"
