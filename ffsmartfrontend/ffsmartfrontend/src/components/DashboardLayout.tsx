import { ReactNode } from "react";
import { Bell, Menu, Settings } from "lucide-react";

interface DashboardLayoutProps {
  children: ReactNode;
  title: string;
  userRole: string;
}

export function DashboardLayout({ children, title, userRole }: DashboardLayoutProps) {
  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
          <div className="flex items-center">
            <button className="p-2 rounded-full hover:bg-gray-200 lg:hidden">
              <Menu className="h-6 w-6" />
            </button>
            <h1 className="text-2xl font-semibold text-gray-900">{title}</h1>
          </div>
          <div className="flex items-center">
            <button className="p-2 rounded-full hover:bg-gray-200">
              <Bell className="h-5 w-5" />
            </button>
            <button className="p-2 rounded-full hover:bg-gray-200 mx-4">
              <Settings className="h-5 w-5" />
            </button>
            <div className="flex items-center">
              <img
                src="/placeholder-avatar.jpg"
                alt="User"
                className="h-8 w-8 rounded-full object-cover"
              />
              <span className="ml-2 text-sm">{userRole}</span>
            </div>
          </div>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
    </div>
  );
}
