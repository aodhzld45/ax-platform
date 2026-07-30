import type { Metadata } from "next";
import Providers from "./providers";
import "@/styles/globals.css";

export const metadata: Metadata = {
  title: "KLCUBE AX Platform",
  description: "KLCUBE AX Platform manager",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
