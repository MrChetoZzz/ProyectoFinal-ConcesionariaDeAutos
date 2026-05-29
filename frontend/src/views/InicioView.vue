<template>
  <div class="inicio-page">
    <div id="Encabezado" class="animate__animated animate__fadeInDown">
      <ul id="lista-de-opciones" class="animate__animated animate__fadeInUp animate__delay-1s">
        <template v-if="user.isLoggedIn">
          <li v-if="user.rol === 'admin'" class="lista-de-opciones"><router-link to="/gestion-vehiculos">Gestion de Vehiculos</router-link></li>
          <li v-if="['admin', 'vendedor'].includes(user.rol)" class="lista-de-opciones"><router-link to="/ventas">Ventas</router-link></li>
          <li v-if="user.rol === 'admin'" class="lista-de-opciones"><router-link to="/reportes">Reportes</router-link></li>
          <li v-if="['admin', 'mecanico'].includes(user.rol)" class="lista-de-opciones"><router-link to="/mecanica">Mecanica</router-link></li>
          <li v-if="['admin', 'vendedor'].includes(user.rol)" class="lista-de-opciones"><router-link to="/clientes">Clientes</router-link></li>
        </template>
        <li v-else class="lista-de-opciones"><router-link to="/marcas">Catalogo</router-link></li>
      </ul>

      <div class="login-icon animate__animated animate__fadeInUp animate__delay-1s" @click="toggleLoginModal">
        <a v-if="user.isLoggedIn" @click.stop="cerrarSesion">Cerrar Sesion</a>
        <a v-else>Iniciar Sesion</a>
        <img src="@/assets/Imagenes/Iconos/user_icon.svg" alt="Usuario">
      </div>
    </div>

    <div id="Mas-Auto" class="animate__animated animate__fadeInDown">
      <div id="Mas-Auto-Informacion">
        <h2 class="typewriter">Explora el carro de tus suenos</h2>
        <router-link to="/marcas" id="Ver-mas" class="btn-accent">Ver Mas</router-link>
      </div>
    </div>

    <div v-if="showLoginModal && !user.isLoggedIn" class="modal-overlay active">
      <div class="login-page-container">
        <div class="card-section login-card">
          <div class="login-header">
            <h1>Iniciar Sesion</h1>
            <button class="modal-close" @click="toggleLoginModal">&times;</button>
          </div>
          <div class="role-actions">
            <button class="btn btn-primary" @click="iniciarSesion('admin')">Administrador</button>
            <button class="btn btn-accent" @click="iniciarSesion('vendedor')">Vendedor</button>
            <button class="btn btn-primary" @click="iniciarSesion('mecanico')">Mecanico</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'

type Rol = 'admin' | 'vendedor' | 'mecanico' | ''

const user = reactive({
  isLoggedIn: false,
  rol: '' as Rol,
})

const showLoginModal = ref(false)

const toggleLoginModal = () => {
  showLoginModal.value = !showLoginModal.value
}

const iniciarSesion = (rol: Rol) => {
  user.isLoggedIn = true
  user.rol = rol
  localStorage.setItem('concesionaria-role', rol)
  showLoginModal.value = false
}

const cerrarSesion = () => {
  user.isLoggedIn = false
  user.rol = ''
  localStorage.removeItem('concesionaria-role')
}

onMounted(() => {
  const savedRole = localStorage.getItem('concesionaria-role') as Rol | null
  if (savedRole && ['admin', 'vendedor', 'mecanico'].includes(savedRole)) {
    user.isLoggedIn = true
    user.rol = savedRole
  }
})
</script>

<style scoped>
@import '@/assets/Css/Global.css';
@import '@/assets/Css/Inicio.css';

.login-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.role-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
}
</style>
