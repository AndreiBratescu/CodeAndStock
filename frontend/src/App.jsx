import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login'
import Register from './pages/Register'
import ManagerDashboard from './pages/ManagerDashboard'
import ProtectedRoute from './components/ProtectedRoute'
import './App.css'
import EmployeePage from "./pages/EmployeePage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/manager"
          element={
            <ProtectedRoute>
              <ManagerDashboard />
            </ProtectedRoute>
          }
        />
          <Route
              path="/stand"
              element={
                  <ProtectedRoute>
                      <EmployeePage />
                  </ProtectedRoute>
              }
          />
      </Routes>
    </BrowserRouter>
  )
}

export default App
