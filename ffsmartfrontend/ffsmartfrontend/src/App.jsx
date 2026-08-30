import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";

// Pages
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignUpPage";
import Inventory from "./pages/Inventory"
import Restock from "./pages/Restock";
import UserLogs from "./pages/UserLogs";
import ReportsPage from "./pages/ReportsPage";
import SettingsPage from "./pages/SettingsPage";
import HeadChefDashboard from "./pages/HeadChefDashboard";
import ManagerDashboard from "./pages/ManagerDashboard";
import DeliveryDashboard from "./pages/DeliveryDashboard";
import Dashboard from "./pages/Dashboard";
import NotificationsPage from "./pages/NotificationsPage";
import HealthAndSafetyDashboard from "./pages/HealthAndSafetyDashboard";  // New route
import { Layout } from "./Layout";
import { useQuery } from "@tanstack/react-query";

function getDashboardByRole(role) {
  switch (role) {
    case "HeadChef":
      return <HeadChefDashboard />;
    case "Manager":
      return <ManagerDashboard />;
    case "Delivery":
      return <DeliveryDashboard />;
    case "HealthAndSafetyOfficer": // New route
      return <HealthAndSafetyDashboard />;
    default:
      return <Navigate to="/menu" />;
  }
}

export default function App() {
  const { data: authUser, isLoading } = useQuery({
    queryKey: ["authUser"],
    queryFn: async () => {
      try {
        const res = await fetch("/api/auth/me", {
          method: "GET",
          credentials: "include", // Include cookies in request
        });

        if (res.status === 401) {
          // User is not authenticated
          return null;
        }

        const data = await res.json();

        if (!res.ok) {
          throw new Error(data.error || "Something went wrong");
        }
        return data;
      } catch (error) {
        console.error("Error fetching auth user:", error);
        return null;
      }
    },
    retry: false,
  });


  // Fetch alerts globally using React Query
// Fetch alerts only when the user is authenticated
const { data: alerts = [], refetch: refetchAlerts } = useQuery({
  queryKey: ["alerts"],
  queryFn: async () => {
    const res = await fetch("/api/alerts", {
      method: "GET",
      credentials: "include",
    });
    if (res.status === 401) {
      throw new Error("You are not authorized to access alerts. Please log in.");
    }
    if (!res.ok) {
      throw new Error("Failed to fetch alerts.");
    }
    return res.json();
  },
  enabled: !!authUser,
});


  if (isLoading) {
    // Show loading state while auth data is being fetched
    return (
      <div className="h-screen flex justify-center items-center">
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <Router>
      <Routes>
        {/* Public routes */}
        <Route
          path="/"
          element={!authUser ? <LoginPage /> : <Navigate to="/dashboard" />}
        />
        <Route
          path="/signup"
          element={!authUser ? <SignupPage /> : <Navigate to="/dashboard" />}
        />

        {/* Protected routes */}
        <Route
          path="/dashboard"
          element={
            authUser ? (
              <Layout>{getDashboardByRole(authUser.role)}</Layout>
            ) : (
              <Navigate to="/" />
            )
          }
        />
        <Route
          path="/menu"
          element={
            authUser ? (
              <Layout>
                <Dashboard />
              </Layout>
            ) : (
              <Navigate to="/" />
            )
          }
        />
        <Route
          path="/inventory"
          element={
            authUser ? (
              <Layout>
                <Inventory />
              </Layout>
            ) : (
              <Navigate to="/" />
            )
          }
        />
        <Route
          path="/restock"
          element={
            authUser ? (
              <Layout>
                <Restock />
              </Layout>
            ) : (
              <Navigate to="/" />
            )
          }
        />
        <Route
          path="/user-logs"
          element={
            authUser && ["Manager", "HealthAndSafetyOfficer"].includes(authUser.role) ? (
              <Layout>
                <UserLogs />
              </Layout>
            ) : (
              <Navigate to="/" />
            )
          }
        />

        <Route
          path="/reports"
          element={
            authUser ? (
              <Layout>
                <ReportsPage />
              </Layout>
            ) : (
              <Navigate to="/" />
            )
          }
        />
        <Route
          path="/settings"
          element={
            authUser && authUser.role === "Manager" ? (
              <Layout>
                <SettingsPage />
              </Layout>
            ) : (
              <Navigate to="/" />
            )
          }
        />
         {/* Notifications page */}
         <Route path="/notifications" 
            element={authUser ? <Layout>
          <NotificationsPage alerts={alerts}
           refetchAlerts={refetchAlerts} 
           />
          </Layout> 
          :
           <Navigate to="/" />} />
      </Routes>
    </Router>
  );
}



//   <Route element={<Layout />}>
//   <Route path="/dashboard" element={<DashboardPage />} />
//   <Route path="/reports" element={<ReportsPage />} />
//   <Route path="/settings" element={<SettingsPage />} />
// </Route>

