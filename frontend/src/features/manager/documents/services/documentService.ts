import { apiClient } from "@/shared/api/client";
import type {
  AiJobCreateResponse,
  AiJobType,
  DocumentItem,
  DocumentListParams,
  DocumentListResponse,
  DocumentUploadResponse,
  FileAssetType,
} from "../types/document";

export async function fetchDocuments(params: DocumentListParams) {
  const { data } = await apiClient.get<DocumentListResponse>(
    "/api/v1/documents",
    {
      params: {
        page: params.page,
        size: params.size,
        documentStatus: params.documentStatus || undefined,
        indexStatus: params.indexStatus || undefined,
      },
    },
  );

  return data;
}

export async function fetchDocumentDetail(documentId: number) {
  const { data } = await apiClient.get<DocumentItem>(
    `/api/v1/documents/${documentId}`,
  );

  return data;
}

export async function uploadDocument(params: {
  title: string;
  file: File;
  assetType: FileAssetType;
}) {
  const formData = new FormData();
  formData.append("title", params.title);
  formData.append("file", params.file);
  formData.append("assetType", params.assetType);

  const { data } = await apiClient.post<DocumentUploadResponse>(
    "/api/v1/documents",
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    },
  );

  return data;
}

export async function createDocumentAiJob(params: {
  documentId: number;
  jobType: AiJobType;
}) {
  const { data } = await apiClient.post<AiJobCreateResponse>(
    `/api/v1/documents/${params.documentId}/ai-jobs`,
    {
      jobType: params.jobType,
    },
  );

  return data;
}
