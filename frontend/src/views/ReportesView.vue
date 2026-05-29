<template>
  <div class="reportes-page">
    <div id="Encabezado" class="animate__animated animate__fadeInDown">
      <img src="@/assets/Imagenes/Iconos/arrow-return-left.svg" id="return" alt="Regresar" @click="router.push('/')">
      <h1 id="Titulo-Pag" class="animate__animated animate__fadeInUp animate__delay-1s">Reportes de Gestion</h1>
      <div class="header-spacer"></div>
    </div>

    <main class="container">
      <section id="Contenedor-Reporte-Vehiculos-Vendidos" class="card-section">
        <h2>Reporte de Vehiculos Vendidos</h2>
        <div class="report-actions">
          <button class="btn btn-primary" :disabled="descargando || filasSeleccionadas.length === 0" @click="descargarPdf">
            {{ descargando ? 'Descargando...' : 'Descargar PDF' }}
          </button>
        </div>
        <p v-if="mensaje" :class="['estado-mensaje', mensajeTipo]">{{ mensaje }}</p>
        <div class="report-table-container">
          <table id="Tabla-Reporte-Vehiculos-Vendidos">
            <thead>
              <tr>
                <th>
                  <input type="checkbox" :checked="todoSeleccionado" :disabled="reporte.length === 0" @change="alternarSeleccionTodos">
                </th>
                <th>Marca</th>
                <th>Modelo</th>
                <th>Unidades</th>
                <th>Total ($)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="cargando">
                <td colspan="5">Cargando reporte...</td>
              </tr>
              <tr v-else-if="reporte.length === 0">
                <td colspan="5">No hay ventas completadas</td>
              </tr>
              <template v-else>
                <tr v-for="fila in reporte" :key="`${fila.marca}-${fila.modelo}`">
                  <td><input type="checkbox" :value="filaKey(fila)" v-model="seleccion"></td>
                  <td>{{ fila.marca }}</td>
                  <td>{{ fila.modelo }}</td>
                  <td>{{ fila.unidades }}</td>
                  <td>{{ formatCurrency(fila.total) }}</td>
                </tr>
                <tr>
                  <th></th>
                  <th>Total</th>
                  <td></td>
                  <td>{{ totalUnidades }}</td>
                  <td>{{ formatCurrency(totalImporte) }}</td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiCall, apiDownload } from '@/services/api'

interface ReporteVehiculoVendido {
  marca: string
  modelo: string
  unidades: number
  total: number
}

const router = useRouter()
const reporte = ref<ReporteVehiculoVendido[]>([])
const cargando = ref(false)
const descargando = ref(false)
const seleccion = ref<string[]>([])
const mensaje = ref('')
const mensajeTipo = ref<'success' | 'error' | 'info'>('info')

const filasSeleccionadas = computed(() =>
  reporte.value.filter((fila) => seleccion.value.includes(filaKey(fila)))
)
const todoSeleccionado = computed(() =>
  reporte.value.length > 0 && filasSeleccionadas.value.length === reporte.value.length
)
const totalUnidades = computed(() => filasSeleccionadas.value.reduce((sum, fila) => sum + fila.unidades, 0))
const totalImporte = computed(() => filasSeleccionadas.value.reduce((sum, fila) => sum + Number(fila.total), 0))

const formatCurrency = (amount: number) =>
  Number(amount ?? 0).toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })

const filaKey = (fila: ReporteVehiculoVendido) => `${fila.marca}::${fila.modelo}`

const mostrarMensaje = (texto: string, tipo: 'success' | 'error' | 'info' = 'info') => {
  mensaje.value = texto
  mensajeTipo.value = tipo
  setTimeout(() => {
    mensaje.value = ''
  }, 5000)
}

const alternarSeleccionTodos = (event: Event) => {
  const checked = (event.target as HTMLInputElement).checked
  seleccion.value = checked ? reporte.value.map(filaKey) : []
}

const descargarPdf = async () => {
  if (filasSeleccionadas.value.length === 0) {
    mostrarMensaje('Selecciona al menos una fila del reporte.', 'info')
    return
  }

  descargando.value = true
  try {
    const blob = await apiDownload('/reportes/vehiculos-vendidos/pdf', {
      method: 'POST',
      body: JSON.stringify({ filas: filasSeleccionadas.value }),
    })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'reporte-vehiculos-vendidos.pdf'
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(url)
    mostrarMensaje('PDF descargado correctamente.', 'success')
  } catch (error) {
    console.error('Error al descargar PDF:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al descargar PDF.', 'error')
  } finally {
    descargando.value = false
  }
}

const cargarReporte = async () => {
  cargando.value = true
  try {
    reporte.value = await apiCall<ReporteVehiculoVendido[]>('/reportes/vehiculos-vendidos')
    seleccion.value = reporte.value.map(filaKey)
  } catch (error) {
    console.error('Error al cargar reporte:', error)
    reporte.value = []
    seleccion.value = []
  } finally {
    cargando.value = false
  }
}

onMounted(cargarReporte)
</script>

<style scoped>
@import '@/assets/Css/Global.css';
@import '@/assets/Css/Reportes.css';
</style>
