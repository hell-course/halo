# Halo

아이디어 검증을 위한 풀스택 프로젝트입니다.

- `backend`: Spring Boot API 서버 (인증, 아이디어 저장, 시장조사 파이프라인)
- `frontend`: React + Vite 웹 앱

## 빠른 시작

### 1) DB 실행

```bash
cd backend
docker compose up -d db
```

### 2) 백엔드 실행

```bash
cd backend
./gradlew bootRun --no-daemon
```

### 3) 프론트 실행

```bash
cd frontend
npm install
npm run dev
```

프론트 기본 주소: `http://localhost:5173`  
백엔드 기본 주소: `http://localhost:8080`

## 문서

- 백엔드 상세: `backend/README.md`
- 프론트 상세: `frontend/README.md`
