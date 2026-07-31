"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { RefreshCcw } from "lucide-react";
import { useState } from "react";
import DocumentDetailPanel from "./components/DocumentDetailPanel";
import DocumentFilters from "./components/DocumentFilters";
import DocumentListTable from "./components/DocumentListTable";
import DocumentPagination from "./components/DocumentPagination";
import DocumentUploadForm from "./components/DocumentUploadForm";
import {
  documentMutations,
  useDocumentDetailQuery,
  useDocumentsQuery,
} from "./hooks/useDocumentsQuery";
import type { DocumentIndexStatus, DocumentStatus } from "./types/document";

const PAGE_SIZE = 10;

export default function DocumentsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [documentStatus, setDocumentStatus] = useState<DocumentStatus | "">("");
  const [indexStatus, setIndexStatus] = useState<DocumentIndexStatus | "">("");
  const [selectedDocumentId, setSelectedDocumentId] = useState<number | null>(
    null,
  );

  const documentsQuery = useDocumentsQuery({
    page,
    size: PAGE_SIZE,
    documentStatus,
    indexStatus,
  });

  const documents = documentsQuery.data?.items ?? [];
  const totalCount = documentsQuery.data?.totalCount ?? 0;
  const totalPages = documentsQuery.data?.totalPages ?? 0;
  const detailDocumentId =
    selectedDocumentId ?? documents[0]?.documentId ?? null;
  const detailQuery = useDocumentDetailQuery(detailDocumentId);
  const uploadMutation = useMutation({
    mutationFn: documentMutations.uploadDocument,
    onSuccess: (response) => {
      setSelectedDocumentId(response.documentId);
      queryClient.invalidateQueries({ queryKey: ["documents"] });
    },
  });
  const createAiJobMutation = useMutation({
    mutationFn: documentMutations.createDocumentAiJob,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["ai-jobs"] });
    },
  });

  function resetPageAndSelectNone() {
    setPage(0);
    setSelectedDocumentId(null);
  }

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="eyebrow">Documents</p>
        <div className="page-heading-row">
          <div>
            <h1>문서 관리</h1>
            <p>등록된 문서와 파일 메타데이터를 조회하고 상세 정보를 확인합니다.</p>
          </div>
          <button
            className="icon-text-button"
            disabled={documentsQuery.isFetching}
            onClick={() => documentsQuery.refetch()}
            type="button"
          >
            <RefreshCcw aria-hidden="true" size={16} />
            {documentsQuery.isFetching ? "조회 중" : "새로고침"}
          </button>
        </div>
      </div>

      <div className="document-workspace">
        <section className="document-list-panel">
          <DocumentUploadForm
            isPending={uploadMutation.isPending}
            onSubmit={(params) => uploadMutation.mutate(params)}
          />

          {uploadMutation.error ? (
            <div className="document-empty error">
              문서를 업로드하지 못했습니다.
            </div>
          ) : null}

          <DocumentFilters
            documentStatus={documentStatus}
            indexStatus={indexStatus}
            onDocumentStatusChange={(value) => {
              setDocumentStatus(value);
              resetPageAndSelectNone();
            }}
            onIndexStatusChange={(value) => {
              setIndexStatus(value);
              resetPageAndSelectNone();
            }}
          />

          {documentsQuery.isLoading ? (
            <div className="document-empty">문서 목록을 불러오는 중입니다.</div>
          ) : null}

          {documentsQuery.error ? (
            <div className="document-empty error">
              문서 목록을 불러오지 못했습니다.
            </div>
          ) : null}

          {documentsQuery.data ? (
            <>
              <DocumentListTable
                documents={documents}
                onSelect={setSelectedDocumentId}
                selectedDocumentId={detailDocumentId}
              />
              <DocumentPagination
                onPageChange={(nextPage) => {
                  setPage(nextPage);
                  setSelectedDocumentId(null);
                }}
                page={page}
                totalCount={totalCount}
                totalPages={totalPages}
              />
            </>
          ) : null}
        </section>

        <DocumentDetailPanel
          document={detailQuery.data}
          isLoading={detailQuery.isLoading}
          isCreatingAiJob={createAiJobMutation.isPending}
          onCreateAiJob={(documentId, jobType) =>
            createAiJobMutation.mutate({
              documentId,
              jobType,
            })
          }
        />
      </div>
    </section>
  );
}
