import { useEffect, useState } from 'react'
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
  Select,
  SpaceBetween,
} from '@cloudscape-design/components'
import '../App.css'

export default function Register() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    email: '',
    storeStandId: null,
  })
  const [stores, setStores] = useState([])
  const [storesLoading, setStoresLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')

  // Fetch stores on mount
  useEffect(() => {
    const fetchStores = async () => {
      try {
        const response = await fetch('/api/auth/store-stands')
        if (!response.ok) {
          throw new Error('Failed to load store stands')
        }
        const data = await response.json()
        setStores(
          data.map((store) => ({
            label: `${store.mallName} (${store.city})`,
            value: String(store.id),
          }))
        )
      } catch (error) {
        setErrorMessage('Unable to load store stands. Please refresh the page.')
      } finally {
        setStoresLoading(false)
      }
    }
    fetchStores()
  }, [])

  const canSubmit = formData.email.trim().length > 0 && formData.storeStandId

  const handleInputChange = (field) => ({ detail }) => {
    setFormData((previousData) => ({
      ...previousData,
      [field]: detail.value,
    }))
  }

  const handleStoreChange = ({ detail }) => {
    setFormData((previousData) => ({
      ...previousData,
      storeStandId: detail.selectedOption.value ? parseInt(detail.selectedOption.value, 10) : null,
    }))
  }

  const handleSubmit = async () => {
    setErrorMessage('')
    setSuccessMessage('')
    setIsSubmitting(true)

    try {
      const response = await fetch('/api/auth/registration-request', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: formData.email.trim(),
          storeStandId: formData.storeStandId,
        }),
      })

      if (!response.ok) {
        throw new Error('Failed to submit registration request')
      }

      const payload = await response.json()
      setSuccessMessage(payload.message || 'Registration request submitted successfully!')
      setFormData({ email: '', storeStandId: null })
    } catch (error) {
      setErrorMessage(error.message || 'Unable to submit registration request. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main
      className="login-page awsui-dark-mode"
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background:
          'radial-gradient(circle at top left, #1e293b 0, #020617 40%, #000 100%)',
        padding: '24px',
      }}
    >
      <section
        className="login-panel"
        aria-label="Register for CodeAndStock"
        style={{
          width: '100%',
          maxWidth: '480px',
          backgroundColor: '#020617',
          borderRadius: '16px',
          boxShadow: '0 18px 45px rgba(0,0,0,0.65)',
          border: '1px solid rgba(148,163,184,0.3)',
        }}
      >
        <Container
          header={
            <Header variant="h1" description="Register to request access to CodeAndStock.">
              Join CodeAndStock
            </Header>
          }
        >
          <SpaceBetween size="l">
            {errorMessage && (
              <Alert type="error" header="Submission failed" statusIconAriaLabel="Error">
                {errorMessage}
              </Alert>
            )}

            {successMessage && (
              <Alert type="success" header="Request submitted" statusIconAriaLabel="Success">
                {successMessage}
                <br />
                <Box fontSize="body-s" color="text-body-secondary" margin={{ top: 'small' }}>
                  An administrator will review your request and contact you soon.
                </Box>
              </Alert>
            )}

            <Form
              onSubmit={({ detail }) => {
                detail.preventDefault()
                if (canSubmit && !isSubmitting) {
                  handleSubmit()
                }
              }}
              actions={
                <Button variant="primary" loading={isSubmitting} disabled={!canSubmit || storesLoading} onClick={handleSubmit}>
                  Submit Request
                </Button>
              }
            >
              <SpaceBetween size="m">
                <FormField label={<span style={{ color: '#f9fafb' }}>Email Address</span>}>
                  <Input
                    value={formData.email}
                    onChange={handleInputChange('email')}
                    placeholder="your@email.com"
                    type="email"
                    autoComplete="email"
                  />
                </FormField>

                <FormField label={<span style={{ color: '#f9fafb' }}>Store Stand Where You Work</span>}>
                  <Select
                    selectedOption={
                      formData.storeStandId
                        ? stores.find((s) => s.value === String(formData.storeStandId)) || null
                        : null
                    }
                    onChange={handleStoreChange}
                    options={stores}
                    placeholder="Select a store stand"
                    disabled={storesLoading}
                    statusType={storesLoading ? 'loading' : 'finished'}
                  />
                </FormField>
              </SpaceBetween>
            </Form>

            <Box fontSize="body-s" color="text-body-secondary">
              Already have an account?{' '}
              <Button
                variant="inline-link"
                onClick={() => navigate('/login')}
              >
                Sign in here
              </Button>
            </Box>
          </SpaceBetween>
        </Container>
      </section>
    </main>
  )
}
