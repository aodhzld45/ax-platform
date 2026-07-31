import type { DocumentItem } from "../../documents/types/document";

type AiJobDocumentSelectorProps = {
  documents: DocumentItem[];
  selectedDocumentId: number | null;
  onSelect: (documentId: number) => void;
};

export default function AiJobDocumentSelector({
  documents,
  selectedDocumentId,
  onSelect,
}: AiJobDocumentSelectorProps) {
  return (
    <label className="ai-job-selector">
      처리 대상 문서
      <select
        disabled={documents.length === 0}
        onChange={(event) => onSelect(Number(event.target.value))}
        value={selectedDocumentId ?? ""}
      >
        {documents.length === 0 ? (
          <option value="">등록된 문서 없음</option>
        ) : null}
        {documents.map((document) => (
          <option key={document.documentId} value={document.documentId}>
            {document.title}
          </option>
        ))}
      </select>
    </label>
  );
}
