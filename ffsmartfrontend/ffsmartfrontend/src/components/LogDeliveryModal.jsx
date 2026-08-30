import { useState } from 'react';

export default function LogDeliveryModal({ onClose, onSubmit }) {
  const [formData, setFormData] = useState({
    itemName: '',
    quantity: '',
    deliveryDate: '',
    deliveredBy: '', // Can be populated from user data
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.itemName || !formData.quantity || !formData.deliveryDate || !formData.deliveredBy) {
      return alert('Please fill out all fields');
    }
    onSubmit({
      itemName: formData.itemName,
      quantity: Number(formData.quantity),
      deliveryDate: new Date(formData.deliveryDate),
      deliveredBy: formData.deliveredBy,
    });
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
      <div className="bg-white p-6 rounded shadow-lg w-96">
        <h2 className="text-xl font-semibold mb-4">Log New Delivery</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Item Name */}
          <div>
            <label htmlFor="itemName" className="block text-sm font-medium">
              Item Name
            </label>
            <input
              id="itemName"
              name="itemName"
              value={formData.itemName}
              onChange={handleChange}
              placeholder="Enter item name"
              className="w-full border border-gray-300 rounded p-2"
              required
            />
          </div>

          {/* Quantity */}
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
              placeholder="Enter quantity"
              className="w-full border border-gray-300 rounded p-2"
              required
            />
          </div>

          {/* Delivery Date */}
          <div>
            <label htmlFor="deliveryDate" className="block text-sm font-medium">
              Delivery Date
            </label>
            <input
              id="deliveryDate"
              name="deliveryDate"
              type="date"
              value={formData.deliveryDate}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded p-2"
              required
            />
          </div>

          {/* Delivered By */}
          <div>
            <label htmlFor="deliveredBy" className="block text-sm font-medium">
              Delivered By
            </label>
            <select
              id="deliveredBy"
              name="deliveredBy"
              value={formData.deliveredBy}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded p-2"
              required
            >
              <option value="">Select delivery personnel</option>
              <option value="John Doe">John Doe</option>
              <option value="Jane Smith">Jane Smith</option>
              <option value="Mike Johnson">Mike Johnson</option>
            </select>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end space-x-2">
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
              Submit
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
