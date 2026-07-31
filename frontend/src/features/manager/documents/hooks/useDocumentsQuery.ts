import { useQuery } from "@tanstack/react-query";
import {
  createDocumentAiJob,
  fetchDocumentDetail,
  fetchDocuments,
  uploadDocument,
} from "../services/documentService";
import type { DocumentListParams } from "../types/document";

export function useDocumentsQuery(params: DocumentListParams) {
  return useQuery({
    queryKey: ["documents", params],
    queryFn: () => fetchDocuments(params),
  });
}

export function useDocumentDetailQuery(documentId: number | null) {
  return useQuery({
    queryKey: ["documents", "detail", documentId],
    queryFn: () => fetchDocumentDetail(documentId as number),
    enabled: documentId !== null,
  });
}

export const documentMutations = {
  uploadDocument,
  createDocumentAiJob,
};
