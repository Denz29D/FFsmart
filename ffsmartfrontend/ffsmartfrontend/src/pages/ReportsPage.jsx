import { useEffect, useState } from "react";
import { Calendar, Filter } from "lucide-react";
import { toast } from "react-hot-toast";

export default function ReportsPage() {
  const [inventory, setInventory] = useState([]);
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    try {
      setLoading(true);

      // Fetch inventory
      const inventoryResponse = await fetch("/api/inventory", { method: "GET", credentials: "include" });
      if (!inventoryResponse.ok) throw new Error("Failed to fetch inventory data");
      const inventoryData = await inventoryResponse.json();
      setInventory(inventoryData);

      // Fetch deliveries
      const deliveriesResponse = await fetch("/api/delivery", { method: "GET", credentials: "include" });
      if (!deliveriesResponse.ok) throw new Error("Failed to fetch deliveries data");
      const deliveriesData = await deliveriesResponse.json();
      setDeliveries(deliveriesData);

    } catch (err) {
      setError(err.message);
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  const downloadReport = async (url, type) => {
    try {
      const response = await fetch(url, { method: "GET", credentials: "include" });
      if (!response.ok) throw new Error(`Failed to download ${type} report`);

      const blob = await response.blob();
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.download = url.includes("pdf") ? `${type}_report.pdf` : `${type}_report.csv`;
      link.click();
    } catch (error) {
      toast.error(error.message);
    }
  };

  if (loading) {
    return <p>Loading reports...</p>;
  }

  if (error) {
    return <p className="text-red-500">Error: {error}</p>;
  }

  return (
    <div className="space-y-6">
      <div className="border-b">
        <button className="px-4 py-2 border-b-2 border-black">Ingredient Reports</button>
        <button className="px-4 py-2">Health and Safety Reports</button>
      </div>

      {/* Inventory Table */}
      <div className="space-y-4">
        <h2 className="text-lg font-medium">Inventory Report</h2>
        <table className="min-w-full border">
          <thead className="bg-gray-200">
            <tr>
              <th className="px-4 py-2 text-left">Item</th>
              <th className="px-4 py-2 text-left">Stock</th>
              <th className="px-4 py-2 text-left">Type</th>
              <th className="px-4 py-2 text-left">Cost USD</th>
              <th className="px-4 py-2 text-left">Expiry Date</th>
            </tr>
          </thead>
          <tbody>
            {inventory.map((item) => (
              <tr key={item.id} className="border-t">
                <td className="px-4 py-2">{item.itemName}</td>
                <td className="px-4 py-2">{item.quantity}</td>
                <td className="px-4 py-2">{item.type}</td>
                <td className="px-4 py-2">${item.cost?.toFixed(2) || "N/A"}</td>
                <td className="px-4 py-2">{new Date(item.expiryDate).toLocaleDateString()}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="flex gap-2">
          <button
            onClick={() => downloadReport("/api/reports/inventory/csv", "inventory")}
            className="px-4 py-2 bg-black text-white rounded"
          >
            Generate CSV
          </button>
          <button
            onClick={() => downloadReport("/api/reports/inventory/pdf", "inventory")}
            className="px-4 py-2 border rounded"
          >
            Generate PDF
          </button>
        </div>
      </div>

      {/* Deliveries Table */}
      <div className="space-y-4">
        <h2 className="text-lg font-medium">Deliveries Report</h2>
        <table className="min-w-full border">
          <thead className="bg-gray-200">
            <tr>
              <th className="px-4 py-2 text-left">Delivery ID</th>
              <th className="px-4 py-2 text-left">Item</th>
              <th className="px-4 py-2 text-left">Quantity</th>
              <th className="px-4 py-2 text-left">Delivered By</th>
              <th className="px-4 py-2 text-left">Approval Status</th>
              <th className="px-4 py-2 text-left">Delivery Date</th>
            </tr>
          </thead>
          <tbody>
            {deliveries.map((delivery) => (
              <tr key={delivery.id} className="border-t">
                <td className="px-4 py-2">{delivery.id}</td>
                <td className="px-4 py-2">{delivery.itemName}</td>
                <td className="px-4 py-2">{delivery.quantity}</td>
                <td className="px-4 py-2">{delivery.deliveredBy}</td>
                <td className="px-4 py-2">
                  <span
                    className={`px-2 py-1 rounded ${
                      delivery.approvalStatus === "Pending"
                        ? "bg-yellow-200 text-yellow-800"
                        : delivery.approvalStatus === "Approved"
                        ? "bg-green-200 text-green-800"
                        : "bg-red-200 text-red-800"
                    }`}
                  >
                    {delivery.approvalStatus}
                  </span>
                </td>
                <td className="px-4 py-2">{new Date(delivery.deliveryDate).toLocaleDateString()}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="flex gap-2">
          <button
            onClick={() => downloadReport("/api/reports/deliveries/csv", "deliveries")}
            className="px-4 py-2 bg-black text-white rounded"
          >
            Generate CSV
          </button>
          <button
            onClick={() => downloadReport("/api/reports/deliveries/pdf", "deliveries")}
            className="px-4 py-2 border rounded"
          >
            Generate PDF
          </button>
        </div>
      </div>
    </div>
  );
}
