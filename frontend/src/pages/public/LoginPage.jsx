import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { Lock, Mail, ArrowRight, ShieldCheck, User, AlertCircle } from 'lucide-react';

export const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [sessionExpiredNotice, setSessionExpiredNotice] = useState(false);
  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('expired') === 'true') {
      setSessionExpiredNotice(true);
    }
  }, [location.search]);

  const from = location.state?.from?.pathname || '/';

  const performLogin = async (userEmail, userPassword) => {
    if (!userEmail || !userPassword) {
      showToast('Please provide both email and password', 'error');
      return;
    }

    setSubmitting(true);
    try {
      const loggedInUser = await login(userEmail, userPassword);
      showToast(`Welcome back, ${loggedInUser.firstName}!`);
      
      // If logging in as admin and destination was generic home, direct to admin products
      if (loggedInUser.role === 'ADMIN' && from === '/') {
        navigate('/admin/products', { replace: true });
      } else {
        navigate(from, { replace: true });
      }
    } catch (err) {
      let msg = 'Invalid email or password';
      if (err.response?.data?.message) {
        msg = err.response.data.message;
      } else if (err.code === 'ERR_NETWORK' || err.message?.includes('Network Error')) {
        msg = 'Cannot connect to backend server. Please verify backend is running on port 8080.';
      } else if (err.message) {
        msg = err.message;
      }
      showToast(msg, 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    performLogin(email, password);
  };

  const handleQuickLogin = (quickEmail, quickPassword) => {
    setEmail(quickEmail);
    setPassword(quickPassword);
    performLogin(quickEmail, quickPassword);
  };

  return (
    <div className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-2xl border border-gray-200 shadow-sm">
        <div className="text-center">
          <h2 className="text-3xl font-extrabold text-gray-900 tracking-tight">Sign in to ShopSphere</h2>
          <p className="mt-2 text-sm text-gray-600">
            Or{' '}
            <Link to="/register" className="font-medium text-emerald-600 hover:text-emerald-500">
              create a new customer account
            </Link>
          </p>
        </div>

        {sessionExpiredNotice && (
          <div className="bg-amber-50 border border-amber-200 text-amber-800 rounded-xl p-3 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0 text-amber-600" />
            <span>Your session has expired. Please sign in again to continue.</span>
          </div>
        )}

        {/* Demo Fast Logins with 1-click execution */}
        <div className="bg-gray-50 border border-gray-200 rounded-xl p-3 space-y-2">
          <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider text-center">
            Demo 1-Click Fast Sign-In
          </p>
          <div className="flex flex-col sm:flex-row gap-2">
            <button
              type="button"
              disabled={submitting}
              onClick={() => handleQuickLogin('admin@shopsphere.com', 'Admin@123')}
              className="flex-1 text-xs py-2 px-3 rounded-lg bg-white border border-gray-300 font-semibold text-gray-700 hover:bg-emerald-50 hover:border-emerald-300 hover:text-emerald-700 flex items-center justify-center gap-1.5 shadow-xs transition disabled:opacity-50"
            >
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" /> Sign In as Admin
            </button>
            <button
              type="button"
              disabled={submitting}
              onClick={() => handleQuickLogin('customer@shopsphere.com', 'Password@123')}
              className="flex-1 text-xs py-2 px-3 rounded-lg bg-white border border-gray-300 font-semibold text-gray-700 hover:bg-blue-50 hover:border-blue-300 hover:text-blue-700 flex items-center justify-center gap-1.5 shadow-xs transition disabled:opacity-50"
            >
              <User className="w-3.5 h-3.5 text-blue-600" /> Sign In as Customer
            </button>
          </div>
        </div>

        <form className="mt-6 space-y-6" onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                Email address
              </label>
              <div className="relative">
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                  className="w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition"
                />
                <Mail className="w-4 h-4 text-gray-400 absolute left-3 top-3.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase tracking-wider mb-1">
                Password
              </label>
              <div className="relative">
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition"
                />
                <Lock className="w-4 h-4 text-gray-400 absolute left-3 top-3.5" />
              </div>
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full py-3 px-4 rounded-xl text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-emerald-500 shadow-md transition disabled:opacity-50 flex items-center justify-center gap-2"
          >
            {submitting ? 'Authenticating...' : 'Sign in'} <ArrowRight className="w-4 h-4" />
          </button>
        </form>
      </div>
    </div>
  );
};
