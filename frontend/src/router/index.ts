// import { createRouter, createWebHistory } from 'vue-router'

// const router = createRouter({
//   history: createWebHistory(import.meta.env.BASE_URL),
//   routes: [],
// })

// export default router

import { createRouter, createWebHistory } from 'vue-router'
import InicioView from '@/views/InicioView.vue'    // <-- importas la vista
import GestionVehiculosView from '@/views/GestionVehiculosView.vue'
import GestionVentasView from '@/views/GestionVentasView.vue'
import ClientesView from '@/views/ClientesView.vue'
import MecanicaView from '@/views/MecanicaView.vue'
import MarcasView from '@/views/MarcasView.vue'
import ReportesView from '@/views/ReportesView.vue'
import BitacoraReportesView from '@/views/BitacoraReportesView.vue'
// ... otros imports

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'inicio',
      component: InicioView
    },
    {
      path: '/gestion-vehiculos',
      name: 'gestion-vehiculos',
      component: GestionVehiculosView
    },
    {
      path: '/ventas',
      name: 'ventas',
      component: GestionVentasView
    },
    {
      path: '/clientes',
      name: 'clientes',
      component: ClientesView
    },
    {
      path: '/mecanica',
      name: 'mecanica',
      component: MecanicaView
    },
    {
      path: '/marcas',
      name: 'marcas',
      component: MarcasView
    },
    {
      path: '/reportes',
      name: 'reportes',
      component: ReportesView
    },
    {
      path: '/bitacora-reportes',
      name: 'bitacora-reportes',
      component: BitacoraReportesView
    }
    // ... más rutas
  ]
})

export default router
