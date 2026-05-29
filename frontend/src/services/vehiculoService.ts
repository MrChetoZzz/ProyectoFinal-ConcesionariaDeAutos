import { apiCall } from './api'

export interface Vehiculo {
  id: number
  marca: string
  modelo: string
  anioModelo: number
  placas?: string | null
  numeroSerie?: string | null
  costo?: number | null
  idVehiculoCondicion: number
  condicion: string
  fechaRegistro: string
  disponible: boolean
  imagenPrincipal?: string | null
}

export interface VehiculoCatalogo {
  id: number
  marca: string
  modelo: string
  anioModelo: number
  costo?: number | null
  idVehiculoCondicion: number
  condicion: string
  unidades: number
  stock: number
  imagenPrincipal?: string | null
}

export interface VehiculoFormData {
  marca: string
  modelo: string
  anioModelo: number
  costo?: number
  condicion: string
  placas?: string
  numeroSerie?: string
}

export interface VehiculoFilters {
  sort?: string
  marca?: string
  modelo?: string
  anio?: number
}

export const vehiculoService = {
  // GET all vehicles with optional filters
  async getAll(filters?: VehiculoFilters): Promise<Vehiculo[]> {
    const params = new URLSearchParams()
    if (filters?.sort) params.set('sort', filters.sort)
    if (filters?.marca) params.set('marca', filters.marca)
    if (filters?.modelo) params.set('modelo', filters.modelo)
    if (filters?.anio) params.set('anio', String(filters.anio))

    return apiCall<Vehiculo[]>(`/vehiculos?${params}`)
  },

  // GET single vehicle
  async getById(id: number): Promise<Vehiculo> {
    return apiCall<Vehiculo>(`/vehiculos/${id}`)
  },

  // GET catalog grouped by vehicle line with stock count
  async getCatalog(filters?: VehiculoFilters): Promise<VehiculoCatalogo[]> {
    const params = new URLSearchParams()
    if (filters?.sort) params.set('sort', filters.sort)
    if (filters?.marca) params.set('marca', filters.marca)
    if (filters?.modelo) params.set('modelo', filters.modelo)
    if (filters?.anio) params.set('anio', String(filters.anio))

    return apiCall<VehiculoCatalogo[]>(`/vehiculos/catalogo?${params}`)
  },

  // GET brands
  async getBrands(): Promise<string[]> {
    return apiCall<string[]>('/vehiculos/marcas')
  },

  // GET models by brand
  async getModelsByBrand(brand: string): Promise<string[]> {
    return apiCall<string[]>(
      `/vehiculos/modelos?marca=${encodeURIComponent(brand)}`
    )
  },

  // POST create new vehicle
  async create(data: VehiculoFormData): Promise<Vehiculo> {
    return apiCall<Vehiculo>('/vehiculos', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  // PUT update vehicle
  async update(id: number, data: VehiculoFormData): Promise<Vehiculo> {
    return apiCall<Vehiculo>(`/vehiculos/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    })
  },

  // DELETE vehicle
  async delete(id: number): Promise<void> {
    await apiCall(`/vehiculos/${id}`, {
      method: 'DELETE',
    })
  },
}
