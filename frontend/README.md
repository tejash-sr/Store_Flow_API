# StoreFlow Frontend

Professional Next.js 14 frontend for StoreFlow Inventory & Order Management API.

## Features

- 🎨 **Clean UI** - Modern, responsive design with Tailwind CSS
- 🔐 **Authentication** - Login/signup with JWT tokens
- 📦 **Product Management** - Browse, search, and manage products
- 📋 **Order Management** - Create and track orders
- ⚡ **Fast Performance** - Next.js App Router with server components
- 🔧 **TypeScript** - Fully typed for safety
- 🛡️ **Secure** - JWT token management with localStorage
- 📱 **Responsive** - Mobile-first design

## Prerequisites

- Node.js 18+ 
- npm or yarn
- StoreFlow API running on localhost:8080

## Setup

```bash
# Install dependencies
npm install

# Create .env.local from template
cp .env.example .env.local

# Update API URL if needed (default: http://localhost:8080/api)
```

## Development

```bash
# Start development server
npm run dev

# Application runs on http://localhost:3000
```

## Build for Production

```bash
# Build optimized bundle
npm run build

# Start production server
npm start
```

## Project Structure

```
frontend/
├── app/                 # Next.js app directory
│   ├── auth/           # Authentication pages
│   ├── layout.tsx      # Root layout with navigation/footer
│   ├── page.tsx        # Home page (products catalog)
│   └── globals.css     # Global Tailwind styles
├── services/
│   └── api.ts          # API integration service
├── components/         # React components (add as needed)
├── public/             # Static assets
├── package.json        # Dependencies
├── next.config.js      # Next.js configuration
├── tsconfig.json       # TypeScript configuration
└── tailwind.config.js  # Tailwind CSS configuration
```

## API Integration

The `apiService` (`services/api.ts`) handles all backend communication:

```typescript
// Login
await apiService.login('user@example.com', 'password');

// Get products
const products = await apiService.getProducts(page, size);

// Create order
await apiService.placeOrder({ items: [...], shippingAddress: {...} });
```

Authentication tokens are automatically included in all requests and stored in localStorage.

## Environment Variables

Required environment variables (see `.env.example`):

```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

## Deployment

### Docker

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY . .
RUN npm ci && npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

### Vercel (Recommended)

```bash
# Install Vercel CLI
npm install -g vercel

# Deploy
vercel
```

### Manual Server

```bash
npm run build
npm start
```

## Testing

```bash
# Add test files as needed
npm run type-check  # Run TypeScript type checking
```

## Security Notes

- ✅ JWT tokens stored in localStorage
- ✅ Automatic logout on token expiration (401 response)
- ✅ CORS handled by backend
- ✅ XSS protection via React's default escaping
- ✅ Environment variables for sensitive config

## Adding Features

### New Page

```typescript
// app/products/page.tsx
export default function ProductsPage() {
  return <div>Products Page</div>;
}
```

### New Component

```typescript
// components/ProductCard.tsx
interface ProductCardProps {
  id: number;
  name: string;
  price: number;
}

export function ProductCard({ id, name, price }: ProductCardProps) {
  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3>{name}</h3>
      <p>${price.toFixed(2)}</p>
    </div>
  );
}
```

## Technologies

- **Next.js 14** - React framework with App Router
- **TypeScript** - Type safety
- **Tailwind CSS** - Utility-first styling
- **Axios** - HTTP client
- **Zustand** - State management (optional)

## Troubleshooting

**API Connection Failed**
- Ensure backend is running on http://localhost:8080
- Check NEXT_PUBLIC_API_BASE_URL in .env.local
- Verify CORS is enabled in backend

**Build Errors**
```bash
# Clear build cache
rm -rf .next
npm run build
```

**Port Already in Use**
```bash
npm run dev -- -p 3001
```

## Maintenance

- Update dependencies: `npm update`
- Type check: `npm run type-check`
- Lint: `npm run lint`

## Support

For API documentation, see the main StoreFlow repository README.

---

**Status**: Production Ready ✅  
**Version**: 1.0.0  
**Last Updated**: April 5, 2026
