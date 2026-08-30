import { useEffect, useState } from 'react';
import { Truck, Box, CheckCircle } from "lucide-react";
import { toast } from 'react-hot-toast';

export default function HeadChefDashboard() {
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDeliveries();
  }, []);

  const fetchDeliveries = async () => {
    try {
      const response = await fetch('/api/delivery', {
        method: 'GET',
        credentials: 'include',
      });
      if (!response.ok) throw new Error('Failed to fetch deliveries');
      const data = await response.json();
      setDeliveries(data);
    } catch (error) {
      toast.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleApproval = async (id, action) => {
    try {
      const endpoint = action === 'approve' ? `/api/delivery/${id}/approve` : `/api/delivery/${id}/decline`;
      const response = await fetch(endpoint, {
        method: 'PUT',
        credentials: 'include',
      });
      if (!response.ok) throw new Error(`Failed to ${action} delivery`);
      toast.success(`Delivery ${action}d successfully`);
      fetchDeliveries(); // Refresh deliveries after action
    } catch (error) {
      toast.error(error.message);
    }
  };

  if (loading) {
    return <p>Loading deliveries...</p>;
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
          <h1 className="text-2xl font-semibold">Head Chef Dashboard</h1>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Summary Cards */}
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          <div className="bg-white p-4 rounded-lg shadow">
            <h3 className="text-sm font-medium">Pending Deliveries</h3>
            <div className="text-2xl font-bold">{deliveries.filter(d => d.approvalStatus === 'Pending').length}</div>
          </div>
          {/* Other summary cards */}
        </div>

        {/* Deliveries Table */}
        <div className="mt-6 bg-white p-4 rounded-lg shadow">
          <h2 className="text-lg font-semibold mb-4">Deliveries</h2>
          <table className="min-w-full border border-gray-300 rounded-lg">
            <thead className="bg-gray-100">
              <tr>
                <th className="border px-4 py-2 text-left">Delivery ID</th>
                <th className="border px-4 py-2 text-left">Item Name</th>
                <th className="border px-4 py-2 text-left">Quantity</th>
                <th className="border px-4 py-2 text-left">Status</th>
                <th className="border px-4 py-2 text-left">Actions</th>
              </tr>
            </thead>
            <tbody>
              {deliveries.map(delivery => (
                <tr key={delivery.id}>
                  <td className="border px-4 py-2">{delivery.id}</td>
                  <td className="border px-4 py-2">{delivery.itemName}</td>
                  <td className="border px-4 py-2">{delivery.quantity}</td>
                  <td className="border px-4 py-2">{delivery.approvalStatus}</td>
                  <td className="border px-4 py-2">
                    {delivery.approvalStatus === 'Pending' && (
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleApproval(delivery.id, 'approve')}
                          className="bg-green-500 text-white px-2 py-1 rounded"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => handleApproval(delivery.id, 'decline')}
                          className="bg-red-500 text-white px-2 py-1 rounded"
                        >
                          Decline
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}
