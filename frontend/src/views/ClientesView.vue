<template>
  <div class="clientes-page">
    <div id="Encabezado" class="animate__animated animate__fadeInDown">
      <img src="@/assets/Imagenes/Iconos/arrow-return-left.svg" id="return" alt="Regresar" @click="router.push('/')">
      <h1 id="Titulo-Pag" class="animate__animated animate__fadeInUp animate__delay-1s">Gestion de Clientes</h1>
      <div class="header-spacer"></div>
    </div>

    <main class="container">
      <section id="Gestion-Clientes" class="card-section">
        <h2 id="form-title">Registrar Nuevo Cliente</h2>
        <form id="Forma-Clientes" class="form-grid" @submit.prevent="agregarCliente">
          <div class="form-group">
            <label for="Nombre">Nombre</label>
            <input type="text" id="Nombre" v-model.trim="form.nombre" required>
          </div>
          <div class="form-group">
            <label for="Apellido-Paterno">Apellido Paterno</label>
            <input type="text" id="Apellido-Paterno" v-model.trim="form.apellidoPaterno">
          </div>
          <div class="form-group">
            <label for="Apellido-Materno">Apellido Materno</label>
            <input type="text" id="Apellido-Materno" v-model.trim="form.apellidoMaterno">
          </div>
          <div class="form-group">
            <label for="Telefono">Telefono</label>
            <input type="text" id="Telefono" v-model.trim="form.telefono" placeholder="Ej. 8671234567" maxlength="10">
          </div>
          <div class="form-group">
            <label for="Curp">CURP</label>
            <input type="text" id="Curp" v-model.trim="form.curp">
          </div>
          <div class="form-group form-group-full">
            <hr class="form-divider">
          </div>
          <div class="form-group">
            <label for="Colonia">Colonia</label>
            <input type="text" id="Colonia" v-model.trim="form.colonia">
          </div>
          <div class="form-group">
            <label for="Calle">Calle</label>
            <input type="text" id="Calle" v-model.trim="form.calle">
          </div>
          <div class="form-group">
            <label for="Num-Ext">Numero Exterior</label>
            <input type="number" id="Num-Ext" v-model.trim="form.numExt">
          </div>
          <div class="form-group form-group-full" id="btn-container">
            <button id="btnRegistrar" type="submit" class="btn btn-accent">Agregar Cliente</button>
          </div>
        </form>
        <p v-if="mensaje" :class="['estado-mensaje', mensajeTipo]">{{ mensaje }}</p>
      </section>

      <section id="Clientes" class="card-section">
        <h2>Clientes registrados</h2>
        <div class="table-responsive">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Nombre completo</th>
                <th>Domicilio</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody id="Tabla-Clientes">
              <tr v-if="cargando">
                <td colspan="4">Cargando clientes...</td>
              </tr>
              <tr v-else-if="clientes.length === 0">
                <td colspan="4">No hay clientes registrados</td>
              </tr>
              <template v-else>
                <tr v-for="cliente in clientes" :key="cliente.id">
                  <td>{{ cliente.id }}</td>
                  <td>{{ cliente.nombreCompleto }}</td>
                  <td>{{ cliente.domicilio || 'Sin domicilio registrado' }}</td>
                  <td>
                    <button class="table-action-btn btn-edit" @click="editarCliente(cliente)">Editar</button>
                    <button class="table-action-btn delete-btn" @click="eliminarCliente(cliente.id)">Eliminar</button>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </section>
    </main>

    <div v-if="mostrarModalEdicion" class="modal-overlay active">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ clienteEnEdicion ? 'Editar Cliente' : 'Nuevo Cliente' }}</h2>
          <button type="button" class="modal-close" @click="cerrarModalEdicion">&times;</button>
        </div>
        <form class="form-grid" @submit.prevent="guardarCliente">
          <div class="form-group">
            <label for="Nombre-Modal">Nombre</label>
            <input type="text" id="Nombre-Modal" v-model.trim="formEdicion.nombre" required>
          </div>
          <div class="form-group">
            <label for="Apellido-Paterno-Modal">Apellido Paterno</label>
            <input type="text" id="Apellido-Paterno-Modal" v-model.trim="formEdicion.apellidoPaterno">
          </div>
          <div class="form-group">
            <label for="Apellido-Materno-Modal">Apellido Materno</label>
            <input type="text" id="Apellido-Materno-Modal" v-model.trim="formEdicion.apellidoMaterno">
          </div>
          <div class="form-group">
            <label for="Telefono-Modal">Telefono</label>
            <input type="text" id="Telefono-Modal" v-model.trim="formEdicion.telefono" placeholder="Ej. 8671234567" maxlength="10">
          </div>
          <div class="form-group">
            <label for="Curp-Modal">CURP</label>
            <input type="text" id="Curp-Modal" v-model.trim="formEdicion.curp">
          </div>
          <div class="form-group form-group-full">
            <hr class="form-divider">
          </div>
          <div class="form-group">
            <label for="Colonia-Modal">Colonia</label>
            <input type="text" id="Colonia-Modal" v-model.trim="formEdicion.colonia">
          </div>
          <div class="form-group">
            <label for="Calle-Modal">Calle</label>
            <input type="text" id="Calle-Modal" v-model.trim="formEdicion.calle">
          </div>
          <div class="form-group">
            <label for="Num-Ext-Modal">Numero Exterior</label>
            <input type="number" id="Num-Ext-Modal" v-model.trim="formEdicion.numExt">
          </div>
          <div class="form-group form-group-full form-group-btn">
            <button type="button" class="btn btn-cancel" @click="cerrarModalEdicion">Cancelar</button>
            <button type="submit" class="btn btn-accent">{{ clienteEnEdicion ? 'Guardar Cambios' : 'Agregar Cliente' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { clientService, type Cliente, type ClienteFormData } from '@/services/clientService'

const router = useRouter()
const clientes = ref<Cliente[]>([])
const cargando = ref(false)
const mensaje = ref('')
const mensajeTipo = ref<'success' | 'error' | 'info'>('info')
const mostrarModalEdicion = ref(false)
const clienteEnEdicion = ref<Cliente | null>(null)

const form = reactive({
  nombre: '',
  apellidoPaterno: '',
  apellidoMaterno: '',
  telefono: '',
  curp: '',
  colonia: '',
  calle: '',
  numExt: '',
})

const formEdicion = reactive({
  nombre: '',
  apellidoPaterno: '',
  apellidoMaterno: '',
  telefono: '',
  curp: '',
  colonia: '',
  calle: '',
  numExt: '',
})

const mostrarMensaje = (texto: string, tipo: 'success' | 'error' | 'info' = 'info') => {
  mensaje.value = texto
  mensajeTipo.value = tipo
  setTimeout(() => {
    mensaje.value = ''
  }, 5000)
}

const limpiarFormulario = () => {
  Object.assign(form, {
    nombre: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    telefono: '',
    curp: '',
    colonia: '',
    calle: '',
    numExt: '',
  })
}

const fetchClientes = async () => {
  cargando.value = true
  try {
    clientes.value = await clientService.getAll()
  } catch (error) {
    console.error('Error al obtener clientes:', error)
    mostrarMensaje('No se pudieron cargar los clientes. Revisa permisos RBAC de Cliente y Persona.', 'error')
  } finally {
    cargando.value = false
  }
}

const agregarCliente = async () => {
  if (form.telefono && form.telefono.length !== 10) {
    mostrarMensaje('El telefono debe tener 10 digitos.', 'error')
    return
  }

  try {
    await clientService.create(form as ClienteFormData)
    mostrarMensaje('Cliente registrado correctamente.', 'success')
    limpiarFormulario()
    await fetchClientes()
  } catch (error) {
    console.error('Error al registrar cliente:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al registrar cliente.', 'error')
  }
}

const editarCliente = (cliente: Cliente) => {
  clienteEnEdicion.value = cliente
  Object.assign(formEdicion, {
    nombre: cliente.nombreCompleto.split(' ')[0] || '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    telefono: '',
    curp: '',
    colonia: '',
    calle: '',
    numExt: '',
  })
  mostrarModalEdicion.value = true
}

const cerrarModalEdicion = () => {
  mostrarModalEdicion.value = false
  clienteEnEdicion.value = null
  Object.assign(formEdicion, {
    nombre: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    telefono: '',
    curp: '',
    colonia: '',
    calle: '',
    numExt: '',
  })
}

const guardarCliente = async () => {
  if (formEdicion.telefono && formEdicion.telefono.length !== 10) {
    mostrarMensaje('El telefono debe tener 10 digitos.', 'error')
    return
  }

  try {
    if (clienteEnEdicion.value) {
      await clientService.update(clienteEnEdicion.value.id, formEdicion as ClienteFormData)
      mostrarMensaje('Cliente actualizado correctamente.', 'success')
    } else {
      await clientService.create(formEdicion as ClienteFormData)
      mostrarMensaje('Cliente registrado correctamente.', 'success')
    }
    cerrarModalEdicion()
    await fetchClientes()
  } catch (error) {
    console.error('Error al guardar cliente:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al guardar cliente.', 'error')
  }
}

const eliminarCliente = async (id: number) => {
  if (!confirm('¿Deseas eliminar este cliente?')) return

  try {
    await clientService.delete(id)
    mostrarMensaje('Cliente eliminado correctamente.', 'success')
    await fetchClientes()
  } catch (error) {
    console.error('Error al eliminar cliente:', error)
    mostrarMensaje(error instanceof Error ? error.message : 'Error al eliminar cliente.', 'error')
  }
}

onMounted(fetchClientes)
</script>

<style scoped>
@import '@/assets/Css/Global.css';
@import '@/assets/Css/Clientes.css';

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

 