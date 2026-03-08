import { useState } from 'react'
import { Button } from '@cloudscape-design/components'
import Login from './pages/Login'
import Register from './pages/Register'
import './App.css'

function App() {
  const [currentPage, setCurrentPage] = useState('login') // 'login' or 'register'

  return (
    <>
      {currentPage === 'login' ? (
        <Login onNavigateToRegister={() => setCurrentPage('register')} />
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
