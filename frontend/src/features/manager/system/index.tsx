"use client";

import { RefreshCcw } from "lucide-react";
import ServiceStatusPanel from "./components/ServiceStatusPanel";
import { useSystemServicesQuery } from "./hooks/useSystemServicesQuery";

export default function SystemPage() {
  const { data, error, isFetching, isLoading, refetch } =
    useSystemServicesQuery();

  return (
    <section className="page-section">
      <div className="page-heading">
        <p className="eyebrow">System</p>
        <div className="page-heading-row">
          <div>
            <h1>서비스 상태</h1>
            <p>Java Platform API와 Python AI API의 연결 상태를 확인합니다.</p>
          </div>
          <button
            className="icon-text-button"
            disabled={isFetching}
            onClick={() => refetch()}
            type="button"
          >
            <RefreshCcw aria-hidden="true" size={16} />
            {isFetching ? "확인 중" : "새로고침"}
          </button>
        </div>
      </div>

      {isLoading ? (
        <div className="system-state-box">서비스 상태를 확인하는 중입니다.</div>
      ) : null}

      {error ? (
        <div className="system-state-box error">
          서비스 상태를 불러오지 못했습니다.
        </div>
      ) : null}

      {data ? (
        <div className="system-grid">
          <ServiceStatusPanel
            description="문서, 파일, AI Job 운영 API"
            service={data.platformApi}
            title="Platform API"
          />
          <ServiceStatusPanel
            description="국문 분석, 글로스 변환, 수어 모션 처리 API"
            service={data.aiApi}
            title="Python AI API"
          />
        </div>
      ) : null}
    </section>
  );
}
