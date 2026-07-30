"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  Activity,
  BriefcaseBusiness,
  FileText,
  LayoutDashboard,
} from "lucide-react";

const navigationItems = [
  {
    href: "/dashboard",
    label: "대시보드",
    icon: LayoutDashboard,
  },
  {
    href: "/documents",
    label: "문서 관리",
    icon: FileText,
  },
  {
    href: "/ai-jobs",
    label: "AI Job",
    icon: BriefcaseBusiness,
  },
  {
    href: "/system",
    label: "서비스 상태",
    icon: Activity,
  },
];

export default function AdminShell({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const pathname = usePathname();

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <span>KLCUBE</span>
          <strong>AX Platform</strong>
        </div>
        <nav className="admin-nav" aria-label="관리자 메뉴">
          {navigationItems.map((item) => {
            const Icon = item.icon;
            const active = pathname === item.href;

            return (
              <Link
                aria-current={active ? "page" : undefined}
                className={active ? "admin-nav-link active" : "admin-nav-link"}
                href={item.href}
                key={item.href}
              >
                <Icon aria-hidden="true" size={18} />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </aside>
      <div className="admin-main">
        <header className="admin-header">
          <div>
            <span className="eyebrow">Manager</span>
            <strong>수어 3D 아바타 생성 운영 콘솔</strong>
          </div>
        </header>
        <main>{children}</main>
      </div>
    </div>
  );
}
