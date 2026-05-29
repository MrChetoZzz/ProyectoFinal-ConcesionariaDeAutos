<template>
  <div class="marcas-page animate__animated animate__fadeInUp">
    <div id="Encabezado" class="animate__animated animate__fadeInDown">
      <img src="@/assets/Imagenes/Iconos/arrow-return-left.svg" id="return" alt="Regresar" @click="router.push('/')">
      <h1 id="Titulo-Pag" class="animate__animated animate__fadeInUp animate__delay-1s">Catalogo de Vehiculos por Marca</h1>
      <div class="header-spacer"></div>
    </div>

    <div class="container page-grid">
      <aside class="lateral-container animate__animated animate__fadeInLeft">
        <h3>Menu de Marcas</h3>
        <a v-for="marca in marcasOrdenadas" :key="marca" :href="`#${marca}Titulo`" class="aside-link">{{ marca }}</a>
      </aside>

      <main class="content-area animate__animated animate__fadeInUp">
        <template v-for="marca in marcasOrdenadas" :key="marca">
          <h1 :id="`${marca}Titulo`" class="marca-titulo">Automoviles de {{ marca }}</h1>
          <section class="marca-section cards-container">
            <article v-if="vehiculosPorMarca(marca).length === 0" class="card empty-card">
              <div class="card-body">
                <h5 class="card-title">{{ marca }}</h5>
                <p class="card-text">Sin vehiculos registrados.</p>
              </div>
            </article>
            <article v-for="vehiculo in vehiculosPorMarca(marca)" :key="vehiculo.id" class="card">
              <img 
                :src="obtenerRutaImagen(vehiculo)" 
                class="card-img-top vehiculo-img" 
                :alt="`Foto de ${vehiculo.marca} ${vehiculo.modelo}`"
                @error="usarLogoFallback($event, vehiculo.marca)"
              >
              <div class="card-body">
                <h5 class="card-title">{{ vehiculo.marca }} {{ vehiculo.modelo }}</h5>
                <p class="card-text">Precio: {{ formatCurrency(vehiculo.costo) }}</p>
                <p class="card-text">Estado: {{ vehiculo.condicion }}</p>
                <p class="card-text">Anio: {{ vehiculo.anioModelo }}</p>
                <p class="card-text">Stock: {{ vehiculo.stock }} de {{ vehiculo.unidades }}</p>
              </div>
            </article>
          </section>
        </template>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import DodgeLogo from '@/assets/Imagenes/Inicio/DodgeLogo.png'
import FordLogo from '@/assets/Imagenes/Inicio/FordLogo.png'
import HondaLogo from '@/assets/Imagenes/Inicio/Honda.png'
import NissanLogo from '@/assets/Imagenes/Inicio/NissanLogo.png'
import ToyotaLogo from '@/assets/Imagenes/Inicio/ToyotaLogo.png'
import { vehiculoService, type VehiculoCatalogo } from '@/services/vehiculoService'

const router = useRouter()
const vehiculos = ref<VehiculoCatalogo[]>([])
const marcasBase = ['Nissan', 'Toyota', 'Dodge', 'Honda', 'Ford']
const defaultLogo = ToyotaLogo
const brandLogos: Record<string, string> = {
  Dodge: DodgeLogo,
  Ford: FordLogo,
  Honda: HondaLogo,
  Nissan: NissanLogo,
  Toyota: ToyotaLogo,
}

const marcasOrdenadas = computed(() => {
  const marcas = new Set([...marcasBase, ...vehiculos.value.map((vehiculo) => vehiculo.marca)])
  return Array.from(marcas)
})

const vehiculosPorMarca = (marca: string) => vehiculos.value.filter((vehiculo) => vehiculo.marca === marca)

const formatCurrency = (amount?: number | null) =>
  Number(amount ?? 0).toLocaleString('es-MX', { style: 'currency', currency: 'MXN' })

const obtenerRutaImagen = (vehiculo: Vehiculo) =>
  vehiculo.imagenPrincipal || brandLogos[vehiculo.marca] || defaultLogo

const usarLogoFallback = (event: Event, marca: string) => {
  const image = event.target as HTMLImageElement
  const fallback = brandLogos[marca] || defaultLogo
  if (image.src !== fallback) {
    image.src = fallback
  }
}

const cargarVehiculos = async () => {
  try {
    vehiculos.value = await vehiculoService.getCatalog({ sort: 'Marca-Ascendente' })
  } catch (error) {
    console.error('Error al cargar catalogo:', error)
    vehiculos.value = []
  }
}

onMounted(cargarVehiculos)
</script>

<style scoped>
@import '@/assets/Css/Global.css';
@import '@/assets/Css/Marcas.css';
@import '@/assets/Css/Responsive.css';

.empty-card {
  height: auto;
  min-height: 150px;
}

.vehiculo-img {
  object-fit: cover;
  height: 200px;
  width: 100%;
}
</style>
