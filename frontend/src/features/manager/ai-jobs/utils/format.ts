import { formatDateTime, formatFileSize } from "../../documents/utils/format";

export { formatDateTime, formatFileSize };

export function getAiJobFileDownloadPath(jobKey: string, aiJobFileId: number) {
  return `/platform-api/api/v1/ai-jobs/${jobKey}/files/${aiJobFileId}/download`;
}
