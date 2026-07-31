import { AlertCircle, CheckCircle2 } from "lucide-react";
import type { ServiceStatus } from "../types/systemServices";

type ServiceStatusPanelProps = {
  title: string;
  description: string;
  service: ServiceStatus;
};

export default function ServiceStatusPanel({
  title,
  description,
  service,
}: ServiceStatusPanelProps) {
  const isUp = service.status === "UP";
  const StatusIcon = isUp ? CheckCircle2 : AlertCircle;

  return (
    <article className="service-panel">
      <div className="service-panel-header">
        <div>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
        <span className={isUp ? "status-pill up" : "status-pill down"}>
          <StatusIcon aria-hidden="true" size={16} />
          {service.status}
        </span>
      </div>

      <dl className="service-metrics">
        <div>
          <dt>응답 시간</dt>
          <dd>
            {service.latencyMs === null ? "-" : `${service.latencyMs}ms`}
          </dd>
        </div>
        <div>
          <dt>오류 코드</dt>
          <dd>{service.errorCode ?? "-"}</dd>
        </div>
      </dl>

      {service.message ? (
        <p className="service-message">{service.message}</p>
      ) : null}
    </article>
  );
}
