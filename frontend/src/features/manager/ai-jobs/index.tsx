"use client";

import { useState } from "react";
import { useDocumentsQuery } from "../documents/hooks/useDocumentsQuery";
import AiJobDetail from "./components/AiJobDetail";
import AiJobDocumentSelector from "./components/AiJobDocumentSelector";
import AiJobFiles from "./components/AiJobFiles";
import AiJobList from "./components/AiJobList";
import {
  useAiJobDetailQuery,
  useAiJobFilesQuery,
  useAiJobsByDocumentQuery,
} from "./hooks/useAiJobsQuery";

const DOCUMENT_PAGE_SIZE = 50;
const JOB_PAGE_SIZE = 20;

export default function AiJobsPage() {
  const [selectedDocumentId, setSelectedDocumentId] = useState<number | null>(
    null,
  );
  const [selectedJobKey, setSelectedJobKey] = useState<string | null>(null);

  const documentsQuery = useDocumentsQuery({
    page: 0,
    size: DOCUMENT_PAGE_SIZE,
  });
  const documents = documentsQuery.data?.items ?? [];
  const targetDocumentId =
    selectedDocumentId ?? documents[0]?.documentId ?? null;
  const jobsQuery = useAiJobsByDocumentQuery({
    documentId: targetDocumentId,
    page: 0,
    size: JOB_PAGE_SIZE,
  });
  const jobs = jobsQuery.data?.items ?? [];
  const targetJobKey = selectedJobKey ?? jobs[0]?.jobKey ?? null;
  const detailQuery = useAiJobDetailQuery(targetJobKey);
  const filesQuery = useAiJobFilesQuery(targetJobKey);

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="eyebrow">AI Jobs</p>
        <h1>AI Job 관리</h1>
        <p>
          문서 처리 요청, Python Callback 상태 전이, Job 산출물 목록을 추적합니다.
        </p>
      </div>

      <div className="ai-job-workspace">
        <section className="ai-job-left">
          <AiJobDocumentSelector
            documents={documents}
            onSelect={(documentId) => {
              setSelectedDocumentId(documentId);
              setSelectedJobKey(null);
            }}
            selectedDocumentId={targetDocumentId}
          />

          {documentsQuery.isLoading ? (
            <div className="ai-job-empty">문서 목록을 불러오는 중입니다.</div>
          ) : null}

          {jobsQuery.isLoading ? (
            <div className="ai-job-empty">AI Job 목록을 불러오는 중입니다.</div>
          ) : null}

          {jobsQuery.error ? (
            <div className="ai-job-empty error">
              AI Job 목록을 불러오지 못했습니다.
            </div>
          ) : null}

          <AiJobList
            jobs={jobs}
            onSelect={setSelectedJobKey}
            selectedJobKey={targetJobKey}
          />
        </section>

        <section className="ai-job-right">
          <AiJobDetail
            isLoading={detailQuery.isLoading}
            job={detailQuery.data}
          />
          <AiJobFiles files={filesQuery.data?.items ?? []} />
        </section>
      </div>
    </section>
  );
}
