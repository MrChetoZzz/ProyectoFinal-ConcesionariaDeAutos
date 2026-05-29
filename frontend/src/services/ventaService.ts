import { apiCall } from './api'

export interface Venta {
  id: number
  fecha: string
  costoTotal: number
  estado: string
  marca: string
  modelo: string
  cliente: string
}

export interface VentaFormData {
  idCliente: number
  idVehiculo: number
  idTipoPago: number
  fecha: string
  monto?: number
}

export interface TipoPago {
  id: number
  tipoPago: string
}

export interface VentaFilters {
  sort?: string
}

export const ventaService = {
  // GET all sales with optional filters
  async getAll(filters?: VentaFilters): Promise<Venta[]> {
    const params = new URLSearchParams()
    if (filters?.sort) params.set('sort', filters.sort)

    return apiCall<Venta[]>(`/ventas?${params}`)
  },

  // GET single sale
  async getById(id: number): Promise<Venta> {
    return apiCall<Venta>(`/ventas/${id}`)
  },

  // GET payment types
  async getPaymentTypes(): Promise<TipoPago[]> {
    return apiCall<TipoPago[]>('/ventas/tipos-pago')
  },

  // POST create new sale
  async create(data: VentaFormData): Promise<Venta> {
    return apiCall<Venta>('/ventas', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  // DELETE sale (if supported by backend)
  async delete(id: number): Promise<void> {
    await apiCall(`/ventas/${id}`, {
      method: 'DELETE',
    })
  },
}
