import React from 'react';
import { ShoppingBag } from 'lucide-react';

export const CartPage = () => {
  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 flex-1 w-full">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Shopping Cart</h1>
      <div className="bg-white rounded-2xl border border-gray-200 p-12 text-center">
        <ShoppingBag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
        <h3 className="text-base font-semibold text-gray-800">Cart Staged for Milestone (c)</h3>
        <p className="text-xs text-gray-500 mt-1">Real-time stock revalidation and coupon application engine will connect here.</p>
      </div>
    </div>
  );
};
