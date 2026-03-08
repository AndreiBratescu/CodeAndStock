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
    }
};