import axios from "axios";

export const apiClient = axios.create({
  baseURL: "/platform-api",
  headers: {
    "Content-Type": "application/json",
  },
});
