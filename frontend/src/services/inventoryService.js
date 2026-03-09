import api from './api';

export const inventoryService = {
    // Apelează @GetMapping("/api/employee/inventory")
    getInventoryByStand: async () => {
        try {
            const response = await api.get('/employee/inventory');
            return response.data; // Returnează List<InventoryItem>
        } catch (error) {
            console.error("Eroare la preluarea inventarului:", error);
            throw error;
        }
    },
    // Apelează @GetMapping("/api/products/inventory/all")
    getAllInventory: async () => {
        try {
            const response = await api.get('/products/inventory/all');
            return response.data; // Returnează List<InventoryItem> pentru toate standurile
        } catch (error) {
            console.error("Eroare la preluarea inventarului complet:", error);
            throw error;
        }
    }
};