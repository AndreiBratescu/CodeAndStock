import React, { useState, useEffect } from 'react';
import "@cloudscape-design/global-styles/index.css";
import {
    AppLayout, Container, Header, ContentLayout, SpaceBetween,
    Button, Table, Box, StatusIndicator, Badge, Grid, TextFilter, Flashbar
} from '@cloudscape-design/components';
import { inventoryService } from '../services/inventoryService';
import { saleService } from '../services/saleService';

const EmployeePage = () => {
    const [inventory, setInventory] = useState([]); // Lista de la backend
    const [loading, setLoading] = useState(true);
    const [cart, setCart] = useState([]);
    const [filteringText, setFilteringText] = useState("");
    const [notifications, setNotifications] = useState([]);

    // 1. Logica de afișare a listei PER STAND la încărcarea paginii
    useEffect(() => {
        loadInventory();
    }, []);

    const loadInventory = async () => {
        setLoading(true);
        try {
            // Apelează @GetMapping("/api/employee/inventory") prin service
            const data = await inventoryService.getInventoryByStand();
            setInventory(data);
        } catch (err) {
            setNotifications([{
                type: "error",
                content: "Nu am putut încărca stocul standului. Verifică conexiunea.",
                id: "err_load"
            }]);
        } finally {
            setLoading(false);
        }
    };

    // 2. Logica pentru Culori Stoc (Verde > 10, Galben 1-10, Roșu = 0)
    const getStatusType = (qty) => {
        if (qty > 10) return "success";
        if (qty > 0) return "warning";
        return "error";
    };

    const addToCart = (item) => {
        if (item.quantity > 0) {
            setCart([...cart, { ...item, cartId: Math.random() }]);
            setInventory(inventory.map(i => i.id === item.id ? { ...i, quantity: i.quantity - 1 } : i));
        }
    };

    const removeFromCart = (cartItem) => {
        setCart(cart.filter(item => item.cartId !== cartItem.cartId));
        setInventory(inventory.map(i => i.id === cartItem.id ? { ...i, quantity: i.quantity + 1 } : i));
    };

    // 3. Salvarea vânzării în Backend
    const handleFinalizeSale = async () => {
        try {
            for (const item of cart) {
                await saleService.saveSale({
                    product: item.product,
                    storeStand: item.storeStand,
                    quantitySold: 1,
                    saleDate: new Date().toISOString().split('T')[0]
                });
            }
            setNotifications([{
                type: "success",
                content: "Vânzare înregistrată cu succes în baza de date!",
                id: "success_sale",
                dismissible: true,
                onDismiss: () => setNotifications([])
            }]);
            setCart([]);
            loadInventory(); // Reîmprospătăm lista pentru a vedea stocul nou din DB
        } catch (err) {
            setNotifications([{
                type: "error",
                content: err.message,
                id: "err_sale"
            }]);
        }
    };

    const filteredItems = inventory.filter(item =>
        item.product.sku.toLowerCase().includes(filteringText.toLowerCase()) ||
        item.product.name.toLowerCase().includes(filteringText.toLowerCase())
    );

    return (
        <div className="awsui-dark-mode">
            <AppLayout
                navigationHide={true}
                toolsHide={true}
                notifications={<Flashbar items={notifications} />}
                content={
                    <ContentLayout
                        header={
                            <Header
                                variant="h1"
                                description="Vizualizare stoc și vânzare pentru standul curent."
                                actions={<Button iconName="refresh" onClick={loadInventory}>Refresh Stoc</Button>}
                            >
                                CodeAndStock - Gestiune Stand
                            </Header>
                        }
                    >
                        <Grid gridDefinition={[{ colspan: 9 }, { colspan: 3 }]}>
                            <Container variant="navigation"
                                       header={
                                           <Header
                                               variant="h2"
                                               actions={
                                                   <TextFilter
                                                       filteringText={filteringText}
                                                       filteringPlaceholder="Caută produs în acest stand..."
                                                       onChange={({ detail }) => setFilteringText(detail.filteringText)}
                                                   />
                                               }
                                           >
                                               Inventar Stand ({filteredItems.length} produse)
                                           </Header>
                                       }
                            >
                                <Table
                                    items={filteredItems}
                                    loading={loading}
                                    loadingText="Se încarcă produsele din standul tău..."
                                    columnDefinitions={[
                                        { id: "sku", header: "SKU", cell: i => <Box variant="code">{i.product.sku}</Box>, width: 220 },
                                        { id: "name", header: "Produs", cell: i => i.product.name, width: 250 },
                                        { id: "price", header: "Preț", cell: i => `${i.product.price} RON` },
                                        { id: "qty", header: "Stoc", cell: i => (
                                                <StatusIndicator type={getStatusType(i.quantity)}>
                                                    {i.quantity} buc.
                                                </StatusIndicator>
                                            )},
                                        { id: "add", header: "Acțiune", cell: i => (
                                                <Button iconName="add-plus" variant="primary" onClick={() => addToCart(i)} disabled={i.quantity <= 0}>
                                                    Vinde
                                                </Button>
                                            )}
                                    ]}
                                />
                            </Container>

                            <Container variant="navigation" header={<Header variant="h2" counter={`(${cart.length})`}>Coș</Header>}
                                       footer={<Box float="right"><Button variant="primary" disabled={cart.length === 0} onClick={handleFinalizeSale}>Save Sale</Button></Box>}
                            >
                                <SpaceBetween size="xs">
                                    {cart.map((c, idx) => (
                                        <Box key={idx} padding="s" style={{ background: "#1a242f", borderRadius: "8px", border: "1px solid #2a3b4d" }}>
                                            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                                                <Box variant="small" fontWeight="bold">{c.product.sku}</Box>
                                                <Button iconName="close" variant="icon" onClick={() => removeFromCart(c)} />
                                            </div>
                                        </Box>
                                    ))}
                                    {cart.length === 0 && <Box textAlign="center" color="text-status-inactive">Selectați produse</Box>}
                                </SpaceBetween>
                            </Container>
                        </Grid>
                    </ContentLayout>
                }
            />
        </div>
    );
};

export default EmployeePage;