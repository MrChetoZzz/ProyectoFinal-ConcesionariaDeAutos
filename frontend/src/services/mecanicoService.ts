import { apiCall } from './api'

export interface Mecanico {
  id: number
  nombre: string
  especializacion: string
}

export interface VehiculoCliente {
  id: number
  marca: string
  modelo: string
  anioModelo: number
}

export interface Reparacion {
  id: number
  marca: string
  modelo: string
  cliente: string
  estado: string
  fechaIngreso: string
  descripcionProblema: string
}

export interface ReparacionFormData {
  idCliente: number
  idVehiculo: number
  idMecanico?: number | null
  fechaIngreso: string
  fechaSalida?: string | null
  descripcionProblema: string
  diagnosticoInicial?: string
  costoEstimado?: number | null
}

export const mecanicoService = {
  // GET all mechanics
  async getMechanics(): Promise<Mecanico[]> {
    return apiCall<Mecanico[]>('/mecanica/mecanicos')
  },

  // GET client vehicles
  async getClientVehicles(clientId: number): Promise<VehiculoCliente[]> {
    return apiCall<VehiculoCliente[]>(
      `/mecanica/clientes/${clientId}/vehiculos`
    )
  },

  // GET repairs by status
  async getRepairs(estado: 'activas' | 'completadas'): Promise<Reparacion[]> {
    return apiCall<Reparacion[]>(`/mecanica/reparaciones?estado=${estado}`)
  },

  // GET single repair
  async getRepairById(id: number): Promise<Reparacion> {
    return apiCall<Reparacion>(`/mecanica/reparaciones/${id}`)
  },

  // POST create new repair
  async createRepair(data: ReparacionFormData): Promise<Reparacion> {
    return apiCall<Reparacion>('/mecanica/reparaciones', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  // PATCH complete repair
  async completeRepair(id: number): Promise<Reparacion> {
    return apiCall<Reparacion>(`/mecanica/reparaciones/${id}/completar`, {
      method: 'PATCH',
      body: JSON.stringify({}),
    })
  },

  // PATCH cancel repair
  async cancelRepair(id: number): Promise<Reparacion> {
    return apiCall<Reparacion>(`/mecanica/reparaciones/${id}/cancelar`, {
      method: 'PATCH',
      body: JSON.stringify({}),
    })
  },
}
