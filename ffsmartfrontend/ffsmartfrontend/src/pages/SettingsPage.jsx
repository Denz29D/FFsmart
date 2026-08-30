import { useEffect, useState } from "react";
import { Eye, Pencil, Trash } from "lucide-react";
import { toast } from "react-hot-toast";
import AddUserModal from "../components/AddUserModal";

export default function SettingsPage() {
  const [selectedTab, setSelectedTab] = useState("manage-users");
  const [users, setUsers] = useState([]);
  const [showModal, setShowModal] = useState(false);

  // Fetch users when the component loads
  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await fetch("/api/users", {
        method: "GET",
        credentials: "include", // Include auth credentials if needed
      });

      if (!response.ok) {
        throw new Error("Failed to fetch users");
      }

      const data = await response.json();
      setUsers(data);
    } catch (error) {
      toast.error(error.message);
    }
  };

  const handleAddUser = async (newUser) => {
    try {
      const response = await fetch("/api/users", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify(newUser),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || "Failed to add user");
      }

      toast.success("User added successfully");
      fetchUsers(); // Refresh the user list
      setShowModal(false); // Close the modal
    } catch (error) {
      toast.error(error.message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Settings</h1>
      </div>

      {/* Tabs */}
      <div className="border-b">
        <button
          className={`px-4 py-2 ${selectedTab === "manage-users" ? "border-b-2 border-black" : ""}`}
          onClick={() => setSelectedTab("manage-users")}
        >
          Manage Users
        </button>
        <button
          className={`px-4 py-2 ${selectedTab === "system-data" ? "border-b-2 border-black" : ""}`}
          onClick={() => setSelectedTab("system-data")}
        >
          System Data
        </button>
      </div>

      {selectedTab === "manage-users" && (
        <div className="space-y-4">
          {/* User Table */}
          <table className="min-w-full border">
            <thead className="bg-gray-200">
              <tr>
                <th className="px-4 py-2 text-left">User</th>
                <th className="px-4 py-2 text-left">Role</th>
                <th className="px-4 py-2 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className="border-t">
                  <td className="px-4 py-2 flex items-center gap-2">
                    <img src= "/avatar-placeholder.png" alt="Avatar" className="w-8 h-8 rounded-full" />
                    {user.fullName || user.username}
                  </td>
                  <td className="px-4 py-2">{user.role}</td>
                  <td className="px-4 py-2 text-right space-x-2">
                    <button className="p-2 bg-gray-100 rounded">
                      <Eye className="h-4 w-4" />
                    </button>
                    <button className="p-2 bg-gray-100 rounded">
                      <Pencil className="h-4 w-4" />
                    </button>
                    <button className="p-2 bg-gray-100 rounded">
                      <Trash className="h-4 w-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <button
            onClick={() => setShowModal(true)}
            className="px-4 py-2 bg-black text-white rounded"
          >
            Add User
          </button>
        </div>
      )}

      {/* Add User Modal */}
      {showModal && <AddUserModal onClose={() => setShowModal(false)} onSubmit={handleAddUser} />}
    </div>
  );
}
