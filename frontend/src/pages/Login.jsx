import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Container,
  Form,
  FormField,
  Header,
  Input,
  SpaceBetween,
} from '@cloudscape-design/components'

const INITIAL_FORM = {
  username: '',
  password: '',
}

const DEMO_USERS = [
  { label: 'employee_plaza', password: 'password123' },
  { label: 'employee_afi', password: 'password123' },
  { label: 'admin_user', password: 'password123' },
]

export default function Login({ onNavigateToRegister }) {
  const navigate = useNavigate()
  const [formData, setFormData] = useState(INITIAL_FORM)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [sessionData, setSessionData] = useState(null)

  const canSubmit = useMemo(() => {
    return formData.username.trim().length > 0 && formData.password.trim().length > 0
  }, [formData])

  const handleInputChange = (field) => ({ detail }) =>
    setFormData((prev) => ({ ...prev, [field]: detail.value }))

  function saveSession(payload) {
    localStorage.setItem('accessToken', payload.accessToken)
    localStorage.setItem('refreshToken', payload.refreshToken)
    localStorage.setItem('username', payload.username)
    localStorage.setItem('roles', payload.roles)
    if (payload.storeStandId) localStorage.setItem('storeStandId', payload.storeStandId)
  }

  const handleLogin = async () => {
    setErrorMessage('')
    setIsSubmitting(true)

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: formData.username.trim(),
          password: formData.password,
        }),
      })

      if (!response.ok) {
        throw new Error('Invalid username or password.')
      }

      const payload = await response.json()
      saveSession(payload)
      setSessionData(payload)
      setFormData(INITIAL_FORM)

      // Navigate based on role
      const roles = payload.roles || ''
      if (roles.includes('ROLE_ADMIN')) {
        navigate('/admin', { replace: true })
      } else if (roles.includes('ROLE_MANAGER')) {
        navigate('/manager', { replace: true })
      } else {
        navigate('/stand', { replace: true })
      }
    } catch (error) {
      setSessionData(null)
      setErrorMessage(error.message || 'Unable to login right now. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="login-page">
      <div className="grain-overlay" />

      <div className="login-card">
        <Container>
          <Form
            header={
              <Header
                variant="h1"
                description="Sign in to access your dashboard."
              >
                CodeAndStock
              </Header>
            }
          >
            <SpaceBetween size="l">
              <FormField label="Username">
                <Input
                  type="text"
                  value={formData.username}
                  onChange={handleInputChange('username')}
                  placeholder="e.g. employee_plaza"
                />
              </FormField>

              <FormField label="Password">
                <Input
                  type="password"
                  value={formData.password}
                  onChange={handleInputChange('password')}
                  placeholder="Your password"
                />
              </FormField>

              {errorMessage && (
                <Alert type="error" dismissible onDismiss={() => setErrorMessage('')}>
                  {errorMessage}
                </Alert>
              )}

              <Button
                variant="primary"
                fullWidth
                loading={isSubmitting}
                disabled={!canSubmit}
                onClick={handleLogin}
              >
                Sign In
              </Button>

              <Box textAlign="center" fontSize="body-s">
                <span style={{ color: '#aaa' }}>Don't have an account? </span>
                <Button
                  variant="inline-link"
                  onClick={() => navigate('/register')}
                >
                  Request access
                </Button>
              </Box>

              {/* Demo users helper */}
              <Box textAlign="center" fontSize="body-s" color="text-body-secondary">
                <details>
                  <summary style={{ cursor: 'pointer' }}>Demo accounts</summary>
                  <SpaceBetween size="xxs">
                    {DEMO_USERS.map((u) => (
                      <Box key={u.label} fontSize="body-s">
                        <code>{u.label}</code> / <code>{u.password}</code>
                      </Box>
                    ))}
                  </SpaceBetween>
                </details>
              </Box>
            </SpaceBetween>
          </Form>
        </Container>
      </div>
    </main>
  )
}
