import api from './axios';

export const catalogApi = {
  // Public Category endpoints
  getCategories: async (tree = true) => {
    const response = await api.get(`/categories?tree=${tree}`);
    return response.data;
  },

  getCategory: async (idOrSlug) => {
    const response = await api.get(`/categories/${idOrSlug}`);
    return response.data;
  },

  // Public Product endpoints
  getProducts: async (params = {}) => {
    const response = await api.get('/products', { params });
    return response.data;
  },

  getFeaturedProducts: async (limit = 8) => {
    const response = await api.get(`/products/featured?limit=${limit}`);
    return response.data;
  },

  getProduct: async (idOrSlug) => {
    const response = await api.get(`/products/${idOrSlug}`);
    return response.data;
  },

  // Admin Category endpoints
  createCategory: async (data) => {
    const response = await api.post('/admin/categories', data);
    return response.data;
  },

  updateCategory: async (id, data) => {
    const response = await api.put(`/admin/categories/${id}`, data);
    return response.data;
  },

  deleteCategory: async (id) => {
    const response = await api.delete(`/admin/categories/${id}`);
    return response.data;
  },

  // Admin Product endpoints
  createProduct: async (data) => {
    const response = await api.post('/admin/products', data);
    return response.data;
  },

  updateProduct: async (id, data) => {
    const response = await api.put(`/admin/products/${id}`, data);
    return response.data;
  },

  deleteProduct: async (id) => {
    const response = await api.delete(`/admin/products/${id}`);
    return response.data;
  },

  uploadProductImage: async (productId, file, isPrimary = false) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('isPrimary', isPrimary);

    const response = await api.post(`/admin/products/${productId}/images`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  deleteProductImage: async (imageId) => {
    const response = await api.delete(`/admin/products/images/${imageId}`);
    return response.data;
  },

  setPrimaryProductImage: async (productId, imageId) => {
    const response = await api.patch(`/admin/products/${productId}/images/${imageId}/primary`);
    return response.data;
  },
};
