import React, { useState } from 'react';
import "@cloudscape-design/global-styles/index.css";
import EmployeePage from './pages/EmployeePage';
import Login from './pages/Login';
import Register from './pages/Register';
import AdminDashboard from './pages/AdminDashboard';
import { Button } from '@cloudscape-design/components';

function App() {
  const [currentPage, setCurrentPage] = useState(() => {
    // Check if already logged in
    const token = localStorage.getItem('accessToken');
    const roles = localStorage.getItem('roles');
    if (token && roles) {
      if (roles.includes('ROLE_ADMIN')) return 'admin';
      return 'employee';
    }
    return 'login';
  });

  const handleLogout = () => {
    localStorage.clear();
    setCurrentPage('login');
  };

  const handleLoginSuccess = () => {
    const roles = localStorage.getItem('roles');
    if (roles && roles.includes('ROLE_ADMIN')) {
      setCurrentPage('admin');
    } else {
      setCurrentPage('employee');
    }
  };

  if (currentPage === 'admin') {
    return <AdminDashboard onLogout={handleLogout} />;
  }

  if (currentPage === 'employee') {
    return <EmployeePage onLogout={handleLogout} />;
  }

  if (currentPage === 'register') {
    return (
      <div>
        <div style={{ position: 'absolute', top: 20, left: 20, zIndex: 1000 }}>
          <Button variant="inline-link" onClick={() => setCurrentPage('login')}>
            ← Back to Sign In
          </Button>
        </div>
        <Register onNavigateToLogin={() => setCurrentPage('login')} />
      </div>
    );
  }

  // Default: login
  return (
    <Login
      onNavigateToRegister={() => setCurrentPage('register')}
      onLoginSuccess={handleLoginSuccess}
    />
  );
}

export default App;