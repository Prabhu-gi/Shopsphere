import React from 'react';
import { LayoutDashboard } from 'lucide-react';

export const AdminDashboardPage = () => {
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 flex-1 w-full">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Administrator Dashboard</h1>
      <div className="bg-white rounded-2xl border border-gray-200 p-12 text-center">
        <LayoutDashboard className="w-12 h-12 text-gray-300 mx-auto mb-3" />
        <h3 className="text-base font-semibold text-gray-800">Admin Control Center</h3>
        <p className="text-xs text-gray-500 mt-1">Product CRUD, category management, inventory adjustment, and analytics dashboard.</p>
      </div>
    </div>
  );
};

