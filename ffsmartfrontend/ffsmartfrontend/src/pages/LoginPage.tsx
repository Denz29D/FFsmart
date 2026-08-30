import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export default function LoginPage() {
  const [formData, setFormData] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const {
    mutate: processLogin,
    isPending: isLoginLoading,
    isError: isLoginError,
    error: loginError,
  } = useMutation({
    mutationFn: async ({ username, password }: { username: string; password: string }) => {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include", // Include cookies
        body: JSON.stringify({ username, password }),
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.message || "Invalid credentials");
      }

      return await res.json();
    },
    onSuccess: async () => {
      // Invalidate and refetch auth user data
      queryClient.invalidateQueries({ queryKey: ["authUser"] });
      navigate("/"); // Redirect after successful login
    },
    onError: (err: any) => {
      setError(err.message || "Login failed. Please try again.");
    },
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    processLogin(formData);
  };

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="w-full max-w-md p-6">
        {error && <div className="text-red-500 mb-4">{error}</div>}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="username" className="block text-sm font-medium">
              Username
            </label>
            <input
              id="username"
              name="username"
              type="text"
              value={formData.username}
              onChange={handleInputChange}
              required
              placeholder="Enter your username"
              className="mt-1 block w-full border rounded p-2"
            />
          </div>
          <div>
            <label htmlFor="password" className="block text-sm font-medium">
              Password
            </label>
            <input
              id="password"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleInputChange}
              required
              placeholder="Enter your password"
              className="mt-1 block w-full border rounded p-2"
            />
          </div>
          <button
            type="submit"
            disabled={isLoginLoading}
            className={`w-full py-2 rounded text-white ${isLoginLoading ? "bg-gray-500" : "bg-black hover:bg-gray-800"}`}
          >
            {isLoginLoading ? "Signing In..." : "Sign In"}
          </button>
          {isLoginError && (
            <p className="text-red-500 mt-2 text-center">{(loginError as any).message}</p>
          )}
          <button
            type="button"
            onClick={() => setError("")}
            className="w-full text-sm text-blue-500 mt-2"
          >
            Forgot password?
          </button>
        </form>
        <div className="mt-6 text-center">
          <p className="text-sm">
            Don't have an account?{" "}
            <button
              type="button"
              onClick={() => navigate("/signup")}
              className="text-blue-500 underline"
            >
              Sign Up
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
