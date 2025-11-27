<script setup lang="ts">
const items = ref([
  {
    label: 'Accueil',
    icon: 'i-lucide-home',
    to: '/'
  },
  {
    label: 'Carte',
    icon: 'i-lucide-map',
    to: '/map'
  },
  {
    label: 'Clients',
    icon: 'i-lucide-users',
    to: '/clients'
  },
  {
    label: 'Commandes',
    icon: 'i-lucide-box',
    to: '/orders'
  },
  {
    label: 'Hydravions',
    icon: 'i-lucide-plane',
    to: '/planes'
  }
])
</script>

<template>
  <UNavigationMenu :items="items" />
</template>
