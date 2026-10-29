import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldCheck, Zap, RefreshCw, ArrowRight, Layers, Star } from 'lucide-react';

export const HomePage = () => {
  return (
    <div className="flex-1">
      {/* Hero Section */}
      <section className="bg-gradient-to-b from-emerald-50 via-white to-gray-50 py-20 px-4 sm:px-6 lg:px-8 border-b border-gray-100">
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
              Browse Catalog <ArrowRight className="w-4 h-4" />
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

      {/* Feature Badges */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition">
            <div className="w-12 h-12 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center mb-4">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-2">Atomic Inventory Locking</h3>
            <p className="text-sm text-gray-600 leading-relaxed">
              Thread-safe database concurrency control guarantees that parallel orders never oversell the final available stock.
            </p>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition">
            <div className="w-12 h-12 rounded-xl bg-teal-100 text-teal-600 flex items-center justify-center mb-4">
              <RefreshCw className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-2">Automated Restock & Webhooks</h3>
            <p className="text-sm text-gray-600 leading-relaxed">
              Order cancellations trigger instantaneous inventory replenishment; Stripe webhooks execute idempotently.
            </p>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition">
            <div className="w-12 h-12 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center mb-4">
              <Star className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-2">Verified-Purchase Reviews</h3>
            <p className="text-sm text-gray-600 leading-relaxed">
              Only authentic customers who completed an order can submit product ratings, keeping the feedback trustworthy.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
};

