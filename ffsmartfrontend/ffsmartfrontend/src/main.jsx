import React from 'react';
import { createRoot } from 'react-dom/client';
import './App.css';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';

// Create the query client with default options
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false, // Prevent refetching on window focus
    },
  },
});

// Render the application
createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>
);
