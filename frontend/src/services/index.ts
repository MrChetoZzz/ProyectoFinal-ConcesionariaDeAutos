// Re-export all services for convenience
export { apiCall, type ApiError } from './api'
export { clientService, type Cliente, type ClienteFormData } from './clientService'
export { vehiculoService, type Vehiculo, type VehiculoFormData, type VehiculoFilters } from './vehiculoService'
export { ventaService, type Venta, type VentaFormData, type TipoPago, type VentaFilters } from './ventaService'
export { mecanicoService, type Mecanico, type VehiculoCliente, type Reparacion, type ReparacionFormData } from './mecanicoService'
