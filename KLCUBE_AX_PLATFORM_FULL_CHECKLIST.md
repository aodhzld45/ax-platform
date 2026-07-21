# KLcube AX Platform Prototype
## 전체 구현 로드맵 및 단계별 체크리스트

> 프로젝트명: `klcube-ax-prototype`  
> 목적: Java 운영 플랫폼, Python AI API, React 관리자 화면, RAG·Agent·품질관제 구조를 순차적으로 구현한다.  
> 체크 방식: 구현 및 검증이 끝난 항목은 `[x]`, 아직 진행하지 않은 항목은 `[ ]`로 관리한다.

---

# 0-A. 최종 목표 — 언어 말뭉치 기반 수어 3D 아바타 생성

모두의 말뭉치와 같은 대규모 국문 언어 데이터를 활용하여 한국어 문장의 의미와 문법 구조를 분석하고, 이를 수어 글로스와 수어 동작 데이터로 변환한다. 이후 손동작, 표정, 시선, 고개 움직임 등의 비수지 표현을 조합하여 최종적으로 3D 아바타 수어 영상을 생성하는 것을 목표로 한다.

```text
국문 말뭉치 및 문서
→ 문장 분석
→ 국문-글로스 변환
→ 수어 동작 데이터 검색·조합
→ 비수지 표현 적용
→ 3D 아바타 모션 적용
→ 수어 영상 생성
```

## 역할 분리

```text
Java Platform API
→ 문서, 파일, 도메인 Entity, AI Job, 처리 상태, 결과 파일 관리

Python AI API
→ 국문 분석, 임베딩 생성, 글로스 변환, 수어 동작 조합, 비수지 표현 매핑, AI 워크플로우 담당
```

## 핵심 변환 계층

```text
Document
→ CorpusSentence
→ CorpusEmbedding
→ GlossSequence
→ SignMotion
→ NonManualSignal
→ AvatarMotionTimeline
→ JobOutput
```

---

# 0. 현재 진행 상태

## 현재 완료된 마일스톤

- [x] Java Spring Boot 운영 API 프로젝트 생성
- [x] Java 17 실행 환경 확인
- [x] Gradle Wrapper 8.5 적용
- [x] Spring Boot 애플리케이션 실행
- [x] H2 및 JPA 초기화 확인
- [x] Java Health API 구성
- [x] 공통 파일 저장 구조 구성
- [x] Python 3.12 및 uv 기반 AI API 프로젝트 생성
- [x] FastAPI 애플리케이션 실행
- [x] Python Health API 구성
- [x] Java `RestClient` 설정
- [x] Java → Python 내부 REST 호출
- [x] Java 경유 Python Echo API 호출 테스트
- [x] Java 요청값이 Python까지 전달되고 다시 Java 응답으로 반환되는 흐름 확인

## 현재 확인된 경유 테스트 결과

```json
{
  "status": "SUCCESS",
  "result": "Python AI API received: Java 경유 Python 호출 테스트",
  "received_message": "Java 경유 Python 호출 테스트"
}
```

## 0-1. 현재 시스템 연결 상태

```text
Client 또는 Postman
        │
        ▼
Java Platform API
localhost:8080
        │
        │ RestClient
        ▼
Python AI API
localhost:8000
        │
        ▼
Python 처리 결과
        │
        ▼
Java 응답 반환
```

현재는 **Java 운영 API와 Python AI API 사이의 최소 통신 경로가 완성된 상태**다.

## 0-2. 현재 파일 저장 구조 상태

```text
현재 FileUtil은 파일을 물리 저장한 후 다음과 같은 정보를 StoredFileInfo로 반환한다.

원본 파일명
저장 파일명
저장 상대 경로
외부 접근 경로
확장자
Content-Type
파일 크기

외부 접근 경로는 다음과 같은 형태다.

/files/document/korean-source/{resourceKey}/{version}/{storedFileName}

하지만 현재는 /files/** URL과 실제 로컬 업로드 디렉터리를 연결하는 Spring MVC 설정이 아직 없다.

StoredFileInfo.storedPath
/files/...

        │
        ▼

Spring Boot ResourceHandler
아직 미구현

        │
        ▼

실제 물리 파일 경로
{file.storage.root-path}/...

따라서 현재 바로 진행할 작업은 다음과 같다.

파일 저장 구조 구성 완료
→ 실제 파일 저장 가능
→ /files/... 논리 경로 반환 가능
→ /files/** 정적 리소스 매핑
→ 브라우저 및 Postman 파일 조회 검증
→ 파일 메타데이터 DB 연결
```

---

# 1. 최종 기술 스택

| 구분 | 적용 기술 |
|---|---|
| 운영 API | Java 17, Spring Boot, Gradle 8.5 |
| Java Web | Spring Web |
| Java ORM | Spring Data JPA |
| Java 내부 통신 | Spring RestClient |
| AI API | Python 3.12, uv, FastAPI |
| AI 워크플로우 | LangGraph |
| RAG | LlamaIndex |
| AI 관측 | Langfuse |
| 프론트엔드 | React 19, Vite, TypeScript |
| 라우팅 | React Router |
| 서버 상태 | TanStack Query |
| 전역 UI 상태 | Zustand |
| HTTP 통신 | Axios |
| 초기 DB | H2 |
| 운영형 DB | PostgreSQL |
| 벡터 검색 | pgvector |
| 마이그레이션 | Flyway |
| 로컬 통합 환경 | Docker Compose |

> Spring Boot의 세부 패치 버전은 현재 생성된 `platform-api/build.gradle`을 기준으로 관리한다.  
> 프로젝트가 정상 실행 중이라면 단순히 최신 버전이라는 이유로 임의 변경하지 않는다.

---

# 2. 전체 시스템 구조

```text
React Vite
localhost:5173
       │
       │ REST API
       ▼
Java Platform API
localhost:8080
       │
       │ 내부 REST API
       ▼
Python AI API
localhost:8000
       │
       ├─ LangGraph
       ├─ LlamaIndex
       ├─ Langfuse
       ├─ 문서 파싱
       ├─ 청킹
       ├─ 임베딩
       └─ Vector Search
                │
                ▼
       PostgreSQL + pgvector
```

## 역할 분리

### Java 운영 API

```text
인증 및 권한
관리자와 사용자 관리
문서 메타데이터 관리
문서 업로드 이력
AI Job 상태 관리
업무 트랜잭션
감사 로그
Python AI API 호출
프론트엔드용 API 제공
```

### Python AI API

```text
문서 파싱
문서 청킹
임베딩
LlamaIndex 인덱싱
Vector Search
LangGraph 실행
LLM 호출
Prompt 처리
Langfuse 추적
AI 품질 평가
```

### React 프론트엔드

```text
로그인
대시보드
문서 관리
AI Job 현황
RAG 검색 테스트
Agent 실행
품질 평가 결과
운영 로그
다크모드
공통 로딩
```

---

# 3. 전체 프로젝트 구조

```text
klcube-ax-prototype/
├─ platform-api/       # Java Spring Boot
├─ ai-api/             # Python FastAPI
├─ frontend/           # React Vite
├─ infra/              # Docker, PostgreSQL 설정
├─ docs/               # 설계와 작업 문서
├─ .gitignore
└─ README.md
```

## 프로젝트 루트 구성 체크

- [x] `klcube-ax-prototype` 루트 디렉터리 생성
- [x] `platform-api` 프로젝트 배치
- [x] `ai-api` 프로젝트 배치
- [ ] `frontend` 프로젝트 생성
- [ ] `infra` 디렉터리 구성
- [ ] `docs` 디렉터리 문서 정리
- [ ] 루트 `.gitignore` 작성
- [ ] 루트 `README.md` 작성
- [ ] Git 저장소 초기화
- [ ] 최초 기준 커밋 생성

---

# 4. Phase 1 — Java 운영 API 기본 구성

## 4-1. 프로젝트 생성

- [x] Gradle Groovy 프로젝트 생성
- [x] Java 언어 선택
- [x] Java 17 선택
- [x] Group 설정: `com.hyunsuk`
- [x] Artifact 설정: `platform-api`
- [x] Package 설정: `com.hyunsuk.axplatform`
- [x] Jar Packaging 선택

## 4-2. 초기 의존성

- [x] Spring Web
- [x] Spring Data JPA
- [x] Validation
- [x] Spring Boot Actuator
- [x] H2 Database
- [x] Lombok
- [ ] PostgreSQL Driver
- [ ] Flyway
- [ ] Spring Security
- [ ] Redis
- [ ] WebSocket

## 4-3. IntelliJ 설정

- [x] IntelliJ IDEA Community에서 프로젝트 열기
- [x] Gradle 동기화 성공
- [x] Gradle JVM을 Java 17로 지정
- [x] Gradle Wrapper 사용
- [x] Annotation Processing 활성화
- [x] 애플리케이션 실행 구성 확인

## 4-4. Gradle 확인

- [x] Gradle Wrapper 8.5 적용
- [x] `gradlew.bat --version` 확인
- [x] JVM 17 확인
- [x] Gradle Build 성공
- [ ] Gradle Test 전체 성공 여부 최종 확인

## 4-5. 기본 환경설정

- [x] `spring.application.name` 설정
- [x] 서버 포트 8080 설정
- [x] H2 DataSource 설정
- [x] JPA 초기화 확인
- [x] HikariCP 연결 확인
- [x] `open-in-view: false` 설정
- [x] H2 Console 설정
- [x] Actuator Health 공개
- [ ] 환경별 프로필 분리
  - [ ] `application.yml`
  - [ ] `application-local.yml`
  - [ ] `application-dev.yml`
  - [ ] `application-prod.yml`

## 4-5-1. 공통 파일 저장 구조

- [x] `common.file` 패키지 생성
- [x] `FileUtil` 공통 파일 저장 유틸 구성
- [x] `FileStorageProperties` 업로드 경로 설정 분리
- [x] DB 저장용 파일 메타데이터 `StoredFileInfo` 구성
- [x] 파일 자산 타입 `FileAssetType` 정의
  - [x] `KOREAN_SOURCE_DOCUMENT`
  - [x] `PARALLEL_CORPUS`
  - [x] `GLOSS_DICTIONARY`
  - [x] `JOB_INTERMEDIATE`
  - [ ] `SIGN_MOTION`
  - [ ] `NON_MANUAL_MOTION`
  - [ ] `AVATAR_MODEL`
  - [ ] `JOB_OUTPUT`
- [x] AI Job 파일 단계 `JobFileStage` 정의
  - [x] `INPUT`
  - [x] `INTERMEDIATE`
  - [x] `OUTPUT`
  - [x] `LOG`
- [x] 원천 문서 저장 경로 구조 정의
  - [x] `document/korean-source/{resourceKey}/{version}`
- [x] 데이터셋 저장 경로 구조 정의
  - [x] `dataset/parallel-corpus/{resourceKey}/{version}`
  - [x] `dataset/gloss-dictionary/{resourceKey}/{version}`
- [x] AI Job 산출물 저장 경로 구조 정의
  - [x] `job/{jobId}/input`
  - [x] `job/{jobId}/intermediate`
  - [x] `job/{jobId}/output`
  - [x] `job/{jobId}/log`
- [x] UUID 기반 저장 파일명 생성
- [x] 원본 파일명, 저장 파일명, 저장 경로, 확장자, Content-Type, 파일 크기 반환
- [x] Path Traversal 방지 로직 구성
- [x] 로컬 프로토타입용 업로드 경로 설정
- [x] multipart 업로드 크기 제한 설정
- [x] `/files/**` 정적 리소스 서빙 설정
- [x] `KOREAN_SOURCE_DOCUMENT` 확장자 정책 정리
  - [x] 프로토타입 단계에서 국문 PDF 원본만 허용
  - [x] `FileUploadPolicy` 기준 확장자 `pdf` 허용
  - [x] `application/pdf` MIME Type 검증
- [x] 파일 메타데이터 DB Entity 설계
  - [x] `FileMetadata` Entity 작성
  - [x] `FileMetadataStatus` Enum 작성
  - [x] `FileMetadataRepository` 작성
  - [x] `BaseTimeEntity` 공통 시간 Entity 작성
  - [x] `JpaAuditingConfig` 작성
  - [x] `StoredFileInfo` 경로 필드 분리
    - [x] `storageRelativePath`
    - [x] `accessPath`
    - [x] 기존 호환용 `storedPath` 유지
- [x] 향후 `Document` Entity와 `FileMetadata` 1:1 연결 설계 문서화
- [x] `Document` Entity와 `FileMetadata` 1:1 참조 기반 구현
  - [x] `document.entity.Document` 작성
  - [x] `DocumentStatus` Enum 작성
  - [x] `DocumentIndexStatus` Enum 작성
  - [x] `DocumentRepository` 작성
  - [x] `resourceKey + version` 유니크 제약 구성
- [x] 장기 도메인 구조 적용
  - [x] 국문 원천 문서, 수어 데이터셋, 박물관 매뉴얼, 의료 매뉴얼 Entity가 `Document` 참조
- [x] 업로드 실패 시 DB Rollback 및 파일 정리 정책 적용
  - [x] `FileUtil.deleteByAccessPath` 작성
  - [x] DB 저장 실패 시 저장된 물리 파일 삭제 보상 처리

## 다음 작업

```text
Document 업로드 API 구현
→ POST /api/v1/documents 구현 완료
→ 국문 PDF 업로드 Request/Response DTO 작성 완료
→ KOREAN_SOURCE_DOCUMENT 파일 정책 검증 연결 완료
→ FileUtil로 물리 파일 저장 연결 완료
→ FileMetadataRepository로 파일 메타데이터 저장 연결 완료
→ DocumentRepository로 Document 저장 연결 완료
→ 업로드 실패 시 DB Rollback 및 파일 정리 정책 적용 완료
→ multipart 업로드 201 Created, /files/** 조회, 비허용 파일 400 검증 완료
→ 다음: Document 목록/상세 API 및 공통 ErrorResponse 구현
```

## 현재 프로토타입 파일 처리 흐름

```text
국문 PDF 업로드
→ document/korean-source 저장
→ Python에서 텍스트 추출
→ job/{jobId}/intermediate 파싱 결과 저장
→ 글로스 사전 또는 병렬 말뭉치 검색
→ gloss-sequence.json 생성
→ motion-sequence.json 생성
```

## 우선 지원 파일 자산 타입

```text
KOREAN_SOURCE_DOCUMENT  # 국문 PDF 원본
PARALLEL_CORPUS         # 국문-글로스 매핑 JSON/CSV
GLOSS_DICTIONARY        # 글로스 사전 JSON/CSV
JOB_INTERMEDIATE        # 파싱 결과·글로스 결과 JSON
```

## 이후 확장 파일 자산 타입

```text
SIGN_MOTION
NON_MANUAL_MOTION
AVATAR_MODEL
JOB_OUTPUT
```

## 4-6. Java Health API

- [x] `system.controller` 패키지 생성
- [x] Health Controller 생성
- [x] Health Response DTO 생성
- [x] `GET /api/v1/health` 구현
- [x] JSON 응답 확인
- [x] `GET /actuator/health` 확인
- [ ] Health Controller 테스트 작성
- [ ] `gradlew test` 성공 확인

## 완료 기준

- [x] Spring Boot가 8080 포트에서 실행된다.
- [x] H2와 JPA가 정상 초기화된다.
- [x] `/api/v1/health`가 JSON을 반환한다.
- [x] `/actuator/health`가 `UP`을 반환한다.

---

# 5. Phase 2 — Python AI API 기본 구성

## 5-1. Python 및 uv 환경

- [x] Python 3.12 설치
- [x] uv 설치
- [x] `uv init` 프로젝트 생성
- [x] `.python-version`에 Python 3.12 고정
- [x] uv 가상환경 생성
- [x] `uv run python --version` 확인
- [x] `uv.lock` 생성

## 5-2. 초기 의존성

- [x] FastAPI 설치
- [x] Uvicorn 또는 `fastapi[standard]` 설치
- [x] Pydantic Settings 설치
- [x] HTTP 통신 패키지 설치
- [ ] pytest
- [ ] pytest-asyncio
- [ ] ruff
- [ ] mypy
- [ ] LangGraph
- [ ] LlamaIndex
- [ ] Langfuse

## 5-3. 초기 구조

```text
ai-api/
├─ app/
│  ├─ main.py
│  ├─ api/
│  │  ├─ router.py
│  │  └─ routes/
│  ├─ core/
│  │  └─ config.py
│  ├─ schemas/
│  ├─ services/
│  ├─ graphs/
│  ├─ ingestion/
│  ├─ retrieval/
│  ├─ evaluators/
│  └─ observability/
├─ tests/
├─ pyproject.toml
├─ uv.lock
└─ .python-version
```

- [x] `app/main.py` 생성
- [x] FastAPI 애플리케이션 팩토리 또는 앱 객체 생성
- [x] API Router 구성
- [x] 환경설정 모듈 구성
- [x] Health Route 구성
- [x] Echo 또는 메시지 테스트 Route 구성
- [ ] 공통 예외 처리 모듈
- [ ] 공통 응답 Schema
- [ ] 테스트 디렉터리 구성
- [ ] Ruff 설정
- [ ] pytest 설정

## 5-4. FastAPI 실행

- [x] FastAPI 개발 서버 실행
- [x] 8000 포트 확인
- [x] `/api/v1/health` 확인
- [x] `/docs` Swagger 확인
- [x] `/openapi.json` 확인
- [x] Echo 테스트 API 확인

## 완료 기준

- [x] Python 3.12 환경에서 실행된다.
- [x] uv로 의존성이 관리된다.
- [x] FastAPI가 8000 포트에서 실행된다.
- [x] Health API가 정상 응답한다.
- [x] Swagger UI에 API가 표시된다.

---

# 6. Phase 3 — Java와 Python 연동

## 6-1. Java RestClient 구성

- [x] `RestClientConfig` 생성
- [x] Python AI API Base URL 설정
- [x] Connect Timeout 설정
- [x] Read Timeout 설정
- [x] UTF-8 통신 확인
- [x] `RestClient` Bean 등록
- [x] Java 서비스에서 `RestClient` 주입

## 6-2. 환경설정

예시:

```yaml
ai:
  api:
    base-url: http://localhost:8000
```

- [x] AI API Base URL 환경설정 분리
- [x] Java 실행 로그에서 Base URL 확인
- [ ] local/dev/prod 환경별 AI URL 분리
- [ ] 환경변수 기반 URL 덮어쓰기

## 6-3. Python Echo API

- [x] 요청 메시지 Schema 생성
- [x] 응답 메시지 Schema 생성
- [x] Python Echo API 구현
- [x] 전달받은 메시지 응답 반환
- [x] 한글 UTF-8 메시지 정상 처리

## 6-4. Java AI Client

- [x] Python 요청 DTO 생성
- [x] Python 응답 DTO 생성
- [x] AI API Client 또는 Service 생성
- [x] Java Controller에서 Python 호출
- [x] Python 응답을 Java 응답으로 변환
- [x] Java 경유 호출 테스트 성공

## 6-5. 현재 경유 호출 결과

```json
{
  "status": "SUCCESS",
  "result": "Python AI API received: Java 경유 Python 호출 테스트",
  "received_message": "Java 경유 Python 호출 테스트"
}
```

## 6-6. 예외 처리 보강

- [ ] Python 서버가 꺼졌을 때 연결 오류 처리
- [ ] Connect Timeout 응답 처리
- [ ] Read Timeout 응답 처리
- [ ] Python 4xx 응답 변환
- [ ] Python 5xx 응답 변환
- [ ] 공통 `ErrorResponse` 적용
- [ ] Java 로그에 요청 ID 기록
- [ ] Python 로그에 동일한 요청 ID 기록
- [ ] 민감한 요청 데이터 로그 제외

## 6-7. 통합 상태 API

예정 API:

```text
GET /api/v1/system/services
```

예상 응답:

```json
{
  "platformApi": {
    "status": "UP"
  },
  "aiApi": {
    "status": "UP",
    "latencyMs": 23
  }
}
```

- [ ] Java 자체 상태 조회
- [ ] Python Health API 호출
- [ ] 호출 Latency 계산
- [ ] Python 장애 시 `DOWN` 표시
- [ ] 통합 상태 응답 DTO 작성
- [ ] Controller 테스트 작성

## Phase 3 완료 기준

- [x] Java에서 Python API를 호출할 수 있다.
- [x] 요청 데이터가 Python에 전달된다.
- [x] Python 처리 결과가 Java로 돌아온다.
- [x] Java가 최종 JSON 응답을 반환한다.
- [ ] Python 장애 상황을 Java가 안정적으로 처리한다.
- [ ] 서비스 통합 상태 API가 완성된다.

---

# 7. Phase 4 — PostgreSQL 및 JPA 운영 기반

> 현재 경유 테스트 다음에 진행할 권장 단계

## 7-1. Docker PostgreSQL

- [ ] `infra/docker-compose.yml` 생성
- [ ] PostgreSQL 컨테이너 구성
- [ ] DB 이름 생성
- [ ] DB 사용자 생성
- [ ] DB 비밀번호 환경변수 분리
- [ ] 로컬 Volume 구성
- [ ] 컨테이너 Health Check 추가
- [ ] PostgreSQL 접속 확인

## 7-2. Java DB 전환

- [ ] PostgreSQL Driver 추가
- [ ] H2를 local test 전용으로 분리
- [ ] PostgreSQL DataSource 설정
- [ ] 환경변수 기반 DB 연결
- [ ] JPA 연결 확인
- [ ] HikariCP 연결 확인

## 7-3. Flyway

- [ ] Flyway 의존성 추가
- [ ] `db/migration` 디렉터리 생성
- [ ] `V1__create_document_table.sql`
- [ ] `V2__create_ai_job_table.sql`
- [ ] 마이그레이션 실행 확인
- [ ] 재실행 시 중복 생성 방지 확인

## 7-4. BaseEntity

- [ ] `BaseTimeEntity` 작성
- [ ] 생성일시 필드
- [ ] 수정일시 필드
- [ ] JPA Auditing 설정
- [ ] 등록자·수정자 확장 가능 구조 검토

## 완료 기준

- [ ] PostgreSQL 컨테이너가 실행된다.
- [ ] Java가 PostgreSQL에 연결된다.
- [ ] Flyway가 테이블을 생성한다.
- [ ] 애플리케이션 재시작 후 데이터가 유지된다.

---

# 8. Phase 5 — Document 도메인

## 8-1. Document 모델

- [x] `Document` Entity
- [x] `DocumentStatus` Enum
- [x] `DocumentIndexStatus` Enum
- [x] `DocumentRepository`
- [x] `Document`와 `FileMetadata` 1:1 참조 매핑
- [x] `resourceKey + version` 유니크 제약
- [x] 등록 Request DTO
- [x] 업로드 Response DTO
- [ ] 목록 Response DTO
- [ ] 상세 Response DTO
- [x] Service
- [x] Controller

## Document 주요 필드

```text
id
originalFileName
storedFileName
contentType
fileSize
storagePath
status
indexStatus
createdAt
updatedAt
```

## 8-2. Document API

- [ ] 문서 목록 API
- [ ] 문서 상세 API
- [x] 문서 업로드 API
- [ ] 문서 삭제 API
- [ ] 문서 인덱싱 재요청 API
- [ ] 페이지네이션
- [ ] 상태 필터
- [x] 파일 확장자 검증
- [ ] 파일 크기 검증

## 8-3. 파일 저장

- [x] 로컬 저장 경로 설정
- [x] UUID 파일명 생성
- [x] 원본 파일명 별도 저장
- [x] Path Traversal 방지
- [x] 업로드 실패 시 DB Rollback
- [x] DB 저장 실패 시 파일 정리
- [ ] 저장소 Interface 분리
- [ ] 이후 S3 교체 가능 구조

## 완료 기준

- [ ] Java API로 파일을 업로드할 수 있다.
- [ ] 업로드 메타데이터가 DB에 저장된다.
- [ ] 문서 목록에서 상태를 확인할 수 있다.

---

# 9. Phase 6 — AI Job 상태 관리

## 9-1. AI Job 모델

- [ ] `AiJob` Entity
- [ ] `AiJobType` Enum
- [ ] `AiJobStatus` Enum
- [ ] `AiJobRepository`
- [ ] Service
- [ ] Controller
- [ ] 상태 변경 검증

## 권장 상태

```text
PENDING
PROCESSING
COMPLETED
FAILED
RETRYING
CANCELLED
```

## 주요 필드

```text
id
jobType
referenceType
referenceId
status
progress
retryCount
requestJson
resultJson
errorCode
errorMessage
startedAt
completedAt
createdAt
updatedAt
```

## 9-2. AI Job API

- [ ] Job 생성
- [ ] Job 목록
- [ ] Job 상세
- [ ] Job 상태 갱신
- [ ] Job 실패 처리
- [ ] Job 재시도
- [ ] 진행률 표시
- [ ] 오래된 Processing Job 복구 정책

## 완료 기준

- [ ] 문서 업로드 시 AI Job이 생성된다.
- [ ] Job 상태가 단계별로 변경된다.
- [ ] 실패 사유와 재시도 횟수가 저장된다.

---

# 10. Phase 7 — Java에서 Python 문서 처리 요청

## 10-1. 처리 요청 계약

예정 API:

```text
POST /api/v1/ingestion/jobs
```

요청 예시:

```json
{
  "jobId": 1,
  "documentId": 10,
  "filePath": "/storage/documents/example.pdf",
  "metadata": {
    "domain": "HOSPITAL",
    "category": "APPOINTMENT"
  }
}
```

- [ ] Java Request DTO
- [ ] Python Request Schema
- [ ] Python Response Schema
- [ ] Java Response DTO
- [ ] OpenAPI 계약 정리
- [ ] 필드명 camelCase 정책 확정

## 10-2. 처리 흐름

- [ ] Java에서 AI Job을 `PENDING`으로 생성
- [ ] Python 인덱싱 API 호출
- [ ] Python에서 `PROCESSING` 처리
- [ ] 문서 파싱
- [ ] 청킹
- [ ] 처리 결과 생성
- [ ] Java에 완료 콜백 또는 Polling
- [ ] Java에서 `COMPLETED` 변경
- [ ] 실패 시 `FAILED` 변경

## 완료 기준

- [ ] 문서 업로드 후 Python 처리가 시작된다.
- [ ] Java에서 처리 상태를 추적할 수 있다.
- [ ] 실패 시 오류 정보가 남는다.

---

# 11. Phase 8 — Python 문서 파싱 및 청킹

## 11-1. 문서 파서

- [ ] PDF 텍스트 파서
- [ ] TXT 파서
- [ ] DOCX 파서 검토
- [ ] 확장자별 Parser Interface
- [ ] 텍스트 정규화
- [ ] 빈 페이지 제거
- [ ] 페이지 번호 보존
- [ ] 표와 본문 처리 정책 수립

## 11-2. 청킹 전략

- [ ] 고정 길이 청킹
- [ ] 문단 기반 청킹
- [ ] 제목·섹션 기반 청킹
- [ ] Overlap 설정
- [ ] 최소·최대 Chunk 길이 검증
- [ ] Chunk Metadata 생성
- [ ] Chunk ID 생성
- [ ] 원문 위치 추적

## 권장 Chunk Metadata

```json
{
  "documentId": 10,
  "documentVersion": 1,
  "page": 12,
  "section": "예약 변경",
  "domain": "HOSPITAL",
  "category": "APPOINTMENT",
  "sourceType": "MANUAL"
}
```

## 완료 기준

- [ ] 업로드 문서에서 텍스트가 추출된다.
- [ ] 청크가 생성된다.
- [ ] 청크마다 원문 위치와 메타데이터가 보존된다.

---

# 12. Phase 9 — LlamaIndex 인덱싱

## 12-1. 의존성과 설정

- [ ] LlamaIndex 설치
- [ ] Embedding Provider 설정
- [ ] LLM Provider 설정
- [ ] 환경변수 분리
- [ ] API Key Git 제외
- [ ] Provider Adapter 구조 검토

## 12-2. 인덱싱

- [ ] 파싱 결과를 LlamaIndex Document로 변환
- [ ] Node 생성
- [ ] Metadata 유지
- [ ] 임베딩 생성
- [ ] Vector Store 저장
- [ ] Document별 삭제
- [ ] 재인덱싱
- [ ] 버전 관리

## 완료 기준

- [ ] 문서가 LlamaIndex를 통해 인덱싱된다.
- [ ] 문서별로 기존 인덱스를 삭제하거나 갱신할 수 있다.
- [ ] Metadata가 검색 결과에 포함된다.

---

# 13. Phase 10 — pgvector 및 기본 Vector Search

## 13-1. pgvector

- [ ] PostgreSQL에 pgvector Extension 활성화
- [ ] Vector Column 설계
- [ ] Embedding 차원 확정
- [ ] Vector Index 생성
- [ ] 거리 연산 방식 확정
  - [ ] Cosine
  - [ ] Inner Product
  - [ ] L2

## 13-2. 검색 API

예정 API:

```text
POST /api/v1/search/vector
```

- [ ] 검색 Query 임베딩
- [ ] Top-K 검색
- [ ] Metadata Filter
- [ ] Score 반환
- [ ] Document·Page·Section 반환
- [ ] 빈 결과 처리
- [ ] 최소 Score 기준
- [ ] 검색 시간 기록

## 완료 기준

- [ ] 자연어 질문으로 유사 문서를 검색할 수 있다.
- [ ] 검색 결과에 Score와 출처가 표시된다.
- [ ] Metadata 조건 검색이 가능하다.

---

# 14. Phase 11 — Hybrid Search

## 14-1. Dense Search

- [ ] Embedding 기반 검색
- [ ] Top-K 설정
- [ ] 최소 유사도 기준
- [ ] 검색 로그 저장

## 14-2. Sparse Search

- [ ] PostgreSQL Full Text Search 또는 BM25 엔진 선정
- [ ] 키워드 검색 구현
- [ ] 형태소 분석 전략 검토
- [ ] 정확 키워드 가중치 처리

## 14-3. 결합 및 Reranking

- [ ] Dense 결과 정규화
- [ ] Sparse 결과 정규화
- [ ] Weighted Merge
- [ ] Reciprocal Rank Fusion 검토
- [ ] Reranker 적용
- [ ] 최종 Top-N 선정
- [ ] 질문 유형에 따른 비율 조정

## 완료 기준

- [ ] 의미 검색과 키워드 검색 결과를 결합한다.
- [ ] 최종 순위에 Dense·Sparse·Rerank 점수가 남는다.
- [ ] 검색 품질을 테스트 데이터로 비교할 수 있다.

---

# 15. Phase 12 — LangGraph Agentic RAG

## 15-1. State 정의

- [ ] Agent State 정의
- [ ] Query
- [ ] Normalized Query
- [ ] Intent
- [ ] Domain
- [ ] Retrieved Documents
- [ ] Generated Answer
- [ ] Evaluation Scores
- [ ] Retry Count
- [ ] Route

## 15-2. Node 구성

- [ ] Query Normalize Node
- [ ] Intent Classification Node
- [ ] Search Decision Node
- [ ] Hybrid Retrieval Node
- [ ] Retrieval Evaluation Node
- [ ] Query Rewrite Node
- [ ] Answer Generation Node
- [ ] Answer Evaluation Node
- [ ] Human Review Node
- [ ] Final Response Node

## 15-3. 분기 및 재시도

- [ ] 검색 결과 부족 시 재검색
- [ ] 답변 품질 부족 시 재생성
- [ ] Retry Count 제한
- [ ] 무한 루프 방지
- [ ] 위험 답변 Human Review
- [ ] 실패 종료 상태

## 완료 기준

- [ ] 질문에 따라 검색 여부를 판단한다.
- [ ] 검색 결과가 부족하면 Query를 수정한다.
- [ ] 답변 생성 후 품질을 평가한다.
- [ ] 반복 횟수를 제한한다.

---

# 16. Phase 13 — Prompt Engineering

## 16-1. Prompt 구조

- [ ] System Prompt
- [ ] Context Prompt
- [ ] User Prompt
- [ ] Citation 규칙
- [ ] 모르는 내용 응답 규칙
- [ ] Structured Output
- [ ] 언어 설정
- [ ] 금지 표현

## 16-2. Prompt 버전 관리

- [ ] Prompt Template 테이블
- [ ] Prompt Version 테이블
- [ ] 활성 버전 지정
- [ ] 변경 이력
- [ ] Rollback
- [ ] 실행 시 Prompt Version 기록

## 완료 기준

- [ ] Prompt 변경 이력이 저장된다.
- [ ] Agent 실행 결과에 Prompt Version이 연결된다.
- [ ] 이전 Prompt로 되돌릴 수 있다.

---

# 17. Phase 14 — Langfuse 관측

## 17-1. Langfuse 연결

- [ ] Langfuse 프로젝트 생성
- [ ] Public Key 설정
- [ ] Secret Key 설정
- [ ] Host 설정
- [ ] 환경변수 적용
- [ ] 연결 테스트

## 17-2. Trace 구조

```text
Trace
├─ query-normalization
├─ intent-classification
├─ dense-retrieval
├─ sparse-retrieval
├─ reranking
├─ answer-generation
└─ answer-evaluation
```

- [ ] Agent 실행 단위 Trace
- [ ] Node 단위 Span
- [ ] 입력·출력 기록
- [ ] Token 사용량
- [ ] 비용
- [ ] Latency
- [ ] 검색 Chunk ID
- [ ] Prompt Version
- [ ] 평가 점수
- [ ] 오류 내용

## 완료 기준

- [ ] 한 번의 Agent 실행을 Trace로 확인할 수 있다.
- [ ] 어느 Node에서 시간이 오래 걸렸는지 확인할 수 있다.
- [ ] Prompt와 검색 결과를 비교할 수 있다.

---

# 18. Phase 15 — AI 품질 평가

## 18-1. 평가 항목

- [ ] Groundedness
- [ ] Answer Relevance
- [ ] Citation Accuracy
- [ ] Completeness
- [ ] Format Compliance
- [ ] Risk Level
- [ ] Hallucination 여부

## 18-2. 평가 정책

예시:

```text
0.8 이상
→ PASS

0.6 이상 0.8 미만
→ REGENERATE

0.6 미만
→ HUMAN_REVIEW
```

- [ ] 기준 점수 확정
- [ ] 재생성 횟수 제한
- [ ] Human Review 상태
- [ ] 평가 결과 DB 저장
- [ ] Langfuse Score 기록
- [ ] 테스트 데이터셋 구성

## 완료 기준

- [ ] 생성된 답변을 자동 평가한다.
- [ ] 품질이 낮으면 재생성한다.
- [ ] 위험 결과는 사람 검수로 보낸다.

---

# 19. Phase 16 — React 19 + Vite 프로젝트

## 19-1. 프로젝트 생성

- [ ] Node.js 버전 확인
- [ ] React Vite TypeScript 프로젝트 생성
- [ ] React 19 확인
- [ ] Vite 실행 확인
- [ ] ESLint 확인
- [ ] Build 확인

## 19-2. 의존성

- [ ] React Router
- [ ] TanStack Query
- [ ] Axios
- [ ] Zustand
- [ ] lucide-react
- [ ] 스타일 방식 확정
- [ ] Toast 라이브러리 검토

## 19-3. 기본 구조

```text
frontend/src/
├─ app/
│  ├─ App.tsx
│  ├─ router.tsx
│  └─ providers.tsx
├─ api/
│  └─ client.ts
├─ components/
│  ├─ layout/
│  ├─ ui/
│  └─ feedback/
├─ features/
│  ├─ dashboard/
│  ├─ documents/
│  ├─ ai-jobs/
│  ├─ rag-search/
│  ├─ agents/
│  ├─ evaluations/
│  └─ operations/
├─ stores/
├─ styles/
├─ lib/
├─ types/
└─ main.tsx
```

- [ ] App Provider 구성
- [ ] QueryClient 구성
- [ ] Router 구성
- [ ] Axios Client 구성
- [ ] 공통 Layout 구성
- [ ] 404 화면
- [ ] Error Boundary 검토

## 완료 기준

- [ ] React가 5173 포트에서 실행된다.
- [ ] Java API를 호출할 수 있다.
- [ ] 기본 관리자 Layout이 표시된다.

---

# 20. Phase 17 — Zustand 전역 상태

## 20-1. Theme

- [ ] `themeStore` 생성
- [ ] Light/Dark 상태
- [ ] Local Storage Persist
- [ ] 새로고침 후 테마 유지
- [ ] HTML Dataset 또는 Class 적용

## 20-2. Global Loading

- [ ] `uiStore` 생성
- [ ] `loadingCount` 적용
- [ ] `startLoading`
- [ ] `stopLoading`
- [ ] `resetLoading`
- [ ] Global Spinner 구성
- [ ] 중복 요청 시 스피너 오작동 방지

## 20-3. Sidebar 및 공통 UI

- [ ] Sidebar 상태
- [ ] 공통 Modal 상태
- [ ] Toast 상태
- [ ] 모바일 Sidebar 대응

## 원칙

```text
Zustand
→ 로그인 화면 상태, 테마, Sidebar, 공통 UI

TanStack Query
→ 서버에서 조회한 데이터와 Mutation 상태
```

---

# 21. Phase 18 — 인증 및 권한

## 21-1. Java 인증

- [ ] Spring Security 추가
- [ ] 관리자 Entity
- [ ] 권한 Entity
- [ ] 로그인 API
- [ ] 로그아웃 API
- [ ] Refresh API
- [ ] 현재 사용자 API
- [ ] 메뉴 권한 API

## 21-2. Token 정책

```text
Access Token
→ 프론트 메모리

Refresh Token
→ HttpOnly Cookie
```

- [ ] Access Token 발급
- [ ] Refresh Token 발급
- [ ] Refresh Token Cookie
- [ ] Access Token 재발급
- [ ] 로그아웃 시 Refresh 폐기
- [ ] Token 만료 처리

## 21-3. React 인증 상태

- [ ] `authStore` 생성
- [ ] 현재 사용자 복원
- [ ] 권한 복원
- [ ] Protected Route
- [ ] 401 Refresh 1회
- [ ] Refresh 실패 시 로그인 이동
- [ ] 권한 없는 메뉴 숨김
- [ ] 권한 없는 버튼 숨김

## 완료 기준

- [ ] 로그인 상태가 새로고침 후 복원된다.
- [ ] 권한에 따라 메뉴가 달라진다.
- [ ] 인증 만료를 안전하게 처리한다.

---

# 22. Phase 19 — 프론트 주요 화면

## 22-1. Dashboard

- [ ] 전체 문서 수
- [ ] 인덱싱 성공·실패
- [ ] 진행 중 AI Job
- [ ] Agent 실행 횟수
- [ ] 평균 Latency
- [ ] 최근 오류

## 22-2. Document

- [ ] 문서 목록
- [ ] 문서 업로드 Modal
- [ ] 상태 Badge
- [ ] 상세 Panel
- [ ] 인덱싱 재시도
- [ ] 문서 삭제

## 22-3. AI Job

- [ ] Job 목록
- [ ] 상태 Filter
- [ ] 진행률
- [ ] 실패 메시지
- [ ] 재시도
- [ ] Polling

## 22-4. RAG Search

- [ ] 질문 입력
- [ ] Vector 검색 결과
- [ ] Hybrid 검색 결과
- [ ] Score 표시
- [ ] 원문 Chunk
- [ ] Metadata 표시

## 22-5. Agent

- [ ] Agent 질문 입력
- [ ] 답변 결과
- [ ] Citation
- [ ] Node 진행 상태
- [ ] 평가 점수
- [ ] Trace 링크

## 22-6. Evaluation

- [ ] Prompt 버전 비교
- [ ] Groundedness 통계
- [ ] Relevance 통계
- [ ] 실패 사례
- [ ] Human Review 목록

---

# 23. Phase 20 — 공통 오류 및 응답 구조

## Java 공통 응답

- [ ] `ApiResponse<T>`
- [ ] `ErrorResponse`
- [ ] Error Code Enum
- [ ] Global Exception Handler
- [ ] Validation Error 처리
- [ ] AI API Error 처리

## Python 공통 응답

- [ ] Pydantic Base Response
- [ ] Error Response Schema
- [ ] Exception Handler
- [ ] Validation Error 변환
- [ ] Trace ID 포함

## 공통 요청 ID

- [ ] Java에서 Request ID 생성
- [ ] Python 호출 Header에 전달
- [ ] Python 로그에 Request ID 기록
- [ ] 응답 Header 또는 Body에 Request ID 포함
- [ ] Langfuse Trace와 연결

---

# 24. Phase 21 — 테스트

## Java

- [ ] Controller Test
- [ ] Service Unit Test
- [ ] Repository Test
- [ ] RestClient Mock Test
- [ ] Python 장애 테스트
- [ ] 파일 업로드 테스트
- [ ] AI Job 상태 테스트

## Python

- [ ] Health API 테스트
- [ ] Echo API 테스트
- [ ] Parser 테스트
- [ ] Chunker 테스트
- [ ] Retriever 테스트
- [ ] LangGraph Node 테스트
- [ ] Graph 분기 테스트
- [ ] 평가기 테스트

## Frontend

- [ ] Build
- [ ] Lint
- [ ] API Service 테스트 검토
- [ ] 주요 UI 상태 확인
- [ ] Loading
- [ ] Empty
- [ ] Error
- [ ] Success
- [ ] 권한 없음

---

# 25. Phase 22 — Docker Compose 통합

## 컨테이너 후보

```text
postgres
platform-api
ai-api
frontend
```

- [ ] PostgreSQL Dockerfile 또는 Image 구성
- [ ] Java Dockerfile
- [ ] Python Dockerfile
- [ ] Frontend Dockerfile
- [ ] Docker Compose
- [ ] 내부 Network
- [ ] 환경변수
- [ ] Volume
- [ ] Health Check
- [ ] 서비스 시작 순서
- [ ] 로컬 전체 실행 확인

## 완료 기준

- [ ] `docker compose up`으로 전체 서비스가 실행된다.
- [ ] Java에서 Docker 내부 Python 주소로 호출한다.
- [ ] PostgreSQL 데이터가 Volume에 유지된다.

---

# 26. Phase 23 — 운영 문서화

## 필수 문서

- [ ] 루트 README
- [ ] Java 실행 가이드
- [ ] Python 실행 가이드
- [ ] Frontend 실행 가이드
- [ ] 환경변수 목록
- [ ] API 계약 문서
- [ ] 데이터베이스 ERD
- [ ] RAG Pipeline 문서
- [ ] LangGraph 상태도
- [ ] 장애 대응 문서
- [ ] Prompt 평가 정책
- [ ] Docker 실행 가이드

## README 필수 내용

```text
프로젝트 목적
기술 스택
전체 구조
로컬 실행 순서
환경변수
주요 API
현재 구현 범위
향후 로드맵
```

---

# 27. Phase 24 — STT 및 수어 연계 확장

> RAG·Agent 플랫폼 기본 기능이 안정화된 후 진행한다.

## STT

- [ ] STT Provider Interface
- [ ] 음성 파일 업로드
- [ ] 마이크 입력
- [ ] STT 결과 저장
- [ ] 상담 세션 연결
- [ ] STT Latency 기록

## 수어 콘텐츠

- [ ] 수어 문장 Entity
- [ ] Gloss Entity
- [ ] 수어 자산 Entity
- [ ] 검수 상태
- [ ] 문장 검색
- [ ] Gloss 후보 검색
- [ ] 영상 또는 애니메이션 재생
- [ ] 미등록 문장 Human Review

---

# 28. Phase 25 — 언어 말뭉치 및 임베딩 적재

## 28-1. 말뭉치 데이터 모델

- [ ] `CorpusDocument` Entity 설계
- [ ] `CorpusSentence` Entity 설계
- [ ] `CorpusEmbedding` Entity 설계
- [ ] 문서·문단·문장 단위 식별자 설계
- [ ] 문장 원문 `rawText` 저장
- [ ] 정규화 문장 `normalizedText` 저장
- [ ] 도메인 메타데이터 저장
  - [ ] 국문 원천 문서
  - [ ] 박물관 안내 매뉴얼
  - [ ] 의료 매뉴얼
  - [ ] 수어 데이터셋
- [ ] 문장 위치 메타데이터 저장
  - [ ] page
  - [ ] section
  - [ ] paragraphIndex
  - [ ] sentenceIndex

## 28-2. 임베딩 생성 및 적재

- [ ] Python AI API 임베딩 생성 API 설계
- [ ] Embedding Provider 확정
- [ ] Embedding 차원 확정
- [ ] `pgvector` 컬럼 설계
- [ ] CorpusSentence별 embedding 저장
- [ ] embedding model/version 저장
- [ ] 재임베딩 정책 설계
- [ ] 문서 삭제 시 embedding 삭제 정책 설계

## 28-3. 검색 기준

- [ ] 문장 임베딩 유사도 검색
- [ ] 도메인 필터 검색
- [ ] 문서/섹션 메타데이터 필터
- [ ] 최소 유사도 기준 설정
- [ ] Top-K 검색 결과 반환

---

# 29. Phase 26 — 국문-글로스 변환 데이터

## 29-1. 글로스 사전

- [ ] `GlossDictionary` Entity 설계
- [ ] `glossCode` 고유키 설계
- [ ] 국문 의미 매핑
- [ ] 품사 및 문법 역할 저장
- [ ] 도메인별 의미 차이 저장
- [ ] 동의어/유의어 매핑

## 29-2. 병렬 말뭉치

- [ ] `ParallelCorpus` Entity 설계
- [ ] 국문 문장 저장
- [ ] 글로스 시퀀스 저장
- [ ] 도메인 메타데이터 저장
- [ ] 변환 신뢰도 저장
- [ ] 검수 상태 저장

## 29-3. 글로스 시퀀스 생성

- [ ] 국문 문장 분석 결과 수신
- [ ] 병렬 말뭉치 유사 예문 검색
- [ ] 글로스 사전 후보 검색
- [ ] 규칙 기반 어순 변환
- [ ] LLM 기반 후보 보정
- [ ] `gloss-sequence.json` 산출물 생성
- [ ] Job intermediate 파일로 저장

---

# 30. Phase 27 — 수어 모션 및 비수지 표현

## 30-1. 수어 모션 자산

- [ ] `SignMotion` Entity 설계
- [ ] `motionId` 고유키 설계
- [ ] `glossCode`와 모션 연결
- [ ] 손 모양 정보 저장
- [ ] 움직임 유형 저장
- [ ] 모션 파일 `FileMetadata` 연결
- [ ] 모션 duration 저장

## 30-2. 비수지 표현

- [ ] `NonManualSignal` Entity 설계
- [ ] 표정 유형 저장
- [ ] 시선 방향 저장
- [ ] 고개 움직임 저장
- [ ] 문장 유형별 비수지 규칙 설계
  - [ ] 요청
  - [ ] 질문
  - [ ] 부정
  - [ ] 강조
- [ ] 비수지 표현 파일 `FileMetadata` 연결

## 30-3. 모션 시퀀스 생성

- [ ] 글로스별 SignMotion 검색
- [ ] 비수지 표현 후보 매핑
- [ ] 모션 duration 기반 timeline 생성
- [ ] motion blending 규칙 설계
- [ ] `motion-sequence.json` 산출물 생성
- [ ] Job intermediate 파일로 저장

---

# 31. Phase 28 — 3D 아바타 타임라인 및 영상 생성

## 31-1. 아바타 모델

- [ ] `AvatarModel` Entity 설계
- [ ] 기본 아바타 모델 등록
- [ ] GLB/FBX 파일 `FileMetadata` 연결
- [ ] skeleton/rig 호환성 정보 저장
- [ ] avatar version 관리

## 31-2. 아바타 타임라인

- [ ] `AvatarMotionTimeline` 산출물 포맷 설계
- [ ] SignMotion clip 연결
- [ ] NonManualSignal clip 연결
- [ ] startMs/endMs 계산
- [ ] animation blending 구간 계산
- [ ] `avatar-timeline.json` 생성

## 31-3. 3D 엔진 연동

- [ ] 3D 엔진 후보 확정
  - [ ] Blender
  - [ ] Unity
  - [ ] Unreal
  - [ ] Web 기반 Three.js
- [ ] Python AI API에서 렌더링 Job 요청
- [ ] 아바타 엔진에 timeline 전달
- [ ] 렌더링 결과 mp4/webm 생성
- [ ] `JOB_OUTPUT` 파일 저장
- [ ] 최종 영상 accessPath 반환

---

# 32. Phase 29 — End-to-End 수어 영상 생성 Job

## 32-1. Job 파이프라인

- [ ] Document 업로드 후 AI Job 생성
- [ ] Python 텍스트 추출 요청
- [ ] 문장 분석 결과 저장
- [ ] 글로스 변환 결과 저장
- [ ] 모션 시퀀스 결과 저장
- [ ] 아바타 타임라인 결과 저장
- [ ] 영상 생성 결과 저장
- [ ] Job 상태 전이 관리

## 32-2. 최종 산출물

- [ ] `gloss-sequence.json`
- [ ] `motion-sequence.json`
- [ ] `avatar-timeline.json`
- [ ] `sign-video.mp4`
- [ ] 생성 결과 메타데이터 저장
- [ ] 프론트엔드에서 영상 재생

---

# 33. 현재 기준 다음 작업

현재 공통 파일 저장 구조, 파일 정책, 정적 리소스 서빙, 파일 메타데이터 Entity 설계가 완료되었으므로 다음 순서로 진행한다.

## 가장 먼저 진행

### 1순위 — Document 업로드 API와 FileMetadata 저장 연결

- [x] `Document` Entity 설계
- [x] `DocumentStatus` Enum 작성
- [x] `DocumentIndexStatus` Enum 작성
- [x] `DocumentRepository` 작성
- [x] `Document`에서 `FileMetadata` 1:1 참조
- [x] 국문 PDF 업로드 Request/Response DTO 작성
- [x] `FileUploadValidator`로 `KOREAN_SOURCE_DOCUMENT` 정책 검증
- [x] `FileUtil`로 원천 문서 저장
- [x] `FileMetadataRepository`로 파일 메타데이터 저장
- [x] `DocumentRepository`로 Document 저장
- [x] 업로드 실패 시 DB Rollback 및 저장 파일 정리
- [x] 실제 multipart 업로드 요청으로 `201 Created` 검증
- [x] 응답 `accessPath`로 `/files/**` 파일 조회 검증
- [x] PDF 이외 파일 업로드 시 400 오류 검증

### 도메인별 Document 참조 구조

- [x] `KoreanSourceDocument` Entity 작성
- [x] `KoreanSourceDocumentRepository` 작성
- [x] `SignLanguageDataset` Entity 작성
- [x] `SignLanguageDatasetRepository` 작성
- [x] `MuseumManual` Entity 작성
- [x] `MuseumManualRepository` 작성
- [x] `MedicalManual` Entity 작성
- [x] `MedicalManualRepository` 작성
- [x] 각 도메인 Entity에서 `Document` 1:1 참조

### 다음 작업

- [ ] Document 목록 API 구현
- [ ] Document 상세 API 구현
- [ ] 국문 원천 문서 도메인 업로드 흐름에서 `KoreanSourceDocument` 생성 연결
- [ ] 도메인별 등록 API 설계
- [ ] 공통 ErrorResponse 적용
- [ ] 업로드 파일 크기 정책 검증 보강

### 2순위 — Java↔Python 연동 안정화

- [ ] Python 서버 중지 시 예외 처리
- [ ] Timeout 처리
- [ ] 공통 Error Response
- [ ] 통합 서비스 상태 API
- [ ] 요청 ID 전달

### 3순위 — PostgreSQL 및 Flyway

- [ ] Docker PostgreSQL
- [ ] PostgreSQL Driver
- [ ] Flyway
- [x] BaseEntity

### 4순위 — AI Job

- [ ] AiJob Entity
- [ ] 인덱싱 Job 생성

### 5순위 — Python 문서 파싱

- [ ] 파서
- [ ] 청킹
- [ ] 결과 반환

---

# 34. 전체 마일스톤

## Milestone 1 — 서비스 실행

- [x] Java 실행
- [x] Python 실행
- [ ] React 실행

## Milestone 2 — 서비스 통신

- [x] Java → Python 호출
- [x] 요청·응답 전달
- [ ] 장애·Timeout 처리
- [ ] 통합 상태 API

## Milestone 3 — 운영 데이터

- [ ] PostgreSQL
- [ ] Flyway
- [ ] Document
- [ ] AI Job

## Milestone 4 — 문서 처리

- [ ] 업로드
- [ ] 파싱
- [ ] 청킹
- [ ] 인덱싱

## Milestone 5 — 검색

- [ ] Vector Search
- [ ] Metadata Filter
- [ ] Hybrid Search
- [ ] Reranker

## Milestone 6 — Agent

- [ ] LangGraph
- [ ] Query Rewrite
- [ ] 답변 생성
- [ ] 품질 평가

## Milestone 7 — 관측

- [ ] Langfuse Trace
- [ ] Token
- [ ] Cost
- [ ] Latency
- [ ] Prompt Version

## Milestone 8 — 관리자 화면

- [ ] Dashboard
- [ ] Document
- [ ] AI Job
- [ ] RAG Search
- [ ] Agent
- [ ] Evaluation

## Milestone 9 — 확장

- [ ] STT
- [ ] 상담 Assist
- [ ] 수어 콘텐츠 검색
- [ ] 운영 지표

## Milestone 10 — 수어 3D 아바타 생성

- [ ] 언어 말뭉치 임베딩 적재
- [ ] 국문-글로스 변환
- [ ] 수어 모션 데이터 연결
- [ ] 비수지 표현 적용
- [ ] 3D 아바타 타임라인 생성
- [ ] 수어 영상 생성

---

# 35. 최종 목표 문장

프로젝트가 완료되면 다음과 같이 설명할 수 있어야 한다.

> Java Spring Boot는 인증, 문서, AI Job, 운영 로그 등 안정성이 필요한 플랫폼 기능을 담당하고, Python FastAPI는 문서 파싱, LlamaIndex 검색, LangGraph Agent, Langfuse 품질 관측을 담당하도록 분리했다. React 관리자 화면에서는 문서 업로드부터 검색, Agent 실행, 평가 결과와 장애 상태까지 통합 관리할 수 있다.

---


최종적으로는 대규모 국문 말뭉치와 도메인 문서를 기반으로 한국어 문장을 분석하고, 국문-글로스 변환, 수어 모션 조합, 비수지 표현 적용, 3D 아바타 타임라인 생성을 거쳐 수어 영상을 생성할 수 있어야 한다.

---

# 36. 문서 업데이트 규칙

작업이 끝날 때마다 다음 규칙으로 체크한다.

```text
구현만 완료
→ 아직 체크하지 않는다.

로컬 실행 및 API 확인 완료
→ 해당 구현 항목 체크

정상·오류 시나리오 확인 완료
→ 완료 기준 체크

테스트 코드까지 통과
→ 테스트 항목 체크
```

예시:

```markdown
- [x] Java → Python Echo 호출
- [ ] Python 서버 중지 시 예외 처리
```

이 문서를 프로젝트의 진행 현황 기준 문서로 사용하며, 각 Phase를 완료할 때마다 체크박스를 갱신한다.
