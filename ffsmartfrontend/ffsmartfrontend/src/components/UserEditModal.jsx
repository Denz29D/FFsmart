import { useState } from "react";

export default function UserEditModal({
  user,
  onClose,
  onSave,
  onDelete // <-- still needed for DELETE action
}) {
  const [formData, setFormData] = useState({
    ...user,
    permissions: user.permissions || { "User can't add/remove items": false },
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handlePermissionChange = (e) => {
    const { checked } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      permissions: {
        ...prevData.permissions,
        "User can't add/remove items": checked,
      },
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(formData);
  };

  const handleDeleteClick = () => {
    if (
      window.confirm(
        `Are you sure you want to delete user "${user.username}"? This action cannot be undone.`
      )
    ) {
      onDelete(user.id);
    }
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-gray-800 bg-opacity-50 z-50">
      <div className="bg-white p-6 rounded-lg shadow-lg max-w-md w-full">
        <h2 className="text-xl font-bold">Edit User</h2>
        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label className="block text-sm">Full Name</label>
            <input
              type="text"
              name="fullName"
              value={formData.fullName}
              onChange={handleChange}
              className="w-full border p-2 rounded"
            />
          </div>
          <div>
            <label className="block text-sm">Role</label>
            <select
              name="role"
              value={formData.role}
              onChange={handleChange}
              className="w-full border p-2 rounded"
            >
              <option value="HeadChef">Head Chef</option>
              <option value="Manager">Manager</option>
              <option value="Delivery">Delivery</option>
              <option value="Chef">Chef</option>
            </select>
          </div>
          <div>
            <label className="block text-sm">Status</label>
            <select
              name="status"
              value={formData.status || "Active"} // Fallback if undefined
              onChange={handleChange}
              className="w-full border p-2 rounded"
            >
              <option value="Active">Active</option>
              <option value="Inactive">Inactive</option>
            </select>
          </div>

          {/* Permissions Section */}
          <div>
            <label className="block text-sm font-semibold">Permissions</label>
            <div className="flex items-center">
              <input
                type="checkbox"
                checked={formData.permissions["User can't add/remove items"] || false}
                onChange={handlePermissionChange}
              />
              <span className="ml-2">User can't add/remove items</span>
            </div>
          </div>

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-700 bg-gray-200 rounded hover:bg-gray-300"
            >
              Cancel
            </button>

            {/* Always show Delete button (Manager-only page) */}
            <button
              type="button"
              onClick={handleDeleteClick}
              className="px-4 py-2 text-white bg-red-500 rounded hover:bg-red-600"
            >
              Delete
            </button>

            <button
              type="submit"
              className="px-4 py-2 text-white bg-blue-500 rounded hover:bg-blue-600"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
