import { ChevronLeft, ChevronRight } from "lucide-react";

type DocumentPaginationProps = {
  page: number;
  totalPages: number;
  totalCount: number;
  onPageChange: (page: number) => void;
};

export default function DocumentPagination({
  page,
  totalPages,
  totalCount,
  onPageChange,
}: DocumentPaginationProps) {
  const currentPage = page + 1;
  const safeTotalPages = Math.max(totalPages, 1);

  return (
    <div className="document-pagination">
      <span>총 {totalCount.toLocaleString("ko-KR")}건</span>
      <div>
        <button
          disabled={page <= 0}
          onClick={() => onPageChange(page - 1)}
          type="button"
        >
          <ChevronLeft aria-hidden="true" size={16} />
        </button>
        <strong>
          {currentPage} / {safeTotalPages}
        </strong>
        <button
          disabled={currentPage >= safeTotalPages}
          onClick={() => onPageChange(page + 1)}
          type="button"
        >
          <ChevronRight aria-hidden="true" size={16} />
        </button>
      </div>
    </div>
  );
}
