# Halo Frontend

React + Vite 기반 웹 앱입니다.

## 기술 스택

- React
- TypeScript
- Vite
- React Router

## 실행 방법

```bash
npm install
npm run dev
```

기본 주소: `http://localhost:5173`

## 백엔드 연동

`vite.config.ts`에서 `/api` 프록시를 `http://localhost:8080`으로 연결합니다.

백엔드가 실행 중이어야 로그인/시장조사/아이디어 API가 정상 동작합니다.

## 빌드

```bash
npm run build
npm run preview
```
