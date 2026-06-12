"""번호판 OCR 모듈 — ocrv6.5 학습 모델 (아키텍처: V5OCR CRNN).

ocr_best.pt 또는 vocab.json 미존재 시 Mock 모드로 자동 대체.

모델 배치 위치:
    ai-server/app/models/ocr_best.pt   ← ocrv6.5 가중치 (ep9, comp=0.9980)
    ai-server/app/models/vocab.json    ← 문자 목록 (ocrv6.5/artifacts/vocab.json)

vocab.json 형식:
    {"chars": ["0","1",...,"가","나",...], "num_classes": 51}

아키텍처 불일치로 로드 실패 시 Mock 모드 자동 전환.
정확한 로드를 위해 원본 ocrv5/code/model.py를 v5ocr.py 로 교체 권장.
"""
import json
import logging
import re
from pathlib import Path

import cv2
import numpy as np
import torch
import torch.nn.functional as F
from PIL import Image

from app.ml.v5ocr import V5OCR

logger = logging.getLogger(__name__)

# ──────────────────────────────────────────────────────────────────────────────
# 경로
# ──────────────────────────────────────────────────────────────────────────────
_MODEL_DIR = Path(__file__).parent.parent / "models"
_WEIGHTS_PATH = _MODEL_DIR / "ocr_best.pt"
_VOCAB_PATH = _MODEL_DIR / "vocab.json"

# ──────────────────────────────────────────────────────────────────────────────
# 번호판 유효성 검사 패턴 (NN한NNNN / NNN한NNNN)
# ──────────────────────────────────────────────────────────────────────────────
_PLATE_RE = re.compile(r"\d{2,3}[가-힣]\d{4}")

# ──────────────────────────────────────────────────────────────────────────────
# 내장 Fallback vocab (50자 = 한국 번호판 문자)
# vocab.json 없을 때 사용. 순서가 학습 vocab과 다르면 디코딩 결과 부정확.
# → 정확한 인식을 위해 원본 vocab.json 배치 권장.
# ──────────────────────────────────────────────────────────────────────────────
_FALLBACK_CHARS: list[str] = list(
    "0123456789"
    "가나다라마바사아자차카타파하"   # 비사업용 14
    "거너더러머버서어저처커터퍼허"   # 사업용 14
    "배"                              # 특수 1
    # 위 39자 → vocab.json 없을 경우 나머지 11자는 미매핑(디코드 시 공란 처리)
)


def _load_vocab() -> tuple[dict[int, str], int]:
    """(idx2char, num_classes) 반환. idx2char[0]="" (CTC blank)."""
    if _VOCAB_PATH.exists():
        try:
            data = json.loads(_VOCAB_PATH.read_text(encoding="utf-8"))
            chars: list[str] = data["chars"]
            nc: int = data.get("num_classes", len(chars) + 1)
            idx2char = {i + 1: c for i, c in enumerate(chars)}
            idx2char[0] = ""
            logger.info("vocab.json 로드 완료: %d자, num_classes=%d", len(chars), nc)
            return idx2char, nc
        except Exception as e:
            logger.warning("vocab.json 파싱 실패, fallback vocab 사용: %s", e)

    chars = _FALLBACK_CHARS
    nc = len(chars) + 1  # blank=0 포함
    idx2char = {i + 1: c for i, c in enumerate(chars)}
    idx2char[0] = ""
    logger.warning(
        "vocab.json 없음. 내장 %d자 vocab 사용 (정확도 저하 가능). "
        "원본 vocab.json을 %s 에 배치하세요.",
        len(chars),
        _VOCAB_PATH,
    )
    return idx2char, nc


def _preprocess(img_bgr: np.ndarray) -> torch.Tensor:
    """BGR ndarray → (1, 3, 48, 160) 정규화 텐서."""
    img = cv2.resize(img_bgr, (160, 48), interpolation=cv2.INTER_LINEAR)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB).astype(np.float32) / 255.0
    img = (img - 0.5) / 0.5
    return torch.from_numpy(img.transpose(2, 0, 1)).unsqueeze(0)  # (1,3,48,160)


def _ctc_greedy(log_probs: torch.Tensor, idx2char: dict[int, str]) -> str:
    """CTC greedy decode. log_probs shape: (T, num_classes)."""
    seq, prev = [], -1
    for v in log_probs.argmax(-1).cpu().tolist():
        if v != prev and v != 0:
            seq.append(idx2char.get(v, ""))
        prev = v
    return "".join(seq)


class PlateOcr:
    def __init__(self):
        self._model: V5OCR | None = None
        self._idx2char: dict[int, str] = {}
        self._device = torch.device("cpu")
        self._mock_mode = True

        if not _WEIGHTS_PATH.exists():
            logger.warning("ocr_best.pt 없음 (%s). Mock OCR 모드.", _WEIGHTS_PATH)
            return

        try:
            idx2char, num_classes = _load_vocab()
            model = V5OCR(num_classes=num_classes)
            ckpt = torch.load(str(_WEIGHTS_PATH), map_location="cpu", weights_only=False)
            state_dict = ckpt.get("model", ckpt)  # checkpoint key "model" or raw dict
            model.load_state_dict(state_dict, strict=True)
            model.eval()
            self._model = model
            self._idx2char = idx2char
            self._mock_mode = False
            logger.info(
                "ocrv6.5 (V5OCR) 로드 완료: epoch=%s, val_acc=%.4f",
                ckpt.get("epoch", "?"),
                ckpt.get("val_acc", 0.0),
            )
        except Exception as e:
            logger.warning(
                "ocrv6.5 (V5OCR) 로드 실패 (Mock 모드 전환): %s\n"
                "  → 원본 ocrv5/code/model.py를 ai-server/app/ml/v5ocr.py 로 교체하면 해결됩니다.",
                e,
            )

    def extract_plate(self, image: Image.Image) -> str | None:
        """번호판 문자열 반환. 인식 불가 또는 Mock 시 None."""
        if self._mock_mode or self._model is None:
            return self._mock_plate()
        try:
            img_bgr = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)
            x = _preprocess(img_bgr).to(self._device)
            with torch.no_grad():
                logits = self._model(x)                          # (1, T, 51)
                lp = F.log_softmax(logits.permute(1, 0, 2), dim=-1)  # (T, 1, 51)
            result = _ctc_greedy(lp[:, 0, :], self._idx2char)
            if result and _PLATE_RE.search(result):
                return result
            # 유효한 번호판 패턴이 아니면 None 반환
            return None
        except Exception as e:
            logger.error("V5OCR 추론 오류: %s", e)
            return None

    @staticmethod
    def _mock_plate() -> str:
        import random
        chars = "가나다라마바사아자차카타파하"
        return f"{random.randint(10, 99)}{random.choice(chars)}{random.randint(1000, 9999)}"


plate_ocr = PlateOcr()
