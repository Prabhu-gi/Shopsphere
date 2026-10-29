import React, { useState, useEffect } from 'react';
import {
  Plus, Search, Edit3, Trash2, Image, CheckCircle,
  X, AlertCircle, Upload, Eye, Layers, ArrowUpDown
} from 'lucide-react';
import { catalogApi } from '../../api/catalog.api';
import { useToast } from '../../context/ToastContext';

export const AdminProductsPage = () => {
  const { showToast } = useToast();

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  // Modals
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isImageModalOpen, setIsImageModalOpen] = useState(false);
  const [activeProduct, setActiveProduct] = useState(null);

  // Product Form State
  const [formData, setFormData] = useState({
    name: '',
    categoryId: '',
    basePrice: '',
    description: '',
    active: true,
    featured: false,
    variants: [],
  });

  // Image Upload State
  const [selectedFile, setSelectedFile] = useState(null);
  const [isPrimaryImage, setIsPrimaryImage] = useState(false);
  const [uploadingImage, setUploadingImage] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const [prodRes, catRes] = await Promise.all([
        catalogApi.getProducts({ size: 50, sort: 'newest' }),
        catalogApi.getCategories(true),
      ]);
      if (prodRes?.data) setProducts(prodRes.data.content || []);
      if (catRes?.data) setCategories(catRes.data);
    } catch (err) {
      console.error('Failed to load admin catalog:', err);
      showToast('Failed to load catalog data', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const openCreateModal = () => {
    setActiveProduct(null);
    setFormData({
      name: '',
      categoryId: categories[0]?.id || '',
      basePrice: '',
      description: '',
      active: true,
      featured: false,
      variants: [
        { sku: '', title: 'Standard', sizeAttr: '', colorAttr: '', priceAdjustment: '0.00', stockQuantity: 10, active: true },
      ],
    });
    setIsEditModalOpen(true);
  };

  const openEditModal = async (product) => {
    setActiveProduct(product);
    try {
      const res = await catalogApi.getProduct(product.slug || product.id);
      if (res?.data) {
        const p = res.data;
        setFormData({
          name: p.name,
          categoryId: p.categoryId,
          basePrice: p.basePrice,
          description: p.description || '',
          active: p.active,
          featured: p.featured,
          variants: p.variants?.map((v) => ({
            id: v.id,
            sku: v.sku,
            title: v.title,
            sizeAttr: v.sizeAttr || '',
            colorAttr: v.colorAttr || '',
            priceAdjustment: v.priceAdjustment,
            stockQuantity: v.stockQuantity,
            active: v.active,
          })) || [],
        });
        setIsEditModalOpen(true);
      }
    } catch (err) {
      showToast('Failed to load product details for editing', 'error');
    }
  };

  const handleAddVariantRow = () => {
    setFormData((prev) => ({
      ...prev,
      variants: [
        ...prev.variants,
        {
          sku: `${formData.name.substring(0, 4).toUpperCase()}-V${prev.variants.length + 1}`,
          title: `Option ${prev.variants.length + 1}`,
          sizeAttr: '',
          colorAttr: '',
          priceAdjustment: '0.00',
          stockQuantity: 10,
          active: true,
        },
      ],
    }));
  };

  const handleRemoveVariantRow = (index) => {
    setFormData((prev) => ({
      ...prev,
      variants: prev.variants.filter((_, i) => i !== index),
    }));
  };

  const handleVariantChange = (index, field, value) => {
    setFormData((prev) => {
      const copy = [...prev.variants];
      copy[index] = { ...copy[index], [field]: value };
      return { ...prev, variants: copy };
    });
  };

  const handleSaveProduct = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        name: formData.name,
        categoryId: Number(formData.categoryId),
        basePrice: parseFloat(formData.basePrice),
        description: formData.description,
        active: formData.active,
        featured: formData.featured,
        variants: formData.variants.map((v) => ({
          ...v,
          priceAdjustment: parseFloat(v.priceAdjustment || 0),
          stockQuantity: parseInt(v.stockQuantity || 0, 10),
        })),
      };

      if (activeProduct) {
        await catalogApi.updateProduct(activeProduct.id, payload);
        showToast('Product updated successfully!', 'success');
      } else {
        await catalogApi.createProduct(payload);
        showToast('Product created successfully!', 'success');
      }
      setIsEditModalOpen(false);
      loadData();
    } catch (err) {
      showToast(err.response?.data?.message || 'Error saving product', 'error');
    }
  };

  const handleDeleteProduct = async (id) => {
    if (!window.confirm('Are you sure you want to deactivate this product?')) return;
    try {
      await catalogApi.deleteProduct(id);
      showToast('Product deactivated successfully', 'success');
      loadData();
    } catch (err) {
      showToast('Failed to deactivate product', 'error');
    }
  };

  // Image management
  const openImageModal = async (product) => {
    setActiveProduct(product);
    try {
      const res = await catalogApi.getProduct(product.slug || product.id);
      if (res?.data) {
        setActiveProduct(res.data);
        setIsImageModalOpen(true);
      }
    } catch (err) {
      showToast('Failed to load images', 'error');
    }
  };

  const handleUploadImage = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      showToast('Please select an image file', 'error');
      return;
    }
    setUploadingImage(true);
    try {
      await catalogApi.uploadProductImage(activeProduct.id, selectedFile, isPrimaryImage);
      showToast('Image uploaded successfully!', 'success');
      setSelectedFile(null);
      // Reload product images
      const res = await catalogApi.getProduct(activeProduct.id);
      if (res?.data) setActiveProduct(res.data);
      loadData();
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to upload image', 'error');
    } finally {
      setUploadingImage(false);
    }
  };

  const handleDeleteImage = async (imageId) => {
    try {
      await catalogApi.deleteProductImage(imageId);
      showToast('Image deleted successfully', 'success');
      const res = await catalogApi.getProduct(activeProduct.id);
      if (res?.data) setActiveProduct(res.data);
      loadData();
    } catch (err) {
      showToast('Failed to delete image', 'error');
    }
  };

  const handleSetPrimaryImage = async (imageId) => {
    try {
      await catalogApi.setPrimaryProductImage(activeProduct.id, imageId);
      showToast('Primary image updated', 'success');
      const res = await catalogApi.getProduct(activeProduct.id);
      if (res?.data) setActiveProduct(res.data);
      loadData();
    } catch (err) {
      showToast('Failed to update primary image', 'error');
    }
  };

  const filteredProducts = products.filter((p) =>
    p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.categoryName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 flex-1 w-full">
      {/* Top action bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 border-b border-gray-200 gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">Product Catalog Management</h1>
          <p className="text-sm text-gray-500 mt-1">Manage storefront inventory, variant SKUs, prices, and media</p>
        </div>
        <button
          onClick={openCreateModal}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-emerald-600 text-white font-bold text-xs hover:bg-emerald-700 shadow-md hover:shadow-lg transition"
        >
          <Plus className="w-4 h-4" /> Add New Product
        </button>
      </div>

      {/* Filter / Search Bar */}
      <div className="mt-6 mb-6 flex items-center gap-4">
        <div className="relative flex-1 max-w-md">
          <input
            type="text"
            placeholder="Search by product name or category..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 rounded-xl border border-gray-300 text-xs sm:text-sm bg-white shadow-2xs focus:ring-2 focus:ring-emerald-500"
          />
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
        </div>
        <span className="text-xs text-gray-500 font-medium">
          Showing {filteredProducts.length} of {products.length} products
        </span>
      </div>

      {/* Products Table */}
      <div className="bg-white rounded-2xl border border-gray-200 shadow-xs overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-sm text-gray-500">Loading catalog items...</div>
        ) : filteredProducts.length === 0 ? (
          <div className="p-12 text-center text-sm text-gray-500">No products matching "{searchTerm}"</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50 border-b border-gray-200 text-gray-700 uppercase font-bold tracking-wider text-[11px]">
                <tr>
                  <th className="py-3.5 px-4">Product</th>
                  <th className="py-3.5 px-4">Category</th>
                  <th className="py-3.5 px-4">Base Price</th>
                  <th className="py-3.5 px-4">Stock</th>
                  <th className="py-3.5 px-4">Status</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filteredProducts.map((p) => (
                  <tr key={p.id} className="hover:bg-gray-50/60 transition">
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-3">
                        <img
                          src={p.primaryImageUrl || 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=200&auto=format&fit=crop&q=80'}
                          alt=""
                          className="w-10 h-10 rounded-lg object-cover border border-gray-200"
                        />
                        <div>
                          <span className="font-bold text-gray-900 block max-w-xs truncate">{p.name}</span>
                          <span className="text-[11px] text-gray-400 font-mono">/{p.slug}</span>
                        </div>
                      </div>
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="px-2.5 py-1 rounded-full text-[10px] font-semibold bg-gray-100 text-gray-700">
                        {p.categoryName}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-bold text-gray-900">
                      ${Number(p.basePrice).toFixed(2)}
                    </td>
                    <td className="py-3.5 px-4">
                      <span className={`font-bold ${p.inStock ? 'text-emerald-600' : 'text-rose-500'}`}>
                        {p.totalStock} units
                      </span>
                    </td>
                    <td className="py-3.5 px-4">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                        p.active ? 'bg-emerald-100 text-emerald-800' : 'bg-gray-100 text-gray-500'
                      }`}>
                        {p.active ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                      {p.featured && (
                        <span className="ml-1.5 px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                          FEATURED
                        </span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <div className="inline-flex items-center gap-1.5">
                        <button
                          onClick={() => openImageModal(p)}
                          title="Manage Images"
                          className="p-1.5 rounded-lg border border-gray-300 text-gray-600 hover:text-emerald-600 hover:border-emerald-500 transition"
                        >
                          <Image className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => openEditModal(p)}
                          title="Edit Product"
                          className="p-1.5 rounded-lg border border-gray-300 text-gray-600 hover:text-blue-600 hover:border-blue-500 transition"
                        >
                          <Edit3 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteProduct(p.id)}
                          title="Deactivate"
                          className="p-1.5 rounded-lg border border-gray-300 text-gray-600 hover:text-rose-600 hover:border-rose-500 transition"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal: Create / Edit Product */}
      {isEditModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs overflow-y-auto">
          <div className="bg-white rounded-3xl max-w-2xl w-full p-6 max-h-[90vh] overflow-y-auto shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-gray-200 mb-6">
              <h2 className="text-xl font-bold text-gray-900">
                {activeProduct ? 'Edit Product & Variants' : 'Create New Product'}
              </h2>
              <button onClick={() => setIsEditModalOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSaveProduct} className="space-y-5">
              <div>
                <label className="block text-xs font-bold text-gray-700 mb-1">Product Title</label>
                <input
                  type="text"
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-3 py-2 rounded-xl border border-gray-300 text-xs focus:ring-2 focus:ring-emerald-500"
                  placeholder="e.g. Minimalist Noise-Canceling Headphones"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-gray-700 mb-1">Category</label>
                  <select
                    value={formData.categoryId}
                    onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-gray-300 text-xs focus:ring-2 focus:ring-emerald-500"
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-bold text-gray-700 mb-1">Base Price ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    value={formData.basePrice}
                    onChange={(e) => setFormData({ ...formData, basePrice: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-gray-300 text-xs focus:ring-2 focus:ring-emerald-500"
                    placeholder="99.99"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 mb-1">Description</label>
                <textarea
                  rows="3"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full px-3 py-2 rounded-xl border border-gray-300 text-xs focus:ring-2 focus:ring-emerald-500"
                  placeholder="Provide product specifications, materials, and benefits..."
                />
              </div>

              <div className="flex items-center gap-6">
                <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-gray-700">
                  <input
                    type="checkbox"
                    checked={formData.active}
                    onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                    className="w-4 h-4 text-emerald-600 rounded"
                  />
                  Active in Storefront
                </label>
                <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-gray-700">
                  <input
                    type="checkbox"
                    checked={formData.featured}
                    onChange={(e) => setFormData({ ...formData, featured: e.target.checked })}
                    className="w-4 h-4 text-emerald-600 rounded"
                  />
                  Show in Featured Section
                </label>
              </div>

              {/* Dynamic Variants Section */}
              <div className="pt-4 border-t border-gray-200">
                <div className="flex items-center justify-between mb-3">
                  <h3 className="text-xs font-bold uppercase tracking-wider text-gray-500">
                    Product Variants ({formData.variants.length})
                  </h3>
                  <button
                    type="button"
                    onClick={handleAddVariantRow}
                    className="text-xs font-bold text-emerald-600 hover:text-emerald-700 flex items-center gap-1"
                  >
                    <Plus className="w-3.5 h-3.5" /> Add Variant
                  </button>
                </div>

                <div className="space-y-3">
                  {formData.variants.map((v, idx) => (
                    <div key={idx} className="p-3 rounded-xl border border-gray-200 bg-gray-50/50 grid grid-cols-6 gap-2 items-center text-xs">
                      <div className="col-span-2">
                        <label className="text-[10px] text-gray-400 block">SKU</label>
                        <input
                          type="text"
                          required
                          value={v.sku}
                          onChange={(e) => handleVariantChange(idx, 'sku', e.target.value)}
                          className="w-full px-2 py-1 rounded border border-gray-300 bg-white"
                          placeholder="SKU-001"
                        />
                      </div>
                      <div>
                        <label className="text-[10px] text-gray-400 block">Color</label>
                        <input
                          type="text"
                          value={v.colorAttr}
                          onChange={(e) => handleVariantChange(idx, 'colorAttr', e.target.value)}
                          className="w-full px-2 py-1 rounded border border-gray-300 bg-white"
                          placeholder="e.g. Black"
                        />
                      </div>
                      <div>
                        <label className="text-[10px] text-gray-400 block">Size</label>
                        <input
                          type="text"
                          value={v.sizeAttr}
                          onChange={(e) => handleVariantChange(idx, 'sizeAttr', e.target.value)}
                          className="w-full px-2 py-1 rounded border border-gray-300 bg-white"
                          placeholder="e.g. L"
                        />
                      </div>
                      <div>
                        <label className="text-[10px] text-gray-400 block">Stock</label>
                        <input
                          type="number"
                          min="0"
                          required
                          value={v.stockQuantity}
                          onChange={(e) => handleVariantChange(idx, 'stockQuantity', e.target.value)}
                          className="w-full px-2 py-1 rounded border border-gray-300 bg-white"
                        />
                      </div>
                      <div className="text-right pt-3">
                        {formData.variants.length > 1 && (
                          <button
                            type="button"
                            onClick={() => handleRemoveVariantRow(idx)}
                            className="text-rose-500 hover:text-rose-700 font-bold"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Submit Buttons */}
              <div className="flex items-center justify-end gap-3 pt-6 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setIsEditModalOpen(false)}
                  className="px-5 py-2.5 rounded-xl border border-gray-300 text-xs font-bold text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-6 py-2.5 rounded-xl bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-700 shadow-md"
                >
                  Save Product
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Product Media / Images */}
      {isImageModalOpen && activeProduct && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
          <div className="bg-white rounded-3xl max-w-xl w-full p-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-gray-200 mb-6">
              <div>
                <h2 className="text-lg font-bold text-gray-900">Manage Product Images</h2>
                <p className="text-xs text-gray-500">{activeProduct.name}</p>
              </div>
              <button onClick={() => setIsImageModalOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Current Images */}
            <div className="mb-6">
              <h3 className="text-xs font-bold uppercase tracking-wider text-gray-400 mb-3">Existing Photos</h3>
              {(!activeProduct.images || activeProduct.images.length === 0) ? (
                <p className="text-xs text-gray-400 italic">No images uploaded yet.</p>
              ) : (
                <div className="grid grid-cols-3 gap-3">
                  {activeProduct.images.map((img) => (
                    <div key={img.id} className="relative aspect-square rounded-xl overflow-hidden border border-gray-200 group">
                      <img src={img.imageUrl} alt="" className="w-full h-full object-cover" />
                      {img.primary && (
                        <span className="absolute top-1 left-1 px-1.5 py-0.5 rounded text-[9px] font-bold bg-emerald-600 text-white">
                          PRIMARY
                        </span>
                      )}
                      <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition flex items-center justify-center gap-2">
                        {!img.primary && (
                          <button
                            onClick={() => handleSetPrimaryImage(img.id)}
                            className="p-1 rounded bg-white text-emerald-600 text-[10px] font-bold"
                          >
                            Set Primary
                          </button>
                        )}
                        <button
                          onClick={() => handleDeleteImage(img.id)}
                          className="p-1 rounded bg-rose-600 text-white"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Upload form */}
            <form onSubmit={handleUploadImage} className="pt-4 border-t border-gray-200">
              <h3 className="text-xs font-bold uppercase tracking-wider text-gray-400 mb-3">Upload New Image</h3>
              <div className="space-y-3">
                <input
                  type="file"
                  accept="image/png, image/jpeg, image/webp"
                  onChange={(e) => setSelectedFile(e.target.files[0])}
                  className="w-full text-xs text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-emerald-50 file:text-emerald-700 hover:file:bg-emerald-100"
                />
                <label className="flex items-center gap-2 text-xs text-gray-700 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={isPrimaryImage}
                    onChange={(e) => setIsPrimaryImage(e.target.checked)}
                    className="w-4 h-4 text-emerald-600 rounded"
                  />
                  Mark as Primary Showcase Photo
                </label>
                <button
                  type="submit"
                  disabled={uploadingImage || !selectedFile}
                  className="w-full py-2.5 rounded-xl bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-700 disabled:opacity-50 transition"
                >
                  {uploadingImage ? 'Uploading...' : 'Upload Image File'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
