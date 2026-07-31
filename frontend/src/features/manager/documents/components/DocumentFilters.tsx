import type { DocumentIndexStatus, DocumentStatus } from "../types/document";

type DocumentFiltersProps = {
  documentStatus: DocumentStatus | "";
  indexStatus: DocumentIndexStatus | "";
  onDocumentStatusChange: (value: DocumentStatus | "") => void;
  onIndexStatusChange: (value: DocumentIndexStatus | "") => void;
};

const documentStatuses: Array<DocumentStatus | ""> = [
  "",
  "ACTIVE",
  "INACTIVE",
  "DELETED",
];

const indexStatuses: Array<DocumentIndexStatus | ""> = [
  "",
  "NOT_REQUESTED",
  "REQUESTED",
  "INDEXING",
  "INDEXED",
  "FAILED",
];

export default function DocumentFilters({
  documentStatus,
  indexStatus,
  onDocumentStatusChange,
  onIndexStatusChange,
}: DocumentFiltersProps) {
  return (
    <div className="document-filters">
      <label>
        문서 상태
        <select
          onChange={(event) =>
            onDocumentStatusChange(event.target.value as DocumentStatus | "")
          }
          value={documentStatus}
        >
          {documentStatuses.map((status) => (
            <option key={status || "ALL"} value={status}>
              {status || "전체"}
            </option>
          ))}
        </select>
      </label>

      <label>
        인덱싱 상태
        <select
          onChange={(event) =>
            onIndexStatusChange(event.target.value as DocumentIndexStatus | "")
          }
          value={indexStatus}
        >
          {indexStatuses.map((status) => (
            <option key={status || "ALL"} value={status}>
              {status || "전체"}
            </option>
          ))}
        </select>
      </label>
    </div>
  );
}
