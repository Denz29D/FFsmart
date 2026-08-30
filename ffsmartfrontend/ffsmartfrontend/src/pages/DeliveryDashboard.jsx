import { useEffect, useState } from 'react';
import { DashboardLayout } from "../components/DashboardLayout";
import { Truck, Box, CheckCircle } from "lucide-react";
import { toast } from 'react-hot-toast';
import LogDeliveryModal from "../components/LogDeliveryModal";

export default function DeliveryDashboard() {
  const [deliveries, setDeliveries] = useState([]);
  const [pendingCount, setPendingCount] = useState(0);
  const [completedToday, setCompletedToday] = useState(0);
  const [itemsToRestock, setItemsToRestock] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDeliveries();
  }, []);

  const fetchDeliveries = async () => {
    try {
      const response = await fetch('/api/delivery', {
        method: 'GET',
        credentials: 'include', // Include credentials if needed for auth
      });

      if (!response.ok) {
        throw new Error('Failed to fetch deliveries');
      }

      const data = await response.json();
      setDeliveries(data);
      calculateSummary(data);
    } catch (error) {
      setError(error.message);
      toast.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleNewDelivery = async (newDelivery) => {
    try {
      const response = await fetch('/api/delivery', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(newDelivery),
      });
      if (!response.ok) throw new Error('Failed to create new delivery');
      toast.success('New delivery logged successfully');
      setIsModalOpen(false);
      fetchDeliveries(); // Refresh deliveries after new delivery
    } catch (error) {
      toast.error(error.message);
    }
  };

  const calculateSummary = (data) => {
    const pending = data.filter(delivery => delivery.status === 'Pending').length;
    const completed = data.filter(delivery => new Date(delivery.completedAt)?.toDateString() === new Date().toDateString()).length;
    const restockItems = data.reduce((total, delivery) => total + delivery.quantity, 0);

    setPendingCount(pending);
    setCompletedToday(completed);
    setItemsToRestock(restockItems);
  };

  if (loading) {
    return (
      <DashboardLayout title="Delivery Dashboard" userRole="Delivery">
        <p>Loading...</p>
      </DashboardLayout>
    );
  }

  if (error) {
    return (
      <DashboardLayout title="Delivery Dashboard" userRole="Delivery">
        <p className="text-red-500">{error}</p>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout title="Delivery Dashboard" userRole="Delivery">
      {/* Summary Cards */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {/* Pending Deliveries */}
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex justify-between items-center pb-2">
            <h3 className="text-sm font-medium">Pending Deliveries</h3>
            <Truck className="h-4 w-4 text-gray-400" />
          </div>
          <div>
            <div className="text-2xl font-bold">{pendingCount}</div>
            <p className="text-xs text-gray-400">To be completed today</p>
          </div>
        </div>

        {/* Items to Restock */}
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex justify-between items-center pb-2">
            <h3 className="text-sm font-medium">Items to Restock</h3>
            <Box className="h-4 w-4 text-gray-400" />
          </div>
          <div>
            <div className="text-2xl font-bold">{itemsToRestock}</div>
            <p className="text-xs text-gray-400">Across all deliveries</p>
          </div>
        </div>

        {/* Completed Today */}
        <div className="bg-white p-4 rounded-lg shadow">
          <div className="flex justify-between items-center pb-2">
            <h3 className="text-sm font-medium">Completed Today</h3>
            <CheckCircle className="h-4 w-4 text-gray-400" />
          </div>
          <div>
            <div className="text-2xl font-bold">{completedToday}</div>
            <p className="text-xs text-gray-400">Deliveries processed</p>
          </div>
        </div>
      </div>

      {/* Upcoming Deliveries Table */}
      <div className="mt-6 bg-white p-4 rounded-lg shadow">
        <h2 className="text-lg font-semibold mb-4">Upcoming Deliveries</h2>
        <table className="min-w-full border-collapse border border-gray-300 rounded-lg">
          <thead className="bg-gray-100">
            <tr>
              <th className="border border-gray-300 px-4 py-2 text-left text-sm font-medium">Delivery ID</th>
              <th className="border border-gray-300 px-4 py-2 text-left text-sm font-medium">Restaurant</th>
              <th className="border border-gray-300 px-4 py-2 text-left text-sm font-medium">Items</th>
              <th className="border border-gray-300 px-4 py-2 text-left text-sm font-medium">Status</th>
              <th className="border border-gray-300 px-4 py-2 text-left text-sm font-medium">Actions</th>
            </tr>
          </thead>
          <tbody>
            {deliveries.map((delivery) => (
              <tr key={delivery.id} className="border-t">
                <td className="border border-gray-300 px-4 py-2">{delivery.id}</td>
                <td className="border border-gray-300 px-4 py-2">{delivery.restaurantName || 'Unknown'}</td>
                <td className="border border-gray-300 px-4 py-2">{delivery.quantity}</td>
                <td className="border border-gray-300 px-4 py-2">
                  <span
                    className={`px-2 py-1 text-sm rounded ${
                      delivery.status === 'Pending'
                        ? 'bg-yellow-200 text-yellow-800'
                        : 'bg-blue-200 text-blue-800'
                    }`}
                  >
                    {delivery.approvalStatus}
                  </span>
                </td>
                <td className="border border-gray-300 px-4 py-2">
                  <button className="text-sm bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600">
                    {delivery.approvalStatus === 'Pending' ? 'Start Delivery' : 'View Details'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Log New Delivery Button */}
      <div className="mt-6">
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          Log New Delivery
        </button>
      </div>

      {/* Log Delivery Modal */}
      {isModalOpen && (
        <LogDeliveryModal
          onClose={() => setIsModalOpen(false)}
          onSubmit={handleNewDelivery}
        />
      )}
    </DashboardLayout>
  );
}
