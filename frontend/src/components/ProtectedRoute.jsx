import { Navigate, useLocation } from 'react-router-dom';

/**
 * Redirects to /login if there is no access token (manager/employee area).
 */
export default function ProtectedRoute({ children }) {
  const location = useLocation();
  const token = localStorage.getItem('accessToken');
  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children;
}
