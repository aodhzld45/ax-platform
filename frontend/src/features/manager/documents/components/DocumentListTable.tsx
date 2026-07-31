import type { DocumentItem } from "../types/document";
import { formatDateTime, formatFileSize } from "../utils/format";

type DocumentListTableProps = {
  documents: DocumentItem[];
  selectedDocumentId: number | null;
  onSelect: (documentId: number) => void;
};

export default function DocumentListTable({
  documents,
  selectedDocumentId,
  onSelect,
}: DocumentListTableProps) {
  if (documents.length === 0) {
    return <div className="document-empty">조회된 문서가 없습니다.</div>;
  }

  return (
    <div className="document-table-wrap">
      <table className="document-table">
        <thead>
          <tr>
            <th>문서</th>
            <th>파일 타입</th>
            <th>상태</th>
            <th>인덱싱</th>
            <th>크기</th>
            <th>등록일</th>
          </tr>
        </thead>
        <tbody>
          {documents.map((document) => (
            <tr
              className={
                selectedDocumentId === document.documentId ? "selected" : ""
              }
              key={document.documentId}
              onClick={() => onSelect(document.documentId)}
            >
              <td>
                <strong>{document.title}</strong>
                <span>{document.file.originalFileName}</span>
              </td>
              <td>{document.file.assetType}</td>
              <td>
                <span className="table-status">{document.documentStatus}</span>
              </td>
              <td>{document.indexStatus}</td>
              <td>{formatFileSize(document.file.fileSize)}</td>
              <td>{formatDateTime(document.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
