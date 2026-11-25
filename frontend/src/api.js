import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8080/api',
});

API.interceptors.request.use(config => {
  try {
    const session = JSON.parse(sessionStorage.getItem('coc_session') || '{}');
    if (session.token) {
      config.headers.Authorization = `Bearer ${session.token}`;
    }
  } catch {
    sessionStorage.removeItem('coc_session');
  }
  return config;
});

API.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/login')) {
      sessionStorage.removeItem('coc_session');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default API;
