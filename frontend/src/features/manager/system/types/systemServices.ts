export type ServiceHealthStatus = "UP" | "DOWN";

export type ServiceStatus = {
  status: ServiceHealthStatus | string;
  latencyMs: number | null;
  errorCode?: string | null;
  message?: string | null;
};

export type SystemServicesResponse = {
  platformApi: ServiceStatus;
  aiApi: ServiceStatus;
};
