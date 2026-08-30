import { Link, useNavigate } from "react-router-dom";
import { BarChart3, Box, History, LayoutDashboard, RefreshCw, Settings, Grid, LogOut } from "lucide-react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";

// Sidebar menu items
const menuItems = [
  { title: "Dashboard", to: "/dashboard", icon: Grid, description: "View your role-specific dashboard" },
  { title: "Menu", to: "/menu", icon: LayoutDashboard, description: "Access the main menu" },
  { title: "Inventory", to: "/inventory", icon: Box, description: "View the fridge inventory" },
  { title: "Restock", to: "/restock", icon: RefreshCw, description: "Update the fridge's stock" },
  { title: "User Logs", to: "/user-logs", icon: History, description: "View the fridge replenishment history" },
  { title: "Reports", to: "/reports", icon: BarChart3, description: "Get a report of what's been replenished" },
  { title: "Settings", to: "/settings", icon: Settings, description: "System settings" },
];

export function Sidebar() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { mutate: ProcessLogout } = useMutation({
    mutationFn: async () => {
      const res = await fetch("/api/auth/logout", {
        method: "POST",
        credentials: "include", // Include cookies
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.error || "Logout failed");
      }
    },
    onSuccess: () => {
      toast.success("Logged out successfully");
      queryClient.invalidateQueries({ queryKey: ["authUser"] });
      navigate("/"); // Redirect to login page
    },
    onError: () => {
      toast.error("Logout failed. Please try again.");
    },
  });

  const handleLogout = () => {
    ProcessLogout();
  };

  return (
    <div className="w-64 min-h-screen bg-white border-r flex flex-col justify-between">
      <div>
        <div className="p-4 border-b">
          <h1 className="text-xl font-bold">The FFsmart</h1>
        </div>
        <div className="py-4">
          <div className="px-4 mb-2 text-sm font-medium">Menu</div>
          {menuItems.map((item) => (
            <Link
              key={item.to}
              to={item.to}
              className="flex items-center gap-3 px-4 py-2 text-sm transition-colors hover:bg-gray-100"
            >
              <item.icon className="w-4 h-4" />
              <div>
                <div>{item.title}</div>
                <div className="text-xs text-gray-500">{item.description}</div>
              </div>
            </Link>
          ))}
        </div>
      </div>
      {/* Logout button */}
      <button
        onClick={handleLogout}
        className="flex items-center gap-3 px-4 py-3 text-sm text-red-500 hover:bg-gray-100 transition-colors"
      >
        <LogOut className="w-4 h-4" />
        <span>Logout</span>
      </button>
    </div>
  );
}
