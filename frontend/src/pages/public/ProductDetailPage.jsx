import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Star, CheckCircle, AlertCircle, ShoppingBag, ShieldCheck,
  Truck, ArrowLeft, RefreshCw, Heart, Share2, Plus, Minus
} from 'lucide-react';
import { catalogApi } from '../../api/catalog.api';
import { useToast } from '../../context/ToastContext';

export const ProductDetailPage = () => {
  const { idOrSlug } = useParams();
  const { showToast } = useToast();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Gallery state
  const [selectedImage, setSelectedImage] = useState(null);

  // Variant state
  const [selectedVariant, setSelectedVariant] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [activeTab, setActiveTab] = useState('description');

  useEffect(() => {
    const fetchProduct = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await catalogApi.getProduct(idOrSlug);
        if (res?.data) {
          const prod = res.data;
          setProduct(prod);

          // Default active image
          if (prod.images && prod.images.length > 0) {
            const primary = prod.images.find((img) => img.primary) || prod.images[0];
            setSelectedImage(primary.imageUrl);
          }

          // Default active variant
          if (prod.variants && prod.variants.length > 0) {
            const firstActive = prod.variants.find((v) => v.active && v.stockQuantity > 0) || prod.variants[0];
            setSelectedVariant(firstActive);
          }
        }
      } catch (err) {
        console.error('Failed to load product detail:', err);
        setError('Product not found or unavailable');
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [idOrSlug]);

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 flex-1 w-full animate-pulse">
        <div className="h-6 bg-gray-200 rounded w-1/4 mb-8" />
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
          <div className="w-full aspect-square bg-gray-200 rounded-2xl" />
          <div className="space-y-4">
            <div className="h-8 bg-gray-200 rounded w-3/4" />
            <div className="h-6 bg-gray-200 rounded w-1/4" />
            <div className="h-20 bg-gray-200 rounded" />
            <div className="h-12 bg-gray-200 rounded w-1/2" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 flex-1 w-full text-center">
        <div className="w-16 h-16 rounded-full bg-rose-100 text-rose-600 flex items-center justify-center mx-auto mb-4">
          <AlertCircle className="w-8 h-8" />
        </div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Product Not Found</h2>
        <p className="text-gray-500 mb-6 text-sm">The product you are looking for does not exist or has been discontinued.</p>
        <Link
          to="/products"
          className="inline-flex items-center gap-2 px-6 py-2.5 rounded-full bg-emerald-600 text-white font-medium text-xs hover:bg-emerald-700 transition"
        >
          <ArrowLeft className="w-4 h-4" /> Back to Catalog
        </Link>
      </div>
    );
  }

  // Calculate dynamic price based on base price + variant adjustment
  const basePrice = Number(product.basePrice || 0);
  const adjustment = Number(selectedVariant?.priceAdjustment || 0);
  const unitPrice = basePrice + adjustment;
  const inStock = selectedVariant ? selectedVariant.active && selectedVariant.stockQuantity > 0 : false;
  const maxStock = selectedVariant ? selectedVariant.stockQuantity : 0;

  // Extract unique colors and sizes for clean pill selection
  const uniqueColors = Array.from(
    new Set(product.variants?.map((v) => v.colorAttr).filter(Boolean))
  );
  const uniqueSizes = Array.from(
    new Set(product.variants?.map((v) => v.sizeAttr).filter(Boolean))
  );

  const handleColorSelect = (color) => {
    // Find matching variant with this color and current size, or first variant with this color
    const match =
      product.variants.find(
        (v) => v.colorAttr === color && (!selectedVariant?.sizeAttr || v.sizeAttr === selectedVariant.sizeAttr)
      ) || product.variants.find((v) => v.colorAttr === color);
    if (match) {
      setSelectedVariant(match);
      setQuantity(1);
    }
  };

  const handleSizeSelect = (size) => {
    const match =
      product.variants.find(
        (v) => v.sizeAttr === size && (!selectedVariant?.colorAttr || v.colorAttr === selectedVariant.colorAttr)
      ) || product.variants.find((v) => v.sizeAttr === size);
    if (match) {
      setSelectedVariant(match);
      setQuantity(1);
    }
  };

  const handleAddToCart = () => {
    if (!inStock) return;
    showToast(`Added ${quantity}x ${product.name} (${selectedVariant?.title || 'Standard'}) to cart!`, 'success');
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full">
      {/* Breadcrumbs */}
      <nav className="flex items-center gap-2 text-xs text-gray-500 mb-8 overflow-x-auto whitespace-nowrap">
        <Link to="/" className="hover:text-gray-900 transition">Home</Link>
        <span>/</span>
        <Link to="/products" className="hover:text-gray-900 transition">Products</Link>
        <span>/</span>
        <Link to={`/products?categorySlug=${product.categorySlug}`} className="hover:text-gray-900 transition">
          {product.categoryName}
        </Link>
        <span>/</span>
        <span className="text-gray-900 font-semibold truncate max-w-xs">{product.name}</span>
      </nav>

      {/* Product Main Showcase */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 pb-16 border-b border-gray-200">
        {/* Left: Gallery */}
        <div className="space-y-4">
          <div className="relative aspect-square w-full rounded-3xl overflow-hidden bg-gray-100 border border-gray-200 shadow-sm">
            <img
              src={selectedImage || 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80'}
              alt={product.name}
              className="w-full h-full object-cover object-center transition-all duration-300"
            />
            {product.featured && (
              <span className="absolute top-4 left-4 px-3 py-1 rounded-full text-xs font-bold bg-amber-500 text-white shadow-xs">
                FEATURED
              </span>
            )}
          </div>

          {/* Thumbnails row */}
          {product.images && product.images.length > 1 && (
            <div className="flex items-center gap-3 overflow-x-auto pb-2">
              {product.images.map((img) => (
                <button
                  key={img.id}
                  onClick={() => setSelectedImage(img.imageUrl)}
                  className={`relative w-20 h-20 rounded-xl overflow-hidden border-2 shrink-0 transition ${
                    selectedImage === img.imageUrl
                      ? 'border-emerald-600 ring-2 ring-emerald-500/20'
                      : 'border-gray-200 hover:border-gray-400'
                  }`}
                >
                  <img src={img.imageUrl} alt="" className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Right: Buy Box & Options */}
        <div className="flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="px-3 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                {product.categoryName}
              </span>
              <div className="flex items-center gap-1 text-xs text-amber-500 font-bold">
                <Star className="w-4 h-4 fill-amber-400 text-amber-400" />
                <span>{product.ratingAvg ? Number(product.ratingAvg).toFixed(1) : '5.0'}</span>
                <span className="text-gray-400 font-normal">({product.reviewCount || 0} reviews)</span>
              </div>
            </div>

            <h1 className="text-3xl sm:text-4xl font-extrabold text-gray-900 tracking-tight leading-tight mb-4">
              {product.name}
            </h1>

            {/* Price & Stock status */}
            <div className="flex items-baseline gap-4 mb-6">
              <span className="text-3xl font-extrabold text-gray-900 tracking-tight">
                ${unitPrice.toFixed(2)}
              </span>
              {adjustment > 0 && (
                <span className="text-xs font-semibold text-emerald-600">
                  (+${adjustment.toFixed(2)} selected option)
                </span>
              )}
              <div className="ml-auto">
                {inStock ? (
                  <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800">
                    <CheckCircle className="w-3.5 h-3.5 text-emerald-600" /> In Stock ({maxStock} available)
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-rose-100 text-rose-800">
                    Out of Stock
                  </span>
                )}
              </div>
            </div>

            <p className="text-gray-600 text-sm leading-relaxed mb-6">
              {product.description}
            </p>

            {/* Color Selector */}
            {uniqueColors.length > 0 && (
              <div className="mb-6">
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 mb-2">
                  Color: <span className="text-gray-900 normal-case">{selectedVariant?.colorAttr || 'Standard'}</span>
                </label>
                <div className="flex flex-wrap gap-2">
                  {uniqueColors.map((color) => {
                    const isSelected = selectedVariant?.colorAttr === color;
                    return (
                      <button
                        key={color}
                        type="button"
                        onClick={() => handleColorSelect(color)}
                        className={`px-4 py-2 rounded-xl text-xs font-semibold border transition ${
                          isSelected
                            ? 'bg-gray-900 text-white border-gray-900 shadow-xs'
                            : 'bg-white text-gray-700 border-gray-300 hover:border-gray-400'
                        }`}
                      >
                        {color}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Size Selector */}
            {uniqueSizes.length > 0 && (
              <div className="mb-6">
                <label className="block text-xs font-bold uppercase tracking-wider text-gray-500 mb-2">
                  Size / Option: <span className="text-gray-900 normal-case">{selectedVariant?.sizeAttr || 'Standard'}</span>
                </label>
                <div className="flex flex-wrap gap-2">
                  {uniqueSizes.map((size) => {
                    const isSelected = selectedVariant?.sizeAttr === size;
                    return (
                      <button
                        key={size}
                        type="button"
                        onClick={() => handleSizeSelect(size)}
                        className={`px-4 py-2 rounded-xl text-xs font-semibold border transition ${
                          isSelected
                            ? 'bg-emerald-600 text-white border-emerald-600 shadow-xs'
                            : 'bg-white text-gray-700 border-gray-300 hover:border-gray-400'
                        }`}
                      >
                        {size}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Quantity Selector & Add to Cart */}
            <div className="space-y-4 pt-4 border-t border-gray-100">
              <div className="flex items-center gap-4">
                <div className="flex items-center border border-gray-300 rounded-xl bg-white shadow-2xs">
                  <button
                    type="button"
                    onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                    disabled={quantity <= 1 || !inStock}
                    className="p-2.5 text-gray-600 hover:text-black disabled:opacity-30 disabled:cursor-not-allowed"
                  >
                    <Minus className="w-4 h-4" />
                  </button>
                  <span className="w-12 text-center text-sm font-bold text-gray-900">{quantity}</span>
                  <button
                    type="button"
                    onClick={() => setQuantity((q) => Math.min(maxStock, q + 1))}
                    disabled={quantity >= maxStock || !inStock}
                    className="p-2.5 text-gray-600 hover:text-black disabled:opacity-30 disabled:cursor-not-allowed"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                </div>

                <button
                  type="button"
                  onClick={handleAddToCart}
                  disabled={!inStock}
                  className="flex-1 flex items-center justify-center gap-2 py-3 px-8 rounded-xl font-bold text-sm bg-emerald-600 text-white hover:bg-emerald-700 shadow-md hover:shadow-lg disabled:bg-gray-200 disabled:text-gray-400 disabled:cursor-not-allowed transition transform hover:-translate-y-0.5"
                >
                  <ShoppingBag className="w-4 h-4" /> {inStock ? 'Add to Cart' : 'Currently Unavailable'}
                </button>
              </div>
            </div>
          </div>

          {/* Guarantees */}
          <div className="mt-8 pt-6 border-t border-gray-100 grid grid-cols-2 gap-4 text-xs text-gray-600">
            <div className="flex items-center gap-2">
              <Truck className="w-4 h-4 text-emerald-600" />
              <span>Fast Tracked Dispatch</span>
            </div>
            <div className="flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>2-Year Full Warranty</span>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs: Description, Specs, Reviews */}
      <div className="mt-12">
        <div className="flex items-center gap-8 border-b border-gray-200 text-sm font-bold">
          <button
            onClick={() => setActiveTab('description')}
            className={`pb-4 border-b-2 transition ${
              activeTab === 'description'
                ? 'border-emerald-600 text-emerald-600'
                : 'border-transparent text-gray-500 hover:text-gray-800'
            }`}
          >
            Product Overview
          </button>
          <button
            onClick={() => setActiveTab('specs')}
            className={`pb-4 border-b-2 transition ${
              activeTab === 'specs'
                ? 'border-emerald-600 text-emerald-600'
                : 'border-transparent text-gray-500 hover:text-gray-800'
            }`}
          >
            Variants & Specifications
          </button>
          <button
            onClick={() => setActiveTab('reviews')}
            className={`pb-4 border-b-2 transition ${
              activeTab === 'reviews'
                ? 'border-emerald-600 text-emerald-600'
                : 'border-transparent text-gray-500 hover:text-gray-800'
            }`}
          >
            Verified Reviews ({product.reviewCount || 0})
          </button>
        </div>

        <div className="py-8 text-sm text-gray-600 leading-relaxed max-w-3xl">
          {activeTab === 'description' && (
            <div>
              <p className="mb-4">{product.description}</p>
              <ul className="list-disc pl-5 space-y-2 text-gray-700">
                <li>Engineered with premium, long-lasting materials and strict QA.</li>
                <li>Instant sync with thread-safe stock reservations.</li>
                <li>Zero-defect packaging ensuring mint arrival.</li>
              </ul>
            </div>
          )}

          {activeTab === 'specs' && (
            <div className="bg-white rounded-2xl border border-gray-200 overflow-hidden">
              <table className="w-full text-left text-xs">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="py-3 px-4 font-bold text-gray-700">SKU</th>
                    <th className="py-3 px-4 font-bold text-gray-700">Variant Title</th>
                    <th className="py-3 px-4 font-bold text-gray-700">Color</th>
                    <th className="py-3 px-4 font-bold text-gray-700">Size</th>
                    <th className="py-3 px-4 font-bold text-gray-700">Adjustment</th>
                    <th className="py-3 px-4 font-bold text-gray-700">Stock</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {product.variants?.map((v) => (
                    <tr key={v.sku} className="hover:bg-gray-50/50">
                      <td className="py-3 px-4 font-mono font-medium text-gray-900">{v.sku}</td>
                      <td className="py-3 px-4 font-semibold text-gray-800">{v.title}</td>
                      <td className="py-3 px-4 text-gray-600">{v.colorAttr || '-'}</td>
                      <td className="py-3 px-4 text-gray-600">{v.sizeAttr || '-'}</td>
                      <td className="py-3 px-4 text-gray-600">
                        {Number(v.priceAdjustment) > 0 ? `+$${Number(v.priceAdjustment).toFixed(2)}` : '$0.00'}
                      </td>
                      <td className="py-3 px-4 font-bold text-emerald-600">{v.stockQuantity}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {activeTab === 'reviews' && (
            <div>
              <div className="flex items-center gap-3 p-4 rounded-2xl bg-amber-50 border border-amber-200 mb-6">
                <Star className="w-5 h-5 fill-amber-400 text-amber-400 shrink-0" />
                <div>
                  <span className="font-bold text-amber-900">Average Rating: {Number(product.ratingAvg).toFixed(2)} / 5.0</span>
                  <p className="text-xs text-amber-700">Verified purchases are eligible to review after receiving orders.</p>
                </div>
              </div>
              <p className="text-xs text-gray-400 italic">No public reviews written yet for this recently seeded item.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
