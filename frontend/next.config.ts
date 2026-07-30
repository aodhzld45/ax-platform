import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/platform-api/:path*",
        destination: `${process.env.PLATFORM_API_BASE_URL ?? "http://localhost:8080"}/:path*`,
      },
    ];
  },
};

export default nextConfig;
