import axios from 'axios';

const instance = axios.create({
  baseURL: 'http://localhost:9000',
  withCredentials: true, // opsiyonel, JWT cookie kullanıyorsan gerekebilir
  headers: {
    'Content-Type': 'application/json',
  },
});

instance.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

export default instance;