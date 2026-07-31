import { useQuery } from "@tanstack/react-query";
import { fetchSystemServices } from "../services/systemService";

export function useSystemServicesQuery() {
  return useQuery({
    queryKey: ["system", "services"],
    queryFn: fetchSystemServices,
    refetchInterval: 30_000,
  });
}
