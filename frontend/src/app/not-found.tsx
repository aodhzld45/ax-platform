import Link from "next/link";

export default function NotFoundPage() {
  return (
    <main className="not-found-page">
      <section>
        <p className="eyebrow">404</p>
        <h1>페이지를 찾을 수 없습니다.</h1>
        <p>요청하신 관리자 화면이 존재하지 않거나 이동되었습니다.</p>
        <Link className="text-link" href="/dashboard">
          대시보드로 이동
        </Link>
      </section>
    </main>
  );
}
