<template>
  <div class="mecanica-page">
    <div id="Encabezado" class="animate__animated animate__fadeInDown">
      <img src="@/assets/Imagenes/Iconos/arrow-return-left.svg" id="return" alt="Regresar" @click="router.push('/')">
      <h1 id="Titulo-Pag" class="animate__animated animate__fadeInUp animate__delay-1s">Taller Mecanico</h1>
      <div class="header-spacer"></div>
    </div>

    <main class="container">
      <section id="Registro-Reparacion-Section" class="card-section">
        <h2>Registrar Nueva Reparacion</h2>
        <form id="Forma-Registrar-Nueva-Reparacion" @submit.prevent="registrarReparacion">
          <div class="form-group-mecanica">
            <label for="Cliente">Cliente</label>
            <select id="Cliente" v-model.number="form.idCliente" @change="cargarVehiculosCliente" required>
              <option :value="null" disabled>Seleccione un cliente</option>
              <option v-for="cliente in clientes" :key="cliente.id" :value="cliente.id">{{ cliente.nombreCompleto }}</option>
            </select>
          </div>

          <div class="form-group-mecanica vehiculo-group">
            <label for="Vehiculo">Vehiculo</label>
            <select id="Vehiculo" v-model.number="form.idVehiculo" :disabled="vehiculosCliente.length === 0" required>
              <option :value="null" disabled>Seleccione un vehiculo</option>
              <option v-for="vehiculo in vehiculosCliente" :key="vehiculo.id" :value="vehiculo.id">
                {{ vehiculo.marca }} {{ vehiculo.modelo }} {{ vehiculo.anioModelo }}
              </option>
            </select>
          </div>

          <div class="form-group-mecanica">
            <label for="Mecanico">Mecanico</label>
            <select id="Mecanico" v-model.number="form.idMecanico">
              <option :value="null">Asignar automaticamente</option>
              <option v-for="mecanico in mecanicos" :key="mecanico.id" :value="mecanico.id">
                {{ mecanico.nombre }} - {{ mecanico.especializacion }}
              </option>
            </select>
          </div>

          <div class="form-group-mecanica">
            <label for="Fecha-Entrada">Fecha de entrada</label>
            <input type="date" id="Fecha-Entrada" v-model="form.fechaIngreso" required>
          </div>

          <div class="form-group-mecanica">
            <label for="Fecha-Salida">Fecha de salida estimada</label>
            <input type="date" id="Fecha-Salida" v-model="form.fechaSalida">
          </div>

          <div class="form-group-mecanica">
            <label for="Costo-Estimado">Costo estimado</label>
            <input type="number" id="Costo-Estimado" v-model.number="form.costoEstimado" min="0">
          </div>

          <div class="form-group-mecanica">
            <label for="Problema">Problema reportado</label>
            <input type="text" id="Problema" v-model.trim="form.descripcionProblema" required>
          </div>

          <div class="form-group-mecanica">
            <label for="Diagnostico-Mecanico">Diagnostico inicial</label>
            <input type="text" id="Diagnostico-Mecanico" v-model.trim="form.diagnosticoInicial">
          </div>

          <div class="button-group-full">
            <button type="button" class="btn btn-cancel" @click="limpiarFormulario">Cancelar</button>
            <button type="submit" class="btn btn-primary">Registrar Reparacion</button>
          </div>
        </form>
        <p v-if="mensaje" :class="['estado-mensaje', mensajeTipo]">{{ mensaje }}</p>
      </section>

      <section id="Vehiculos-Reparacion" class="card-section">
        <h2>Vehiculos en Reparacion</h2>
        <div class="table-responsive">
          <table id="Tabla-Vehiculos-Reparacion">
            <thead>
              <tr>
                <th>ID</th>
                <th>Vehiculo</th>
                <th>Cliente</th>
                <th>Fecha de Entrada</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="reparacionesActivas.length === 0">
                <td colspan="6">No hay reparaciones activas</td>
              </tr>
              <tr v-for="reparacion in reparacionesActivas" :key="reparacion.id">
                <td>{{ reparacion.id }}</td>
                <td>{{ reparacion.marca }} {{ reparacion.modelo }}</td>
                <td>{{ reparacion.cliente }}</td>
                <td>{{ formatDate(reparacion.fechaIngreso) }}</td>
                <td>{{ reparacion.estado }}</td>
                <td>
                  <button class="table-action-btn btn-completar" @click="completarReparacion(reparacion.id)">Completar</button>
                  <button class="delete-btn" @click="cancelarReparacion(reparacion.id)">Cancelar</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section id="Historial-Reparaciones" class="card-section">
        <h2>Reparaciones completadas</h2>
        <div class="table-responsive">
          <table id="Tabla-Historial-Reparaciones">
            <thead>
              <tr>
                <th>ID</th>
                <th>Vehiculo</th>
                <th>Cliente</th>
                <th>Fecha de entrada</th>
                <th>Problema</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="reparacionesCompletadas.length === 0">
                <td colspan="5">No hay reparaciones completadas</td>
              </tr>
              <tr v-for="reparacion in reparacionesCompletadas" :key="reparacion.id">
                <td>{{ reparacion.id }}</td>
                <td>{{ reparacion.marca }} {{ reparacion.modelo }}</td>
                <td>{{ reparacion.cliente }}</td>
                <td>{{ formatDate(reparacion.fechaIngreso) }}</td>
                <td>{{ reparacion.descripcionProblema }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { mecanicoService } from '@/services/mecanicoService'
import { clientService } from '@/services/clientService'

interface Cliente {
  id: number
  nombreCompleto: string
}

interface Mecanico {
  id: number
  nombre: string
  especializacion: string
}

interface VehiculoCliente {
  id: number
  marca: string
  modelo: string
  anioModelo: number
}

interface Reparacion {
  id: number
  marca: string
  modelo: string
  cliente: string
  estado: string
  fechaIngreso: string
  descripcionProblema: string
}

const router = useRouter()
const clientes = ref<Cliente[]>([])
const mecanicos = ref<Mecanico[]>([])
const vehiculosCliente = ref<VehiculoCliente[]>([])
const reparacionesActivas = ref<Reparacion[]>([])
const reparacionesCompletadas = ref<Reparacion[]>([])
const mensaje = ref('')
const mensajeTipo = ref<'success' | 'error' | 'info'>('info')

const form = reactive({
  idCliente: null as number | null,
  idVehiculo: null as number | null,
  idMecanico: null as number | null,
  fechaIngreso: new Date().toISOString().slice(0, 10),
  fechaSalida: '',
  descripcionProblema: '',
  diagnosticoInicial: '',
  costoEstimado: null as number | null,
})

const mostrarMensaje = (texto: string, tipo: 'success' | 'error' | 'info' = 'info') => {
  mensaje.value = texto
  mensajeTipo.value = tipo
  setTimeout(() => {
    mensaje.value = ''
  }, 5000)
}

const formatDate = (value: string) => value ? new Date(value).toLocaleDateString('es-MX') : ''

const cargarClientes = async () => {
  try {
    clientes.value = await clientService.getAll()
  } catch (error) {
    console.error('Error al cargar clientes:', error)
    mostrarMensaje('Error al cargar clientes.', 'error')
  }
}

const cargarMecanicos = async () => {
  try {
    mecanicos.value = await mecanicoService.getMechanics()
  } catch (error) {
    console.error('Error al cargar mecanicos:', error)
    mostrarMensaje('Error al cargar mecánicos.', 'error')
  }
}

const cargarVehiculosCliente = async () => {
  form.idVehiculo = null
  vehiculosCliente.value = []
  if (!form.idCliente) return

  try {
    vehiculosCliente.value = await mecanicoService.getClientVehicles(form.idCliente)
    if (vehiculosCliente.value.length === 0) {
      mostrarMensaje('El cliente no tiene vehiculos vendidos completados para taller.', 'info')
    }
  } catch (error) {
    console.error('Error al cargar vehiculos del cliente:', error)
    mostrarMensaje('Error al cargar vehículos del cliente.', 'error')
  }
}

const cargarReparaciones = async () => {
  try {
    const [activas, completadas] = await Promise.all([
      mecanicoService.getRepairs('activas'),
      mecanicoService.getRepairs('completadas'),
    ])
    reparacionesActivas.value = activas
    reparacionesCompletadas.value = completadas
  } catch (error) {
    console.error('Error al cargar reparaciones:', error)
    mostrarMensaje('Error al cargar reparaciones.', 'error')
  }
}

const limpiarFormulario = () => {
  Object.assign(form, {
    idCliente: null,
    idVehiculo: null,
    idMecanico: null,
    fechaIngreso: new Date().toISOString().slice(0, 10),
    fechaSalida: '',
    descripcionProblema: '',
    diagnosticoInicial: '',
    costoEstimado: null,
  })
  vehiculosCliente.value = []
}

const registrarReparacion = async () => {
  if (form.fechaSalida && new Date(form.fechaSalida) < new Date(form.fechaIngreso)) {
    mostrarMensaje('La fecha de salida no puede ser anterior a la fecha de entrada.', 'error')
    return
  }

  if (!form.idCliente || !form.idVehiculo || !form.descripcionProblema) {
    mostrarMensaje('Por favor completa todos los campos requeridos.', 'error')
    return
  }

  const idCliente = form.idCliente
  const idVehiculo = form.idVehiculo

  try {
    await mecanicoService.createRepair({
      idCliente,
      idVehiculo,
      idMecanico: form.idMecanico,
      fechaIngreso: form.fechaIngreso,
      fechaSalida: form.fechaSalida || null,
      descripcionProblema: form.descripcionProblema,
      diagnosticoInicial: form.diagnosticoInicial,
      costoEstimado: form.costoEstimado,
    })
    mostrarMensaje('Reparacion registrada correctamente.', 'success')
    limpiarFormulario()
    await cargarReparaciones()
  } catch (error) {
    console.error('Error al registrar reparacion:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al registrar reparacion.', 'error')
  }
}

const completarReparacion = async (id: number) => {
  try {
    await mecanicoService.completeRepair(id)
    mostrarMensaje('Reparación completada.', 'success')
    await cargarReparaciones()
  } catch (error) {
    console.error('Error al completar reparacion:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al completar reparacion.', 'error')
  }
}

const cancelarReparacion = async (id: number) => {
  if (!confirm('¿Deseas cancelar esta reparación?')) return

  try {
    await mecanicoService.cancelRepair(id)
    mostrarMensaje('Reparación cancelada.', 'success')
    await cargarReparaciones()
  } catch (error) {
    console.error('Error al cancelar reparacion:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al cancelar reparacion.', 'error')
  }
}

onMounted(async () => {
  await Promise.all([cargarClientes(), cargarMecanicos(), cargarReparaciones()])
})
</script>

<style scoped>
@import '@/assets/Css/Global.css';
@import '@/assets/Css/Mecanica.css';

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
