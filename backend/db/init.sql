-- [db/init.sql]
-- 1. 벡터 검색 기능 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Product Hunt 데이터 적재/검색 테이블
CREATE TABLE IF NOT EXISTS product_hunt_item (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(128) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    tagline TEXT,
    description TEXT,
    product_hunt_url VARCHAR(500),
    website_url VARCHAR(500),
    votes_count INTEGER,
    launched_at TIMESTAMP,
    source_updated_at TIMESTAMP,
    embedding vector(384),
    embedding_retry_count INTEGER NOT NULL DEFAULT 0,
    last_embedding_error TEXT,
    next_embedding_retry_at TIMESTAMP,
    embedding_updated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_hunt_sync_state (
    sync_key VARCHAR(64) PRIMARY KEY,
    last_cursor TEXT,
    has_next_page BOOLEAN NOT NULL DEFAULT TRUE,
    next_retry_at TIMESTAMP,
    consecutive_failures INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    last_run_started_at TIMESTAMP,
    last_run_finished_at TIMESTAMP,
    last_upserted_count INTEGER NOT NULL DEFAULT 0,
    last_page_count INTEGER NOT NULL DEFAULT 0,
    total_upserted_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
