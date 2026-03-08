import { useEffect, useState } from 'react'
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

export default function Register({ onNavigateToLogin }) {
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
    <main className="login-page">
      <div className="grain-overlay" />
      <section className="login-panel" aria-label="Register for CodeAndStock">
        <Container>
          <SpaceBetween size="l">
            <Header variant="h1" description="Register to request access to CodeAndStock.">
              Join CodeAndStock
            </Header>

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
                <FormField label="Email Address">
                  <Input
                    value={formData.email}
                    onChange={handleInputChange('email')}
                    placeholder="your@email.com"
                    type="email"
                    autoComplete="email"
                  />
                </FormField>

                <FormField label="Store Stand Where You Work">
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
                onClick={onNavigateToLogin}
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
