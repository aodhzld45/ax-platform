# AX 플랫폼 파일 저장 구조 후속 도메인·API 설계서

> 국문 원천 문서 등록부터 파일 저장, DB 메타데이터 기록, AI Job 생성, Python 처리 요청까지 연결하기 위한 후속 설계안

- **프로젝트**: AX 플랫폼 프로토타입
- **처리 목표**: 국문 → 글로스 → 수어 동작 조합 → 비수지 표현 → 3D 아바타 결과
- **선행 문서**: `AX플랫폼_국문-글로스-수어아바타_파일저장구조_설계서`
- **현재 우선 구현 대상**: `KoreanSourceDocument` 도메인 등록 API
- **작성일**: 2026-07-24

---

## 1. 문서 목적

선행 설계서는 다음 항목을 정의한다.

- `/home/upload/ax-platform` 기준의 파일 저장 루트
- 국문 문서, 데이터셋, 글로스 사전, 수어 모션, 아바타, AI Job 파일 분류
- `FileAssetType`, `JobFileStage`, `StoredFileInfo`, `FileUtil` 책임
- `document`, `dataset`, `motion`, `avatar`, `job` 디렉터리 구조
- 공통 `file_asset` 메타데이터와 파일 수명주기

본 문서는 위 저장 구조를 실제 애플리케이션 기능으로 연결하기 위해 추가로 필요한 설계를 정의한다.

1. `KoreanSourceDocument` 도메인과 상태 전이
2. 국문 원천 문서 등록 API
3. `Document`와 `FileAsset`의 관계
4. 파일 저장과 DB 저장의 정합성 보장
5. 파일 조회·다운로드 접근 방식
6. `AiJob` 생성 및 Python API 요청 계약
7. 삭제·재처리·장애 복구 정책
8. 공통 예외와 테스트 완료 기준

> 본 설계는 입사 전 프로토타입 기준의 제안안이며, 케이엘큐브 내부의 실제 데이터 모델·수어 변환 엔진·3D 렌더링 규칙을 단정하지 않는다.

---

## 2. 후속 설계가 필요한 이유

물리 파일 저장 규칙만으로는 다음 문제를 해결할 수 없다.

| 문제 | 필요한 설계 |
|---|---|
| 파일은 저장됐지만 DB 저장이 실패한 경우 | 보상 삭제 및 트랜잭션 경계 |
| DB에는 문서가 있으나 실제 파일이 없는 경우 | 무결성 점검 및 상태 복구 |
| `/files/**` URL을 누구나 직접 조회하는 문제 | 다운로드 권한 검증 API |
| 같은 파일이 여러 번 업로드되는 문제 | SHA-256 기반 중복 정책 |
| Python 처리가 중복 요청되는 문제 | `jobId` 및 멱등성 정책 |
| 처리 실패 후 어디서 재시작할지 알 수 없는 문제 | Job 단계·중간 산출물·재시도 정책 |
| 문서 삭제 시 파일과 Job의 처리 기준이 불명확한 문제 | 논리 삭제와 물리 삭제 분리 |

따라서 다음 구조로 책임을 분리한다.

```text
Controller
  └─ 요청 검증 및 응답 변환
       ↓
KoreanSourceDocumentService
  ├─ 문서 상태 관리
  ├─ FileAsset 관계 관리
  ├─ DB 트랜잭션 제어
  └─ 실패 시 파일 보상 삭제
       ↓
FileStorageService / FileUtil
  ├─ 안전한 경로 생성
  ├─ 물리 파일 저장·삭제
  └─ StoredFileInfo 반환
       ↓
AiJobService
  ├─ Job 생성
  ├─ Python 요청
  └─ 처리 상태 추적
```

---

## 3. 도메인 경계

### 3.1 주요 도메인

| 도메인 | 책임 |
|---|---|
| `KoreanSourceDocument` | 국문 원천 문서의 업무 상태와 표시 정보 관리 |
| `FileAsset` | 실제 파일의 경로·크기·MIME·해시·상태 관리 |
| `AiJob` | 문서 처리 요청, 진행 상태, 재시도, 실패 사유 관리 |
| `AiJobFile` 또는 `FileAsset.ownerType=JOB` | Job 입력·중간 산출물·결과 파일 연결 |

### 3.2 권장 관계

```text
KoreanSourceDocument 1 ─── 1 FileAsset(original)
KoreanSourceDocument 1 ─── N AiJob
AiJob               1 ─── N FileAsset(input/intermediate/output/log)
```

프로토타입에서는 `KoreanSourceDocument.fileAssetId`로 단순 연결할 수 있다. 이후 문서 한 건에 원본·미리보기·변환본 등 여러 파일이 필요해지면 별도의 연결 테이블을 추가한다.

```text
document_file
- id
- document_id
- file_asset_id
- file_role: ORIGINAL | PREVIEW | CONVERTED | ATTACHMENT
- created_at
```

---

## 4. KoreanSourceDocument 설계

### 4.1 엔티티 필드

| 필드 | 타입 예시 | 설명 |
|---|---|---|
| `id` | `BIGINT` | 내부 PK |
| `documentKey` | `VARCHAR(50)` | 외부 노출용 식별자, 예: `DOC_20260724_0001` |
| `title` | `VARCHAR(200)` | 화면 표시 제목 |
| `description` | `VARCHAR(1000)` | 문서 설명 |
| `documentType` | `VARCHAR(30)` | PDF, HWP, HWPX, DOCX, TXT 등 |
| `status` | `VARCHAR(30)` | 문서 상태 |
| `originalFileAssetId` | `BIGINT` | 원본 파일 자산 ID |
| `latestJobId` | `BIGINT` | 가장 최근 AI Job ID, 선택값 |
| `createdBy` | `BIGINT` | 등록 사용자 ID |
| `createdAt` | `TIMESTAMP` | 생성일시 |
| `updatedAt` | `TIMESTAMP` | 수정일시 |
| `deletedAt` | `TIMESTAMP` | 논리 삭제일시 |

### 4.2 문서 상태

```java
public enum KoreanSourceDocumentStatus {
    UPLOADING,
    UPLOADED,
    PROCESSING,
    READY,
    FAILED,
    DELETED
}
```

### 4.3 상태 전이

```text
[등록 요청]
    ↓
UPLOADING
    ├─ 파일·DB 저장 성공 → UPLOADED
    └─ 저장 실패          → FAILED 또는 레코드 제거

UPLOADED
    └─ AI 처리 요청       → PROCESSING

PROCESSING
    ├─ 처리 완료          → READY
    └─ 처리 실패          → FAILED

FAILED
    ├─ 재처리 요청        → PROCESSING
    └─ 관리자 삭제        → DELETED

READY
    ├─ 재처리 요청        → PROCESSING
    └─ 관리자 삭제        → DELETED
```

문서 상태는 문서의 대표 상태만 보여준다. 상세 처리 단계는 `AiJob.status`와 `AiJob.currentStage`에서 관리한다.

### 4.4 엔티티 예시

```java
@Entity
@Table(name = "korean_source_document")
@Getter
@Setter
@ToString(exclude = {"originalFileAsset", "latestJob"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KoreanSourceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String documentKey;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KoreanSourceDocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KoreanSourceDocumentStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_file_asset_id")
    private FileAsset originalFileAsset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "latest_job_id")
    private AiJob latestJob;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
```

> 양방향 연관관계는 프로토타입 단계에서 최소화한다. 목록 조회 성능과 JSON 순환 참조를 피하기 위해 필요한 방향만 매핑한다.

---

## 5. FileAsset 상세 설계

### 5.1 권장 필드

```text
file_asset
- id
- asset_type
- owner_type
- owner_id
- file_role
- original_filename
- stored_filename
- stored_path
- extension
- content_type
- file_size
- sha256
- version
- status
- created_at
- deleted_at
```

### 5.2 상태

```java
public enum FileAssetStatus {
    TEMPORARY,
    ACTIVE,
    DELETE_PENDING,
    DELETED,
    FAILED
}
```

### 5.3 경로 저장 기준

DB에는 서버 절대 경로가 아니라 저장 루트 기준 상대 경로를 저장한다.

```text
권장 DB 값
- document/korean-source/DOC_21/original/UUID.pdf

외부 응답 URL
- /api/v1/files/{fileAssetId}/download

서버 내부 절대 경로
- /home/upload/ax-platform/document/korean-source/DOC_21/original/UUID.pdf
```

`storedPath`에 `/files/...` URL과 파일 시스템 상대 경로가 혼재하면 운영 환경 변경이 어려워진다. 다음과 같이 분리하는 것을 권장한다.

```java
private String storagePath;   // 파일 시스템 상대 경로
private String publicUrl;     // 필요 시 응답 단계에서 계산
```

기존 `StoredFileInfo.storedPath`를 유지한다면, 값의 의미를 **파일 시스템 상대 경로**로 명확히 고정한다.

---

## 6. 국문 원천 문서 등록 API

### 6.1 API 목록

| 기능 | Method | URL |
|---|---|---|
| 문서 등록 | `POST` | `/api/v1/korean-source-documents` |
| 문서 목록 | `GET` | `/api/v1/korean-source-documents` |
| 문서 상세 | `GET` | `/api/v1/korean-source-documents/{documentId}` |
| 원본 다운로드 | `GET` | `/api/v1/korean-source-documents/{documentId}/file` |
| AI 처리 요청 | `POST` | `/api/v1/korean-source-documents/{documentId}/jobs` |
| 재처리 요청 | `POST` | `/api/v1/korean-source-documents/{documentId}/reprocess` |
| 문서 삭제 | `DELETE` | `/api/v1/korean-source-documents/{documentId}` |

### 6.2 등록 요청

```http
POST /api/v1/korean-source-documents
Content-Type: multipart/form-data
```

| 파트 | 필수 | 설명 |
|---|---:|---|
| `file` | Y | 국문 원천 파일 |
| `title` | Y | 화면 표시 제목 |
| `description` | N | 문서 설명 |
| `startProcessing` | N | 저장 직후 AI Job 생성 여부, 기본값 `false` |

Spring DTO 예시:

```java
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KoreanSourceDocumentCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    private boolean startProcessing;
}
```

Controller 예시:

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<KoreanSourceDocumentCreateResponse> create(
        @Valid @RequestPart("request") KoreanSourceDocumentCreateRequest request,
        @RequestPart("file") MultipartFile file
) {
    KoreanSourceDocumentCreateResponse response =
            koreanSourceDocumentService.create(request, file);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### 6.3 등록 응답

```json
{
  "documentId": 21,
  "documentKey": "DOC_20260724_0001",
  "title": "민원 안내 국문 문서",
  "status": "UPLOADED",
  "file": {
    "fileAssetId": 83,
    "originalFilename": "민원안내.pdf",
    "contentType": "application/pdf",
    "fileSize": 248021,
    "downloadUrl": "/api/v1/files/83/download"
  },
  "job": null,
  "createdAt": "2026-07-24T13:30:00"
}
```

`startProcessing=true`인 경우 응답에 생성된 Job 정보를 포함한다.

```json
{
  "jobId": "JOB_20260724_0001",
  "status": "PENDING"
}
```

---

## 7. 등록 서비스 처리 흐름

### 7.1 기본 처리 순서

```text
1. 요청 메타데이터 검증
2. 파일 확장자·크기·MIME 검증
3. KoreanSourceDocument를 UPLOADING 상태로 저장
4. documentKey 생성
5. FileUtil.saveAsset(...) 실행
6. StoredFileInfo 반환
7. FileAsset를 ACTIVE 상태로 저장
8. Document와 FileAsset 연결
9. Document 상태를 UPLOADED로 변경
10. startProcessing=true이면 AiJob 생성
11. API 응답 반환
```

### 7.2 서비스 의사 코드

```java
@Transactional
public KoreanSourceDocumentCreateResponse create(
        KoreanSourceDocumentCreateRequest request,
        MultipartFile file
) {
    validateUploadFile(file);

    KoreanSourceDocument document = createUploadingDocument(request, file);
    documentRepository.save(document);

    StoredFileInfo storedFile = null;

    try {
        storedFile = fileUtil.saveAsset(
                FileAssetType.KOREAN_SOURCE_DOCUMENT,
                document.getDocumentKey(),
                "original",
                file
        );

        FileAsset fileAsset = fileAssetService.createOriginalFileAsset(
                document,
                storedFile
        );

        document.setOriginalFileAsset(fileAsset);
        document.setStatus(KoreanSourceDocumentStatus.UPLOADED);

        AiJob job = null;
        if (request.isStartProcessing()) {
            job = aiJobService.createPendingJob(document);
            document.setLatestJob(job);
        }

        return responseMapper.toCreateResponse(document, job);

    } catch (RuntimeException exception) {
        if (storedFile != null) {
            fileUtil.deleteFileQuietly(storedFile.getStoredPath());
        }
        throw exception;
    }
}
```

### 7.3 트랜잭션 주의점

파일 시스템은 DB 트랜잭션에 참여하지 않는다. `@Transactional`만 적용해도 물리 파일은 자동 롤백되지 않는다.

따라서 다음 두 방식 중 하나를 적용한다.

#### 1차 프로토타입: 보상 삭제 방식

```text
파일 저장 성공
    ↓
DB 저장 실패
    ↓
catch 블록 또는 TransactionSynchronization.afterCompletion
    ↓
저장된 물리 파일 삭제
```

#### 운영 확장: 임시 저장 후 확정 이동 방식

```text
temp/{requestId}/UUID.pdf.part 저장
    ↓
DB 메타데이터 저장 성공
    ↓
최종 document/... 경로로 원자적 이동
    ↓
FileAsset ACTIVE 전환
```

운영 안정성은 두 번째 방식이 더 높지만, 현재 프로토타입에서는 보상 삭제 방식부터 구현한다.

---

## 8. 파일 검증 정책

### 8.1 국문 문서 허용 형식

```java
public enum KoreanSourceDocumentType {
    PDF,
    HWP,
    HWPX,
    DOCX,
    TXT,
    CSV,
    JSON
}
```

### 8.2 검증 순서

1. 빈 파일 여부
2. 파일 크기 제한
3. 원본 파일명 존재 여부
4. 확장자 허용 목록
5. 요청 `Content-Type`
6. 실제 파일 시그니처 또는 Apache Tika 기반 MIME 확인
7. 악성 파일 검사 연동 지점 확보
8. SHA-256 계산

### 8.3 중복 파일 정책

SHA-256가 같은 파일이 이미 존재하더라도 업무적으로 별도 문서일 수 있으므로 기본 정책은 **등록 허용 + 중복 경고**로 한다.

```json
{
  "duplicateDetected": true,
  "duplicateDocumentIds": [8, 13]
}
```

향후 관리자 옵션으로 다음 정책을 추가할 수 있다.

- 중복 등록 허용
- 기존 파일 재사용
- 동일 사용자·동일 해시 중복 차단

---

## 9. 파일 조회 및 다운로드 설계

### 9.1 `/files/**` 직접 매핑의 역할

`ResourceHandler`를 이용한 `/files/**` 정적 매핑은 로컬 개발과 빠른 프로토타입 확인에는 유용하다.

```java
@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    private final FileStorageProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = properties.getUploadPath()
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }
}
```

하지만 문서와 음성에는 개인정보 또는 내부 자료가 포함될 수 있으므로 운영 환경에서는 정적 URL 직접 공개를 기본 방식으로 사용하지 않는다.

### 9.2 권장 운영 방식

```http
GET /api/v1/files/{fileAssetId}/download
```

처리 순서:

```text
1. FileAsset 조회
2. DELETED/FAILED 상태 차단
3. 사용자 권한 및 owner 접근 권한 확인
4. storagePath를 절대 경로로 안전하게 변환
5. 실제 파일 존재 여부 확인
6. Content-Disposition 헤더 설정
7. Resource 스트리밍 응답
8. 다운로드 접근 로그 저장
```

### 9.3 응답 헤더

```http
Content-Type: application/pdf
Content-Length: 248021
Content-Disposition: attachment; filename*=UTF-8''민원안내.pdf
```

대용량 MP4 또는 GLB는 전체 메모리 적재보다 `Resource`, `InputStreamResource`, Range Request 또는 Nginx 내부 전달 방식을 검토한다.

---

## 10. AiJob 연동 설계

### 10.1 Job 필드

| 필드 | 설명 |
|---|---|
| `id` | DB PK |
| `jobKey` | 외부 식별자, 예: `JOB_20260724_0001` |
| `documentId` | 입력 문서 |
| `jobType` | `KOREAN_TO_SIGN_AVATAR` 등 |
| `status` | Job 상태 |
| `currentStage` | 현재 처리 단계 |
| `progress` | 0~100 |
| `retryCount` | 재시도 횟수 |
| `errorCode` | 표준 오류 코드 |
| `errorMessage` | 운영자 확인용 메시지 |
| `startedAt` | 시작일시 |
| `completedAt` | 완료일시 |

### 10.2 권장 상태

```java
public enum AiJobStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    RETRYING,
    CANCELLED
}
```

### 10.3 처리 단계

```java
public enum AiJobStage {
    FILE_PREPARATION,
    TEXT_EXTRACTION,
    KOREAN_NORMALIZATION,
    GLOSS_GENERATION,
    MOTION_MAPPING,
    NON_MANUAL_MAPPING,
    AVATAR_SCENE_BUILD,
    RENDERING,
    RESULT_FINALIZATION
}
```

현재 프로토타입에서는 다음 단계까지만 우선 구현한다.

```text
FILE_PREPARATION
→ TEXT_EXTRACTION
→ KOREAN_NORMALIZATION
→ GLOSS_GENERATION
```

### 10.4 Java → Python 요청

같은 볼륨을 공유하는 로컬 프로토타입:

```json
{
  "jobId": "JOB_20260724_0001",
  "documentId": 21,
  "fileAssetId": 83,
  "storagePath": "document/korean-source/DOC_20260724_0001/original/UUID.pdf",
  "absolutePath": "/home/upload/ax-platform/document/korean-source/DOC_20260724_0001/original/UUID.pdf",
  "callbackUrl": "http://platform-api:8080/api/v1/ai-jobs/JOB_20260724_0001/callback"
}
```

서버 또는 컨테이너가 분리되는 운영 구조:

```json
{
  "jobId": "JOB_20260724_0001",
  "documentId": 21,
  "fileAssetId": 83,
  "downloadUrl": "http://platform-api:8080/internal/files/83/download",
  "callbackUrl": "http://platform-api:8080/api/v1/ai-jobs/JOB_20260724_0001/callback"
}
```

운영 확장 시 절대 경로 전달보다 `fileAssetId` 또는 서명된 다운로드 URL을 우선한다.

### 10.5 Python 즉시 응답

```json
{
  "jobId": "JOB_20260724_0001",
  "accepted": true,
  "status": "PENDING"
}
```

### 10.6 Callback 요청

```json
{
  "jobId": "JOB_20260724_0001",
  "status": "PROCESSING",
  "stage": "KOREAN_NORMALIZATION",
  "progress": 40,
  "message": "국문 정규화가 완료되었습니다.",
  "files": [
    {
      "stage": "INTERMEDIATE",
      "role": "NORMALIZED_KOREAN",
      "storagePath": "job/JOB_20260724_0001/intermediate/normalized-korean_UUID.json"
    }
  ]
}
```

Callback 완료 예시:

```json
{
  "jobId": "JOB_20260724_0001",
  "status": "COMPLETED",
  "stage": "RESULT_FINALIZATION",
  "progress": 100,
  "result": {
    "glossCount": 3,
    "motionCount": 3,
    "outputFileAssetIds": [101, 102]
  }
}
```

### 10.7 멱등성

Python 처리 요청과 Callback은 네트워크 재시도로 중복 전달될 수 있다.

- `jobId`는 전역 유일값으로 생성한다.
- Python은 동일 `jobId`가 이미 처리 중이면 새 Job을 만들지 않는다.
- Callback은 `jobId + stage + eventSequence` 조합으로 중복 반영을 방지한다.
- 완료 상태에서 이전 단계 Callback이 늦게 도착해도 상태를 역행시키지 않는다.

---

## 11. 삭제와 재처리 정책

### 11.1 문서 삭제

기본은 즉시 물리 삭제가 아닌 논리 삭제로 한다.

```text
1. Document.status = DELETED
2. Document.deletedAt 기록
3. FileAsset.status = DELETE_PENDING
4. 진행 중 Job이 있으면 취소 가능 여부 확인
5. 정리 스케줄러가 물리 파일 삭제
6. 성공 시 FileAsset.status = DELETED
```

즉시 삭제가 필요한 프로토타입이라면 다음 순서를 사용한다.

```text
권한 확인
→ 진행 중 Job 확인
→ 물리 파일 삭제
→ DB 상태 DELETED 반영
```

물리 삭제 실패 시 DB 레코드를 먼저 제거하지 않는다.

### 11.2 재처리

재처리는 기존 Job을 덮어쓰지 않고 새 `AiJob`을 생성한다.

```text
Document 21
├─ Job 1: FAILED
├─ Job 2: COMPLETED
└─ Job 3: PROCESSING
```

`latestJobId`는 가장 최근 Job을 가리키며, 과거 Job과 중간 산출물은 이력으로 유지한다.

### 11.3 단계 재실행

2차 확장에서는 전체 재처리 외에 특정 단계부터 재실행할 수 있도록 한다.

```text
TEXT_EXTRACTION부터 재실행
KOREAN_NORMALIZATION부터 재실행
GLOSS_GENERATION부터 재실행
RENDERING만 재실행
```

재실행 시 이전 단계 산출물의 버전과 해시를 기록해 어떤 입력으로 결과가 생성됐는지 추적한다.

---

## 12. 공통 예외 설계

### 12.1 오류 코드

| 코드 | HTTP | 설명 |
|---|---:|---|
| `FILE_EMPTY` | 400 | 빈 파일 |
| `FILE_EXTENSION_NOT_ALLOWED` | 400 | 허용되지 않은 확장자 |
| `FILE_SIZE_EXCEEDED` | 413 | 최대 용량 초과 |
| `FILE_SIGNATURE_INVALID` | 400 | 확장자와 실제 파일 형식 불일치 |
| `FILE_STORAGE_FAILED` | 500 | 물리 파일 저장 실패 |
| `FILE_NOT_FOUND` | 404 | DB 또는 물리 파일 없음 |
| `DOCUMENT_NOT_FOUND` | 404 | 문서 없음 |
| `DOCUMENT_STATUS_INVALID` | 409 | 현재 상태에서 요청 수행 불가 |
| `DUPLICATE_JOB_REQUEST` | 409 | 동일 처리 Job 중복 요청 |
| `AI_API_UNAVAILABLE` | 503 | Python API 연결 실패 |
| `AI_API_TIMEOUT` | 504 | Python API 시간 초과 |
| `AI_JOB_CALLBACK_INVALID` | 400 | Callback 데이터 오류 |

### 12.2 공통 응답

```json
{
  "timestamp": "2026-07-24T13:45:12",
  "status": 500,
  "code": "FILE_STORAGE_FAILED",
  "message": "파일 저장 중 오류가 발생했습니다.",
  "requestId": "REQ-2d1a9f8c",
  "path": "/api/v1/korean-source-documents"
}
```

사용자 응답에는 내부 절대 경로, 스택 트레이스, Python 내부 오류 원문을 직접 노출하지 않는다.

---

## 13. 로깅 및 추적성

모든 요청에 `requestId`, 모든 AI 처리에 `jobId`, 모든 파일에 `fileAssetId`를 사용한다.

```text
[requestId=REQ-2d1a9f8c]
[documentKey=DOC_20260724_0001]
[jobId=JOB_20260724_0001]
[fileAssetId=83]
```

주요 로그 지점:

1. 문서 등록 요청 수신
2. 파일 검증 성공·실패
3. 파일 저장 시작·완료
4. FileAsset DB 저장
5. Document 상태 전이
6. AiJob 생성
7. Python 요청·응답·타임아웃
8. Callback 수신 및 상태 전이
9. 파일 삭제·보상 삭제
10. orphan 파일 탐지

원본 파일명은 운영 로그에 기록할 수 있지만 개인정보가 포함될 가능성이 있으므로 접근 가능한 로그 범위를 제한한다.

---

## 14. DB 마이그레이션 권장 순서

### V1__create_file_asset.sql

- `file_asset` 테이블 생성
- `asset_type`, `owner_type`, `status`, `sha256` 인덱스
- `stored_path` 유니크 여부는 정책에 따라 적용

### V2__create_korean_source_document.sql

- `korean_source_document` 테이블 생성
- `document_key` 유니크 인덱스
- `status`, `created_at` 조회 인덱스
- `original_file_asset_id` FK

### V3__create_ai_job.sql

- `ai_job` 테이블 생성
- `job_key` 유니크 인덱스
- `document_id`, `status`, `created_at` 인덱스

### V4__add_document_latest_job.sql

- `korean_source_document.latest_job_id` 추가
- 필요 시 FK 적용

프로토타입에서는 H2 자동 생성으로 먼저 검증할 수 있지만, PostgreSQL 전환 시점부터 Flyway를 기준으로 스키마를 관리한다.

---

## 15. 테스트 설계

### 15.1 FileUtil 단위 테스트

- 정상 PDF 저장
- 영문·한글 원본 파일명 저장
- UUID 저장명 생성
- 경로 정규화
- `../` 경로 차단
- 허용되지 않은 확장자 차단
- SHA-256 계산 확인
- 저장 실패 시 부분 파일 제거

### 15.2 서비스 통합 테스트

- Document와 FileAsset 동시 저장
- 파일 저장 후 DB 실패 시 물리 파일 삭제
- 중복 해시 감지
- `startProcessing=false`일 때 Job 미생성
- `startProcessing=true`일 때 PENDING Job 생성
- Python 연결 실패 시 Document/FileAsset 보존 및 Job FAILED 처리
- 문서 삭제 시 FileAsset 상태 전이

### 15.3 API 테스트

```text
POST   문서 등록 성공                → 201
POST   빈 파일                       → 400
POST   용량 초과                     → 413
POST   미허용 확장자                 → 400
GET    문서 상세                     → 200
GET    존재하지 않는 문서            → 404
GET    다운로드 권한 없음            → 403
POST   이미 처리 중인 문서 재요청     → 409
DELETE 진행 중 Job이 있는 문서        → 정책에 따라 409 또는 취소 후 삭제
```

### 15.4 장애 시나리오

- 파일 시스템 디스크 용량 부족
- 업로드 디렉터리 권한 없음
- DB 연결 실패
- Python API 다운
- Python API Timeout
- Callback 중복 수신
- Callback 순서 역전
- 실제 파일만 삭제된 상태
- DB 레코드만 삭제된 orphan 파일

---

## 16. 구현 패키지 구조

```text
com.hyunsuk.axplatform
├─ document
│  ├─ controller
│  │  └─ KoreanSourceDocumentController.java
│  ├─ service
│  │  └─ KoreanSourceDocumentService.java
│  ├─ domain
│  │  ├─ KoreanSourceDocument.java
│  │  ├─ KoreanSourceDocumentStatus.java
│  │  └─ KoreanSourceDocumentType.java
│  ├─ repository
│  │  └─ KoreanSourceDocumentRepository.java
│  └─ dto
│     ├─ KoreanSourceDocumentCreateRequest.java
│     ├─ KoreanSourceDocumentCreateResponse.java
│     ├─ KoreanSourceDocumentDetailResponse.java
│     └─ KoreanSourceDocumentListResponse.java
├─ file
│  ├─ config
│  │  ├─ FileStorageProperties.java
│  │  └─ FileResourceConfig.java
│  ├─ domain
│  │  ├─ FileAsset.java
│  │  ├─ FileAssetType.java
│  │  └─ FileAssetStatus.java
│  ├─ repository
│  │  └─ FileAssetRepository.java
│  ├─ service
│  │  ├─ FileAssetService.java
│  │  └─ FileDownloadService.java
│  └─ util
│     ├─ FileUtil.java
│     └─ StoredFileInfo.java
└─ aijob
   ├─ controller
   │  └─ AiJobCallbackController.java
   ├─ service
   │  └─ AiJobService.java
   ├─ domain
   │  ├─ AiJob.java
   │  ├─ AiJobStatus.java
   │  └─ AiJobStage.java
   └─ client
      └─ AiApiClient.java
```

---

## 17. 단계별 구현 순서

### Phase 1. 국문 문서 등록 완성

- [ ] `FileStorageProperties` 적용
- [ ] `FileAssetType.KOREAN_SOURCE_DOCUMENT` 확인
- [ ] `StoredFileInfo` 구현
- [ ] `FileUtil.saveAsset()` 구현
- [ ] `FileAsset` 엔티티 및 Repository 구현
- [ ] `KoreanSourceDocument` 엔티티 및 Repository 구현
- [ ] 문서 등록 Request/Response DTO 구현
- [ ] `POST /api/v1/korean-source-documents` 구현
- [ ] 저장 실패 시 물리 파일 보상 삭제
- [ ] Postman multipart 등록 확인

### Phase 2. 조회·다운로드·삭제

- [ ] 문서 목록 및 상세 조회
- [ ] 상태·등록일·제목 검색 조건
- [ ] 파일 다운로드 API
- [ ] 권한 검증 지점 추가
- [ ] 문서 논리 삭제
- [ ] 물리 파일 정리 정책

### Phase 3. AI Job 연동

- [ ] `AiJob` 엔티티 및 상태 정의
- [ ] 문서 기반 PENDING Job 생성
- [ ] Java → Python 처리 요청
- [ ] Timeout 및 4xx/5xx 예외 변환
- [ ] Callback API
- [ ] Job 진행률·현재 단계 조회
- [ ] 실패 재시도

### Phase 4. 운영 안정화

- [ ] Flyway 마이그레이션
- [ ] PostgreSQL 전환
- [ ] requestId 로그 연동
- [ ] orphan 파일 탐지
- [ ] temp 파일 정리 스케줄러
- [ ] 용량 모니터링
- [ ] Object Storage 전환을 위한 `StorageProvider` 추상화

---

## 18. 현재 단계의 최소 완료 기준

다음 조건을 만족하면 `KoreanSourceDocument` 등록 API의 1차 구현이 완료된 것으로 본다.

- [ ] PDF 또는 허용된 국문 문서를 multipart로 업로드할 수 있다.
- [ ] 문서 레코드가 `UPLOADING → UPLOADED`로 전이된다.
- [ ] 원본 파일이 `document/korean-source/{documentKey}/original`에 저장된다.
- [ ] `FileAsset`에 원본명, 저장명, 상대 경로, MIME, 크기, SHA-256가 기록된다.
- [ ] Document와 원본 FileAsset이 연결된다.
- [ ] 응답에 `documentId`, `documentKey`, `fileAssetId`, 다운로드 URL이 포함된다.
- [ ] DB 저장 실패 시 저장된 물리 파일이 삭제된다.
- [ ] 경로 조작 문자열로 업로드 루트 밖에 접근할 수 없다.
- [ ] 빈 파일, 미허용 확장자, 용량 초과 요청이 표준 오류 응답으로 반환된다.
- [ ] 통합 테스트로 정상·실패 시나리오가 검증된다.

---

## 19. 다음 설계 문서 권장 목록

본 문서 이후에는 기능 단위로 다음 설계서를 분리하는 것이 좋다.

1. **KoreanSourceDocument API 상세 명세서**  
   요청·응답, 검증, 검색 조건, 페이징, 오류 코드

2. **AiJob 상태 전이 및 재시도 설계서**  
   단계별 상태, Callback, Timeout, 재처리, 중복 방지

3. **Python 문서 파싱·중간 산출물 계약서**  
   `extracted-text.json`, `normalized-korean.json`, `gloss-sequence.json` 스키마

4. **글로스 후보 검색 및 평가 설계서**  
   병렬 말뭉치·글로스 사전 조회, 후보 점수, 문법 규칙 보정

5. **수어 모션·비수지 표현 매핑 설계서**  
   `glossCode → motionId`, 연결 구간, 표정·시선·고개 신호

6. **3D 아바타 렌더링 입출력 계약서**  
   `avatar-scene.json`, GLB/MP4/썸네일 생성 규칙

7. **파일 보안·백업·정리 운영 설계서**  
   권한, 보존 기간, 암호화, 백업, orphan 탐지, 용량 정책

---

## 20. 설계 결론

선행 파일 저장 구조가 **파일을 어디에 어떤 규칙으로 저장할지**를 정의했다면, 본 후속 설계는 **저장된 파일을 도메인·DB·API·AI Job과 어떻게 안전하게 연결할지**를 정의한다.

현재 가장 먼저 구현할 범위는 다음과 같다.

```text
Multipart 요청
    ↓
KoreanSourceDocument UPLOADING 생성
    ↓
FileUtil 원본 저장
    ↓
FileAsset 메타데이터 저장
    ↓
Document UPLOADED 전환
    ↓
선택적으로 AiJob PENDING 생성
    ↓
Python 문서 파싱 요청
```

이 흐름이 완성되면 이후의 `normalized-korean.json`, `gloss-sequence.json`, `motion-sequence.json`, 3D 아바타 결과 파일을 동일한 `jobId`와 `FileAsset` 구조 안에서 확장할 수 있다.
