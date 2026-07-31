# KLCUBE AX Platform Frontend

Next.js App Router 기반의 AX Platform 관리자 화면입니다.

## 실행

```bash
npm run dev
```

브라우저에서 `http://localhost:3000`으로 접속합니다.

## 주요 구조

```text
src/
├─ app/
│  ├─ layout.tsx
│  ├─ page.tsx
│  ├─ providers.tsx
│  └─ (admin)/
│     ├─ dashboard/page.tsx
│     ├─ documents/page.tsx
│     ├─ ai-jobs/page.tsx
│     └─ system/page.tsx
├─ features/
│  └─ manager/
│     ├─ dashboard/
│     ├─ documents/
│     ├─ ai-jobs/
│     └─ system/
├─ shared/
│  ├─ api/
│  └─ components/
└─ styles/
```

Next.js `page.tsx`는 라우팅만 담당하고, 실제 화면 조립은
`src/features/manager/*` 아래 feature-local 구조에서 담당합니다.

## API 프록시

프론트는 Spring Boot API를 직접 호출하지 않고 Next rewrite를 통해 호출합니다.

```text
Frontend /platform-api/api/v1/**
→ Spring Boot /api/v1/**
```

기본 대상은 `http://localhost:8080`이며, 배포 또는 로컬 환경에서 다음 값으로 변경할 수 있습니다.

```bash
PLATFORM_API_BASE_URL=http://localhost:8080
```

## 현재 구현된 관리자 화면

- `/dashboard`: 관리자 대시보드 placeholder
- `/documents`: 문서 관리 placeholder
- `/ai-jobs`: AI Job 관리 placeholder
- `/system`: `GET /api/v1/system/services` 기반 서비스 상태 조회

## 검증

```bash
npm run lint
npm run build
```
