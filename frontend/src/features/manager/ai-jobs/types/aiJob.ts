import type { FileAssetType } from "../../documents/types/document";

export type AiJobStatus =
  | "PENDING"
  | "PROCESSING"
  | "COMPLETED"
  | "FAILED"
  | "RETRYING"
  | "CANCELLED";

export type AiJobStage =
  | "FILE_PREPARATION"
  | "TEXT_EXTRACTION"
  | "KOREAN_NORMALIZATION"
  | "GLOSS_GENERATION"
  | "MOTION_MAPPING"
  | "NON_MANUAL_MAPPING"
  | "AVATAR_TIMELINE_BUILD"
  | "RENDERING"
  | "RESULT_FINALIZATION";

export type AiJobType =
  | "KOREAN_TO_GLOSS"
  | "KOREAN_TO_SIGN_MOTION"
  | "KOREAN_TO_SIGN_AVATAR";

export type AiJobFileRole =
  | "TEXT_EXTRACTION_RESULT"
  | "NORMALIZED_KOREAN"
  | "GLOSS_SEQUENCE"
  | "MOTION_SEQUENCE"
  | "NON_MANUAL_SEQUENCE"
  | "AVATAR_TIMELINE"
  | "SIGN_VIDEO";

export type AiJob = {
  aiJobId: number;
  jobKey: string;
  documentId: number;
  documentTitle: string;
  jobType: AiJobType;
  status: AiJobStatus;
  currentStage: AiJobStage;
  progress: number;
  retryCount: number;
  maxRetryCount: number;
  resultJson: string | null;
  errorCode: string | null;
  errorMessage: string | null;
  requestedAt: string;
  startedAt: string | null;
  completedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type AiJobListResponse = {
  items: AiJob[];
  totalCount: number;
  totalPages: number;
};

export type AiJobFile = {
  aiJobFileId: number;
  jobKey: string;
  stage: AiJobStage;
  role: AiJobFileRole;
  fileMetadataId: number;
  assetType: FileAssetType;
  originalFileName: string;
  storedFileName: string;
  extension: string;
  contentType: string | null;
  fileSize: number;
  storageRelativePath: string;
  accessPath: string;
  createdAt: string;
};

export type AiJobFileListResponse = {
  items: AiJobFile[];
  totalCount: number;
};
