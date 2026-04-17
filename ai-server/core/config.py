from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    app_name: str = "E-pit AI Server"
    debug: bool = False

    database_url: str = "postgresql+asyncpg://epit_user:epit_password@localhost:5432/epit_db"
    redis_url: str = "redis://localhost:6379"

    yolo_model_path: str = "models/weights/yolov8_epit.pt"
    lstm_model_path: str = "models/weights/lstm_charger.pt"
    risk_model_path: str = "models/weights/risk_classifier.pkl"

    model_version: str = "1.0.0"

    # 위반 판정 임계값
    detection_confidence_threshold: float = 0.75
    failure_alert_threshold: float = 0.70


settings = Settings()
