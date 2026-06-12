-- Flyway가 마이그레이션을 관리하므로 이 파일은 최초 DB 생성 시에만 실행됨.
-- PostgreSQL 확장 활성화 (Flyway 마이그레이션 전에 필요)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- 번호판 텍스트 검색 최적화
