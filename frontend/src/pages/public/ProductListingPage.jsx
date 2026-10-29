import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import {
  Search, Filter, Star, CheckCircle, SlidersHorizontal,
  X, ChevronRight, ChevronLeft, ArrowUpDown, ShoppingBag
} from 'lucide-react';
import { catalogApi } from '../../api/catalog.api';

export const ProductListingPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  // State
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [mobileFilterOpen, setMobileFilterOpen] = useState(false);

  // Filters extracted from URL search params
  const currentCategory = searchParams.get('categorySlug') || '';
  const currentSearch = searchParams.get('search') || '';
  const currentSort = searchParams.get('sort') || 'newest';
  const currentPage = parseInt(searchParams.get('page') || '0', 10);
  const currentMinPrice = searchParams.get('minPrice') || '';
  const currentMaxPrice = searchParams.get('maxPrice') || '';
  const currentMinRating = searchParams.get('minRating') || '';
  const currentInStockOnly = searchParams.get('inStockOnly') === 'true';

  // Local filter form state
  const [searchInput, setSearchInput] = useState(currentSearch);
  const [minPriceInput, setMinPriceInput] = useState(currentMinPrice);
  const [maxPriceInput, setMaxPriceInput] = useState(currentMaxPrice);

  // Load Categories once
  useEffect(() => {
    const loadCategories = async () => {
      try {
        const res = await catalogApi.getCategories(true);
        if (res?.data) setCategories(res.data);
      } catch (err) {
        console.error('Failed to load categories:', err);
      }
    };
    loadCategories();
  }, []);

  // Fetch Products whenever searchParams change
  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      try {
        const params = {
          page: currentPage,
          size: 12,
          sort: currentSort,
        };
        if (currentCategory) params.categorySlug = currentCategory;
        if (currentSearch) params.search = currentSearch;
        if (currentMinPrice) params.minPrice = currentMinPrice;
        if (currentMaxPrice) params.maxPrice = currentMaxPrice;
        if (currentMinRating) params.minRating = currentMinRating;
        if (currentInStockOnly) params.inStockOnly = true;

        const res = await catalogApi.getProducts(params);
        if (res?.data) {
          setProducts(res.data.content || []);
          setTotalPages(res.data.totalPages || 0);
          setTotalElements(res.data.totalElements || 0);
        }
      } catch (err) {
        console.error('Failed to load products:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [searchParams]);

  // Sync search input when URL changes
  useEffect(() => {
    setSearchInput(currentSearch);
    setMinPriceInput(currentMinPrice);
    setMaxPriceInput(currentMaxPrice);
  }, [currentSearch, currentMinPrice, currentMaxPrice]);

  const updateParam = (key, value) => {
    const newParams = new URLSearchParams(searchParams);
    if (value !== null && value !== '' && value !== false) {
      newParams.set(key, value);
    } else {
      newParams.delete(key);
    }
    // Always reset to page 0 on filter change
    if (key !== 'page') {
      newParams.set('page', '0');
    }
    setSearchParams(newParams);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    updateParam('search', searchInput.trim());
  };

  const handlePriceApply = (e) => {
    e.preventDefault();
    const newParams = new URLSearchParams(searchParams);
    if (minPriceInput) newParams.set('minPrice', minPriceInput);
    else newParams.delete('minPrice');

    if (maxPriceInput) newParams.set('maxPrice', maxPriceInput);
    else newParams.delete('maxPrice');

    newParams.set('page', '0');
    setSearchParams(newParams);
  };

  const clearAllFilters = () => {
    setSearchParams(new URLSearchParams());
    setSearchInput('');
    setMinPriceInput('');
    setMaxPriceInput('');
  };

  const hasActiveFilters =
    Boolean(currentCategory) ||
    Boolean(currentSearch) ||
    Boolean(currentMinPrice) ||
    Boolean(currentMaxPrice) ||
    Boolean(currentMinRating) ||
    currentInStockOnly;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full">
      {/* Header bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-gray-200">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
            {currentCategory
              ? categories.find((c) => c.slug === currentCategory)?.name || 'Products'
              : 'All Products'}
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 mt-1">
            {loading ? 'Searching catalog...' : `Showing ${products.length} of ${totalElements} items`}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {/* Search box */}
          <form onSubmit={handleSearchSubmit} className="relative flex-1 sm:w-64">
            <input
              type="text"
              placeholder="Search products..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              className="w-full pl-9 pr-4 py-2 rounded-xl border border-gray-300 text-sm focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-transparent bg-white shadow-2xs"
            />
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
          </form>

          {/* Sort Dropdown */}
          <div className="flex items-center gap-1.5 bg-white border border-gray-300 rounded-xl px-3 py-2 text-sm shadow-2xs">
            <ArrowUpDown className="w-4 h-4 text-gray-500" />
            <select
              value={currentSort}
              onChange={(e) => updateParam('sort', e.target.value)}
              className="bg-transparent focus:outline-hidden text-gray-700 text-xs sm:text-sm cursor-pointer"
            >
              <option value="newest">Newest Arrivals</option>
              <option value="price_asc">Price: Low to High</option>
              <option value="price_desc">Price: High to Low</option>
              <option value="rating_desc">Highest Rated</option>
            </select>
          </div>

          {/* Mobile Filter Button */}
          <button
            onClick={() => setMobileFilterOpen(true)}
            className="md:hidden flex items-center gap-1.5 px-3 py-2 bg-emerald-50 text-emerald-700 font-semibold rounded-xl text-sm border border-emerald-200"
          >
            <Filter className="w-4 h-4" /> Filters
          </button>
        </div>
      </div>

      {/* Main Layout */}
      <div className="flex gap-8 mt-8">
        {/* Sidebar Filters (Desktop) */}
        <aside className="hidden md:block w-64 shrink-0 space-y-6">
          {/* Active Filters Clear Button */}
          {hasActiveFilters && (
            <div className="flex items-center justify-between p-3 rounded-xl bg-emerald-50 border border-emerald-100">
              <span className="text-xs font-semibold text-emerald-800">Filters active</span>
              <button
                onClick={clearAllFilters}
                className="text-xs font-bold text-emerald-700 hover:text-emerald-900 underline"
              >
                Clear all
              </button>
            </div>
          )}

          {/* Category Filter */}
          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-2xs">
            <h3 className="text-sm font-bold text-gray-900 mb-3 uppercase tracking-wider text-[11px] text-gray-400">
              Categories
            </h3>
            <ul className="space-y-1.5 text-sm">
              <li>
                <button
                  onClick={() => updateParam('categorySlug', null)}
                  className={`w-full text-left px-3 py-2 rounded-lg font-medium transition ${
                    !currentCategory
                      ? 'bg-emerald-600 text-white'
                      : 'text-gray-600 hover:bg-gray-100'
                  }`}
                >
                  All Categories
                </button>
              </li>
              {categories.map((cat) => (
                <li key={cat.id}>
                  <button
                    onClick={() => updateParam('categorySlug', cat.slug)}
                    className={`w-full text-left px-3 py-2 rounded-lg font-medium transition flex items-center justify-between ${
                      currentCategory === cat.slug
                        ? 'bg-emerald-600 text-white font-bold'
                        : 'text-gray-600 hover:bg-gray-100'
                    }`}
                  >
                    <span>{cat.name}</span>
                    {cat.subcategories?.length > 0 && <ChevronRight className="w-3.5 h-3.5 opacity-60" />}
                  </button>
                </li>
              ))}
            </ul>
          </div>

          {/* Price Range Filter */}
          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-2xs">
            <h3 className="text-sm font-bold text-gray-900 mb-3 uppercase tracking-wider text-[11px] text-gray-400">
              Price Range ($)
            </h3>
            <form onSubmit={handlePriceApply} className="space-y-3">
              <div className="flex items-center gap-2">
                <input
                  type="number"
                  placeholder="Min"
                  min="0"
                  value={minPriceInput}
                  onChange={(e) => setMinPriceInput(e.target.value)}
                  className="w-full px-2.5 py-1.5 rounded-lg border border-gray-300 text-xs focus:ring-1 focus:ring-emerald-500"
                />
                <span className="text-gray-400">-</span>
                <input
                  type="number"
                  placeholder="Max"
                  min="0"
                  value={maxPriceInput}
                  onChange={(e) => setMaxPriceInput(e.target.value)}
                  className="w-full px-2.5 py-1.5 rounded-lg border border-gray-300 text-xs focus:ring-1 focus:ring-emerald-500"
                />
              </div>
              <button
                type="submit"
                className="w-full py-1.5 rounded-lg bg-gray-900 text-white font-medium text-xs hover:bg-black transition"
              >
                Apply Range
              </button>
            </form>
          </div>

          {/* Rating Filter */}
          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-2xs">
            <h3 className="text-sm font-bold text-gray-900 mb-3 uppercase tracking-wider text-[11px] text-gray-400">
              Customer Rating
            </h3>
            <div className="space-y-2 text-sm">
              {[4.5, 4.0, 3.5].map((stars) => (
                <button
                  key={stars}
                  onClick={() => updateParam('minRating', currentMinRating === String(stars) ? null : stars)}
                  className={`w-full flex items-center justify-between px-3 py-1.5 rounded-lg text-xs font-medium transition ${
                    currentMinRating === String(stars)
                      ? 'bg-amber-100 text-amber-900 font-bold border border-amber-300'
                      : 'text-gray-600 hover:bg-gray-50'
                  }`}
                >
                  <span className="flex items-center gap-1 text-amber-500">
                    <Star className="w-3.5 h-3.5 fill-amber-400" />
                    <span>{stars} & up</span>
                  </span>
                </button>
              ))}
            </div>
          </div>

          {/* Stock Availability */}
          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-2xs">
            <label className="flex items-center gap-2.5 cursor-pointer">
              <input
                type="checkbox"
                checked={currentInStockOnly}
                onChange={(e) => updateParam('inStockOnly', e.target.checked)}
                className="w-4 h-4 text-emerald-600 rounded border-gray-300 focus:ring-emerald-500"
              />
              <span className="text-sm font-medium text-gray-700">In Stock Items Only</span>
            </label>
          </div>
        </aside>

        {/* Product Grid Area */}
        <main className="flex-1">
          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {[1, 2, 3, 4, 5, 6].map((i) => (
                <div key={i} className="bg-white rounded-2xl p-4 border border-gray-200 animate-pulse">
                  <div className="w-full aspect-square bg-gray-200 rounded-xl mb-4" />
                  <div className="h-4 bg-gray-200 rounded w-1/3 mb-2" />
                  <div className="h-5 bg-gray-200 rounded w-3/4 mb-3" />
                  <div className="h-6 bg-gray-200 rounded w-1/2" />
                </div>
              ))}
            </div>
          ) : products.length === 0 ? (
            <div className="bg-white rounded-2xl border border-gray-200 p-12 text-center max-w-lg mx-auto">
              <ShoppingBag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
              <h3 className="text-base font-bold text-gray-900">No matching products found</h3>
              <p className="text-xs text-gray-500 mt-1 mb-6">
                Try loosening your filters, searching for a different keyword, or clearing applied filters.
              </p>
              <button
                onClick={clearAllFilters}
                className="px-6 py-2.5 rounded-full bg-emerald-600 text-white font-semibold text-xs hover:bg-emerald-700 transition"
              >
                Clear All Filters
              </button>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {products.map((product) => (
                  <Link
                    key={product.id}
                    to={`/products/${product.slug}`}
                    className="group bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-2xs hover:shadow-xl transition-all duration-300 flex flex-col transform hover:-translate-y-1"
                  >
                    <div className="relative aspect-square w-full overflow-hidden bg-gray-100">
                      <img
                        src={product.primaryImageUrl || 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80'}
                        alt={product.name}
                        className="w-full h-full object-cover object-center group-hover:scale-105 transition-transform duration-500"
                      />
                      <div className="absolute top-3 left-3 flex flex-col gap-1">
                        {product.featured && (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500 text-white shadow-2xs">
                            FEATURED
                          </span>
                        )}
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-white/95 text-gray-700 backdrop-blur-xs shadow-2xs">
                          {product.categoryName}
                        </span>
                      </div>
                      <div className="absolute top-3 right-3">
                        {product.inStock ? (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500 text-white shadow-2xs flex items-center gap-1">
                            <CheckCircle className="w-2.5 h-2.5" /> In Stock
                          </span>
                        ) : (
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-rose-500 text-white shadow-2xs">
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
                          <span className="text-[11px] text-gray-400 block font-medium">Price</span>
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

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-center gap-2 mt-10">
                  <button
                    disabled={currentPage === 0}
                    onClick={() => updateParam('page', currentPage - 1)}
                    className="p-2 rounded-xl border border-gray-300 disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50 text-gray-700"
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </button>

                  {Array.from({ length: totalPages }, (_, idx) => (
                    <button
                      key={idx}
                      onClick={() => updateParam('page', idx)}
                      className={`w-9 h-9 rounded-xl font-bold text-xs transition ${
                        currentPage === idx
                          ? 'bg-emerald-600 text-white shadow-xs'
                          : 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50'
                      }`}
                    >
                      {idx + 1}
                    </button>
                  ))}

                  <button
                    disabled={currentPage >= totalPages - 1}
                    onClick={() => updateParam('page', currentPage + 1)}
                    className="p-2 rounded-xl border border-gray-300 disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50 text-gray-700"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
                </div>
              )}
            </>
          )}
        </main>
      </div>

      {/* Mobile Filters Drawer */}
      {mobileFilterOpen && (
        <div className="fixed inset-0 z-50 flex md:hidden">
          <div className="fixed inset-0 bg-black/50 backdrop-blur-2xs" onClick={() => setMobileFilterOpen(false)} />
          <div className="relative ml-auto w-full max-w-xs bg-white h-full p-6 overflow-y-auto shadow-2xl flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between pb-4 border-b border-gray-200 mb-6">
                <h3 className="font-bold text-lg text-gray-900">Catalog Filters</h3>
                <button onClick={() => setMobileFilterOpen(false)} className="p-1 text-gray-400 hover:text-gray-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Categories */}
              <div className="mb-6">
                <h4 className="text-xs font-bold uppercase text-gray-400 mb-2">Category</h4>
                <div className="space-y-1">
                  <button
                    onClick={() => {
                      updateParam('categorySlug', null);
                      setMobileFilterOpen(false);
                    }}
                    className={`w-full text-left px-3 py-2 rounded-lg text-xs font-semibold ${
                      !currentCategory ? 'bg-emerald-600 text-white' : 'text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    All Categories
                  </button>
                  {categories.map((c) => (
                    <button
                      key={c.id}
                      onClick={() => {
                        updateParam('categorySlug', c.slug);
                        setMobileFilterOpen(false);
                      }}
                      className={`w-full text-left px-3 py-2 rounded-lg text-xs font-semibold ${
                        currentCategory === c.slug ? 'bg-emerald-600 text-white' : 'text-gray-700 hover:bg-gray-100'
                      }`}
                    >
                      {c.name}
                    </button>
                  ))}
                </div>
              </div>

              {/* In stock */}
              <div className="mb-6">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={currentInStockOnly}
                    onChange={(e) => updateParam('inStockOnly', e.target.checked)}
                    className="w-4 h-4 text-emerald-600 rounded border-gray-300"
                  />
                  <span className="text-xs font-semibold text-gray-700">In Stock Only</span>
                </label>
              </div>
            </div>

            <button
              onClick={() => {
                clearAllFilters();
                setMobileFilterOpen(false);
              }}
              className="w-full py-2.5 rounded-xl border border-gray-300 text-xs font-bold text-gray-700 hover:bg-gray-50"
            >
              Reset All Filters
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
