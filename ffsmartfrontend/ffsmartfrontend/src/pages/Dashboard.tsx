import { useState } from 'react';

export default function Dashboard() {
  const [userRole] = useState('User'); // Simulating a user role

  return (
    <div>
      {/* Cards Section */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {/* Total Items Card */}
        <div className="bg-white shadow rounded-lg p-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-medium">Total Items</h2>
            <span className="text-gray-400">&#128717;</span>
          </div>
          <div className="mt-2 text-2xl font-bold">1,234</div>
          <p className="text-xs text-gray-500">across all fridges</p>
        </div>

        {/* Items to Restock Card */}
        <div className="bg-white shadow rounded-lg p-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-medium">Items to Restock</h2>
            <span className="text-gray-400">&#128722;</span>
          </div>
          <div className="mt-2 text-2xl font-bold">23</div>
          <p className="text-xs text-gray-500">
            <span className="text-red-500 font-medium">5 urgent</span>
          </p>
        </div>

        {/* Expiring Soon Card */}
        <div className="bg-white shadow rounded-lg p-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-medium">Expiring Soon</h2>
            <span className="text-gray-400">&#9888;</span>
          </div>
          <div className="mt-2 text-2xl font-bold">17</div>
          <p className="text-xs text-gray-500">within 3 days</p>
        </div>

        {/* Active Users Card */}
        <div className="bg-white shadow rounded-lg p-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-medium">Active Users</h2>
            <span className="text-gray-400">&#128101;</span>
          </div>
          <div className="mt-2 text-2xl font-bold">12</div>
          <p className="text-xs text-gray-500">in last 24 hours</p>
        </div>
      </div>

      {/* Tabs Section */}
      <div className="mt-6">
        <div className="border-b">
          <button className="px-4 py-2 border-b-2 border-blue-500">Overview</button>
          <button className="px-4 py-2">Alerts</button>
          <button className="px-4 py-2">Recent Activity</button>
        </div>

        {/* Overview Content */}
        <div className="mt-4">
          <div className="bg-white shadow rounded-lg p-4">
            <h3 className="text-lg font-bold mb-4">Fridge Status Overview</h3>
            {['Main Kitchen', 'Prep Area', 'Bar'].map((fridge) => (
              <div key={fridge} className="flex justify-between items-center mb-2">
                <span>{fridge} Fridge</span>
                <div className="flex items-center gap-2">
                  <span className="text-sm border px-2 py-1 rounded-full">75% Full</span>
                  <span className="text-sm border px-2 py-1 rounded-full">2°C</span>
                  <button className="bg-blue-500 text-white text-sm px-3 py-1 rounded">
                    View Details
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Alerts Content */}
        <div className="mt-4 hidden">
          <div className="bg-white shadow rounded-lg p-4">
            <h3 className="text-lg font-bold mb-4">Recent Alerts</h3>
            <ul>
              <li className="flex justify-between items-center">
                <span className="text-red-500">Temperature High</span>
                <span className="text-gray-500 text-sm">Main Kitchen Fridge - 10 mins ago</span>
              </li>
              <li className="flex justify-between items-center mt-2">
                <span className="text-yellow-500">Low Stock</span>
                <span className="text-gray-500 text-sm">Tomatoes - 1 hour ago</span>
              </li>
              <li className="flex justify-between items-center mt-2">
                <span className="text-orange-500">Expiring Soon</span>
                <span className="text-gray-500 text-sm">Milk - 2 hours ago</span>
              </li>
            </ul>
          </div>
        </div>

        {/* Activity Content */}
        <div className="mt-4 hidden">
          <div className="bg-white shadow rounded-lg p-4">
            <h3 className="text-lg font-bold mb-4">Recent Activity</h3>
            <ul>
              <li className="flex justify-between items-center">
                <span>Restocked Vegetables</span>
                <span className="text-gray-500 text-sm">John Doe - 30 mins ago</span>
              </li>
              <li className="flex justify-between items-center mt-2">
                <span>Updated Inventory Count</span>
                <span className="text-gray-500 text-sm">Jane Smith - 1 hour ago</span>
              </li>
              <li className="flex justify-between items-center mt-2">
                <span>Removed Expired Items</span>
                <span className="text-gray-500 text-sm">Mike Johnson - 2 hours ago</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      {/* Footer Buttons */}
      <div className="mt-6 flex space-x-4">
        <button className="bg-blue-500 text-white px-4 py-2 rounded">Generate Report</button>
        <button className="border px-4 py-2 rounded">Manage Alerts</button>
      </div>
    </div>
  );
}
