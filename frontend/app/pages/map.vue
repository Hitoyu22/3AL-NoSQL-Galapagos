<script setup lang="ts">
import { ref } from 'vue'
import { seaplaneService } from '~/graphql/seaplanes'
import { portService } from '~/graphql/ports'
import Map from '~/components/map/Map.vue'
import MapCard from '~/components/map/MapCard.vue'

const { $graphql } = useNuxtApp()
const gqlClient = $graphql.default

const selectedFilters = ref(['Ports & Entrepôts', 'Hydravions', 'Itinéraires'])
const filterOptions = ['Ports & Entrepôts', 'Hydravions', 'Itinéraires']

const { data, pending, error } = await useAsyncData('map-data', async () => {
  const [ports, planes] = await Promise.all([
    portService.getAll(gqlClient),
    seaplaneService.getAll(gqlClient)
  ])
  return { ports, planes }
})

const ports = computed(() => data.value?.ports || [])
const planes = computed(() => data.value?.planes || [])

</script>

<template>
  <div class="flex h-[calc(100vh-10rem)]">
    <div class="w-7/10 h-full">

      <div class="w-full h-full rounded-xl overflow-hidden border border-gray-200 dark:border-gray-700 shadow-sm relative">
      
      <div v-if="pending" class="absolute inset-0 z-50 flex items-center justify-center bg-gray-50/80 dark:bg-gray-900/80">
        <UIcon name="i-lucide-loader-2" class="w-10 h-10 animate-spin text-primary" />
      </div>

      <div v-if="error" class="absolute inset-0 z-50 flex items-center justify-center bg-red-50 dark:bg-red-900/20">
        <div class="text-center text-red-500">
          <UIcon name="i-lucide-alert-triangle" class="w-10 h-10 mb-2 mx-auto" />
          <p>Impossible de charger les données de la carte.</p>
        </div>
      </div>

      <Map 
        v-if="!pending && !error"
        :ports="ports" 
        :planes="planes" 
        :active-filters="selectedFilters" 
      />
    </div>
      
    </div>
    
    <MapCard 
        v-model="selectedFilters" 
        :options="filterOptions"
    />

  </div>
</template>