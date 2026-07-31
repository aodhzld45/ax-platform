import type { AiJob } from "../types/aiJob";
import { formatDateTime } from "../utils/format";

type AiJobDetailProps = {
  job: AiJob | undefined;
  isLoading: boolean;
};

export default function AiJobDetail({ job, isLoading }: AiJobDetailProps) {
  if (isLoading) {
    return <div className="ai-job-card">AI Job 상세를 불러오는 중입니다.</div>;
  }

  if (!job) {
    return <div className="ai-job-card">AI Job을 선택해주세요.</div>;
  }

  return (
    <article className="ai-job-card">
      <div className="ai-job-card-heading">
        <span className="eyebrow">Job Detail</span>
        <h2>{job.jobKey}</h2>
        <p>{job.documentTitle}</p>
      </div>

      <div className="job-progress">
        <div>
          <span>{job.currentStage}</span>
          <strong>{job.progress}%</strong>
        </div>
        <progress max={100} value={job.progress} />
      </div>

      <dl className="document-detail-list">
        <div>
          <dt>상태</dt>
          <dd>{job.status}</dd>
        </div>
        <div>
          <dt>작업 타입</dt>
          <dd>{job.jobType}</dd>
        </div>
        <div>
          <dt>재시도</dt>
          <dd>
            {job.retryCount} / {job.maxRetryCount}
          </dd>
        </div>
        <div>
          <dt>요청일</dt>
          <dd>{formatDateTime(job.requestedAt)}</dd>
        </div>
        <div>
          <dt>시작일</dt>
          <dd>{job.startedAt ? formatDateTime(job.startedAt) : "-"}</dd>
        </div>
        <div>
          <dt>완료일</dt>
          <dd>{job.completedAt ? formatDateTime(job.completedAt) : "-"}</dd>
        </div>
      </dl>

      {job.errorCode || job.errorMessage ? (
        <div className="ai-job-error">
          <strong>{job.errorCode}</strong>
          <p>{job.errorMessage}</p>
        </div>
      ) : null}

      {job.resultJson ? (
        <pre className="ai-job-result">{job.resultJson}</pre>
      ) : null}
    </article>
  );
}
