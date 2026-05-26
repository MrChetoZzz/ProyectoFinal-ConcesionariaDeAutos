# Endpoint Testing Guide

## Quick Start

### 1. Start the Application

```bash
cd c:\Users\cesar\Source\Repos\ProyectoFinal-BD\app
.\mvnw.cmd spring-boot:run
```

The backend will start on `http://localhost:8080`

### 2. Run Tests

#### Windows (PowerShell)
```powershell
cd c:\Users\cesar\Source\Repos\ProyectoFinal-BD
.\TEST_ENDPOINTS.bat
```

#### Linux/Mac (Bash)
```bash
cd c:\Users\cesar\Source\Repos\ProyectoFinal-BD
bash TEST_ENDPOINTS.sh
```

---

## Endpoint Summary & Tests

### 1. CLIENTES Controller (`/api/clientes`)

#### GET /api/clientes
List all active clients
```bash
curl -s http://localhost:8080/api/clientes | jq
```
Expected Response: Array of `ClienteResumenDto`
```json
[
  {
    "id": 1,
    "nombreCompleto": "Juan Perez Lopez",
    "domicilio": "Calle Principal No. 123 | Tel. 5551234567",
    "activo": true
  }
]
```

#### GET /api/clientes/{id}
❌ **Not exposed** - Use GET /api/clientes and filter, or implement if needed

#### POST /api/clientes
Create new client
```bash
curl -X POST http://localhost:8080/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellidoPaterno": "Perez",
    "apellidoMaterno": "Lopez",
    "telefono": "5551234567",
    "colonia": "Centro",
    "calle": "Avenida Principal",
    "numExt": "123"
  }' | jq
```
Expected Response: Success message

#### PUT /api/clientes/{id}
Update client information
```bash
curl -X PUT http://localhost:8080/api/clientes/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellidoPaterno": "Perez",
    "apellidoMaterno": "Lopez",
    "telefono": "5559876543",
    "colonia": "Nuevo Centro",
    "calle": "Nueva Avenida",
    "numExt": "456"
  }' | jq
```
Expected Response: Success message

#### DELETE /api/clientes/{id}
Deactivate (soft delete) client
```bash
curl -X DELETE http://localhost:8080/api/clientes/1
```
Expected Response: Success message

---

### 2. VEHICULOS Controller (`/api/vehiculos`)

#### GET /api/vehiculos
List vehicles with optional filters
```bash
curl -s http://localhost:8080/api/vehiculos | jq
curl -s "http://localhost:8080/api/vehiculos?marca=Toyota" | jq
curl -s "http://localhost:8080/api/vehiculos?modelo=Corolla&sort=Precio-Ascendente" | jq
```
Expected Response: Array of `VehiculoDto`
```json
[
  {
    "id": 1,
    "marca": "Toyota",
    "modelo": "Corolla",
    "anioModelo": 2023,
    "placas": "ABC-1234",
    "numeroSerie": "SN123456",
    "costo": 150000.00,
    "idVehiculoCondicion": 1,
    "condicion": "Nuevo",
    "fechaRegistro": "2024-01-15T10:30:00",
    "disponible": true
  }
]
```

#### GET /api/vehiculos/{id}
Get specific vehicle
```bash
curl -s http://localhost:8080/api/vehiculos/1 | jq
```

#### GET /api/vehiculos/marcas
List all brands
```bash
curl -s http://localhost:8080/api/vehiculos/marcas | jq
```
Expected Response: `["Toyota", "Honda", "Ford", ...]`

#### GET /api/vehiculos/modelos?marca=Toyota
List models for brand
```bash
curl -s "http://localhost:8080/api/vehiculos/modelos?marca=Toyota" | jq
```
Expected Response: `["Corolla", "Camry", "RAV4", ...]`

#### POST /api/vehiculos
Create new vehicle (✅ ADDED)
```bash
curl -X POST http://localhost:8080/api/vehiculos \
  -H "Content-Type: application/json" \
  -d '{
    "marca": "Toyota",
    "modelo": "Corolla",
    "anioModelo": 2024,
    "costo": 150000.00,
    "condicion": "Nuevo",
    "placas": "XYZ-5678",
    "numeroSerie": "SN789012"
  }' | jq
```

#### PUT /api/vehiculos/{id}
Update vehicle details (✅ ADDED)
```bash
curl -X PUT http://localhost:8080/api/vehiculos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "marca": "Honda",
    "modelo": "Civic",
    "anioModelo": 2024,
    "costo": 160000.00,
    "condicion": "Nuevo",
    "placas": "XYZ-5678",
    "numeroSerie": "SN789012"
  }' | jq
```

#### DELETE /api/vehiculos/{id}
Delete vehicle (✅ ADDED)
```bash
curl -X DELETE http://localhost:8080/api/vehiculos/1
```
Expected Response: `{"mensaje": "Vehiculo eliminado correctamente."}`

---

### 3. VENTAS Controller (`/api/ventas`)

#### GET /api/ventas
List sales with sorting
```bash
curl -s http://localhost:8080/api/ventas | jq
curl -s "http://localhost:8080/api/ventas?sort=Fecha-Ascendente" | jq
```

#### GET /api/ventas/tipos-pago
List payment types
```bash
curl -s http://localhost:8080/api/ventas/tipos-pago | jq
```
Expected Response:
```json
[
  {"id": 1, "tipoPago": "Efectivo"},
  {"id": 2, "tipoPago": "Tarjeta de Crédito"}
]
```

#### POST /api/ventas
Register new sale
```bash
curl -X POST http://localhost:8080/api/ventas \
  -H "Content-Type: application/json" \
  -d '{
    "idCliente": 1,
    "idVehiculo": 1,
    "idTipoPago": 1,
    "monto": 150000.00
  }' | jq
```

---

### 4. MECANICA Controller (`/api/mecanica`)

#### GET /api/mecanica/mecanicos
List active mechanics
```bash
curl -s http://localhost:8080/api/mecanica/mecanicos | jq
```
Expected Response:
```json
[
  {
    "id": 1,
    "nombre": "Carlos Martinez",
    "especializacion": "Motores"
  }
]
```

#### GET /api/mecanica/clientes/{id}/vehiculos
List vehicles owned by client
```bash
curl -s http://localhost:8080/api/mecanica/clientes/1/vehiculos | jq
```

#### GET /api/mecanica/reparaciones
List active repairs
```bash
curl -s http://localhost:8080/api/mecanica/reparaciones | jq
curl -s "http://localhost:8080/api/mecanica/reparaciones?estado=completadas" | jq
```

#### POST /api/mecanica/reparaciones
Create new repair ticket
```bash
curl -X POST http://localhost:8080/api/mecanica/reparaciones \
  -H "Content-Type: application/json" \
  -d '{
    "idCliente": 1,
    "idVehiculo": 1,
    "descripcionProblema": "Motor hace ruido extraño",
    "costoEstimado": 2000.00,
    "diagnosticoInicial": "Posible problema en pastillas de freno"
  }' | jq
```

#### PUT /api/mecanica/reparaciones/{id}
Update repair details (✅ ADDED)
```bash
curl -X PUT http://localhost:8080/api/mecanica/reparaciones/1 \
  -H "Content-Type: application/json" \
  -d '{
    "idMecanico": 1,
    "costoEstimado": 2500.00,
    "descripcionProblema": "Cambio de pastillas de freno y aceite"
  }' | jq
```

#### PATCH /api/mecanica/reparaciones/{id}/completar
Mark repair as complete
```bash
curl -X PATCH http://localhost:8080/api/mecanica/reparaciones/1/completar \
  -H "Content-Type: application/json" \
  -d '{"costoFinal": 2300.00}' | jq
```

#### PATCH /api/mecanica/reparaciones/{id}/cancelar
Cancel repair
```bash
curl -X PATCH http://localhost:8080/api/mecanica/reparaciones/1/cancelar
```

---

## Status Codes Reference

| Code | Meaning | Example |
|------|---------|---------|
| 200  | OK | GET, PUT, PATCH successful |
| 201  | Created | POST successful |
| 400  | Bad Request | Missing required fields |
| 404  | Not Found | Resource doesn't exist |
| 500  | Server Error | Database connection issue |

---

## Common Issues & Solutions

### Issue: "No route matches"
- **Cause**: Wrong URL or method
- **Solution**: Check endpoint path and HTTP method

### Issue: "Cliente no encontrado"
- **Cause**: ID doesn't exist or inactive
- **Solution**: Verify ID exists via GET /clientes

### Issue: "No hay usuarios activos"
- **Cause**: Creating sale but no active users in database
- **Solution**: Create a user in Usuario table

### Issue: Connection refused
- **Cause**: Backend not running or wrong port
- **Solution**: Start with `.\mvnw.cmd spring-boot:run`

---

## Frontend Integration Checklist

✅ **Before connecting frontend:**
1. [ ] Backend started on http://localhost:8080
2. [ ] Database connection working
3. [ ] GET /api/clientes returns 200 OK
4. [ ] GET /api/vehiculos returns 200 OK
5. [ ] CORS configured (wildcard allows all origins)
6. [ ] Error responses contain proper status codes
7. [ ] All DTOs using camelCase in JSON

✅ **Update frontend to use:**
- Base URL: `http://localhost:8080/api`
- Header: `Content-Type: application/json`
- All endpoints are RESTful (GET, POST, PUT, DELETE, PATCH)
- Handle HTTP error codes with try-catch blocks
