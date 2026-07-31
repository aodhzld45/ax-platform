import type { AiJob } from "../types/aiJob";
import { formatDateTime } from "../utils/format";

type AiJobListProps = {
  jobs: AiJob[];
  selectedJobKey: string | null;
  onSelect: (jobKey: string) => void;
};

export default function AiJobList({
  jobs,
  selectedJobKey,
  onSelect,
}: AiJobListProps) {
  if (jobs.length === 0) {
    return <div className="ai-job-empty">조회된 AI Job이 없습니다.</div>;
  }

  return (
    <div className="ai-job-list">
      {jobs.map((job) => (
        <button
          className={selectedJobKey === job.jobKey ? "selected" : ""}
          key={job.jobKey}
          onClick={() => onSelect(job.jobKey)}
          type="button"
        >
          <span>
            <strong>{job.jobKey}</strong>
            <em>{formatDateTime(job.createdAt)}</em>
          </span>
          <span className={`job-status ${job.status.toLowerCase()}`}>
            {job.status}
          </span>
        </button>
      ))}
    </div>
  );
}
