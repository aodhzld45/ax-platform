import { apiClient } from "@/shared/api/client";
import type { SystemServicesResponse } from "../types/systemServices";

export async function fetchSystemServices() {
  const { data } = await apiClient.get<SystemServicesResponse>(
    "/api/v1/system/services",
  );

  return data;
}
