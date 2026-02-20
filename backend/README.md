# Halo Backend

Spring Boot 기반 API 서버입니다.

## 기술 스택

- Java 21
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL + pgvector

## 주요 기능

- 인증: 회원가입/로그인/JWT 검증
- 아이디어 게시글 저장/조회/검색
- 시장조사 파이프라인
  - Product Hunt 수집(sync/backfill)
  - 임베딩 배치 처리
  - 벡터 검색/평가 API

## 실행 방법

### 1) DB 실행

```bash
docker compose up -d db
```

### 2) 서버 실행

```bash
./gradlew bootRun --no-daemon
```

기본 주소: `http://localhost:8080`

## 설정 파일

- 기본 설정: `src/main/resources/application.yml`
- 시크릿 설정: `src/main/resources/application-secret.yml`

`application-secret.yml` 예시:

```yml
gemini:
  api:
    key: "YOUR_GEMINI_API_KEY"
producthunt:
  api:
    token: "YOUR_PRODUCTHUNT_TOKEN"
```

## 스케줄링 (기본값)

- 증분 수집: 30분마다
- 임베딩 배치: 5분마다
- 백필: 매일 오후 3시

## 주요 API

- `POST /api/market-research/sync?limit=50&maxPages=10`
- `POST /api/market-research/backfill?limit=50&maxPages=20`
- `GET /api/market-research/sync-status`
- `POST /api/market-research/embed?batchSize=200`
- `GET /api/market-research/search?query=ai&limit=10`
- `GET /api/market-research/evaluate?query=ai&limit=10`
