import React, { useState, useEffect } from 'react';
import "@cloudscape-design/global-styles/index.css";
import {
    AppLayout, Container, Header, ContentLayout, SpaceBetween,
    Button, Table, Box, StatusIndicator, Grid, TextFilter, Flashbar
} from '@cloudscape-design/components';
import { inventoryService } from '../services/inventoryService';
import { saleService } from '../services/saleService';

const EmployeePage = () => {
    const [inventory, setInventory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [cart, setCart] = useState([]);
    const [filteringText, setFilteringText] = useState("");
    const [notifications, setNotifications] = useState([]);

    useEffect(() => {
        loadInventory();
    }, []);

    const loadInventory = async () => {
        setLoading(true);
        try {
            const data = await inventoryService.getInventoryByStand();
            setInventory(data);
        } catch (err) {
            setNotifications([{
                type: "error",
                content: "Nu am putut încărca stocul standului.",
                id: "err_load"
            }]);
        } finally {
            setLoading(false);
        }
    };

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

    const handleFinalizeSale = async () => {
        try {
            for (const item of cart) {
                await saleService.saveSale({
                    product: { id: item.product.id },
                    storeStand: { id: item.storeStand.id },
                    quantitySold: 1,
                    saleDate: new Date().toISOString().split('T')[0]
                });
            }
            setNotifications([{
                type: "success",
                content: `Vânzare înregistrată cu succes!`,
                id: "success_sale",
                dismissible: true,
                onDismiss: () => setNotifications([])
            }]);
            setCart([]);
            loadInventory();
        } catch (err) {
            setNotifications([{
                type: "error",
                content: "Eroare la salvare: " + (err.response?.data || err.message),
                id: "err_sale",
                dismissible: true
            }]);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userRole');
        localStorage.removeItem('standId');
        window.location.href = "/login";
    };

    const filteredItems = inventory.filter(item =>
        item.product.sku.toLowerCase().includes(filteringText.toLowerCase()) ||
        item.product.name.toLowerCase().includes(filteringText.toLowerCase())
    );

    // ... restul importurilor tale (rămân la fel)

    return (
        <div className="awsui-dark-mode">
            <AppLayout
                navigationHide={true}
                toolsHide={true}
                notifications={<Flashbar items={notifications} />}
                content={
                    <Box padding={{ top: "xxl", horizontal: "l" }}>
                        <ContentLayout
                            header={
                                <Box margin={{ bottom: "xxxl" }}>
                                    <Header
                                        variant="h1"
                                        description="Vizualizare stoc și vânzare pentru standul curent."
                                        actions={
                                            <SpaceBetween direction="horizontal" size="xs">
                                                <Button iconName="refresh" onClick={loadInventory}>Refresh Stock</Button>
                                                <Button
                                                    iconName="status-warning"
                                                    onClick={handleLogout}
                                                    variant="primary"
                                                >
                                                    Log Out
                                                </Button>
                                            </SpaceBetween>
                                        }
                                    >
                                        CodeAndStock - Gestiune Stand
                                    </Header>
                                </Box>
                            }
                        >
                            {/* Spațiere forțată suplimentară înainte de Grid */}
                            <div style={{ height: "40px" }} />

                            <Grid gridDefinition={[{ colspan: 9 }, { colspan: 3 }]}>
                                <Container
                                    header={
                                        <Header
                                            variant="h2"
                                            actions={
                                                <TextFilter
                                                    filteringText={filteringText}
                                                    filteringPlaceholder="Caută produs..."
                                                    onChange={({ detail }) => setFilteringText(detail.filteringText)}
                                                />
                                            }
                                        >
                                            Inventar Stand
                                        </Header>
                                    }
                                >
                                    <Table
                                        variant="embedded"
                                        items={filteredItems}
                                        loading={loading}
                                        trackBy="id"
                                        columnDefinitions={[
                                            { id: "sku", header: "SKU", cell: i => <Box variant="code">{i.product.sku}</Box> },
                                            { id: "name", header: "Produs", cell: i => i.product.name },
                                            { id: "price", header: "Preț", cell: i => `${i.product.price} RON` },
                                            { id: "qty", header: "Stoc", cell: i => (
                                                    <StatusIndicator type={getStatusType(i.quantity)}>
                                                        {i.quantity} buc.
                                                    </StatusIndicator>
                                                )},
                                            { id: "add", header: "Acțiune", cell: i => (
                                                    <Button iconName="add-plus" variant="primary" onClick={() => addToCart(i)} disabled={i.quantity <= 0}>
                                                        Sell
                                                    </Button>
                                                )}
                                        ]}
                                    />
                                </Container>

                                <Container header={<Header variant="h2" counter={`(${cart.length})`}>Coș</Header>}
                                           footer={<Box float="right"><Button variant="primary" disabled={cart.length === 0} onClick={handleFinalizeSale}>Save Sale</Button></Box>}
                                >
                                    <SpaceBetween size="xs">
                                        {cart.map((c) => (
                                            <Box key={c.cartId} padding="s" style={{ background: "#1a242f", borderRadius: "8px", border: "1px solid #2a3b4d" }}>
                                                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                                                    <Box variant="small" fontWeight="bold">{c.product.sku}</Box>
                                                    <Button iconName="close" variant="icon" onClick={() => removeFromCart(c)} />
                                                </div>
                                            </Box>
                                        ))}
                                        {cart.length === 0 && <Box textAlign="center" color="text-status-inactive">Coș gol</Box>}
                                    </SpaceBetween>
                                </Container>
                            </Grid>
                        </ContentLayout>
                    </Box>
                }
            />
        </div>
    );
};

export default EmployeePage;