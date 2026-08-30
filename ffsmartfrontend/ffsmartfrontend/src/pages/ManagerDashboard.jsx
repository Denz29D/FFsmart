import { useEffect, useState } from "react";
import { Users, ShieldCheck, AlertTriangle, TrendingUp } from "lucide-react";
import { DashboardLayout } from "../components/DashboardLayout";
import UserEditModal from "../components/UserEditModal";
import toast from "react-hot-toast";

export default function ManagerDashboard() {
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [isModalOpen, setModalOpen] = useState(false);

  // Fetch users on component mount
  useEffect(() => {
    fetch("/api/users", { credentials: "include" })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to load users");
        return res.json();
      })
      .then((data) => setUsers(data))
      .catch(() => toast.error("Failed to load users"));
  }, []);

  // Open the edit modal
  const handleEdit = (user) => {
    setSelectedUser(user);
    setModalOpen(true);
  };

  // Close modal
  const closeModal = () => {
    setSelectedUser(null);
    setModalOpen(false);
  };

  // Handle user update (PUT)
  const handleUserUpdate = (updatedUser) => {
    fetch(`/api/users/${updatedUser.id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(updatedUser),
    })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to update user");
        return res.json();
      })
      .then(() => {
        // Update user list locally
        setUsers((prev) =>
          prev.map((u) => (u.id === updatedUser.id ? updatedUser : u))
        );
        toast.success("User updated successfully");
        closeModal();
      })
      .catch(() => toast.error("Failed to update user"));
  };

  // **Handle user delete (DELETE)**
  const handleUserDelete = (userId) => {
    fetch(`/api/users/${userId}`, {
      method: "DELETE",
      credentials: "include",
    })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to delete user");
        return res.json();
      })
      .then(() => {
        // Remove user from local state
        setUsers((prev) => prev.filter((user) => user.id !== userId));
        toast.success("User deleted successfully");
        closeModal();
      })
      .catch(() => toast.error("Failed to delete user"));
  };

  // Generate report
  const generateReport = () => {
    fetch("/api/reports/management", { method: "POST", credentials: "include" })
      .then((res) => {
        if (!res.ok) throw new Error("Failed to generate report");
        return res.json();
      })
      .then(() => toast.success("Management report generated"))
      .catch(() => toast.error("Failed to generate report"));
  };

  return (
    <DashboardLayout title="Restaurant Manager Dashboard" userRole="Manager">
      {/* Summary Cards */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        <SummaryCard title="Active Users" value="18" icon={<Users />} description="3 new this week" />
        <SummaryCard title="Compliance Score" value="98%" icon={<ShieldCheck />} description="+2% from last month" />
        <SummaryCard title="Safety Alerts" value="2" icon={<AlertTriangle />} description="Require attention" />
        <SummaryCard title="Efficiency Rating" value="92%" icon={<TrendingUp />} description="+5% from last week" />
      </div>

      {/* User Management Table */}
      <div className="bg-white p-4 rounded-lg shadow mt-6">
        <h2 className="text-lg font-semibold">User Management</h2>
        <div className="overflow-x-auto mt-4">
          <table className="min-w-full border border-gray-300 rounded-lg">
            <thead className="bg-gray-100">
              <tr>
                <th className="border px-4 py-2">Name</th>
                <th className="border px-4 py-2">Role</th>
                <th className="border px-4 py-2">Status</th>
                <th className="border px-4 py-2">Last Active</th>
                <th className="border px-4 py-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td className="border px-4 py-2">{user.fullName}</td>
                  <td className="border px-4 py-2">{user.role}</td>
                  <td className="border px-4 py-2">
                    <span
                      className={`px-2 py-1 text-sm rounded ${
                        user.status === "Active"
                          ? "bg-green-200 text-green-800"
                          : "bg-gray-300 text-gray-800"
                      }`}
                    >
                      {user.status}
                    </span>
                  </td>
                  <td className="border px-4 py-2">{user.lastActive}</td>
                  <td className="border px-4 py-2">
                    <button
                      onClick={() => handleEdit(user)}
                      className="text-sm bg-gray-100 px-3 py-1 rounded hover:bg-gray-200"
                    >
                      Edit
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Generate Report Button */}
      <div className="mt-6">
        <button
          onClick={generateReport}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          Generate Management Report
        </button>
      </div>

      {/* User Edit Modal */}
      {isModalOpen && selectedUser && (
        <UserEditModal
          user={selectedUser}
          onClose={closeModal}
          onSave={handleUserUpdate}
          onDelete={handleUserDelete} // <--- pass in the DELETE handler
        />
      )}
    </DashboardLayout>
  );
}

// Summary card component
const SummaryCard = ({ title, value, icon, description }) => (
  <div className="bg-white p-4 rounded-lg shadow">
    <div className="flex justify-between items-center pb-2">
      <h3 className="text-sm font-medium">{title}</h3>
      {icon}
    </div>
    <div>
      <div className="text-2xl font-bold">{value}</div>
      <p className="text-xs text-gray-400">{description}</p>
    </div>
  </div>
);
