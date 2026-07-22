# Domain Registration API Design

## 목적

업로드된 `Document`를 각 업무 도메인 엔티티가 참조하도록 분리한다.
파일 업로드와 물리 파일 메타데이터 관리는 `Document`까지에서 끝내고,
국문 데이터, 수어 데이터, 박물관/의료 매뉴얼 같은 업무 분류는 별도 도메인 등록 API에서 처리한다.

## 공통 원칙

- 도메인 등록 API는 파일을 직접 업로드하지 않는다.
- 요청은 기존 `documentId`와 도메인별 업무 메타데이터를 받는다.
- 각 도메인 엔티티는 `Document`를 1:1로 참조한다.
- `Document`는 `FileMetadata`를 통해 물리 파일 정보에 접근한다.
- 도메인 API는 문서의 업무 의미를 부여하고, 파일 저장 책임은 갖지 않는다.

## 예정 API

```text
POST /api/v1/korean-source-documents
POST /api/v1/sign-language-datasets
POST /api/v1/museum-manuals
POST /api/v1/medical-manuals
```

## 요청 예시

### KoreanSourceDocument

```json
{
  "documentId": 1,
  "sourceDomain": "PUBLIC_SERVICE",
  "description": "민원 안내 국문 원천 문서"
}
```

### SignLanguageDataset

```json
{
  "documentId": 2,
  "datasetType": "GLOSS_CORPUS",
  "description": "국문-글로스 병렬 말뭉치"
}
```

### MuseumManual

```json
{
  "documentId": 3,
  "museumName": "국립중앙박물관",
  "manualCategory": "전시 안내"
}
```

### MedicalManual

```json
{
  "documentId": 4,
  "department": "응급의학과",
  "manualCategory": "접수 안내"
}
```

## 처리 흐름

```text
DomainController
-> DomainRegisterRequest 수신
-> Document 조회
-> 이미 등록된 Document 여부 검증
-> 도메인 Entity 생성
-> 도메인 Repository 저장
-> DomainRegisterResponse 반환
```

## SignLanguageDataset 등록 API 설계

### 목적

`Document`로 저장된 수어 관련 데이터셋 파일에 `SignLanguageDataset` 도메인 의미를 부여한다.
이 API는 파일을 직접 업로드하지 않고, 이미 저장된 `Document`를 참조한다.

### 예정 API

```text
POST /api/v1/sign-language-datasets
Content-Type: application/json
```

### 요청 예시

```json
{
  "documentId": 2,
  "datasetType": "PARALLEL_CORPUS",
  "description": "국문-글로스 병렬 말뭉치"
}
```

### 허용 파일 자산 타입

프로토타입 현재 단계에서는 다음 두 타입만 `SignLanguageDataset` 등록 대상으로 허용한다.

```text
PARALLEL_CORPUS
GLOSS_DICTIONARY
```

`SIGN_MOTION`, `NON_MANUAL_MOTION`은 이후 수어 모션 자산 도메인이 구체화된 뒤 확장한다.

### 선행 조건

`POST /api/v1/documents` 업로드 흐름은 `assetType` 요청값을 통해 데이터셋 파일 저장을 지원한다.
`assetType`이 생략되면 기존 흐름과의 호환을 위해 `KOREAN_SOURCE_DOCUMENT`로 저장한다.

```text
Document 업로드 API 확장
-> assetType 입력 지원
-> PARALLEL_CORPUS / GLOSS_DICTIONARY 파일 정책 연결
-> JSON / CSV 확장자 및 MIME Type 검증
-> FileMetadata.assetType에 데이터셋 타입 저장
-> SignLanguageDataset 등록 API에서 해당 assetType 검증
```

### 검증 정책

```text
1. documentId 필수
2. Document 존재 여부 검증
3. Document.fileMetadata.assetType이 PARALLEL_CORPUS 또는 GLOSS_DICTIONARY인지 검증
4. 같은 Document로 SignLanguageDataset 중복 등록 방지
5. datasetType이 파일 자산 타입과 의미상 일치하는지 검증
```

### 실패 코드

```text
DOCUMENT_NOT_FOUND
SIGN_LANGUAGE_DATASET_ALREADY_REGISTERED
INVALID_SIGN_LANGUAGE_DATASET_ASSET_TYPE
INVALID_SIGN_LANGUAGE_DATASET_TYPE
```

### 예상 응답

```json
{
  "signLanguageDatasetId": 1,
  "documentId": 2,
  "resourceKey": "8d106a45-7188-44d4-85eb-b50c7490e94e",
  "version": 1,
  "title": "국문-글로스 병렬 말뭉치",
  "datasetType": "PARALLEL_CORPUS",
  "description": "국문-글로스 병렬 말뭉치",
  "file": {
    "assetType": "PARALLEL_CORPUS",
    "originalFileName": "parallel-corpus.csv",
    "accessPath": "/files/dataset/parallel-corpus/..."
  }
}
```

### 구현 순서

```text
1. FileUploadPolicy에 PARALLEL_CORPUS / GLOSS_DICTIONARY 정책 추가 완료
2. Document 업로드 API에서 dataset assetType 저장 경로 확장 완료
3. SignLanguageDataset Request/Response DTO 작성 완료
4. SignLanguageDatasetService 등록 로직 작성 완료
5. SignLanguageDatasetController 작성 완료
6. 등록 성공, 중복 등록, 잘못된 assetType 테스트 작성 완료
```

## MuseumManual / MedicalManual 등록 API 구현 상태

박물관 매뉴얼과 의료 매뉴얼은 현재 단계에서 국문 원천 문서 기반 업무 매뉴얼로 취급한다.
따라서 등록 대상 `Document.fileMetadata.assetType`은 `KOREAN_SOURCE_DOCUMENT`만 허용한다.

```text
POST /api/v1/museum-manuals
POST /api/v1/medical-manuals
```

### 공통 검증 정책

```text
1. documentId 필수
2. Document 존재 여부 검증
3. Document.fileMetadata.assetType이 KOREAN_SOURCE_DOCUMENT인지 검증
4. 같은 Document로 같은 도메인 중복 등록 방지
```

## 다음 구현 범위

- 도메인별 목록/상세 조회 API 구현
- Document 목록 API 페이지네이션 및 상태 필터 적용
- PostgreSQL / Flyway 전환 시 도메인 테이블 마이그레이션 작성
- 도메인별 목록/상세 API는 관리 화면 요구사항에 맞춰 분리 구현
