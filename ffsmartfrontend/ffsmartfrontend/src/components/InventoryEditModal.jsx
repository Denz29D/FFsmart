import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';

export default function InventoryEditModal({ item, onClose, onSave }) {
  const [formData, setFormData] = useState({
    id: item.id,
    itemName: item.itemName || '',
    quantity: item.quantity || 0,
    expiryDate: item.expiryDate ? new Date(item.expiryDate).toISOString().split('T')[0] : '',
    fridgeLocation: item.fridgeLocation || '',
    type: item.type || 'Vegetable',
    thresholdQuantity: item.thresholdQuantity || 0,
  });

  const { data: authUser } = useQuery({ queryKey: ['authUser'] });

  useEffect(() => {
    setFormData({
      id: item.id,
      itemName: item.itemName || '',
      quantity: item.quantity || 0,
      expiryDate: item.expiryDate ? new Date(item.expiryDate).toISOString().split('T')[0] : '',
      fridgeLocation: item.fridgeLocation || '',
      type: item.type || 'Vegetable',
      thresholdQuantity: item.thresholdQuantity || 0,
    });
  }, [item]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'quantity' || name === 'thresholdQuantity' ? Number(value) : value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(formData);
  };

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this item?')) return;
    try {
      const response = await fetch(`/api/inventory/${item.id}`, {
        method: 'DELETE',
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to delete item');
      }

      toast.success('Item deleted successfully');
      onClose(); // Close the modal after deletion
    } catch (error) {
      toast.error(error.message);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
      <div className="bg-white p-6 rounded shadow-lg w-96">
        <h2 className="text-xl font-semibold mb-4">Edit Inventory Item</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="itemName" className="block text-sm font-medium">
              Item Name
            </label>
            <input
              id="itemName"
              name="itemName"
              value={formData.itemName}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded p-2"
              required
            />
          </div>

          <div>
            <label htmlFor="quantity" className="block text-sm font-medium">
              Quantity
            </label>
            <input
              id="quantity"
              name="quantity"
              type="number"
              value={formData.quantity}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded p-2"
              required
            />
          </div>

          <div>
            <label htmlFor="expiryDate" className="block text-sm font-medium">
              Expiry Date
            </label>
            <input
              id="expiryDate"
              name="expiryDate"
              type="date"
              value={formData.expiryDate}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded p-2"
              required
            />
          </div>

          <div>
            <label htmlFor="fridgeLocation" className="block text-sm font-medium">
              Fridge Location
            </label>
            <input
              id="fridgeLocation"
              name="fridgeLocation"
              value={formData.fridgeLocation}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded p-2"
              required
            />
          </div>

          <div>
            <label htmlFor="type" className="block text-sm font-medium">
              Type
            </label>
            <select
              id="type"
              name="type"
              value={formData.type}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded p-2"
              required
            >
              <option value="Vegetable">Vegetable</option>
              <option value="Dairy">Dairy</option>
              <option value="Meat">Meat</option>
              <option value="Beverage">Beverage</option>
            </select>
          </div>

          <div>
            <label htmlFor="thresholdQuantity" className="block text-sm font-medium">
              Threshold Quantity
            </label>
            <input
              id="thresholdQuantity"
              name="thresholdQuantity"
              type="number"
              value={formData.thresholdQuantity}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded p-2"
              required
            />
          </div>

          <div className="flex justify-between space-x-2">
            <button
              type="button"
              onClick={onClose}
              className="bg-gray-300 px-4 py-2 rounded hover:bg-gray-400"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
            >
              Save
            </button>
          </div>

          <div className="mt-4">
            <button
              type="button"
              onClick={handleDelete}
              className={`bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 ${
                authUser?.role === 'Delivery' ? 'opacity-50 cursor-not-allowed' : ''
              }`}
              disabled={authUser?.role === 'Delivery'}
            >
              Remove Item
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
