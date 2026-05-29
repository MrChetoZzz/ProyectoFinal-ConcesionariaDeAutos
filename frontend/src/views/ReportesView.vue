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
        <div style="text-align: right; margin-bottom: 20px;">
          <button class="btn btn-primary" @click="imprimir">Imprimir Reporte</button>
        </div>
        <div class="report-table-container">
          <table id="Tabla-Reporte-Vehiculos-Vendidos">
            <thead>
              <tr>
                <th>Marca</th>
                <th>Modelo</th>
                <th>Unidades</th>
                <th>Total ($)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="cargando">
                <td colspan="4">Cargando reporte...</td>
              </tr>
              <tr v-else-if="reporte.length === 0">
                <td colspan="4">No hay ventas completadas</td>
              </tr>
              <template v-else>
                <tr v-for="fila in reporte" :key="`${fila.marca}-${fila.modelo}`">
                  <td>{{ fila.marca }}</td>
                  <td>{{ fila.modelo }}</td>
                  <td>{{ fila.unidades }}</td>
                  <td>{{ formatCurrency(fila.total) }}</td>
                </tr>
                <tr>
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

interface ReporteVehiculoVendido {
  marca: string
  modelo: string
  unidades: number
  total: number
}

const router = useRouter()
const reporte = ref<ReporteVehiculoVendido[]>([])
const cargando = ref(false)

const totalUnidades = computed(() => reporte.value.reduce((sum, fila) => sum + fila.unidades, 0))
const totalImporte = computed(() => reporte.value.reduce((sum, fila) => sum + Number(fila.total), 0))

const formatCurrency = (amount: number) =>
  Number(amount ?? 0).toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })

const imprimir = () => window.print()

const cargarReporte = async () => {
  cargando.value = true
  try {
    const response = await fetch('/api/reportes/vehiculos-vendidos')
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    reporte.value = await response.json()
  } catch (error) {
    console.error('Error al cargar reporte:', error)
    reporte.value = []
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
