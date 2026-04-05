'use client';

import { useEffect, useState } from 'react';
import { apiService } from '@/services/api';

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  status: string;
}

export default function Home() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await apiService.getProducts();
      setProducts(data);
    } catch (err) {
      setError('Failed to load products');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="py-12">
      <div className="mb-8">
        <h1 className="text-4xl font-bold text-gray-900 mb-2">Products Catalog</h1>
        <p className="text-gray-600">Browse our complete inventory</p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading products...</p>
        </div>
      ) : products.length === 0 ? (
        <div className="text-center py-12 bg-gray-100 rounded-lg">
          <p className="text-gray-600 text-lg">No products found</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {products.map((product) => (
            <div
              key={product.id}
              className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow overflow-hidden"
            >
              <div className="bg-gradient-to-r from-primary to-blue-600 h-32"></div>
              <div className="p-6">
                <h3 className="text-xl font-bold text-gray-900 mb-2">{product.name}</h3>
                <p className="text-gray-600 text-sm mb-4 line-clamp-2">{product.description}</p>
                
                <div className="flex justify-between items-center mb-4">
                  <span className="text-2xl font-bold text-primary">${product.price.toFixed(2)}</span>
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                    product.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                  }`}>
                    {product.status}
                  </span>
                </div>

                <div className="mb-4">
                  <p className="text-sm text-gray-600">
                    Stock: <span className="font-bold text-gray-900">{product.stockQuantity} units</span>
                  </p>
                </div>

                <button className="w-full bg-primary text-white py-2 rounded-lg hover:bg-blue-700 font-medium">
                  View Details
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
