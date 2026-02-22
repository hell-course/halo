# Halo

아이디어를 빠르게 검증할 수 있도록 만든 **AI 기반 시장조사/프로토타이핑 풀스택 서비스**입니다.  
사용자가 아이디어를 입력하면 Product Hunt 데이터 기반으로 유사 서비스를 검색하고, 검색 품질 지표까지 제공합니다.

## 미리보기

![Halo Preview](./frontend/public/vite.svg)

## 프로젝트 요약

- 목적: 창업/서비스 아이디어의 초기 검증 시간 단축
- 형태: `Spring Boot + React` 기반 풀스택 웹 서비스
- 핵심 포인트:
  - JWT 인증 기반 사용자 흐름 구현
  - Product Hunt 수집/백필/임베딩 배치 파이프라인 구축
  - PostgreSQL + pgvector 기반 벡터 검색 API 제공
  - Vector vs Keyword 검색 품질 비교 리포트 제공

## 주요 기능

1. 사용자 인증
- 회원가입/로그인
- JWT 발급 및 보호 라우트 접근 제어

2. 아이디어/게시글 관리
- 아이디어 저장/조회
- 홈 화면에서 백엔드 연동 데이터 확인

3. 시장조사 파이프라인
- Product Hunt 데이터 증분 동기화(`/sync`)와 백필(`/backfill`)
- 임베딩 배치 처리(`/embed`)
- 유사 서비스 검색(`/search`) 및 검색 품질 평가(`/evaluate`)

4. 프론트 사용자 경험
- 아이디어 입력 -> 시장 유사 서비스 탐색 -> 프로토타입 화면으로 이어지는 흐름
- 검색 결과 유사도, 토픽, 외부 링크 등 핵심 정보 시각화

## 기술 스택

- Backend: Java 21, Spring Boot 3.2, Spring Security, JWT, Spring Data JPA
- Data: PostgreSQL, pgvector
- Frontend: React, TypeScript, Vite, React Router
- Infra/Tools: Docker Compose, Gradle

## 아키텍처

- `frontend`: 사용자 UI/라우팅, `/api` 프록시를 통한 백엔드 연동
- `backend`: 인증, 게시글 API, 시장조사 파이프라인 API
- `postgres`: 서비스 데이터 + 벡터 임베딩 저장

## 핵심 API

- `POST /api/market-research/sync?limit=30&maxPages=5`
- `POST /api/market-research/backfill?limit=50&maxPages=20`
- `GET /api/market-research/sync-status`
- `POST /api/market-research/embed?batchSize=30`
- `GET /api/market-research/search?query=...&limit=10`
- `GET /api/market-research/evaluate?query=...&limit=10`

## 로컬 실행

1. DB 실행
```bash
cd backend
docker compose up -d db
```

2. 백엔드 실행
```bash
cd backend
./gradlew bootRun --no-daemon
```

3. 프론트 실행
```bash
cd frontend
npm install
npm run dev
```

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`

## 상세 문서

- 백엔드 상세: `backend/README.md`
- 프론트 상세: `frontend/README.md`
