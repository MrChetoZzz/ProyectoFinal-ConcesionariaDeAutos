<template>
  <div class="reportes-page">
    <div id="Encabezado" class="animate__animated animate__fadeInDown">
      <img src="@/assets/Imagenes/Iconos/arrow-return-left.svg" id="return" alt="Regresar" @click="router.push('/')">
      <h1 id="Titulo-Pag" class="animate__animated animate__fadeInUp animate__delay-1s">Bitacora de Reportes</h1>
      <div class="header-spacer"></div>
    </div>

    <main class="container">
      <section class="card-section">
        <div class="report-header-row">
          <h2>Reportes descargados</h2>
          <button class="btn btn-primary" :disabled="cargando" @click="cargarBitacora">
            {{ cargando ? 'Cargando...' : 'Actualizar' }}
          </button>
        </div>
        <p v-if="mensaje" :class="['estado-mensaje', mensajeTipo]">{{ mensaje }}</p>
        <div class="report-table-container">
          <table>
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Reporte</th>
                <th>Formato</th>
                <th>Modulo</th>
                <th>IP</th>
                <th>Filtros</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="cargando">
                <td colspan="6">Cargando bitacora...</td>
              </tr>
              <tr v-else-if="descargas.length === 0">
                <td colspan="6">No hay descargas registradas</td>
              </tr>
              <template v-else>
                <tr v-for="descarga in descargas" :key="descarga.id">
                  <td>{{ formatDate(descarga.fechaHora) }}</td>
                  <td>{{ descarga.nombreReporte || descarga.idReporte }}</td>
                  <td>{{ descarga.formato }}</td>
                  <td>{{ descarga.modulo }}</td>
                  <td>{{ descarga.ip || 'Sin IP' }}</td>
                  <td>{{ formatFiltros(descarga.filtrosUsados) }}</td>
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
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiCall } from '@/services/api'

interface DescargaReporte {
  id: string
  fechaHora: string | null
  idUsuario?: number | null
  modulo?: string | null
  ip?: string | null
  correo?: string | null
  idReporte?: string | null
  nombreReporte?: string | null
  formato?: string | null
  filtrosUsados?: Record<string, unknown> | null
}

const router = useRouter()
const descargas = ref<DescargaReporte[]>([])
const cargando = ref(false)
const mensaje = ref('')
const mensajeTipo = ref<'success' | 'error' | 'info'>('info')

const mostrarMensaje = (texto: string, tipo: 'success' | 'error' | 'info' = 'info') => {
  mensaje.value = texto
  mensajeTipo.value = tipo
  setTimeout(() => {
    mensaje.value = ''
  }, 5000)
}

const formatDate = (value: string | null) =>
  value ? new Date(value).toLocaleString('es-MX') : 'Sin fecha'

const formatFiltros = (filtros?: Record<string, unknown> | null) => {
  if (!filtros || Object.keys(filtros).length === 0) return 'Sin filtros'
  return Object.entries(filtros)
    .map(([key, value]) => `${key}: ${String(value)}`)
    .join(', ')
}

const cargarBitacora = async () => {
  cargando.value = true
  try {
    descargas.value = await apiCall<DescargaReporte[]>('/bitacora/reportes-descargados')
  } catch (error) {
    console.error('Error al cargar bitacora de reportes:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'No se pudo cargar la bitacora.', 'error')
  } finally {
    cargando.value = false
  }
}

onMounted(cargarBitacora)
</script>

<style scoped>
@import '@/assets/Css/Global.css';
@import '@/assets/Css/Reportes.css';

.report-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
</style>
