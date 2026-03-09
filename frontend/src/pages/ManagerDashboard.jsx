import { useEffect, useState, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Button,
  Container,
  Header,
  Input,
  Table,
  Grid,
  SpaceBetween,
  Flashbar,
  StatusIndicator,
} from '@cloudscape-design/components'
import { inventoryService } from '../services/inventoryService'
import { saleService } from '../services/saleService'
import { planningService } from '../services/planningService'

function ManagerDashboard() {
  const navigate = useNavigate()
  const username = localStorage.getItem('username') || 'Utilizator'

  const [keyword, setKeyword] = useState('')
  const [stock, setStock] = useState([])
  const [stockLoading, setStockLoading] = useState(true)
  const [sales, setSales] = useState([])
  const [salesLoading, setSalesLoading] = useState(true)
  const [report, setReport] = useState(null)
  const [reportLoading, setReportLoading] = useState(false)
  const [notifications, setNotifications] = useState([])

  useEffect(() => {
    loadStock()
    loadSales()
    loadPrediction()
  }, [])

  const loadStock = async () => {
    setStockLoading(true)
    try {
      const data = await inventoryService.getAllInventory()
      setStock(Array.isArray(data) ? data : [])
    } catch (err) {
      setNotifications((n) => [
        ...n,
        { type: 'error', content: 'Nu s-a putut încărca stocul.', id: 'err-stock', dismissible: true },
      ])
    } finally {
      setStockLoading(false)
    }
  }

  const loadSales = async () => {
    setSalesLoading(true)
    try {
      const data = await saleService.getAllSales()
      setSales(Array.isArray(data) ? data : [])
    } catch (err) {
      setNotifications((n) => [
        ...n,
        { type: 'error', content: 'Nu s-a putut încărca analiza vânzărilor.', id: 'err-sales', dismissible: true },
      ])
    } finally {
      setSalesLoading(false)
    }
  }

  const loadPrediction = async () => {
    // curățăm eventualul mesaj vechi de eroare pentru redistribuire
    setNotifications((n) => n.filter((item) => item.id !== 'err-report'))
    setReportLoading(true)
    try {
      const data = await planningService.getRedistributionReport(100, 50)
      setReport(data)
    } catch (err) {
      setNotifications((n) => [
        ...n,
        { type: 'error', content: 'Nu s-a putut încărca predicția de redistribuire.', id: 'err-report', dismissible: true },
      ])
      setReport(null)
    } finally {
      setReportLoading(false)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('username')
    localStorage.removeItem('roles')
    localStorage.removeItem('storeStandId')
    localStorage.removeItem('tokenExpiresAt')
    navigate('/login', { replace: true })
  }

  const filteredStock = useMemo(() => {
    const k = (keyword || '').trim().toLowerCase()
    if (!k) return stock
    return stock.filter(
      (item) =>
        (item.product?.sku && item.product.sku.toLowerCase().includes(k)) ||
        (item.product?.name && item.product.name.toLowerCase().includes(k))
    )
  }, [stock, keyword])

  const salesByProduct = useMemo(() => {
    const map = new Map()
    for (const s of sales) {
      const p = s.product
      if (!p) continue
      const key = p.id ?? p.sku ?? p.name
      const prev = map.get(key) || { product: p, totalSold: 0 }
      prev.totalSold += s.quantitySold ?? 0
      map.set(key, prev)
    }
    return Array.from(map.values()).sort((a, b) => b.totalSold - a.totalSold)
  }, [sales])

  const transfers = report?.transfers ?? []

  return (
    <div
      className="manager-dashboard awsui-dark-mode"
      style={{
        minHeight: '100vh',
        backgroundColor: '#020617', // fundal închis ca în ecranul de gestiune stand
        color: '#f9fafb',
      }}
    >
      <Box padding={{ top: 'm', horizontal: 'l' }}>
        <SpaceBetween size="m">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
            <Header variant="h1" description="Stoc, analiză și predicție pentru toate standurile.">
              CodeAndStock – Manager magazin
            </Header>
            <SpaceBetween direction="horizontal" size="s">
              <Box fontWeight="bold" color="text-body-secondary">
                {username}
              </Box>
              <Button variant="link" onClick={handleLogout}>
                LogOut
              </Button>
            </SpaceBetween>
          </div>

          {notifications.length > 0 && (
            <Flashbar
              items={notifications}
              onDismiss={({ detail }) => setNotifications((n) => n.filter((i) => i.id !== detail.id))}
            />
          )}

          <Grid
            gridDefinition={[
              { colspan: { default: 12, xs: 12, s: 12, m: 6 } },
              { colspan: { default: 12, xs: 12, s: 12, m: 6 } },
            ]}
          >
            <Container
              header={
                <Header
                  variant="h2"
                  description="Filtrează după cuvânt cheie."
                  actions={
                    <Input
                      type="search"
                      value={keyword}
                      onChange={({ detail }) => setKeyword(detail.value)}
                      placeholder="cuvânt cheie"
                    />
                  }
                >
                  Filtru căutare
                </Header>
              }
            >
              <Container header={<Header variant="h3">Stock</Header>}>
                <Table
                  columnDefinitions={[
                    { id: 'sku', header: 'SKU', cell: (i) => i.product?.sku ?? '-' },
                    { id: 'name', header: 'Produs', cell: (i) => i.product?.name ?? '-' },
                    { id: 'stand', header: 'Stand', cell: (i) => `${i.storeStand?.mallName ?? '-'} (${i.storeStand?.city?.name ?? '-'})` },
                    {
                      id: 'qty',
                      header: 'Stoc',
                      cell: (i) => (
                        <StatusIndicator type={i.quantity > 10 ? 'success' : i.quantity > 0 ? 'warning' : 'error'}>
                          {i.quantity ?? 0} buc.
                        </StatusIndicator>
                      ),
                    },
                  ]}
                  items={filteredStock}
                  loading={stockLoading}
                  loadingText="Se încarcă stocul..."
                  empty="Niciun produs în stoc."
                />
              </Container>
            </Container>

            <Container
              header={
                <Header variant="h2">
                  Analiza stoc
                </Header>
              }
            >
              <Table
                columnDefinitions={[
                  { id: 'sku', header: 'SKU', cell: (i) => i.product?.sku ?? '-' },
                  { id: 'name', header: 'Produs', cell: (i) => i.product?.name ?? '-' },
                  { id: 'sold', header: 'Vândute', cell: (i) => i.totalSold },
                ]}
                items={salesByProduct}
                loading={salesLoading}
                loadingText="Se încarcă vânzările..."
                empty="Nicio vânzare înregistrată."
              />
            </Container>
          </Grid>

          <Box margin={{ top: 'l' }}>
            <Container
              header={
                <Header
                  variant="h2"
                  actions={
                    <Button loading={reportLoading} onClick={loadPrediction}>
                      Reîmprospătare predicție
                    </Button>
                  }
                >
                  Redistribuire stoc
                </Header>
              }
            >
              {report?.summaryExplanation && (
                <Box margin={{ bottom: 'm' }} padding="s" variant="div">
                  {report.summaryExplanation}
                </Box>
              )}
              <Table
                columnDefinitions={[
                  { id: 'product', header: 'Produs', cell: (t) => t.productName ?? t.productSku ?? '-' },
                  { id: 'from', header: 'De la', cell: (t) => `${t.sourceMall ?? ''} (${t.sourceCity ?? ''})` },
                  { id: 'to', header: 'Către', cell: (t) => `${t.targetMall ?? ''} (${t.targetCity ?? ''})` },
                  { id: 'qty', header: 'Cantitate', cell: (t) => t.quantityToMove ?? 0 },
                  { id: 'explanation', header: 'Explicație', cell: (t) => t.explanation ?? '-' },
                ]}
                items={transfers}
                loading={reportLoading}
                loadingText="Se calculează predicția..."
                empty="Nicio mutare sugerată momentan."
              />
              <Box margin={{ top: 'm' }}>
                <SpaceBetween direction="horizontal" size="s">
                  <Button variant="primary" iconName="arrow-up" iconAlign="right" onClick={loadPrediction}>
                    Buton Move Stock
                  </Button>
                  <Button onClick={loadStock}>Button Stock</Button>
                </SpaceBetween>
              </Box>
            </Container>
          </Box>
        </SpaceBetween>
      </Box>
    </div>
  )
}

export default ManagerDashboard
