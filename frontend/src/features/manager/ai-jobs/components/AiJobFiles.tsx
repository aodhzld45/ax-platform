import { Download, ExternalLink } from "lucide-react";
import type { AiJobFile } from "../types/aiJob";
import {
  formatDateTime,
  formatFileSize,
  getAiJobFileDownloadPath,
} from "../utils/format";

type AiJobFilesProps = {
  files: AiJobFile[];
};

export default function AiJobFiles({ files }: AiJobFilesProps) {
  if (files.length === 0) {
    return <div className="ai-job-card">등록된 산출물이 없습니다.</div>;
  }

  return (
    <article className="ai-job-card">
      <div className="ai-job-card-heading">
        <span className="eyebrow">Files</span>
        <h2>산출물 목록</h2>
      </div>

      <div className="ai-job-file-list">
        {files.map((file) => (
          <div key={file.aiJobFileId}>
            <span>
              <strong>{file.originalFileName}</strong>
              <em>
                {file.role} · {file.stage} · {formatFileSize(file.fileSize)}
              </em>
              <em>{formatDateTime(file.createdAt)}</em>
            </span>
            <nav>
              <a href={getAiJobFileDownloadPath(file.jobKey, file.aiJobFileId)}>
                <Download aria-hidden="true" size={15} />
                다운로드
              </a>
              <a
                href={`/platform-api${file.accessPath}`}
                rel="noreferrer"
                target="_blank"
              >
                <ExternalLink aria-hidden="true" size={15} />
                열기
              </a>
            </nav>
          </div>
        ))}
      </div>
    </article>
  );
}
