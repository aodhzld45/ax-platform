import { BrainCircuit, Download, ExternalLink } from "lucide-react";
import type { AiJobType, DocumentItem } from "../types/document";
import {
  formatDateTime,
  formatFileSize,
  getFileDownloadPath,
} from "../utils/format";

type DocumentDetailPanelProps = {
  document: DocumentItem | undefined;
  isLoading: boolean;
  isCreatingAiJob: boolean;
  onCreateAiJob: (documentId: number, jobType: AiJobType) => void;
};

export default function DocumentDetailPanel({
  document,
  isLoading,
  isCreatingAiJob,
  onCreateAiJob,
}: DocumentDetailPanelProps) {
  if (isLoading) {
    return (
      <aside className="document-detail-panel">
        <p className="document-detail-placeholder">상세 정보를 불러오는 중입니다.</p>
      </aside>
    );
  }

  if (!document) {
    return (
      <aside className="document-detail-panel">
        <p className="document-detail-placeholder">
          목록에서 문서를 선택하면 상세 정보가 표시됩니다.
        </p>
      </aside>
    );
  }

  return (
    <aside className="document-detail-panel">
      <div className="document-detail-heading">
        <span className="eyebrow">Detail</span>
        <h2>{document.title}</h2>
        <p>{document.resourceKey}</p>
      </div>

      <dl className="document-detail-list">
        <div>
          <dt>문서 ID</dt>
          <dd>{document.documentId}</dd>
        </div>
        <div>
          <dt>버전</dt>
          <dd>{document.version}</dd>
        </div>
        <div>
          <dt>문서 상태</dt>
          <dd>{document.documentStatus}</dd>
        </div>
        <div>
          <dt>인덱싱 상태</dt>
          <dd>{document.indexStatus}</dd>
        </div>
        <div>
          <dt>파일명</dt>
          <dd>{document.file.originalFileName}</dd>
        </div>
        <div>
          <dt>자산 타입</dt>
          <dd>{document.file.assetType}</dd>
        </div>
        <div>
          <dt>Content-Type</dt>
          <dd>{document.file.contentType ?? "-"}</dd>
        </div>
        <div>
          <dt>파일 크기</dt>
          <dd>{formatFileSize(document.file.fileSize)}</dd>
        </div>
        <div>
          <dt>저장 상대 경로</dt>
          <dd>{document.file.storageRelativePath}</dd>
        </div>
        <div>
          <dt>HTTP 접근 경로</dt>
          <dd>{document.file.accessPath}</dd>
        </div>
        <div>
          <dt>등록일</dt>
          <dd>{formatDateTime(document.createdAt)}</dd>
        </div>
        <div>
          <dt>수정일</dt>
          <dd>{formatDateTime(document.updatedAt)}</dd>
        </div>
      </dl>

      <div className="document-detail-actions">
        <button
          className="icon-text-button"
          disabled={isCreatingAiJob}
          onClick={() => onCreateAiJob(document.documentId, "KOREAN_TO_GLOSS")}
          type="button"
        >
          <BrainCircuit aria-hidden="true" size={16} />
          {isCreatingAiJob ? "요청 중" : "AI Job 생성"}
        </button>
        <a
          className="secondary-button"
          href={getFileDownloadPath(document.file.fileMetadataId)}
        >
          <Download aria-hidden="true" size={16} />
          다운로드
        </a>
        <a
          className="secondary-button"
          href={`/platform-api${document.file.accessPath}`}
          rel="noreferrer"
          target="_blank"
        >
          <ExternalLink aria-hidden="true" size={16} />
          열기
        </a>
      </div>
    </aside>
  );
}
