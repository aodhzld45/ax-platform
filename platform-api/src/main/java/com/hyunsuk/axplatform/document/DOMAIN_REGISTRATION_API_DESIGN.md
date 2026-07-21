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

## 다음 구현 범위

- `KoreanSourceDocument` 등록 API 우선 구현
- 이후 `SignLanguageDataset`, `MuseumManual`, `MedicalManual` 등록 API 확장
- 도메인별 목록/상세 API는 관리 화면 요구사항에 맞춰 분리 구현
