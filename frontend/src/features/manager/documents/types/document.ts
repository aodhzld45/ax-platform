export type DocumentStatus = "ACTIVE" | "INACTIVE" | "DELETED";

export type DocumentIndexStatus =
  | "NOT_REQUESTED"
  | "REQUESTED"
  | "INDEXING"
  | "INDEXED"
  | "FAILED";

export type FileMetadataStatus = "ACTIVE" | "DELETED" | "FAILED";

export type FileAssetType =
  | "KOREAN_SOURCE_DOCUMENT"
  | "PARALLEL_CORPUS"
  | "GLOSS_DICTIONARY"
  | "GRAMMAR_RULE"
  | "SIGN_MOTION"
  | "NON_MANUAL_MOTION"
  | "AVATAR_MODEL"
  | "AVATAR_TEXTURE"
  | "AVATAR_CONFIG"
  | "AUDIO_INPUT"
  | "JOB_INPUT"
  | "JOB_INTERMEDIATE"
  | "JOB_OUTPUT";

export type DocumentFile = {
  fileMetadataId: number;
  assetType: FileAssetType;
  status: FileMetadataStatus;
  originalFileName: string;
  storedFileName: string;
  extension: string;
  contentType: string | null;
  fileSize: number;
  storageRelativePath: string;
  accessPath: string;
  checksumSha256: string | null;
};

export type DocumentItem = {
  documentId: number;
  resourceKey: string;
  version: number;
  title: string;
  documentStatus: DocumentStatus;
  indexStatus: DocumentIndexStatus;
  file: DocumentFile;
  createdAt: string;
  updatedAt: string;
};

export type DocumentListResponse = {
  items: DocumentItem[];
  totalCount: number;
  totalPages: number;
};

export type DocumentListParams = {
  page: number;
  size: number;
  documentStatus?: DocumentStatus | "";
  indexStatus?: DocumentIndexStatus | "";
};

export type DocumentUploadResponse = {
  documentId: number;
  resourceKey: string;
  version: number;
  title: string;
  originalFileName: string;
  accessPath: string;
  documentStatus: DocumentStatus;
  indexStatus: DocumentIndexStatus;
};

export type AiJobType =
  | "KOREAN_TO_GLOSS"
  | "KOREAN_TO_SIGN_MOTION"
  | "KOREAN_TO_SIGN_AVATAR";

export type AiJobCreateResponse = {
  aiJobId: number;
  jobKey: string;
  documentId: number;
  documentTitle: string;
  jobType: AiJobType;
  status: string;
  currentStage: string;
  progress: number;
};
