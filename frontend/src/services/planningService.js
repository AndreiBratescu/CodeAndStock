import api from './api';

/**
 * Raport de redistribuire stoc (predictie + transferuri sugerate).
 * GET /api/planning/redistribution-report
 */
export const planningService = {
  getRedistributionReport: async (staleDays = 100, maxQtyPerTransfer = 50) => {
    const { data } = await api.get('/planning/redistribution-report', {
      params: { staleDays, maxQtyPerTransfer },
    });
    return data;
  },
};
