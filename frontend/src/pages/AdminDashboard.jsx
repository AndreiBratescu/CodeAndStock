import { useCallback, useEffect, useRef, useState } from 'react'
import {
  Alert,
  AppLayout,
  Box,
  Button,
  Container,
  ContentLayout,
  Header,
  Input,
  Modal,
  Select,
  SpaceBetween,
  Spinner,
  StatusIndicator,
  Table,
  Textarea,
} from '@cloudscape-design/components'

const API_BASE = '/api/admin'
const POLL_INTERVAL_MS = 5000

const ROLE_OPTIONS = [
  { label: 'Employee', value: 'ROLE_EMPLOYEE' },
  { label: 'Manager', value: 'ROLE_MANAGER' },
  { label: 'Admin', value: 'ROLE_ADMIN' },
]

function getAuthHeaders() {
  return {
    Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
    'Content-Type': 'application/json',
  }
}

// ─── Main Admin Dashboard ──────────────────────────────────────────

export default function AdminDashboard({ onLogout }) {
  const [requests, setRequests] = useState([])
  const [users, setUsers] = useState([])
  const [requestsLoading, setRequestsLoading] = useState(true)
  const [usersLoading, setUsersLoading] = useState(true)
  const [approvalAlert, setApprovalAlert] = useState(null)
  const [errorAlert, setErrorAlert] = useState(null)

  // Credentials and roles for pending requests
  const [credentials, setCredentials] = useState({})
  const [selectedRoles, setSelectedRoles] = useState({})
  const generatedIds = useRef(new Set())

  // Action loading states
  const [actionLoading, setActionLoading] = useState({})
  const actionInProgress = useRef(false)

  // Reject modal
  const [rejectModalId, setRejectModalId] = useState(null)
  const [rejectNotes, setRejectNotes] = useState('')

  // Delete modal
  const [deleteModalId, setDeleteModalId] = useState(null)

  // ─── Data Fetching ─────────────────────────────────────────────

  const fetchRequests = useCallback(async () => {
    if (actionInProgress.current) return
    try {
      const res = await fetch(`${API_BASE}/registration-requests`, {
        headers: getAuthHeaders(),
      })
      if (res.ok) setRequests(await res.json())
    } catch (e) {
      console.error('Failed to fetch requests', e)
    } finally {
      setRequestsLoading(false)
    }
  }, [])

  const forceFetchRequests = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/registration-requests`, {
        headers: getAuthHeaders(),
      })
      if (res.ok) setRequests(await res.json())
    } catch (e) {
      console.error('Failed to fetch requests', e)
    }
  }, [])

  const fetchUsers = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/users`, {
        headers: getAuthHeaders(),
      })
      if (res.ok) setUsers(await res.json())
    } catch (e) {
      console.error('Failed to fetch users', e)
    } finally {
      setUsersLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchRequests()
    fetchUsers()
    const interval = setInterval(fetchRequests, POLL_INTERVAL_MS)
    return () => clearInterval(interval)
  }, [fetchRequests, fetchUsers])

  // ─── Generate credentials for pending requests ─────────────────

  const pendingRequests = requests.filter((r) => r.status === 'PENDING')

  useEffect(() => {
    pendingRequests.forEach(async (req) => {
      if (generatedIds.current.has(req.id)) return
      generatedIds.current.add(req.id)
      try {
        const res = await fetch(`${API_BASE}/generate-credentials/${req.id}`, {
          headers: getAuthHeaders(),
        })
        if (res.ok) {
          const data = await res.json()
          setCredentials((prev) => (prev[req.id] ? prev : { ...prev, [req.id]: data }))
        }
      } catch (e) {
        console.error('Credential generation failed', e)
      }
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pendingRequests.length])

  // ─── Approve ───────────────────────────────────────────────────

  const handleApprove = async (requestId) => {
    actionInProgress.current = true
    setActionLoading((prev) => ({ ...prev, [`approve_${requestId}`]: true }))
    setErrorAlert(null)
    const cred = credentials[requestId] || {}
    const role = selectedRoles[requestId] || 'ROLE_EMPLOYEE'
    try {
      const res = await fetch(`${API_BASE}/registration-requests/${requestId}/approve`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify({
          role,
          username: cred.username || '',
          password: cred.password || '',
        }),
      })
      if (res.ok) {
        const data = await res.json()
        setApprovalAlert(data)
        setTimeout(() => setApprovalAlert(null), 15000)
        // Clean up
        setCredentials((prev) => { const n = { ...prev }; delete n[requestId]; return n })
        setSelectedRoles((prev) => { const n = { ...prev }; delete n[requestId]; return n })
      } else {
        try {
          const errData = await res.json()
          setErrorAlert(errData.error || 'Failed to approve user.')
        } catch {
          setErrorAlert('Failed to approve user. Status: ' + res.status)
        }
      }
    } catch (e) {
      setErrorAlert('Network error during approval. Please try again.')
    } finally {
      setActionLoading((prev) => ({ ...prev, [`approve_${requestId}`]: false }))
      actionInProgress.current = false
      forceFetchRequests()
      fetchUsers()
    }
  }

  // ─── Reject ────────────────────────────────────────────────────

  const handleRejectConfirm = async () => {
    if (!rejectModalId) return
    const rid = rejectModalId
    actionInProgress.current = true
    setActionLoading((prev) => ({ ...prev, [`reject_${rid}`]: true }))
    try {
      await fetch(`${API_BASE}/registration-requests/${rid}/reject`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify({ adminNotes: rejectNotes }),
      })
      setCredentials((prev) => { const n = { ...prev }; delete n[rid]; return n })
    } catch (e) {
      setErrorAlert('Failed to reject request.')
    } finally {
      setActionLoading((prev) => ({ ...prev, [`reject_${rid}`]: false }))
      actionInProgress.current = false
      setRejectModalId(null)
      setRejectNotes('')
      forceFetchRequests()
    }
  }

  // ─── Change role ───────────────────────────────────────────────

  const handleRoleChange = async (userId, newRole) => {
    setActionLoading((prev) => ({ ...prev, [`role_${userId}`]: true }))
    try {
      await fetch(`${API_BASE}/users/${userId}/role`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify({ role: newRole }),
      })
    } catch (e) {
      setErrorAlert('Failed to update role.')
    } finally {
      setActionLoading((prev) => ({ ...prev, [`role_${userId}`]: false }))
      fetchUsers()
    }
  }

  // ─── Delete ────────────────────────────────────────────────────

  const handleDeleteConfirm = async () => {
    if (!deleteModalId) return
    const uid = deleteModalId
    setActionLoading((prev) => ({ ...prev, [`delete_${uid}`]: true }))
    try {
      const res = await fetch(`${API_BASE}/users/${uid}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
      })
      if (!res.ok) {
        try {
          const errData = await res.json()
          setErrorAlert(errData.error || 'Cannot delete this user.')
        } catch {
          setErrorAlert('Cannot delete this user. Status: ' + res.status)
        }
      }
    } catch (e) {
      setErrorAlert('Failed to delete user.')
    } finally {
      setActionLoading((prev) => ({ ...prev, [`delete_${uid}`]: false }))
      setDeleteModalId(null)
      fetchUsers()
    }
  }

  // ─── Logout ────────────────────────────────────────────────────

  const handleLogout = async () => {
    try {
      await fetch('/api/auth/logout', { method: 'POST', headers: getAuthHeaders() })
    } catch (e) { /* ignore */ }
    localStorage.clear()
    onLogout()
  }

  // ─── Render ────────────────────────────────────────────────────

  return (
    <div className="awsui-dark-mode">
      <AppLayout
        navigationHide={true}
        toolsHide={true}
        content={
          <ContentLayout
            header={
              <Header
                variant="h1"
                description={`Logged in as ${localStorage.getItem('username') || 'admin'}`}
                actions={<Button onClick={handleLogout}>LogOut</Button>}
              >
                Admin Dashboard
              </Header>
            }
          >
            <SpaceBetween size="l">

              {errorAlert && (
                <Alert type="error" dismissible onDismiss={() => setErrorAlert(null)}>
                  {errorAlert}
                </Alert>
              )}

              {approvalAlert && (
                <Alert type="success" dismissible onDismiss={() => setApprovalAlert(null)}
                  header="User registered successfully">
                  <SpaceBetween size="xxs">
                    <span><strong>Username:</strong> {approvalAlert.username}</span>
                    <span><strong>Password:</strong> {approvalAlert.password}</span>
                    <span><strong>Role:</strong> {approvalAlert.role}</span>
                    <span><strong>Store:</strong> {approvalAlert.storeMallName}</span>
                    <Box fontSize="body-s" color="text-body-secondary">
                      Credentials sent to {approvalAlert.email} via email.
                    </Box>
                  </SpaceBetween>
                </Alert>
              )}

              {/* ── Pending Users ─────────────────────────────── */}
              <Container header={
                <Header variant="h2" description="Approve or reject registration requests"
                  counter={`(${pendingRequests.length})`}>
                  Pending Users
                </Header>
              }>
                {requestsLoading && pendingRequests.length === 0 ? (
                  <Box textAlign="center" padding="l"><Spinner size="large" /></Box>
                ) : pendingRequests.length === 0 ? (
                  <Box textAlign="center" padding="l" color="text-body-secondary">
                    No pending registration requests
                  </Box>
                ) : (
                  <SpaceBetween size="l">
                    {pendingRequests.map((req) => (
                      <Container key={req.id} variant="stacked">
                        <SpaceBetween size="m">
                          <Box>
                            <Box color="text-body-secondary"><strong>Email</strong></Box>
                            <Box>{req.email}</Box>
                          </Box>
                          <Box>
                            <Box color="text-body-secondary"><strong>Store</strong></Box>
                            <Box>{req.storeMallName} ({req.storeCity})</Box>
                          </Box>
                          <Box>
                            <Box color="text-body-secondary"><strong>Username</strong></Box>
                            <Input
                              value={credentials[req.id]?.username || ''}
                              onChange={({ detail }) =>
                                setCredentials((prev) => ({
                                  ...prev,
                                  [req.id]: { ...prev[req.id], username: detail.value },
                                }))
                              }
                              placeholder="Auto-generated username"
                            />
                          </Box>
                          <Box>
                            <Box color="text-body-secondary"><strong>Password</strong></Box>
                            <Input
                              value={credentials[req.id]?.password || ''}
                              onChange={({ detail }) =>
                                setCredentials((prev) => ({
                                  ...prev,
                                  [req.id]: { ...prev[req.id], password: detail.value },
                                }))
                              }
                              placeholder="Auto-generated password"
                            />
                          </Box>
                          <Box>
                            <Box color="text-body-secondary"><strong>Role</strong></Box>
                            <Select
                              selectedOption={
                                ROLE_OPTIONS.find((o) => o.value === (selectedRoles[req.id] || 'ROLE_EMPLOYEE')) || ROLE_OPTIONS[0]
                              }
                              options={ROLE_OPTIONS}
                              onChange={({ detail }) =>
                                setSelectedRoles((prev) => ({ ...prev, [req.id]: detail.selectedOption.value }))
                              }
                            />
                          </Box>
                          <SpaceBetween direction="horizontal" size="s">
                            <Button variant="primary"
                              loading={actionLoading[`approve_${req.id}`]}
                              disabled={actionLoading[`approve_${req.id}`]}
                              onClick={() => handleApprove(req.id)}>
                              Register
                            </Button>
                            <Button
                              loading={actionLoading[`reject_${req.id}`]}
                              disabled={actionLoading[`reject_${req.id}`]}
                              onClick={() => { setRejectModalId(req.id); setRejectNotes('') }}>
                              Reject
                            </Button>
                          </SpaceBetween>
                        </SpaceBetween>
                      </Container>
                    ))}
                  </SpaceBetween>
                )}
              </Container>

              {/* ── Active Users ──────────────────────────────── */}
              <Container header={
                <Header variant="h2" description="Update roles and manage active users"
                  counter={`(${users.length})`}>
                  Active Users
                </Header>
              }>
                {usersLoading && users.length === 0 ? (
                  <Box textAlign="center" padding="l"><Spinner size="large" /></Box>
                ) : (
                  <Table
                    variant="embedded"
                    columnDefinitions={[
                      {
                        id: 'username',
                        header: 'Username',
                        cell: (item) => <Box fontWeight="bold">{item.username}</Box>,
                      },
                      {
                        id: 'email',
                        header: 'Email',
                        cell: (item) => item.email,
                      },
                      {
                        id: 'store',
                        header: 'Store',
                        cell: (item) => item.storeMallName || '—',
                      },
                      {
                        id: 'role',
                        header: 'Role',
                        cell: (item) => (
                          <Select
                            expandToViewport={true}
                            selectedOption={
                              ROLE_OPTIONS.find((o) => o.value === item.roles) || ROLE_OPTIONS[0]
                            }
                            options={ROLE_OPTIONS}
                            onChange={({ detail }) =>
                              handleRoleChange(item.id, detail.selectedOption.value)
                            }
                            disabled={!!actionLoading[`role_${item.id}`]}
                          />
                        ),
                      },
                      {
                        id: 'status',
                        header: 'Status',
                        cell: (item) => (
                          <StatusIndicator type={item.enabled ? 'success' : 'stopped'}>
                            {item.enabled ? 'Active' : 'Disabled'}
                          </StatusIndicator>
                        ),
                      },
                      {
                        id: 'actions',
                        header: 'Actions',
                        cell: (item) => (
                          <Button
                            loading={!!actionLoading[`delete_${item.id}`]}
                            onClick={() => setDeleteModalId(item.id)}>
                            Delete
                          </Button>
                        ),
                      },
                    ]}
                    items={users}
                    trackBy="id"
                    empty="No active users"
                  />
                )}
              </Container>

            </SpaceBetween>
          </ContentLayout>
        }
      />

      {/* ── Reject Modal ───────────────────────────────── */}
      <Modal
        visible={rejectModalId !== null}
        onDismiss={() => setRejectModalId(null)}
        header="Reject Registration"
        footer={
          <Box float="right">
            <SpaceBetween direction="horizontal" size="xs">
              <Button variant="link" onClick={() => setRejectModalId(null)}>Cancel</Button>
              <Button variant="primary" onClick={handleRejectConfirm}>Confirm Rejection</Button>
            </SpaceBetween>
          </Box>
        }
      >
        <SpaceBetween size="m">
          <Box>Are you sure you want to reject this registration request?</Box>
          <Textarea
            value={rejectNotes}
            onChange={({ detail }) => setRejectNotes(detail.value)}
            placeholder="Optional reason (will be sent via email)"
            rows={3}
          />
        </SpaceBetween>
      </Modal>

      {/* ── Delete Modal ───────────────────────────────── */}
      <Modal
        visible={deleteModalId !== null}
        onDismiss={() => setDeleteModalId(null)}
        header="Delete User"
        footer={
          <Box float="right">
            <SpaceBetween direction="horizontal" size="xs">
              <Button variant="link" onClick={() => setDeleteModalId(null)}>Cancel</Button>
              <Button variant="primary" onClick={handleDeleteConfirm}>Confirm Delete</Button>
            </SpaceBetween>
          </Box>
        }
      >
        Are you sure you want to delete this user? This action cannot be undone.
      </Modal>
    </div>
  )
}
