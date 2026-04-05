import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'StoreFlow - Inventory Management',
  description: 'Professional inventory and order management system',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <nav className="bg-white shadow-md">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center h-16">
              <div className="flex items-center gap-2">
                <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center text-white font-bold text-lg">
                  SF
                </div>
                <h1 className="text-2xl font-bold text-gray-900">StoreFlow</h1>
              </div>
              <ul className="flex gap-6 items-center">
                <li><a href="/" className="hover:text-primary font-medium">Products</a></li>
                <li><a href="/orders" className="hover:text-primary font-medium">Orders</a></li>
                <li><a href="/auth/login" className="bg-primary text-white px-4 py-2 rounded-lg hover:bg-blue-700">Login</a></li>
              </ul>
            </div>
          </div>
        </nav>

        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {children}
        </main>

        <footer className="bg-gray-900 text-white mt-16">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="grid grid-cols-3 gap-8">
              <div>
                <h3 className="text-lg font-bold mb-4">StoreFlow</h3>
                <p className="text-gray-400">Professional inventory and order management</p>
              </div>
              <div>
                <h4 className="font-bold mb-4">Product</h4>
                <ul className="text-gray-400 space-y-2">
                  <li><a href="#" className="hover:text-white">Features</a></li>
                  <li><a href="#" className="hover:text-white">Pricing</a></li>
                  <li><a href="#" className="hover:text-white">API Docs</a></li>
                </ul>
              </div>
              <div>
                <h4 className="font-bold mb-4">Legal</h4>
                <ul className="text-gray-400 space-y-2">
                  <li><a href="#" className="hover:text-white">Privacy</a></li>
                  <li><a href="#" className="hover:text-white">Terms</a></li>
                  <li><a href="#" className="hover:text-white">Contact</a></li>
                </ul>
              </div>
            </div>
            <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
              <p>&copy; 2026 StoreFlow. All rights reserved.</p>
            </div>
          </div>
        </footer>
      </body>
    </html>
  );
}
