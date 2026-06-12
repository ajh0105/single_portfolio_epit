from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", case_sensitive=False)

    backend_url: str = "http://localhost:8080"
    internal_api_token: str = "internal-dev-token"
    model_dir: str = "app/models"
    log_level: str = "info"

    # 고장 예측 임계값
    phm_warning_threshold: float = 0.5
    phm_critical_threshold: float = 0.8

    # YOLO 신뢰도 임계값
    yolo_confidence_threshold: float = 0.5

    # LSTM 시퀀스 길이 (입력 타임스텝 수)
    lstm_sequence_length: int = 50


settings = Settings()
