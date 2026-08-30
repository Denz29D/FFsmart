import { Bell, HelpCircle } from "lucide-react";
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export function Header() {
  const [unacknowledgedCount, setUnacknowledgedCount] = useState(0);

  // Fetch initial unacknowledged alerts on component mount
  useEffect(() => {
    const fetchInitialAlerts = async () => {
      try {
        const response = await fetch("/api/alerts", {
          method: "GET",
          credentials: "include", // Include credentials for authentication
        });

        if (!response.ok) {
          throw new Error("Failed to fetch alerts");
        }

        const alerts = await response.json();
        const initialUnacknowledged = alerts.filter(alert => !alert.acknowledged).length;
        setUnacknowledgedCount(initialUnacknowledged);
      } catch (error) {
        console.error("Error fetching initial alerts:", error);
      }
    };

    fetchInitialAlerts();

    // Create a WebSocket connection using SockJS and Vite proxy setup
    const socket = new SockJS("/ws");
    const stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000, // Attempt reconnect every 5 seconds
      debug: (str) => console.log(str), // Log STOMP frames for debugging
    });

    stompClient.onConnect = () => {
      console.log("Connected to WebSocket in <Header />");

      // Subscribe to /topic/alerts and update the count when new alerts are received
      stompClient.subscribe("/topic/alerts", (message) => {
        const newAlerts = JSON.parse(message.body);
        const newUnacknowledged = newAlerts.filter(alert => !alert.acknowledged).length;
        setUnacknowledgedCount(prev => prev + newUnacknowledged);
      });
    };

    stompClient.onStompError = (frame) => {
      console.error("STOMP Error:", frame.headers["message"]);
      console.error("Details:", frame.body);
    };

    stompClient.activate();

    // Cleanup WebSocket connection on component unmount
    return () => {
      stompClient.deactivate();
    };
  }, []);

  // Function to handle acknowledgment of alerts
  const handleAcknowledge = async (alertId) => {
    try {
      const response = await fetch(`/api/alerts/acknowledge/${alertId}`, {
        method: "POST",
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Failed to acknowledge alert");
      }

      // Decrement the count only if the alert was previously unacknowledged
      setUnacknowledgedCount((prev) => (prev > 0 ? prev - 1 : 0));
    } catch (error) {
      console.error("Error acknowledging alert:", error);
    }
  };

  return (
    <header className="h-14 border-b px-6 flex items-center justify-between">
      <div className="flex items-center gap-2">
        <div className="h-8 w-8 rounded-full bg-gray-200 flex items-center justify-center">
          <img src="/boy1.png" alt="Avatar" className="h-8 w-8 rounded-full" />
        </div>
        <div className="text-sm">
          <div className="font-semibold">User</div>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <Link to="/notifications" className="relative p-2 rounded hover:bg-gray-100">
          <Bell
            className={`h-6 w-6 ${unacknowledgedCount > 0 ? "text-black" : "text-gray-400"}`}
          />
          {unacknowledgedCount > 0 && (
            <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs w-5 h-5 flex items-center justify-center rounded-full">
              {unacknowledgedCount}
            </span>
          )}
        </Link>
        <button className="p-2 rounded hover:bg-gray-100">
          <HelpCircle className="h-4 w-4" />
        </button>
      </div>
    </header>
  );
}
