import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ShieldCheck, Zap, RefreshCw, ArrowRight, Layers, Star, ShoppingBag, CheckCircle, Truck, RotateCcw, Clock } from 'lucide-react';
import { catalogApi } from '../../api/catalog.api';

const INITIAL_CATEGORIES = [
  {
    id: 1,
    name: 'Electronics & Audio',
    slug: 'electronics',
    description: 'High-fidelity audio, mechanical workspace gear, and 4K displays',
    imageUrl: 'https://images.unsplash.com/photo-1498049794561-7780e7231661?w=800&auto=format&fit=crop&q=80',
  },
  {
    id: 2,
    name: 'Apparel & Fashion',
    slug: 'fashion',
    description: '100% organic heavyweight cotton tees, Australian merino knitwear, and bags',
    imageUrl: 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=800&auto=format&fit=crop&q=80',
  },
  {
    id: 3,
    name: 'Home & Living',
    slug: 'home-living',
    description: 'Handcrafted ceramic pour-over sets, stoneware mugs, and cast iron ovens',
    imageUrl: 'https://images.unsplash.com/photo-1513694203232-719a280e022f?w=800&auto=format&fit=crop&q=80',
  },
  {
    id: 4,
    name: 'Fitness & Wellness',
    slug: 'fitness',
    description: 'Dual-grip natural rubber yoga mats, stainless steel vacuum flasks',
    imageUrl: 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800&auto=format&fit=crop&q=80',
  },
];

const INITIAL_FEATURED = [
  {
    id: 1,
    name: 'AeroSound Pro Noise-Canceling Headphones',
    slug: 'aerosound-pro-noise-canceling-headphones',
    categoryName: 'Audio & Headphones',
    basePrice: 199.99,
    ratingAvg: 4.85,
    reviewCount: 42,
    featured: true,
    inStock: true,
    primaryImageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80',
  },
  {
    id: 2,
    name: 'ErgoKey Low-Profile Mechanical Keyboard',
    slug: 'ergokey-low-profile-mechanical-keyboard',
    categoryName: 'Computers & Workspace',
    basePrice: 119.50,
    ratingAvg: 4.72,
    reviewCount: 35,
    featured: true,
    inStock: true,
    primaryImageUrl: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80',
  },
  {
    id: 3,
    name: 'EcoWear 100% Organic Heavyweight Tee',
    slug: 'ecowear-100-organic-heavyweight-tee',
    categoryName: 'Apparel & Fashion',
    basePrice: 29.99,
    ratingAvg: 4.65,
    reviewCount: 84,
    featured: true,
    inStock: true,
    primaryImageUrl: 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800&auto=format&fit=crop&q=80',
  },
  {
    id: 4,
    name: 'Kyoto Ceramic Pour-Over Coffee Station',
    slug: 'kyoto-ceramic-pour-over-coffee-station',
    categoryName: 'Home & Living',
    basePrice: 45.00,
    ratingAvg: 4.92,
    reviewCount: 47,
    featured: true,
    inStock: true,
    primaryImageUrl: 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800&auto=format&fit=crop&q=80',
  },
];

export const HomePage = () => {
  const [categories, setCategories] = useState(INITIAL_CATEGORIES);
  const [featuredProducts, setFeaturedProducts] = useState(INITIAL_FEATURED);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [catRes, prodRes] = await Promise.all([
          catalogApi.getCategories(true),
          catalogApi.getFeaturedProducts(8),
        ]);
        if (catRes?.data) setCategories(catRes.data);
        if (prodRes?.data) setFeaturedProducts(prodRes.data);
      } catch (err) {
        console.error('Failed to load home page catalog data:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  return (
    <div className="flex-1">
      {/* Hero Section */}
      <section className="bg-gradient-to-b from-emerald-50 via-white to-gray-50 py-16 sm:py-24 px-4 sm:px-6 lg:px-8 border-b border-gray-100">
        <div className="max-w-7xl mx-auto text-center">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-semibold mb-6">
            <Zap className="w-3.5 h-3.5 text-emerald-600" /> Production-Grade Spring Boot 3 & React Architecture
          </div>
          <h1 className="text-4xl sm:text-6xl font-extrabold text-gray-900 tracking-tight leading-tight max-w-4xl mx-auto">
            Experience E-Commerce with <span className="text-emerald-600">Zero Overselling</span> & Instant Speed
          </h1>
          <p className="mt-6 text-lg sm:text-xl text-gray-600 max-w-2xl mx-auto leading-relaxed">
            ShopSphere powers verified inventory, optimistic and pessimistic locking, seamless Stripe payments, and fine-grained order lifecycle tracking.
          </p>
          <div className="mt-8 flex flex-wrap items-center justify-center gap-4">
            <Link
              to="/products"
              className="inline-flex items-center gap-2 px-8 py-3.5 rounded-full bg-emerald-600 text-white font-medium text-sm hover:bg-emerald-700 shadow-md hover:shadow-lg transition transform hover:-translate-y-0.5"
            >
              Browse All Products <ArrowRight className="w-4 h-4" />
            </Link>
            <Link
              to="/login"
              className="inline-flex items-center gap-2 px-6 py-3.5 rounded-full bg-white border border-gray-300 text-gray-700 font-medium text-sm hover:bg-gray-50 transition"
            >
              Sign In Demo Account
            </Link>
          </div>
        </div>
      </section>

      {/* Categories Showcase */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Explore by Category</h2>
            <p className="text-sm text-gray-500 mt-1">Carefully curated collections for work, lifestyle, and home</p>
          </div>
          <Link to="/products" className="text-sm font-semibold text-emerald-600 hover:text-emerald-700 flex items-center gap-1">
            View all categories <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {categories.slice(0, 4).map((cat) => (
            <Link
              key={cat.id}
              to={`/products?categorySlug=${cat.slug}`}
              className="group relative h-64 rounded-2xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100 flex flex-col justify-end p-6"
            >
              <img
                src={cat.imageUrl || 'https://images.unsplash.com/photo-1498049794561-7780e7231661?w=600&auto=format&fit=crop&q=80'}
                alt={cat.name}
                className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/30 to-transparent" />
              <div className="relative z-10 text-white">
                <h3 className="text-xl font-bold group-hover:text-emerald-300 transition-colors">{cat.name}</h3>
                <p className="text-xs text-gray-300 line-clamp-2 mt-1">{cat.description}</p>
                <span className="inline-flex items-center gap-1 text-xs font-semibold text-emerald-400 mt-3 group-hover:translate-x-1 transition-transform">
                  Explore Collection <ArrowRight className="w-3.5 h-3.5" />
                </span>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured Products Grid */}
      <section className="bg-gray-50 py-16 border-y border-gray-200/80">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-10 gap-4">
            <div>
              <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-100 text-emerald-800 text-xs font-semibold mb-2">
                <Star className="w-3 h-3 fill-emerald-600 text-emerald-600" /> Featured Highlights
              </div>
              <h2 className="text-3xl font-extrabold text-gray-900 tracking-tight">Trending & Best Sellers</h2>
              <p className="text-gray-500 text-sm mt-1">Top-rated gear backed by verified customer reviews and immediate stock.</p>
            </div>
            <Link
              to="/products"
              className="inline-flex items-center gap-2 text-sm font-semibold text-emerald-600 hover:text-emerald-700 bg-white px-4 py-2 rounded-xl border border-gray-200 shadow-xs hover:border-emerald-200 transition"
            >
              See all products <ArrowRight className="w-4 h-4" />
            </Link>
          </div>

          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="bg-white rounded-2xl p-4 border border-gray-200 animate-pulse">
                  <div className="w-full h-48 bg-gray-200 rounded-xl mb-4" />
                  <div className="h-4 bg-gray-200 rounded w-1/3 mb-2" />
                  <div className="h-5 bg-gray-200 rounded w-3/4 mb-3" />
                  <div className="h-6 bg-gray-200 rounded w-1/2" />
                </div>
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {featuredProducts.map((product) => (
                <Link
                  key={product.id}
                  to={`/products/${product.slug}`}
                  className="group bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-xs hover:shadow-xl transition-all duration-300 flex flex-col transform hover:-translate-y-1"
                >
                  <div className="relative aspect-square w-full overflow-hidden bg-gray-100">
                    <img
                      src={product.primaryImageUrl || 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80'}
                      alt={product.name}
                      className="w-full h-full object-cover object-center group-hover:scale-105 transition-transform duration-500"
                    />
                    <div className="absolute top-3 left-3 flex flex-col gap-1">
                      {product.featured && (
                        <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500 text-white shadow-xs">
                          FEATURED
                        </span>
                      )}
                      <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-white/90 text-gray-700 backdrop-blur-xs shadow-xs">
                        {product.categoryName}
                      </span>
                    </div>
                    <div className="absolute top-3 right-3">
                      {product.inStock ? (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500 text-white shadow-xs flex items-center gap-1">
                          <CheckCircle className="w-2.5 h-2.5" /> In Stock
                        </span>
                      ) : (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-rose-500 text-white shadow-xs">
                          Out of Stock
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="p-5 flex-1 flex flex-col justify-between">
                    <div>
                      <div className="flex items-center gap-1 text-xs text-amber-500 font-semibold mb-1.5">
                        <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                        <span>{product.ratingAvg ? Number(product.ratingAvg).toFixed(1) : '5.0'}</span>
                        <span className="text-gray-400 font-normal">({product.reviewCount || 0})</span>
                      </div>
                      <h3 className="font-bold text-gray-900 group-hover:text-emerald-600 transition-colors line-clamp-2 leading-snug">
                        {product.name}
                      </h3>
                    </div>

                    <div className="mt-4 pt-3 border-t border-gray-100 flex items-center justify-between">
                      <div>
                        <span className="text-xs text-gray-400 block font-medium">Starting at</span>
                        <span className="text-lg font-extrabold text-gray-900">
                          ${Number(product.basePrice).toFixed(2)}
                        </span>
                      </div>
                      <span className="p-2 rounded-xl bg-gray-50 group-hover:bg-emerald-600 group-hover:text-white text-gray-600 transition-colors">
                        <ShoppingBag className="w-4 h-4" />
                      </span>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Value Badges Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
          <div className="flex items-start gap-4">
            <div className="p-3 rounded-2xl bg-emerald-100 text-emerald-700">
              <Truck className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-gray-900">Complimentary Shipping</h4>
              <p className="text-xs text-gray-500 mt-1 leading-relaxed">Free standard delivery on orders over $50 with real-time tracking.</p>
            </div>
          </div>

          <div className="flex items-start gap-4">
            <div className="p-3 rounded-2xl bg-teal-100 text-teal-700">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-gray-900">Encrypted Payments</h4>
              <p className="text-xs text-gray-500 mt-1 leading-relaxed">Stripe test integration with PCI-compliant security and idempotent webhooks.</p>
            </div>
          </div>

          <div className="flex items-start gap-4">
            <div className="p-3 rounded-2xl bg-blue-100 text-blue-700">
              <RotateCcw className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-gray-900">30-Day Hassle-Free Returns</h4>
              <p className="text-xs text-gray-500 mt-1 leading-relaxed">Easy self-service returns with immediate stock restoration on cancel.</p>
            </div>
          </div>

          <div className="flex items-start gap-4">
            <div className="p-3 rounded-2xl bg-indigo-100 text-indigo-700">
              <Clock className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-bold text-gray-900">Live Inventory Sync</h4>
              <p className="text-xs text-gray-500 mt-1 leading-relaxed">Optimistic & pessimistic locking guarantee zero double-allocations.</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
