// Base API configuration
const API_BASE_URL = 'http://localhost:8080/api'

export interface ApiError {
  message: string
  status: number
}

export async function apiCall<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`
  
  try {
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
      ...options,
    })
    
    const data = await response.json().catch(() => ({}))
    
    if (!response.ok) {
      const error = new Error(
        data.message || data.error || `HTTP ${response.status}`
      ) as Error & { status?: number }
      error.status = response.status
      throw error
    }
    
    return data as T
  } catch (error) {
    console.error(`API call failed: ${endpoint}`, error)
    throw error
  }
}
