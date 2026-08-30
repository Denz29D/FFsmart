import { useMutation, useQueryClient } from "@tanstack/react-query";

import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

export default function SignupPage() {
  const [formData, setFormData] = useState({
    fullName: "",
    username: "",
    email: "",
    password: "",
    role: "HeadChef",
  });
  
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const {
    mutate: ProcessSignUp,
    isPending: isSignUpPending,
    isError: isSignUpError,
    error: signUpError,
  } = useMutation({
    mutationFn: async ({ email, username, fullName, password, role }: typeof formData) => {
      const res = await fetch("/api/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ email, username, fullName, password, role }),
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Failed to create account");
      return data;
    },
    onSuccess: () => {
      // Automatically log in the user after signup
      ProcessLogin({ username: formData.username, password: formData.password });
    },
    onError: (err: any) => {
      setError(err.message || "Signup failed");
    },
  });

  const {
    mutate: ProcessLogin,
    isError: isLoginError,
    error: loginError,
  } = useMutation({
    mutationFn: async ({ username, password }: { username: string; password: string }) => {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ username, password }),
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.message || "Invalid credentials");
      }

      return await res.json();
    },
    onSuccess: async () => {
      // Invalidate and refetch the authenticated user data
      const authUser = await queryClient.fetchQuery({
        queryKey: ["authUser"],
        queryFn: async () => {
          const res = await fetch("/api/auth/me", {
            method: "GET",
            credentials: "include",
          });
          return res.json();
        },
      });

      // Redirect to the appropriate dashboard based on the user's role
      switch (authUser.role) {
        case "HeadChef":
          navigate("/dashboard/head-chef");
          break;
        case "Manager":
          navigate("/dashboard/manager");
          break;
        case "Delivery":
          navigate("/dashboard/delivery");
          break;
        default:
          navigate("/dashboard");
          break;
      }
    },
    onError: (err: any) => {
      setError(err.message || "Login failed. Please try again.");
    },
  });

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    ProcessSignUp(formData);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="w-full max-w-md p-6">
        <h1 className="text-2xl font-bold mb-4">Create an Account</h1>
        {error && <div className="text-red-500 mb-4">{error}</div>}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="fullName" className="block text-sm font-medium">
              Full Name
            </label>
            <input
              id="fullName"
              name="fullName"
              type="text"
              required
              placeholder="Enter your full name"
              className="mt-1 block w-full border rounded p-2"
              value={formData.fullName}
              onChange={handleInputChange}
            />
          </div>
          <div>
            <label htmlFor="username" className="block text-sm font-medium">
              Username
            </label>
            <input
              id="username"
              name="username"
              type="text"
              required
              placeholder="Enter your username"
              className="mt-1 block w-full border rounded p-2"
              value={formData.username}
              onChange={handleInputChange}
            />
          </div>
          <div>
            <label htmlFor="email" className="block text-sm font-medium">
              Email
            </label>
            <input
              id="email"
              name="email"
              type="email"
              required
              placeholder="Enter your email"
              className="mt-1 block w-full border rounded p-2"
              value={formData.email}
              onChange={handleInputChange}
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
              required
              placeholder="Enter your password"
              className="mt-1 block w-full border rounded p-2"
              value={formData.password}
              onChange={handleInputChange}
            />
          </div>
          <div>
            <label htmlFor="role" className="block text-sm font-medium">
              Role
            </label>
            <select
              id="role"
              name="role"
              required
              className="mt-1 block w-full border rounded p-2"
              value={formData.role}
              onChange={handleInputChange}
            >
              <option value="HeadChef">Head Chef</option>
              <option value="Manager">Manager</option>
              <option value="Delivery">Delivery</option>
              <option value="Chef">Chef</option>
              <option value="HealthAndSafetyOfficer">Health and Safety officer</option>
            </select>
          </div>
          <button
            type="submit"
            className="w-full bg-black text-white py-2 rounded hover:bg-gray-800"
          >
            {isSignUpPending ? "Creating Account..." : "Sign Up"}
          </button>
        </form>
        <p className="text-sm mt-4">
  Already have an account?{" "}
  <Link 
    to="/" 
    className="ml-1 text-blue-500 hover:underline hover:text-blue-700"
  >
    Log In
  </Link>
</p>


      </div>
    </div>
  );
}
