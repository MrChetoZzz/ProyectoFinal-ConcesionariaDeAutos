<template>
  <div class="gestion-vehiculos-page">
    <div id="Encabezado" class="animate__animated animate__fadeInDown">
      <img src="@/assets/Imagenes/Iconos/arrow-return-left.svg" id="return" alt="Regresar" @click="router.push('/')">
      <h1 id="Titulo-Pag" class="animate__animated animate__fadeInUp animate__delay-1s">Gestion de Vehiculos</h1>
      <div class="header-spacer"></div>
    </div>

    <main class="container">
      <section id="Buscar-vehiculos" class="card-section">
        <h2>Buscar Vehiculos</h2>
        <form id="Form-Buscar-Vehiculos" class="form-grid" @submit.prevent="buscarVehiculos">
          <div class="form-group">
            <label for="MarcaBV">Marca</label>
            <select id="MarcaBV" v-model="filtros.marca" @change="cargarModelos(filtros.marca)">
              <option value="">Todas</option>
              <option v-for="marca in marcas" :key="marca" :value="marca">{{ marca }}</option>
            </select>
          </div>
          <div class="form-group">
            <label for="ModeloBV">Modelo</label>
            <select id="ModeloBV" v-model="filtros.modelo">
              <option value="">Todos</option>
              <option v-for="modelo in modelosBusqueda" :key="modelo" :value="modelo">{{ modelo }}</option>
            </select>
          </div>
          <div class="form-group">
            <label for="AnioBV">Anio</label>
            <select id="AnioBV" v-model.number="filtros.anio">
              <option :value="null">Todos</option>
              <option v-for="anio in anios" :key="anio" :value="anio">{{ anio }}</option>
            </select>
          </div>
          <div class="form-group">
            <button type="submit" class="btn btn-primary">Buscar</button>
          </div>
        </form>
      </section>

      <section id="Vehiculos-Disponibles" class="card-section">
        <h2>Vehiculos Disponibles</h2>
        <div class="filtro-group">
          <label for="Filtrar">Filtrar por:</label>
          <select id="Filtrar" v-model="sort" @change="buscarVehiculos">
            <option value="Marca-Ascendente">Marca ▲</option>
            <option value="Marca-Descendente">Marca ▼</option>
            <option value="Modelo-Ascendente">Modelo ▲</option>
            <option value="Modelo-Descendente">Modelo ▼</option>
            <option value="Precio-Ascendente">Precio ▲</option>
            <option value="Precio-Descendente">Precio ▼</option>
            <option value="Anio-Ascendente">Anio ▲</option>
            <option value="Anio-Descendente">Anio ▼</option>
          </select>
          <button type="button" class="btn btn-accent" @click="abrirModalAgregarVehiculo">Agregar Vehiculo</button>
        </div>
        <p v-if="mensaje" :class="['estado-mensaje', mensajeTipo]">{{ mensaje }}</p>
        <div class="table-responsive">
          <table id="Tabla-Vehiculos-Disponibles">
            <thead>
              <tr>
                <th>ID</th>
                <th>Marca</th>
                <th>Modelo</th>
                <th>Anio</th>
                <th>Precio</th>
                <th>Condicion</th>
                <th>Disponibilidad</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="cargando">
                <td colspan="8">Cargando vehiculos...</td>
              </tr>
              <tr v-else-if="vehiculos.length === 0">
                <td colspan="8">No se encontraron vehiculos</td>
              </tr>
              <template v-else>
                <tr v-for="vehiculo in vehiculos" :key="vehiculo.id">
                  <td>{{ vehiculo.id }}</td>
                  <td>{{ vehiculo.marca }}</td>
                  <td>{{ vehiculo.modelo }}</td>
                  <td>{{ vehiculo.anioModelo }}</td>
                  <td>{{ formatCurrency(vehiculo.costo) }}</td>
                  <td>{{ vehiculo.condicion }}</td>
                  <td>{{ vehiculo.disponible ? 'Disponible' : 'Vendido' }}</td>
                  <td>
                    <button class="table-action-btn btn-edit" @click="editarVehiculo(vehiculo)">Editar</button>
                    <button class="table-action-btn delete-btn" @click="eliminarVehiculo(vehiculo.id)">Eliminar</button>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="mostrarModalAgregar" id="Agregar-Nuevo-Vehiculo" class="card-section modal-overlay active animate__animated">
        <div class="modal-content">
          <div class="modal-header">
            <h2>{{ vehiculoEnEdicion ? 'Editar Vehiculo' : 'Agregar Nuevo Vehiculo' }}</h2>
            <button type="button" class="modal-close" @click="cerrarModalAgregarVehiculo">&times;</button>
          </div>
          <form id="Form-Agregar-Nuevo-Vehiculo" class="form-grid" @submit.prevent="guardarVehiculo">
            <div class="form-group">
              <label for="MarcaANV">Marca</label>
              <select id="MarcaANV" v-model="nuevoVehiculo.marca" required>
                <option value="" disabled>Seleccione una marca</option>
                <option v-for="marca in marcasBase" :key="marca" :value="marca">{{ marca }}</option>
              </select>
            </div>
            <div class="form-group">
              <label for="ModeloANV">Modelo</label>
              <input type="text" id="ModeloANV" v-model.trim="nuevoVehiculo.modelo" required>
            </div>
            <div class="form-group">
              <label for="AnioANV">Anio</label>
              <select id="AnioANV" v-model.number="nuevoVehiculo.anioModelo" required>
                <option v-for="anio in anios" :key="anio" :value="anio">{{ anio }}</option>
              </select>
            </div>
            <div class="form-group">
              <label for="PrecioANV">Precio</label>
              <input type="number" id="PrecioANV" v-model.number="nuevoVehiculo.costo" min="0" required>
            </div>
            <div class="form-group">
              <label for="EstadoANV">Condicion</label>
              <select id="EstadoANV" v-model="nuevoVehiculo.condicion">
                <option value="Nuevo">Nuevo</option>
                <option value="Usado">Usado</option>
              </select>
            </div>
            <div class="form-group">
              <label for="PlacasANV">Placas</label>
              <input type="text" id="PlacasANV" v-model.trim="nuevoVehiculo.placas">
            </div>
            <div class="form-group">
              <label for="SerieANV">Numero de serie</label>
              <input type="text" id="SerieANV" v-model.trim="nuevoVehiculo.numeroSerie" maxlength="17">
            </div>
            <div class="form-group form-group-full form-group-btn">
              <button type="button" class="btn btn-cancel" @click="cerrarModalAgregarVehiculo">Cancelar</button>
              <button type="submit" class="btn btn-accent">{{ vehiculoEnEdicion ? 'Guardar Cambios' : 'Agregar Vehiculo' }}</button>
            </div>
          </form>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { vehiculoService, type Vehiculo, type VehiculoFormData } from '@/services/vehiculoService'

interface Vehiculo {
  id: number
  marca: string
  modelo: string
  anioModelo: number
  placas: string | null
  numeroSerie: string | null
  costo: number | null
  idVehiculoCondicion: number
  condicion: string
  fechaRegistro: string
  disponible: boolean
}

const router = useRouter()
const vehiculos = ref<Vehiculo[]>([])
const marcas = ref<string[]>([])
const modelosBusqueda = ref<string[]>([])
const cargando = ref(false)
const mostrarModalAgregar = ref(false)
const sort = ref('Marca-Ascendente')
const mensaje = ref('')
const mensajeTipo = ref<'success' | 'error' | 'info'>('info')
const vehiculoEnEdicion = ref<Vehiculo | null>(null)

const marcasBase = computed(() => {
  const base = ['Toyota', 'Ford', 'Nissan', 'Dodge', 'Honda']
  return Array.from(new Set([...base, ...marcas.value])).sort()
})

const anios = computed(() => {
  const actual = new Date().getFullYear() + 1
  return Array.from({ length: actual - 1999 }, (_, index) => actual - index)
})

const filtros = reactive({
  marca: '',
  modelo: '',
  anio: null as number | null,
})

const nuevoVehiculo = reactive({
  marca: '',
  modelo: '',
  anioModelo: new Date().getFullYear(),
  costo: 0,
  condicion: 'Nuevo',
  placas: '',
  numeroSerie: '',
})

const formatCurrency = (amount: number | null) =>
  (amount ?? 0).toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })

const mostrarMensaje = (texto: string, tipo: 'success' | 'error' | 'info' = 'info') => {
  mensaje.value = texto
  mensajeTipo.value = tipo
  setTimeout(() => {
    mensaje.value = ''
  }, 5000)
}

const cargarMarcas = async () => {
  try {
    marcas.value = await vehiculoService.getBrands()
  } catch (error) {
    console.error('Error al cargar marcas:', error)
  }
}

const cargarModelos = async (marca: string) => {
  filtros.modelo = ''
  if (!marca) {
    modelosBusqueda.value = []
    return
  }
  try {
    modelosBusqueda.value = await vehiculoService.getModelsByBrand(marca)
  } catch (error) {
    console.error('Error al cargar modelos:', error)
  }
}

const buscarVehiculos = async () => {
  cargando.value = true
  try {
    vehiculos.value = await vehiculoService.getAll({
      sort: sort.value,
      marca: filtros.marca,
      modelo: filtros.modelo,
      anio: filtros.anio ?? undefined,
    })
  } catch (error) {
    console.error('Error al obtener vehiculos:', error)
    mostrarMensaje('No se pudieron cargar los vehiculos.', 'error')
  } finally {
    cargando.value = false
  }
}

const abrirModalAgregarVehiculo = () => {
  vehiculoEnEdicion.value = null
  Object.assign(nuevoVehiculo, {
    marca: '',
    modelo: '',
    anioModelo: new Date().getFullYear(),
    costo: 0,
    condicion: 'Nuevo',
    placas: '',
    numeroSerie: '',
  })
  mostrarModalAgregar.value = true
}

const editarVehiculo = (vehiculo: Vehiculo) => {
  vehiculoEnEdicion.value = vehiculo
  Object.assign(nuevoVehiculo, {
    marca: vehiculo.marca,
    modelo: vehiculo.modelo,
    anioModelo: vehiculo.anioModelo,
    costo: vehiculo.costo || 0,
    condicion: vehiculo.condicion,
    placas: vehiculo.placas || '',
    numeroSerie: vehiculo.numeroSerie || '',
  })
  mostrarModalAgregar.value = true
}

const cerrarModalAgregarVehiculo = () => {
  mostrarModalAgregar.value = false
  vehiculoEnEdicion.value = null
}

const guardarVehiculo = async () => {
  try {
    const datos: VehiculoFormData = {
      marca: nuevoVehiculo.marca,
      modelo: nuevoVehiculo.modelo,
      anioModelo: nuevoVehiculo.anioModelo,
      costo: nuevoVehiculo.costo,
      condicion: nuevoVehiculo.condicion,
      placas: nuevoVehiculo.placas || undefined,
      numeroSerie: nuevoVehiculo.numeroSerie || undefined,
    }

    if (vehiculoEnEdicion.value) {
      await vehiculoService.update(vehiculoEnEdicion.value.id, datos)
      mostrarMensaje('Vehiculo actualizado correctamente.', 'success')
    } else {
      await vehiculoService.create(datos)
      mostrarMensaje('Vehiculo registrado correctamente.', 'success')
    }

    cerrarModalAgregarVehiculo()
    await cargarMarcas()
    await buscarVehiculos()
  } catch (error) {
    console.error('Error al registrar vehiculo:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al registrar vehiculo.', 'error')
  }
}

const eliminarVehiculo = async (id: number) => {
  if (!confirm('¿Deseas eliminar este vehiculo?')) return

  try {
    await vehiculoService.delete(id)
    mostrarMensaje('Vehiculo eliminado correctamente.', 'success')
    await buscarVehiculos()
  } catch (error) {
    console.error('Error al eliminar vehiculo:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al eliminar vehiculo.', 'error')
  }
}

onMounted(async () => {
  await cargarMarcas()
  await buscarVehiculos()
})
</script>

<style scoped>
@import '@/assets/Css/Global.css';
@import '@/assets/Css/GestionDeVehiculos.css';

.estado-mensaje {
  margin: 10px 0;
  font-weight: 600;
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
 