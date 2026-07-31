import { Upload } from "lucide-react";
import { useState } from "react";
import type { FileAssetType } from "../types/document";

type DocumentUploadFormProps = {
  isPending: boolean;
  onSubmit: (params: {
    title: string;
    file: File;
    assetType: FileAssetType;
  }) => void;
};

const uploadAssetTypes: FileAssetType[] = [
  "KOREAN_SOURCE_DOCUMENT",
  "PARALLEL_CORPUS",
  "GLOSS_DICTIONARY",
];

export default function DocumentUploadForm({
  isPending,
  onSubmit,
}: DocumentUploadFormProps) {
  const [title, setTitle] = useState("");
  const [assetType, setAssetType] = useState<FileAssetType>(
    "KOREAN_SOURCE_DOCUMENT",
  );
  const [file, setFile] = useState<File | null>(null);

  return (
    <form
      className="document-upload-form"
      onSubmit={(event) => {
        event.preventDefault();

        if (!file || title.trim().length === 0) {
          return;
        }

        onSubmit({
          title: title.trim(),
          file,
          assetType,
        });
        setTitle("");
        setFile(null);
        event.currentTarget.reset();
      }}
    >
      <label>
        문서 제목
        <input
          onChange={(event) => setTitle(event.target.value)}
          placeholder="국문 수어 변환 원천 문서"
          type="text"
          value={title}
        />
      </label>

      <label>
        파일 자산 타입
        <select
          onChange={(event) => setAssetType(event.target.value as FileAssetType)}
          value={assetType}
        >
          {uploadAssetTypes.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
      </label>

      <label>
        파일
        <input
          onChange={(event) => setFile(event.target.files?.[0] ?? null)}
          type="file"
        />
      </label>

      <button
        className="icon-text-button"
        disabled={isPending || !file || title.trim().length === 0}
        type="submit"
      >
        <Upload aria-hidden="true" size={16} />
        {isPending ? "업로드 중" : "업로드"}
      </button>
    </form>
  );
}
