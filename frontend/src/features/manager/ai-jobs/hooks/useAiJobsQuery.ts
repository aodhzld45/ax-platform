import { useQuery } from "@tanstack/react-query";
import {
  fetchAiJobDetail,
  fetchAiJobFiles,
  fetchAiJobsByDocument,
} from "../services/aiJobService";
import type { AiJobStatus } from "../types/aiJob";

const pollingStatuses: AiJobStatus[] = ["PENDING", "PROCESSING", "RETRYING"];

export function useAiJobsByDocumentQuery(params: {
  documentId: number | null;
  page: number;
  size: number;
}) {
  return useQuery({
    queryKey: ["ai-jobs", "document", params],
    queryFn: () =>
      fetchAiJobsByDocument({
        documentId: params.documentId as number,
        page: params.page,
        size: params.size,
      }),
    enabled: params.documentId !== null,
    refetchInterval: 10_000,
  });
}

export function useAiJobDetailQuery(jobKey: string | null) {
  return useQuery({
    queryKey: ["ai-jobs", "detail", jobKey],
    queryFn: () => fetchAiJobDetail(jobKey as string),
    enabled: jobKey !== null,
    refetchInterval: (query) => {
      const status = query.state.data?.status;

      return status && pollingStatuses.includes(status) ? 5_000 : false;
    },
  });
}

export function useAiJobFilesQuery(jobKey: string | null) {
  return useQuery({
    queryKey: ["ai-jobs", "files", jobKey],
    queryFn: () => fetchAiJobFiles(jobKey as string),
    enabled: jobKey !== null,
    refetchInterval: 10_000,
  });
}
