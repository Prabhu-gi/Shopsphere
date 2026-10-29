import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import App from '../App';

describe('ShopSphere App Integration', () => {
  it('renders navbar brand logo and hero headline on landing', () => {
    render(<App />);
    const shopElements = screen.getAllByText('Shop');
    expect(shopElements.length).toBeGreaterThan(0);
    expect(screen.getByText(/Zero Overselling/i)).toBeInTheDocument();
  });

  it('renders navigation links and auth buttons', () => {
    render(<App />);
    expect(screen.getByText('Catalog')).toBeInTheDocument();
    expect(screen.getByText('Sign in')).toBeInTheDocument();
    expect(screen.getByText('Create account')).toBeInTheDocument();
  });
});
