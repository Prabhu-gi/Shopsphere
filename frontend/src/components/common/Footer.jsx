import React from 'react';
import { Link } from 'react-router-dom';

export const Footer = () => {
  return (
    <footer className="bg-gray-900 text-gray-400 text-sm mt-auto border-t border-gray-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <span className="text-xl font-bold text-white tracking-tight">
              Shop<span className="text-emerald-500">Sphere</span>
            </span>
            <p className="mt-3 text-xs leading-relaxed text-gray-400">
              An enterprise e-commerce platform offering verified stock, resilient transactions, and delightful shopping experiences.
            </p>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-3 text-xs tracking-wider uppercase">Shop</h4>
            <ul className="space-y-2 text-xs">
              <li><Link to="/products" className="hover:text-emerald-400 transition">All Products</Link></li>
              <li><Link to="/products?category=electronics" className="hover:text-emerald-400 transition">Electronics</Link></li>
              <li><Link to="/products?category=fashion" className="hover:text-emerald-400 transition">Fashion</Link></li>
              <li><Link to="/products?category=home" className="hover:text-emerald-400 transition">Home & Living</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-3 text-xs tracking-wider uppercase">Customer Support</h4>
            <ul className="space-y-2 text-xs">
              <li><Link to="/orders" className="hover:text-emerald-400 transition">Order Tracking</Link></li>
              <li><Link to="/profile" className="hover:text-emerald-400 transition">Shipping Addresses</Link></li>
              <li><span className="cursor-pointer hover:text-emerald-400 transition">Help & FAQs</span></li>
              <li><span className="cursor-pointer hover:text-emerald-400 transition">Returns Policy</span></li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-3 text-xs tracking-wider uppercase">Platform</h4>
            <p className="text-xs text-gray-400 mb-2">Powered by Spring Boot 3 & React + Vite.</p>
            <div className="flex items-center gap-2 mt-4 text-xs text-gray-500">
              <span>© {new Date().getFullYear()} ShopSphere Inc. All rights reserved.</span>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

