"""V5OCR 모델 정의: STN → VGG4 → CBAM → BiLSTM(256) → Linear(51).

ocrv6.5 학습 모델(ocr_best.pt)이 사용하는 아키텍처 클래스.
원본: ocrv5/code/model.py (v6.5_REPORT.md 7-2절 참고).

state_dict 키가 불일치할 경우 PlateOcr가 Mock 모드로 자동 대체.
정확한 작동을 원하면 원본 ocrv5/code/model.py 를 이 파일로 교체할 것.
"""

import torch
import torch.nn as nn
import torch.nn.functional as F


class _STN(nn.Module):
    """Spatial Transformer Network for perspective correction."""

    def __init__(self, in_ch: int = 3):
        super().__init__()
        self.loc_net = nn.Sequential(
            nn.Conv2d(in_ch, 32, 3, padding=1),
            nn.BatchNorm2d(32),
            nn.ReLU(inplace=True),
            nn.MaxPool2d(2, 2),
            nn.Conv2d(32, 64, 3, padding=1),
            nn.BatchNorm2d(64),
            nn.ReLU(inplace=True),
            nn.MaxPool2d(2, 2),
            nn.AdaptiveAvgPool2d((2, 8)),
        )
        self.fc_loc = nn.Sequential(
            nn.Flatten(),
            nn.Linear(64 * 2 * 8, 32),
            nn.ReLU(inplace=True),
            nn.Linear(32, 6),
        )
        nn.init.zeros_(self.fc_loc[-1].weight)
        self.fc_loc[-1].bias.data.copy_(
            torch.tensor([1, 0, 0, 0, 1, 0], dtype=torch.float)
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        theta = self.fc_loc(self.loc_net(x)).view(-1, 2, 3)
        grid = F.affine_grid(theta, x.size(), align_corners=False)
        return F.grid_sample(x, grid, align_corners=False)


class _ChannelAttn(nn.Module):
    def __init__(self, ch: int, r: int = 16):
        super().__init__()
        self.avg_pool = nn.AdaptiveAvgPool2d(1)
        self.max_pool = nn.AdaptiveMaxPool2d(1)
        self.fc = nn.Sequential(
            nn.Flatten(),
            nn.Linear(ch, max(ch // r, 1), bias=False),
            nn.ReLU(inplace=True),
            nn.Linear(max(ch // r, 1), ch, bias=False),
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        avg = self.fc(self.avg_pool(x))
        mx = self.fc(self.max_pool(x))
        return x * torch.sigmoid(avg + mx).unsqueeze(-1).unsqueeze(-1)


class _SpatialAttn(nn.Module):
    def __init__(self, k: int = 7):
        super().__init__()
        self.conv = nn.Conv2d(2, 1, k, padding=k // 2, bias=False)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        avg_f = x.mean(dim=1, keepdim=True)
        max_f = x.max(dim=1, keepdim=True).values
        return x * torch.sigmoid(self.conv(torch.cat([avg_f, max_f], dim=1)))


class _CBAM(nn.Module):
    def __init__(self, ch: int):
        super().__init__()
        self.ca = _ChannelAttn(ch)
        self.sa = _SpatialAttn()

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        return self.sa(self.ca(x))


class _VGG4(nn.Module):
    """4-block VGG backbone. (B,3,48,160) → (B,512,3,40)."""

    def __init__(self):
        super().__init__()
        self.block1 = nn.Sequential(
            nn.Conv2d(3, 64, 3, padding=1), nn.BatchNorm2d(64), nn.ReLU(inplace=True),
            nn.MaxPool2d((2, 2)),
        )
        self.block2 = nn.Sequential(
            nn.Conv2d(64, 128, 3, padding=1), nn.BatchNorm2d(128), nn.ReLU(inplace=True),
            nn.MaxPool2d((2, 2)),
        )
        self.block3 = nn.Sequential(
            nn.Conv2d(128, 256, 3, padding=1), nn.BatchNorm2d(256), nn.ReLU(inplace=True),
            nn.Conv2d(256, 256, 3, padding=1), nn.BatchNorm2d(256), nn.ReLU(inplace=True),
            nn.MaxPool2d((2, 1)),
        )
        self.block4 = nn.Sequential(
            nn.Conv2d(256, 512, 3, padding=1), nn.BatchNorm2d(512), nn.ReLU(inplace=True),
            nn.Conv2d(512, 512, 3, padding=1), nn.BatchNorm2d(512), nn.ReLU(inplace=True),
            nn.MaxPool2d((2, 1)),
        )

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        return self.block4(self.block3(self.block2(self.block1(x))))


class V5OCR(nn.Module):
    """V5OCR CRNN: STN → VGG4 → CBAM → BiLSTM(256) → Linear(51).

    출력 shape: (B, T, num_classes). CTC 디코딩 전 permute(1,0,2) 필요.
    """

    def __init__(self, num_classes: int = 51):
        super().__init__()
        self.stn = _STN(in_ch=3)
        self.cnn = _VGG4()
        self.cbam = _CBAM(512)
        self.pool = nn.AdaptiveAvgPool2d((1, None))
        self.rnn = nn.LSTM(512, 256, num_layers=1, bidirectional=True, batch_first=True)
        self.fc = nn.Linear(512, num_classes)

    def forward(self, x: torch.Tensor) -> torch.Tensor:
        x = self.stn(x)           # (B, 3, 48, 160)
        x = self.cnn(x)           # (B, 512, 3, 40)
        x = self.cbam(x)          # (B, 512, 3, 40)
        x = self.pool(x)          # (B, 512, 1, 40)
        x = x.squeeze(2)          # (B, 512, 40)
        x = x.permute(0, 2, 1)   # (B, 40, 512)
        x, _ = self.rnn(x)       # (B, 40, 512)
        return self.fc(x)         # (B, 40, 51)
