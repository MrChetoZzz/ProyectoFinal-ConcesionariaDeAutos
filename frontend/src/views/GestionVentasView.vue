<template>
  <div class="gestion-ventas-page">
    <div id="Encabezado" class="animate__animated animate__fadeInDown">
      <img src="@/assets/Imagenes/Iconos/arrow-return-left.svg" id="return" alt="Regresar" @click="router.push('/')">
      <h1 id="Titulo-Pag" class="animate__animated animate__fadeInUp animate__delay-1s">Gestion de Ventas</h1>
      <div class="header-spacer"></div>
    </div>

    <main class="container">
      <section id="Registro-Venta-Section" class="card-section">
        <h2>Registrar Nueva Venta</h2>
        <form id="Registrar-Nueva-Venta" @submit.prevent="registrarVenta">
          <div class="form-group">
            <label for="Vehiculo">Vehiculo</label>
            <select id="Vehiculo" v-model.number="form.idVehiculo" required>
              <option :value="null" disabled>Seleccione un vehiculo</option>
              <option v-for="vehiculo in vehiculosDisponibles" :key="vehiculo.id" :value="vehiculo.id">
                {{ vehiculo.marca }} {{ vehiculo.modelo }} - {{ formatCurrency(vehiculo.costo) }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="Cliente">Cliente</label>
            <select id="Cliente" v-model.number="form.idCliente" required>
              <option :value="null" disabled>Seleccione un cliente</option>
              <option v-for="cliente in clientes" :key="cliente.id" :value="cliente.id">{{ cliente.nombreCompleto }}</option>
            </select>
          </div>

          <div class="form-group">
            <label for="Fecha">Fecha</label>
            <input type="date" id="Fecha" v-model="form.fecha" required>
          </div>

          <div class="form-group">
            <label for="PrecioFinal">Precio Final</label>
            <input type="number" id="PrecioFinal" :value="vehiculoSeleccionado?.costo || 0" disabled>
          </div>

          <div class="form-group">
            <label for="MetodoPago">Metodo De Pago</label>
            <select id="MetodoPago" v-model.number="form.idTipoPago" required>
              <option :value="null" disabled>Seleccione el metodo de pago</option>
              <option v-for="tipo in tiposPago" :key="tipo.id" :value="tipo.id">{{ tipo.tipoPago }}</option>
            </select>
          </div>

          <div class="form-group">
            <label for="Monto">Monto</label>
            <input type="number" id="Monto" v-model.number="form.monto" min="0" :placeholder="String(vehiculoSeleccionado?.costo || 0)">
          </div>

          <div id="Contenedor-Botones" class="form-group-btn">
            <button type="button" class="btn btn-cancel" @click="limpiarFormulario">Cancelar</button>
            <button type="submit" class="btn btn-primary">Registrar Venta</button>
          </div>
        </form>
        <p v-if="mensaje" :class="['estado-mensaje', mensajeTipo]">{{ mensaje }}</p>
      </section>

      <section id="Historial-Ventas-Section" class="card-section">
        <h2>Historial de Ventas</h2>
        <div id="Contenedor-Historial">
          <div class="filtro-group" id="comprobante-controls">
            <label for="Filtrar">Filtrar por:</label>
            <select id="Filtrar" v-model="sort" @change="cargarVentas">
              <option value="Folio-Ascendente">Folio ▲</option>
              <option value="Folio-Descendente">Folio ▼</option>
              <option value="Nombre-Ascendente">Nombre de Cliente ▲</option>
              <option value="Nombre-Descendente">Nombre de Cliente ▼</option>
              <option value="Marca-Ascendente">Marca ▲</option>
              <option value="Marca-Descendente">Marca ▼</option>
              <option value="Modelo-Ascendente">Modelo ▲</option>
              <option value="Modelo-Descendente">Modelo ▼</option>
              <option value="Precio-Ascendente">Precio ▲</option>
              <option value="Precio-Descendente">Precio ▼</option>
              <option value="Fecha-Ascendente">Fecha ▲</option>
              <option value="Fecha-Descendente">Fecha ▼</option>
            </select>
            <button type="button" class="btn btn-accent" @click="abrirComprobante">Generar Comprobante</button>
          </div>

          <div class="table-responsive" id="table-container">
            <table id="Tabla-Historial-Ventas">
              <thead>
                <tr>
                  <th>Seleccionar</th>
                  <th>Folio</th>
                  <th>Fecha</th>
                  <th>Marca</th>
                  <th>Modelo</th>
                  <th>Cliente</th>
                  <th>Precio</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="ventas.length === 0">
                  <td colspan="9">No hay ventas registradas</td>
                </tr>
                <tr v-for="venta in ventas" :key="venta.id">
                  <td><input type="checkbox" :value="venta.id" v-model="ventasSeleccionadas"></td>
                  <td>{{ venta.id }}</td>
                  <td>{{ formatDate(venta.fecha) }}</td>
                  <td>{{ venta.marca }}</td>
                  <td>{{ venta.modelo }}</td>
                  <td>{{ venta.cliente }}</td>
                  <td>{{ formatCurrency(venta.costoTotal) }}</td>
                  <td>{{ venta.estado }}</td>
                  <td>
                    <button class="table-action-btn delete-btn" @click="eliminarVenta(venta.id)" title="Eliminar venta">Eliminar</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </main>

    <div id="comprobante-modal" :class="['modal-overlay', { active: mostrarComprobante }]">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Comprobante de Venta</h3>
          <button type="button" class="modal-close" @click="mostrarComprobante = false">&times;</button>
        </div>
        <div id="comprobante-body" class="comprobante-scroller">
          <div v-for="venta in comprobantes" :key="venta.id" class="comprobante-individual">
            <p><strong>Folio:</strong> {{ venta.id }}</p>
            <p><strong>Fecha:</strong> {{ formatDate(venta.fecha) }}</p>
            <p><strong>Cliente:</strong> {{ venta.cliente }}</p>
            <p><strong>Vehiculo:</strong> {{ venta.marca }} {{ venta.modelo }}</p>
            <p><strong>Total:</strong> {{ formatCurrency(venta.costoTotal) }}</p>
            <hr>
          </div>
        </div>
        <div style="text-align: right; margin-top: 20px;">
          <button class="btn btn-primary" @click="imprimir">Imprimir Comprobante</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ventaService, type VentaFormData } from '@/services/ventaService'

interface Cliente {
  id: number
  nombreCompleto: string
}

interface Vehiculo {
  id: number
  marca: string
  modelo: string
  costo: number | null
  disponible: boolean
}

interface TipoPago {
  id: number
  tipoPago: string
}

interface Venta {
  id: number
  fecha: string
  costoTotal: number
  estado: string
  marca: string
  modelo: string
  cliente: string
}

const router = useRouter()
const clientes = ref<Cliente[]>([])
const vehiculos = ref<Vehiculo[]>([])
const tiposPago = ref<TipoPago[]>([])
const ventas = ref<Venta[]>([])
const ventasSeleccionadas = ref<number[]>([])
const mostrarComprobante = ref(false)
const sort = ref('Fecha-Descendente')
const mensaje = ref('')
const mensajeTipo = ref<'success' | 'error' | 'info'>('info')

const form = reactive({
  idCliente: null as number | null,
  idVehiculo: null as number | null,
  idTipoPago: null as number | null,
  monto: null as number | null,
  fecha: new Date().toISOString().slice(0, 10),
})

const vehiculosDisponibles = computed(() => vehiculos.value.filter((vehiculo) => vehiculo.disponible))
const vehiculoSeleccionado = computed(() => vehiculos.value.find((vehiculo) => vehiculo.id === form.idVehiculo))
const comprobantes = computed(() => ventas.value.filter((venta) => ventasSeleccionadas.value.includes(venta.id)))

const formatCurrency = (amount: number | null) =>
  Number(amount ?? 0).toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })

const formatDate = (value: string) => value ? new Date(value).toLocaleDateString('es-MX') : ''
const imprimir = () => window.print()

const mostrarMensaje = (texto: string, tipo: 'success' | 'error' | 'info' = 'info') => {
  mensaje.value = texto
  mensajeTipo.value = tipo
  setTimeout(() => {
    mensaje.value = ''
  }, 5000)
}

const cargarCatalogos = async () => {
  try {
    const [clientesData, vehiculosData, tiposPagoData] = await Promise.all([
      fetch('/api/clientes').then(r => r.json()),
      fetch('/api/vehiculos?sort=Marca-Ascendente').then(r => r.json()),
      fetch('/api/ventas/tipos-pago').then(r => r.json()),
    ])
    clientes.value = clientesData || []
    vehiculos.value = vehiculosData || []
    tiposPago.value = tiposPagoData || []
  } catch (error) {
    console.error('Error al cargar catálogos:', error)
    mostrarMensaje('Error al cargar los catálogos.', 'error')
  }
}

const cargarVentas = async () => {
  try {
    ventas.value = await ventaService.getAll({ sort: sort.value })
  } catch (error) {
    console.error('Error al cargar ventas:', error)
    mostrarMensaje('Error al cargar las ventas.', 'error')
  }
}

const limpiarFormulario = () => {
  Object.assign(form, {
    idCliente: null,
    idVehiculo: null,
    idTipoPago: null,
    monto: null,
    fecha: new Date().toISOString().slice(0, 10),
  })
}

const registrarVenta = async () => {
  if (!form.idCliente || !form.idVehiculo || !form.idTipoPago) {
    mostrarMensaje('Por favor completa todos los campos requeridos.', 'error')
    return
  }

  try {
    const datos: VentaFormData = {
      idCliente: form.idCliente,
      idVehiculo: form.idVehiculo,
      idTipoPago: form.idTipoPago,
      fecha: form.fecha,
      monto: form.monto || vehiculoSeleccionado.value?.costo,
    }
    await ventaService.create(datos)
    mostrarMensaje('Venta registrada correctamente.', 'success')
    limpiarFormulario()
    await Promise.all([cargarCatalogos(), cargarVentas()])
  } catch (error) {
    console.error('Error al registrar venta:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al registrar venta.', 'error')
  }
}

const abrirComprobante = () => {
  if (ventasSeleccionadas.value.length === 0) {
    mostrarMensaje('Selecciona al menos una venta para generar comprobante.', 'info')
    return
  }
  mostrarComprobante.value = true
}

const eliminarVenta = async (id: number) => {
  if (!confirm('¿Deseas eliminar esta venta?')) return

  try {
    await ventaService.delete(id)
    mostrarMensaje('Venta eliminada correctamente.', 'success')
    await cargarVentas()
  } catch (error) {
    console.error('Error al eliminar venta:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al eliminar venta.', 'error')
  }
}

onMounted(async () => {
  await Promise.all([cargarCatalogos(), cargarVentas()])
})
</script>

<style scoped>
@import '@/assets/Css/Global.css';
@import '@/assets/Css/GestionDeVentas.css';

.estado-mensaje {
  margin-top: 16px;
  font-weight: 600;
  text-align: center;
}

.estado-mensaje.success {
  color: #20783a;
}

.estado-mensaje.error {
  color: #b42318;
}

.estado-mensaje.info {
  color: #0A2E5E;
}
</style>
