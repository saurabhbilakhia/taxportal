import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { BottomNav } from './BottomNav';

interface ClientLayoutProps {
  title?: string;
  showBackButton?: boolean;
  backPath?: string;
}

export function ClientLayout({ title, showBackButton, backPath }: ClientLayoutProps) {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header title={title} showBackButton={showBackButton} backPath={backPath} />
      <main className="pb-20 md:pb-6">
        <div className="max-w-3xl mx-auto px-4 py-6">
          <Outlet />
        </div>
      </main>
      <BottomNav />
    </div>
  );
}
