package com.example.halo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductHuntSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public ProductHuntSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("""
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
                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                updated_at TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """);
        jdbcTemplate.execute("ALTER TABLE product_hunt_item ADD COLUMN IF NOT EXISTS embedding vector(384)");
        jdbcTemplate.execute("ALTER TABLE product_hunt_item ADD COLUMN IF NOT EXISTS embedding_retry_count INTEGER NOT NULL DEFAULT 0");
        jdbcTemplate.execute("ALTER TABLE product_hunt_item ADD COLUMN IF NOT EXISTS last_embedding_error TEXT");
        jdbcTemplate.execute("ALTER TABLE product_hunt_item ADD COLUMN IF NOT EXISTS next_embedding_retry_at TIMESTAMP");
        jdbcTemplate.execute("ALTER TABLE product_hunt_item ADD COLUMN IF NOT EXISTS embedding_updated_at TIMESTAMP");
        jdbcTemplate.execute("ALTER TABLE product_hunt_item ADD COLUMN IF NOT EXISTS source_updated_at TIMESTAMP");
        jdbcTemplate.execute("ALTER TABLE product_hunt_item ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW()");
        jdbcTemplate.execute("ALTER TABLE product_hunt_item ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW()");
        jdbcTemplate.execute("""
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
                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                updated_at TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """);
    }
}
