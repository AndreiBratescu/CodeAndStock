import { useState } from 'react'
import { Button } from '@cloudscape-design/components'
import Login from './pages/Login'
import Register from './pages/Register'
import AdminDashboard from './pages/AdminDashboard'
import './App.css'

function App() {
  const [currentPage, setCurrentPage] = useState(() => {
    // Check if already logged in
    const token = localStorage.getItem('accessToken')
    const roles = localStorage.getItem('roles')
    if (token && roles) {
      if (roles.includes('ROLE_ADMIN')) return 'admin'
      return 'dashboard' // future: employee/manager pages
    }
    return 'login'
  })

  const handleLogout = () => {
    localStorage.clear()
    setCurrentPage('login')
  }

  if (currentPage === 'admin') {
    return <AdminDashboard onLogout={handleLogout} />
  }

  return (
    <>
      {currentPage === 'login' ? (
        <Login
          onNavigateToRegister={() => setCurrentPage('register')}
          onLoginSuccess={() => {
            const roles = localStorage.getItem('roles')
            if (roles && roles.includes('ROLE_ADMIN')) {
              setCurrentPage('admin')
            } else {
              setCurrentPage('dashboard')
            }
          }}
        />
      ) : (
        <div>
          <div style={{ position: 'absolute', top: 20, left: 20, zIndex: 1000 }}>
            <Button variant="inline-link" onClick={() => setCurrentPage('login')}>
              ← Back to Sign In
            </Button>
          </div>
          <Register onNavigateToLogin={() => setCurrentPage('login')} />
        </div>
      )}
    </>
  )
}

export default App

