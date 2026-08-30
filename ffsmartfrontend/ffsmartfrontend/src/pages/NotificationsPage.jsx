import { useEffect, useState } from 'react';
import { toast } from 'react-hot-toast';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export default function NotificationsPage() {
  const [alerts, setAlerts] = useState([]);

  useEffect(() => {
    // Fetch today's alerts on page load
    const fetchTodayAlerts = async () => {
      try {
        const response = await fetch('/api/alerts', {
          method: 'GET',
          credentials: 'include', // Include cookies for auth 
        });
        if (!response.ok) {
          throw new Error('Failed to fetch alerts');
        }
        const data = await response.json();
        console.log('Fetched alerts:', data);
        setAlerts(data);

      } catch (error) {
        console.error('Error fetching alerts:', error);
      }
    };

    fetchTodayAlerts();

    // Use SockJS and STOMP to establish WebSocket connection (proxied via Vite)
    const socket = new SockJS('/ws');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000, // Attempt reconnect every 5 seconds
      debug: (str) => console.log(str), // Log STOMP frames for debugging
    });

    stompClient.onConnect = () => {
      console.log('Connected to WebSocket');

      // Subscribe to /topic/alerts to receive real-time alerts
      stompClient.subscribe('/topic/alerts', (message) => {
        const newAlert = JSON.parse(message.body);
        console.log('Received new alert:', newAlert);
        setAlerts((prevAlerts) => [newAlert, ...prevAlerts]);
        toast.success('New alert received: ' + newAlert.message);
      });
    };

    stompClient.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      console.error('Details:', frame.body);
    };

    stompClient.activate(); // Activate the connection

    return () => {
      stompClient.deactivate(); // Clean up on component unmount
    };
  }, []);

  const acknowledgeAlert = async (alertId) => {
    try {
      const response = await fetch(`/api/alerts/acknowledge/${alertId}`, {
        method: 'POST',
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to acknowledge alert');
      }

      toast.success('Alert acknowledged successfully');
      setAlerts((prevAlerts) =>
        prevAlerts.map((alert) =>
          alert.id === alertId ? { ...alert, acknowledged: true } : alert
        )
      );
    } catch (error) {
      toast.error(error.message);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <header className="bg-white shadow-sm p-4 mb-6">
        <h1 className="text-2xl font-semibold">Notifications</h1>
      </header>

      {alerts.length === 0 ? (
        <p className="text-gray-600">No alerts at the moment.</p>
      ) : (
        <div className="bg-white shadow p-6 rounded">
          <h2 className="text-lg font-medium mb-4">Expiry Alerts</h2>
          <ul className="divide-y divide-gray-200">
            {alerts.map((alert) => (
              <li key={alert.id} className="py-4 flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium">{alert.message}</p>
                  <p className="text-xs text-gray-500">
                    Generated on: {new Date(alert.createdDate).toLocaleDateString()}
                  </p>
                </div>
                {!alert.acknowledged && (
                  <button
                    onClick={() => acknowledgeAlert(alert.id)}
                    className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                  >
                    Acknowledge
                  </button>
                )}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
