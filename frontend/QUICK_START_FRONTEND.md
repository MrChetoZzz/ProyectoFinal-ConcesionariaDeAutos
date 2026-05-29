# Frontend Quick Start Guide

## Installation & Running

### 1. Install Dependencies
```bash
cd frontend
npm install
```

### 2. Start Development Server
```bash
npm run dev
```
- Opens at `http://localhost:8081` (or next available port)
- Backend API must be running on `http://localhost:8080`

### 3. Build for Production
```bash
npm run build
```
- Creates optimized build in `dist/` folder

### 4. Type Check
```bash
npm run type-check
```

---

## Using the Application

### Login
1. Click the user icon in top right
2. Select role: Administrador, Vendedor, or Mecanico
3. Access role-specific features

### Clientes (Clients)
- **View:** See all registered clients
- **Add:** Fill form and click "Agregar Cliente"
- **Edit:** Click "Editar" button on any row
- **Delete:** Click "Eliminar" with confirmation

### Gestion de Vehiculos (Vehicle Management)
- **Search:** Filter by brand, model, year
- **Sort:** Change sort order (Price, Brand, etc.)
- **Add:** Click "Agregar Vehiculo" button
- **Edit:** Click "Editar" on any vehicle
- **Delete:** Click "Eliminar" with confirmation

### Gestion de Ventas (Sales Management)
- **Register:** Select vehicle, client, date, payment method
- **View:** See all sales history
- **Receipt:** Select sales and click "Generar Comprobante"
- **Delete:** Click "Eliminar" on any sale

### Taller Mecanico (Mechanic Workshop)
- **Register Repair:** Select client, vehicle, mechanic
- **Active:** See repairs in progress with Complete/Cancel options
- **Completed:** See finished repairs history
- **Cancel:** Cancels a repair (with confirmation)

---

## API Endpoints

Backend must provide these endpoints on `http://localhost:8080`:

```
GET    /api/clientes
POST   /api/clientes
GET    /api/clientes/:id
PUT    /api/clientes/:id
DELETE /api/clientes/:id

GET    /api/vehiculos?sort=...&marca=...&modelo=...&anio=...
POST   /api/vehiculos
GET    /api/vehiculos/:id
PUT    /api/vehiculos/:id
DELETE /api/vehiculos/:id
GET    /api/vehiculos/marcas
GET    /api/vehiculos/modelos?marca=...

GET    /api/ventas?sort=...
POST   /api/ventas
GET    /api/ventas/:id
DELETE /api/ventas/:id
GET    /api/ventas/tipos-pago

GET    /api/mecanica/mecanicos
GET    /api/mecanica/clientes/:clientId/vehiculos
GET    /api/mecanica/reparaciones?estado=activas|completadas
POST   /api/mecanica/reparaciones
PATCH  /api/mecanica/reparaciones/:id/completar
PATCH  /api/mecanica/reparaciones/:id/cancelar
```

---

## File Structure

```
frontend/src/
├── services/           # API service layer
│   ├── api.ts         # Base API caller
│   ├── clientService.ts
│   ├── vehiculoService.ts
│   ├── ventaService.ts
│   ├── mecanicoService.ts
│   └── index.ts
├── views/             # Page components
│   ├── ClientesView.vue
│   ├── GestionVehiculosView.vue
│   ├── GestionVentasView.vue
│   ├── MecanicaView.vue
│   ├── InicioView.vue
│   └── ...
├── router/            # Vue Router config
├── assets/            # CSS, images
├── App.vue            # Root component
└── main.ts            # Entry point
```

---

## Troubleshooting

**"Cannot GET /api/..."**
- Ensure backend is running on port 8080
- Check API endpoint spelling in backend

**API errors**
- Check browser console for detailed error messages
- Verify backend database connections
- Check backend RBAC permissions

**Build fails**
- Update Node.js to 20.19.0+
- Clear node_modules: `rm -rf node_modules && npm install`
- Check TypeScript errors: `npm run type-check`

---

## Features Implemented ✅

- ✅ Full CRUD for all modules
- ✅ Modal dialogs for forms
- ✅ Edit/Delete with confirmation
- ✅ Form validation
- ✅ Error/success messages
- ✅ Loading states
- ✅ Responsive tables
- ✅ Currency/Date formatting
- ✅ Service layer with error handling
- ✅ TypeScript support
- ✅ Vue 3 Composition API

---

## Support

For detailed documentation, see: `FRONTEND_IMPLEMENTATION.md`
