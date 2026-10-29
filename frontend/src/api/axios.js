import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const isAuthRoute =
      config.url?.includes('/auth/login') ||
      config.url?.includes('/auth/register') ||
      config.url?.includes('/auth/refresh');

    const token = localStorage.getItem('shopsphere_access_token');
    if (token && !isAuthRoute) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (!originalRequest) {
      return Promise.reject(error);
    }

    const isAuthRoute =
      originalRequest.url?.includes('/auth/login') ||
      originalRequest.url?.includes('/auth/register') ||
      originalRequest.url?.includes('/auth/refresh');

    // Only attempt refresh for non-auth endpoints when getting a 401
    if (error.response?.status === 401 && !originalRequest._retry && !isAuthRoute) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem('shopsphere_refresh_token');

      if (refreshToken) {
        try {
          const baseURL = import.meta.env.VITE_API_BASE_URL || '/api/v1';
          const res = await axios.post(`${baseURL}/auth/refresh`, { refreshToken });
          const newAccessToken = res.data.data?.accessToken || res.data?.accessToken;
          const newRefreshToken = res.data.data?.refreshToken || res.data?.refreshToken;

          localStorage.setItem('shopsphere_access_token', newAccessToken);
          if (newRefreshToken) {
            localStorage.setItem('shopsphere_refresh_token', newRefreshToken);
          }

          api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

          return api(originalRequest);
        } catch (refreshErr) {
          localStorage.removeItem('shopsphere_access_token');
          localStorage.removeItem('shopsphere_refresh_token');
          localStorage.removeItem('shopsphere_user');
          if (!window.location.pathname.startsWith('/login') && !window.location.pathname.startsWith('/register')) {
            window.location.href = '/login?expired=true';
          }
          return Promise.reject(refreshErr);
        }
      }
    }

    return Promise.reject(error);
  }
);

export default api;
