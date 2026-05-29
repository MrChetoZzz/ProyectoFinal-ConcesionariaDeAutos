import { apiCall } from './api'

export interface Cliente {
  id: number
  nombreCompleto: string
  domicilio?: string
  activo: boolean
  nombre?: string
  apellidoPaterno?: string
  apellidoMaterno?: string
  telefono?: string
  curp?: string
  colonia?: string
  calle?: string
  numExt?: string
}

export interface ClienteFormData {
  nombre: string
  apellidoPaterno: string
  apellidoMaterno: string
  telefono: string
  curp: string
  colonia: string
  calle: string
  numExt: string
}

export const clientService = {
  // GET all clients
  async getAll(): Promise<Cliente[]> {
    return apiCall<Cliente[]>('/clientes')
  },

  // GET single client
  async getById(id: number): Promise<Cliente> {
    return apiCall<Cliente>(`/clientes/${id}`)
  },

  // POST create new client
  async create(data: ClienteFormData): Promise<Cliente> {
    return apiCall<Cliente>('/clientes', {
      method: 'POST',
      body: JSON.stringify(data),
    })
  },

  // PUT update client
  async update(id: number, data: ClienteFormData): Promise<Cliente> {
    return apiCall<Cliente>(`/clientes/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    })
  },

  // DELETE client
  async delete(id: number): Promise<void> {
    await apiCall(`/clientes/${id}`, {
      method: 'DELETE',
    })
  },
}
