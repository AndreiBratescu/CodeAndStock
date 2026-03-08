import { useMemo, useState } from 'react'
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
  const [formData, setFormData] = useState(INITIAL_FORM)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [sessionData, setSessionData] = useState(null)

  const canSubmit = useMemo(() => {
    return formData.username.trim().length > 0 && formData.password.length > 0
  }, [formData.password, formData.username])

  const handleInputChange = (field) => ({ detail }) => {
    setFormData((previousData) => ({
      ...previousData,
      [field]: detail.value,
    }))
  }

  const saveSession = (data) => {
    const expiresAt = Date.now() + (data.expiresIn ?? 0) * 1000
    localStorage.setItem('accessToken', data.accessToken ?? '')
    localStorage.setItem('refreshToken', data.refreshToken ?? '')
    localStorage.setItem('username', data.username ?? '')
    localStorage.setItem('roles', data.roles ?? '')
    localStorage.setItem('storeStandId', String(data.storeStandId ?? ''))
    localStorage.setItem('tokenExpiresAt', String(expiresAt))
  }

  const login = async () => {
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
      <section className="login-panel" aria-label="Sign in to CodeAndStock">
        <Container>
          <SpaceBetween size="l">
            <Header variant="h1" description="Sign in to access stock analytics and operations.">
              CodeAndStock Login
            </Header>

            <Box fontSize="body-s" color="text-body-secondary">
              Demo users:
              {' '}
              {DEMO_USERS.map((user) => user.label).join(', ')}
              {' '}
              (password:
              {' '}
              password123)
            </Box>

            {errorMessage && (
              <Alert type="error" header="Login failed" statusIconAriaLabel="Error">
                {errorMessage}
              </Alert>
            )}

            {sessionData && (
              <Alert type="success" header="Login successful" statusIconAriaLabel="Success">
                You are signed in as
                {' '}
                <strong>{sessionData.username}</strong>
                {' '}
                with role
                {' '}
                <strong>{sessionData.roles}</strong>
                .
              </Alert>
            )}

            <Form
              onSubmit={({ detail }) => {
                detail.preventDefault()
                if (canSubmit && !isSubmitting) {
                  login()
                }
              }}
              actions={
                <Button variant="primary" loading={isSubmitting} disabled={!canSubmit} onClick={login}>
                  Sign in
                </Button>
              }
            >
              <SpaceBetween size="m">
                <FormField label="Username">
                  <Input
                    value={formData.username}
                    onChange={handleInputChange('username')}
                    placeholder="employee_plaza"
                    autoComplete="username"
                  />
                </FormField>

                <FormField label="Password">
                  <Input
                    type="password"
                    value={formData.password}
                    onChange={handleInputChange('password')}
                    placeholder="password123"
                    autoComplete="current-password"
                  />
                </FormField>
              </SpaceBetween>
            </Form>

            <Box fontSize="body-s" color="text-body-secondary">
              Don't have an account?{' '}
              <Button
                variant="inline-link"
                onClick={onNavigateToRegister}
              >
                Request access here
              </Button>
            </Box>
          </SpaceBetween>
        </Container>
      </section>
    </main>
  )
}
