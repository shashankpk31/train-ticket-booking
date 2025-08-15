import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { toast } from 'react-toastify';

const Login = () => {
  const { isAuthenticated, login, logout } = useContext(AuthContext);

  const handleLogin = () => {
    login();
    toast.success('Redirecting to login...');
  };

  const handleLogout = () => {
    logout();
    toast.info('Logged out successfully');
  };

  return (
    <div className="flex items-center justify-center h-screen bg-gray-100">
      <div className="p-6 bg-white rounded shadow-md">
        <h1 className="text-2xl font-bold mb-4">Railway Ticket Booking</h1>
        {isAuthenticated ? (
          <button
            onClick={handleLogout}
            className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
          >
            Logout
          </button>
        ) : (
          <button
            onClick={handleLogin}
            className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          >
            Login with Keycloak
          </button>
        )}
      </div>
    </div>
  );
};

export default Login;