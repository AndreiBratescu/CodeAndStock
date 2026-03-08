import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api', // URL-ul de baza al serverului Spring Boot
    headers: {
        'Content-Type': 'application/json'
    }
});

// Interceptor pentru a injecta automat token-ul de logare
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;