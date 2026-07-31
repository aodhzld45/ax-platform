import { apiClient } from "@/shared/api/client";
import type {
  AiJob,
  AiJobFileListResponse,
  AiJobListResponse,
} from "../types/aiJob";

export async function fetchAiJobsByDocument(params: {
  documentId: number;
  page: number;
  size: number;
}) {
  const { data } = await apiClient.get<AiJobListResponse>(
    `/api/v1/documents/${params.documentId}/ai-jobs`,
    {
      params: {
        page: params.page,
        size: params.size,
      },
    },
  );

  return data;
}

export async function fetchAiJobDetail(jobKey: string) {
  const { data } = await apiClient.get<AiJob>(`/api/v1/ai-jobs/${jobKey}`);

  return data;
}

export async function fetchAiJobFiles(jobKey: string) {
  const { data } = await apiClient.get<AiJobFileListResponse>(
    `/api/v1/ai-jobs/${jobKey}/files`,
  );

  return data;
}
