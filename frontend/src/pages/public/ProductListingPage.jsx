import React from 'react';
import { ShoppingBag } from 'lucide-react';

export const ProductListingPage = () => {
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 flex-1 w-full">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Product Catalog</h1>
          <p className="text-sm text-gray-500 mt-1">Explore verified products with live inventory</p>
        </div>
      </div>
      <div className="bg-white rounded-2xl border border-gray-200 p-12 text-center">
        <ShoppingBag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
        <h3 className="text-base font-semibold text-gray-800">Catalog Loading in Milestone (b)</h3>
        <p className="text-xs text-gray-500 mt-1">Category filters, dynamic search, and variant selectors are staged for the next step.</p>
      </div>
    </div>
  );
};
