import api from './api';

export const saleService = {
  /** GET /api/sales/all - vânzări pentru toate standurile (analiză manager) */
  getAllSales: async () => {
    const response = await api.get('/sales/all');
    return response.data;
  },
  /** POST /api/employee/sale */
  saveSale: async (saleData) => {
        try {
            const response = await api.post('/employee/sale', saleData);
            return response.data; // Returnează obiectul Sale salvat
        } catch (error) {
            // Extragem mesajul de eroare din header-ul setat în Java (ex: "Stoc insuficient")
            const errorMessage = error.response?.headers['error-message'] || "Eroare la salvarea vânzării";
            console.error(errorMessage);
            throw new Error(errorMessage);
        }
    }
};