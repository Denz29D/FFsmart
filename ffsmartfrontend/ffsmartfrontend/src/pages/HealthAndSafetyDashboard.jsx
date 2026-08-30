import { useState } from "react";
import { toast } from "react-hot-toast";

export default function HealthAndSafetyDashboard() {
  const [loadingPdf, setLoadingPdf] = useState(false);

  const generateSafetyComplianceReport = async () => {
    setLoadingPdf(true);
    try {
      const response = await fetch(`/api/reports/safety-compliance/pdf`, {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Failed to generate Safety Compliance Report");
      }

      // Convert the response to a blob and download it
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.style.display = "none";
      a.href = url;
      a.download = `SafetyAndComplianceReport.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      toast.error("Error generating Safety Compliance Report: " + error.message);
    } finally {
      setLoadingPdf(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <h1 className="text-2xl font-semibold">Health and Safety Officer Dashboard</h1>
      <div className="mt-4">
        <h2 className="text-lg font-semibold">Generate Report</h2>
        <div className="flex space-x-4 mt-4">
          <button
            onClick={generateSafetyComplianceReport}
            className="bg-red-500 text-white px-4 py-2 rounded"
            disabled={loadingPdf}
          >
            {loadingPdf ? "Generating Safety Report..." : "Safety Compliance Report (PDF)"}
          </button>
        </div>
      </div>
    </div>
  );
}
