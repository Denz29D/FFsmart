import { useQuery } from '@tanstack/react-query';
import { toast } from 'react-hot-toast';

export default function UserLogs() {
  // Get current user's role
  const { data: authUser } = useQuery({ queryKey: ['authUser'] });

  // Fetch all logs
  const fetchLogs = async () => {
    const response = await fetch('/api/audit-logs');
    if (!response.ok) {
      throw new Error('Failed to fetch user logs');
    }
    return response.json();
  };

  const { data: logs, isLoading, isError } = useQuery({
    queryKey: ['userLogs'],
    queryFn: fetchLogs,
    onError: (error) => {
      toast.error(error.message);
    },
  });
  
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-100 p-6 flex items-center justify-center">
        <p>Loading user logs...</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="min-h-screen bg-gray-100 p-6 flex items-center justify-center">
        <p className="text-red-500">Failed to load user logs.</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      {/* Header */}
      <header className="bg-white shadow-sm p-4 mb-6">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <h1 className="text-2xl font-semibold">User Activity Logs</h1>
          <span className="text-sm">{authUser?.role || 'Unknown Role'}</span>
        </div>
      </header>

      {/* User Activity Logs Table */}
      <div className="bg-white shadow p-6 rounded">
        <h2 className="text-lg font-medium mb-4">User Activity Logs</h2>
        <table className="min-w-full border border-gray-300">
          <thead className="bg-gray-200">
            <tr>
              <th className="border px-4 py-2 text-left">Timestamp</th>
              <th className="border px-4 py-2 text-left">User</th>
              <th className="border px-4 py-2 text-left">Action</th>
              <th className="border px-4 py-2 text-left">Details</th>
              <th className="border px-4 py-2 text-left">Status</th>
            </tr>
          </thead>
          <tbody>
            {logs && logs.length > 0 ? (
              logs.map((log, index) => (
                <tr key={index} className="border-t">
                  <td className="border px-4 py-2">{log.timestamp}</td>
                  <td className="border px-4 py-2">{log.username || 'Unknown'}</td>
                  <td className="border px-4 py-2">{log.action}</td>
                  <td className="border px-4 py-2">{log.details}</td>
                  <td className="border px-4 py-2">
                    <span
                      className={`px-2 py-1 text-sm rounded ${
                        log.status === 'Success'
                          ? 'bg-green-100 text-green-600'
                          : 'bg-gray-100 text-gray-600'
                      }`}
                    >
                      {log.status}
                    </span>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="text-center p-4 text-gray-500">
                  No logs found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Action Buttons */}
      <div className="mt-6 flex gap-4">
        <button className="bg-black text-white px-4 py-2 rounded">
          Export Logs
        </button>
        <button className="border border-gray-300 text-black px-4 py-2 rounded">
          Configure Log Retention
        </button>
      </div>
    </div>
  );
}
