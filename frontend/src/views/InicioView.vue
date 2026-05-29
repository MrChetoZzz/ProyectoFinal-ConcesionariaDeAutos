<template>
  <div class="inicio-page">
    <div id="Encabezado" class="inicio-header animate__animated animate__fadeInDown">
      <router-link to="/" class="brand-mark">ALAFE</router-link>

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

      <button type="button" class="login-icon animate__animated animate__fadeInUp animate__delay-1s" @click="toggleLoginModal">
        <span v-if="user.isLoggedIn" @click.stop="cerrarSesion">Cerrar Sesion</span>
        <span v-else>Iniciar Sesion</span>
        <img src="@/assets/Imagenes/Iconos/user_icon.svg" alt="Usuario">
      </button>
    </div>

    <section id="Mas-Auto" class="inicio-hero animate__animated animate__fadeInDown">
      <div id="Mas-Auto-Informacion" class="hero-copy">
        <h1>ALAFE</h1>
        <div class="hero-actions">
          <router-link :to="rutaPrincipal" id="Ver-mas" class="btn btn-accent">{{ textoAccionPrincipal }}</router-link>
          <router-link to="/marcas" class="btn btn-ghost">Ver catalogo</router-link>
        </div>
      </div>
    </section>

    <section class="brand-strip" aria-label="Marcas disponibles">
      <img src="@/assets/Imagenes/Inicio/NissanLogo.png" alt="Nissan">
      <img src="@/assets/Imagenes/Inicio/ToyotaLogo.png" alt="Toyota">
      <img src="@/assets/Imagenes/Inicio/FordLogo.png" alt="Ford">
      <img src="@/assets/Imagenes/Inicio/Honda.png" alt="Honda">
      <img src="@/assets/Imagenes/Inicio/DodgeLogo.png" alt="Dodge">
    </section>

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
import { computed, onMounted, reactive, ref } from 'vue'

type Rol = 'admin' | 'vendedor' | 'mecanico' | ''

const user = reactive({
  isLoggedIn: false,
  rol: '' as Rol,
})

const showLoginModal = ref(false)

const rutaPrincipal = computed(() => {
  if (!user.isLoggedIn) return '/marcas'
  if (user.rol === 'mecanico') return '/mecanica'
  if (user.rol === 'vendedor') return '/ventas'
  return '/gestion-vehiculos'
})

const textoAccionPrincipal = computed(() => user.isLoggedIn ? 'Ir al panel' : 'Explorar autos')

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
