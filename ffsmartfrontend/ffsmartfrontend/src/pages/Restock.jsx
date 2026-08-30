import { useEffect, useState } from "react";
import { toast } from "react-hot-toast";
import { useQuery } from "@tanstack/react-query";

export default function DeliveryPage() {
  const { data: authUser } = useQuery({ queryKey: ["authUser"] });
  const [deliveries, setDeliveries] = useState([]);
  const [formData, setFormData] = useState({ itemName: "", quantity: "" });

  // Fetch all deliveries on load
  useEffect(() => {
    fetchDeliveries();
  }, []);

  const fetchDeliveries = async () => {
    try {
      const response = await fetch("/api/delivery", {
        method: "GET",
        credentials: "include",
      });
      if (!response.ok) throw new Error("Failed to fetch deliveries");
      const data = await response.json();
      setDeliveries(data);
    } catch (error) {
      toast.error(error.message);
    }
  };

  // Create a new delivery
  const handleCreateDelivery = async (e) => {
    e.preventDefault();

    // Basic validation
    if (!formData.itemName || !formData.quantity) {
      toast.error("Please provide an item name and a quantity.");
      return;
    }

    try {
      const response = await fetch("/api/delivery", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          itemName: formData.itemName,
          quantity: Number(formData.quantity),
        }),
      });

      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || "Failed to create delivery");
      }
      const newDelivery = await response.json();
      setDeliveries((prev) => [...prev, newDelivery]);
      setFormData({ itemName: "", quantity: "" });
      toast.success("Delivery requested successfully");
    } catch (error) {
      toast.error(error.message);
    }
  };

  // Approve a delivery (Manager or HeadChef only)
  const handleApprove = async (deliveryId) => {
    try {
      const response = await fetch(`/api/delivery/${deliveryId}/approve`, {
        method: "PUT",
        credentials: "include",
      });
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || "Failed to approve delivery");
      }
      toast.success("Delivery approved");
      // Refresh list after approving
      fetchDeliveries();
    } catch (error) {
      toast.error(error.message);
    }
  };

  // Decline a delivery (Manager or HeadChef only)
  const handleDecline = async (deliveryId) => {
    try {
      const response = await fetch(`/api/delivery/${deliveryId}/decline`, {
        method: "PUT",
        credentials: "include",
      });
      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || "Failed to decline delivery");
      }
      toast.success("Delivery declined");
      // Refresh list after declining
      fetchDeliveries();
    } catch (error) {
      toast.error(error.message);
    }
  };

  // Only Manager or HeadChef can approve/decline
  const canApproveOrDecline = ["Manager", "HeadChef"].includes(authUser?.role);

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      {/* Header */}
      <header className="bg-white shadow-sm p-4 mb-6">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <h1 className="text-2xl font-semibold">Delivery Management</h1>
          <span className="text-sm">{authUser?.role}</span>
        </div>
      </header>

      {/* Create Delivery Form */}
      <div className="bg-white shadow p-6 rounded mb-6 max-w-xl mx-auto">
        <h2 className="text-lg font-medium mb-4">Request a New Delivery</h2>
        <form onSubmit={handleCreateDelivery} className="space-y-4">
          <div>
            <label
              htmlFor="itemName"
              className="block text-sm font-medium mb-1"
            >
              Item Name
            </label>
            <input
              id="itemName"
              name="itemName"
              value={formData.itemName}
              onChange={(e) =>
                setFormData({ ...formData, itemName: e.target.value })
              }
              className="w-full border border-gray-300 rounded p-2"
              placeholder="Enter the item name"
              required
            />
          </div>
          <div>
            <label
              htmlFor="quantity"
              className="block text-sm font-medium mb-1"
            >
              Quantity
            </label>
            <input
              id="quantity"
              name="quantity"
              type="number"
              min={1}
              value={formData.quantity}
              onChange={(e) =>
                setFormData({ ...formData, quantity: e.target.value })
              }
              className="w-full border border-gray-300 rounded p-2"
              placeholder="Enter the quantity"
              required
            />
          </div>
          <button type="submit" className="bg-black text-white px-4 py-2 rounded">
            Create Delivery
          </button>
        </form>
      </div>

      {/* Delivery List */}
      <div className="bg-white shadow p-6 rounded">
        <h2 className="text-lg font-medium mb-4">All Deliveries</h2>
        <table className="min-w-full border border-gray-300">
          <thead className="bg-gray-200">
            <tr>
              <th className="border px-4 py-2 text-left">Item Name</th>
              <th className="border px-4 py-2 text-left">Quantity</th>
              <th className="border px-4 py-2 text-left">Delivered By</th>
              <th className="border px-4 py-2 text-left">Delivery Date</th>
              <th className="border px-4 py-2 text-left">Approval Status</th>

              {/** Only show "Actions" column if role can approve/decline */}
              {canApproveOrDecline && <th className="border px-4 py-2 text-left">Actions</th>}
            </tr>
          </thead>
          <tbody>
            {deliveries.map((delivery) => {
              const { id, itemName, quantity, deliveredBy, deliveryDate, approvalStatus } =
                delivery;
              const showButtons =
                canApproveOrDecline && approvalStatus === "Pending";

              return (
                <tr key={id} className="border-t">
                  <td className="border px-4 py-2">{itemName}</td>
                  <td className="border px-4 py-2">{quantity}</td>
                  <td className="border px-4 py-2">{deliveredBy}</td>
                  <td className="border px-4 py-2">
                    {new Date(deliveryDate).toLocaleString()}
                  </td>
                  <td className="border px-4 py-2">{approvalStatus}</td>

                  {/** If role can approve/decline, show the last column */}
                  {canApproveOrDecline && (
                    <td className="border px-4 py-2 space-x-2">
                      {/** Show Approve/Decline only if still 'Pending' */}
                      {showButtons && (
                        <>
                          <button
                            onClick={() => handleApprove(id)}
                            className="bg-green-500 text-white px-2 py-1 rounded text-sm"
                          >
                            Approve
                          </button>

                          <button
                            onClick={() => handleDecline(id)}
                            className="bg-red-500 text-white px-2 py-1 rounded text-sm"
                          >
                            Decline
                          </button>
                        </>
                      )}
                    </td>
                  )}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
