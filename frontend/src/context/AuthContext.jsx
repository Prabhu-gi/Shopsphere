import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/auth.api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem('shopsphere_user');
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });
  const [accessToken, setAccessToken] = useState(() => localStorage.getItem('shopsphere_access_token'));
  const [loading, setLoading] = useState(false);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await authApi.login(email.trim(), password);
      const authData = res?.data || res;

      if (!authData?.accessToken) {
        throw new Error('Authentication response did not contain an access token');
      }

      const userData = {
        id: authData.userId,
        email: authData.email,
        firstName: authData.firstName,
        lastName: authData.lastName,
        role: authData.role,
      };

      localStorage.setItem('shopsphere_access_token', authData.accessToken);
      if (authData.refreshToken) {
        localStorage.setItem('shopsphere_refresh_token', authData.refreshToken);
      }
      localStorage.setItem('shopsphere_user', JSON.stringify(userData));

      setAccessToken(authData.accessToken);
      setUser(userData);
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const register = async (formData) => {
    setLoading(true);
    try {
      const payload = {
        ...formData,
        email: formData.email ? formData.email.trim() : '',
      };
      const res = await authApi.register(payload);
      const authData = res?.data || res;

      if (!authData?.accessToken) {
        throw new Error('Registration response did not contain an access token');
      }

      const userData = {
        id: authData.userId,
        email: authData.email,
        firstName: authData.firstName,
        lastName: authData.lastName,
        role: authData.role,
      };

      localStorage.setItem('shopsphere_access_token', authData.accessToken);
      if (authData.refreshToken) {
        localStorage.setItem('shopsphere_refresh_token', authData.refreshToken);
      }
      localStorage.setItem('shopsphere_user', JSON.stringify(userData));

      setAccessToken(authData.accessToken);
      setUser(userData);
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    try {
      const refreshToken = localStorage.getItem('shopsphere_refresh_token');
      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } catch {
      // Continue cleanup even if server unreachable
    } finally {
      localStorage.removeItem('shopsphere_access_token');
      localStorage.removeItem('shopsphere_refresh_token');
      localStorage.removeItem('shopsphere_user');
      setAccessToken(null);
      setUser(null);
    }
  };

  const updateUser = (updatedData) => {
    setUser((prev) => {
      const newUser = { ...prev, ...updatedData };
      localStorage.setItem('shopsphere_user', JSON.stringify(newUser));
      return newUser;
    });
  };

  const isAuthenticated = !!user && !!accessToken;
  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        loading,
        isAuthenticated,
        isAdmin,
        login,
        register,
        logout,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
