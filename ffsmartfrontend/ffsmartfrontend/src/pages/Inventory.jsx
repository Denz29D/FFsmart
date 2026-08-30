import { useEffect, useState } from 'react';
import InventoryEditModal from '../components/InventoryEditModal'; // Edit Modal Component
import { toast } from 'react-hot-toast'; // For showing notifications
import { useQuery } from '@tanstack/react-query';
export default function Inventory() {
  const { data: authUser } = useQuery({ queryKey: ['authUser'] });
  const [selectedTab, setSelectedTab] = useState('all');
  const [inventoryData, setInventoryData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const [selectedItem, setSelectedItem] = useState(null); // For modal editing

  useEffect(() => {
    fetchInventory();
  }, []);

  useEffect(() => {
    if (selectedTab === 'expiring') {
      filterExpiringSoonItems();
    } else if (selectedTab === 'low') {
      filterLowStockItems();
    } else {
      setFilteredData(inventoryData);
    }
  }, [selectedTab, inventoryData]);

  const fetchInventory = async () => {
    try {
      const response = await fetch('/api/inventory', {
        method: 'GET',
        credentials: 'include', // To include JWT cookies if required
      });
      if (!response.ok) {
        throw new Error('Failed to fetch inventory');
      }
      const data = await response.json();
      setInventoryData(data);
    } catch (error) {
      toast.error(error.message);
    }
  };

  const determineStockStatus = (quantity, threshold) => {
    if (quantity <= threshold / 2) {
      return 'Low';
    } else if (quantity > threshold / 2 && quantity <= threshold) {
      return 'Medium';
    }
    return 'High';
  };

  const filterLowStockItems = () => {
    const lowStockItems = inventoryData.filter((item) => determineStockStatus(item.quantity, item.thresholdQuantity) === 'Low');
    setFilteredData(lowStockItems);
  };

  const filterExpiringSoonItems = () => {
    const today = new Date();
    const filteredItems = inventoryData.filter((item) => {
      const expiryDate = new Date(item.expiryDate);
      const daysUntilExpiry = Math.ceil((expiryDate - today) / (1000 * 60 * 60 * 24)); // Calculate days difference
      return daysUntilExpiry === 5 || daysUntilExpiry === 3 || daysUntilExpiry === 0;
    });
    setFilteredData(filteredItems);
  };

  const handleAddItem = async (e) => {
    e.preventDefault();
    if (authUser?.permissions?.["User can't add/remove items"]) {
      toast.error("You don't have permission to add items.");
      return;
    }
    const formData = new FormData(e.target);
    const newItem = {
      itemName: formData.get('itemName'),
      quantity: Number(formData.get('quantity')),
      expiryDate: formData.get('expiryDate'),
      fridgeLocation: formData.get('fridgeLocation'),
      type: formData.get('type'),
      thresholdQuantity: Number(formData.get('thresholdQuantity')),
    };

    try {
      const response = await fetch('/api/inventory', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(newItem),
      });
      if (!response.ok) {
        throw new Error('Failed to add item');
      }
      await fetchInventory(); // Refresh the inventory
      toast.success('Item added successfully');
    } catch (error) {
      toast.error(error.message);
    }
  };

  const handleEditItem = async (updatedItem) => {
    try {
      const response = await fetch(`/api/inventory/${updatedItem.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(updatedItem),
      });

      if (!response.ok) {
        throw new Error('Failed to update item');
      }

      await fetchInventory(); // Refresh the inventory list
      setSelectedItem(null); // Close the modal
      toast.success('Item updated successfully');
    } catch (error) {
      toast.error(error.message);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <header className="bg-white shadow-sm p-4 mb-6">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <h1 className="text-2xl font-semibold">Inventory Management</h1>
          <span className="text-sm">{authUser.role}</span>
        </div>
      </header>

      {/* Quick Add Item Form */}
      <div className="bg-white shadow p-6 rounded mb-6">
        <h2 className="text-lg font-medium mb-4">Quick Add Item</h2>
        <form onSubmit={handleAddItem} className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label htmlFor="itemName" className="block text-sm font-medium mb-1">Item Name</label>
            <input id="itemName" name="itemName" placeholder="Enter item name" className="w-full border border-gray-300 rounded p-2" required disabled={authUser?.permissions?.["User can't add/remove items"]} />
          </div>
          <div>
            <label htmlFor="quantity" className="block text-sm font-medium mb-1">Quantity</label>
            <input id="quantity" name="quantity" type="number" placeholder="Enter quantity" className="w-full border border-gray-300 rounded p-2" required disabled={authUser?.permissions?.["User can't add/remove items"]} />
          </div>
          <div>
            <label htmlFor="expiryDate" className="block text-sm font-medium mb-1">Expiry Date</label>
            <input id="expiryDate" name="expiryDate" type="date" className="w-full border border-gray-300 rounded p-2" required disabled={authUser?.permissions?.["User can't add/remove items"]} />
          </div>
          <div>
            <label htmlFor="fridgeLocation" className="block text-sm font-medium mb-1">Fridge Location</label>
            <input id="fridgeLocation" name="fridgeLocation" placeholder="Enter fridge location" className="w-full border border-gray-300 rounded p-2" required disabled={authUser?.permissions?.["User can't add/remove items"]} />
          </div>
          <div>
            <label htmlFor="type" className="block text-sm font-medium mb-1">Type</label>
            <select id="type" name="type" className="w-full border border-gray-300 rounded p-2" required>
              <option value="Vegetable">Vegetable</option>
              <option value="Dairy">Dairy</option>
              <option value="Meat">Meat</option>
              <option value="Beverage">Beverage</option>
            </select>
          </div>
          <div>
            <label htmlFor="thresholdQuantity" className="block text-sm font-medium mb-1">Threshold Quantity</label>
            <input id="thresholdQuantity" name="thresholdQuantity" type="number" placeholder="Enter threshold quantity" className="w-full border border-gray-300 rounded p-2" required />
          </div>
          <div className="col-span-4">
            <button type="submit" className="bg-black text-white px-4 py-2 rounded" disabled={authUser?.permissions?.["User can't add/remove items"]} >Add Item</button>
          </div>
        </form>
      </div>

      {/* Tabs */}
      <div>
        <div className="border-b mb-6 flex">
          <button className={`px-4 py-2 ${selectedTab === 'all' ? 'border-b-2 border-black' : ''}`} onClick={() => setSelectedTab('all')}>All Items</button>
          <button className={`px-4 py-2 ${selectedTab === 'expiring' ? 'border-b-2 border-black' : ''}`} onClick={() => setSelectedTab('expiring')}>Expiring Soon</button>
          <button className={`px-4 py-2 ${selectedTab === 'low' ? 'border-b-2 border-black' : ''}`} onClick={() => setSelectedTab('low')}>Low Stock</button>
        </div>

        {/* Inventory Table */}
        <div className="bg-white shadow p-6 rounded">
          <h2 className="text-lg font-medium mb-4">{selectedTab === 'expiring' ? 'Expiring Soon Items' : selectedTab === 'low' ? 'Low Stock Items' : 'All Inventory Items'}</h2>
          <table className="min-w-full border border-gray-300">
            <thead className="bg-gray-200">
              <tr>
                <th className="border px-4 py-2 text-left">Item</th>
                <th className="border px-4 py-2 text-left">Quantity</th>
                <th className="border px-4 py-2 text-left">Expiry Date</th>
                <th className="border px-4 py-2 text-left">Fridge Location</th>
                <th className="border px-4 py-2 text-left">Status</th>
                <th className="border px-4 py-2 text-left">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredData.map((row) => (
                <tr key={row.id}>
                  <td className="border px-4 py-2">{row.itemName}</td>
                  <td className="border px-4 py-2">{row.quantity} units</td>
                  <td className="border px-4 py-2">{new Date(row.expiryDate).toLocaleDateString()}</td>
                  <td className="border px-4 py-2">{row.fridgeLocation}</td>
                  <td className="border px-4 py-2">
                    <span className={`px-2 py-1 text-sm rounded ${
                      determineStockStatus(row.quantity, row.thresholdQuantity) === 'Low'
                        ? 'bg-red-100 text-red-600'
                        : determineStockStatus(row.quantity, row.thresholdQuantity) === 'Medium'
                        ? 'bg-yellow-100 text-yellow-600'
                        : 'bg-green-100 text-green-600'
                    }`}>
                      {determineStockStatus(row.quantity, row.thresholdQuantity)}
                    </span>
                  </td>
                  <td className="border px-4 py-2">
                  <button onClick={() => setSelectedItem(row)} className="px-2 py-1 bg-blue-500 text-white rounded" disabled={authUser?.permissions?.["User can't add/remove items"]}>
                  Edit
                </button>

                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Edit Modal */}
      {selectedItem && (
        <InventoryEditModal item={selectedItem} onClose={() => setSelectedItem(null)} onSave={handleEditItem} />
      )}
    </div>
  );
}
