<script setup lang="ts">
import PlaneMiniMap from './PlaneMiniMap.vue'
import type { Seaplane } from '~/graphql/types'

const props = defineProps<{
  modelValue: boolean
  plane: Seaplane | null
}>()

const emit = defineEmits(['update:modelValue'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const getStatusColor = (status: string) => {
    switch(status) {
        case 'AVAILABLE': return 'success';
        case 'MAINTENANCE': return 'warning';
        case 'IN_FLIGHT': return 'primary';
        case 'AT_PORT': return 'neutral';
        default: return 'neutral';
    }
}
</script>

<template>
  <USlideover v-model:open="isOpen" title="Détails de l'appareil">
    <template #body>
      <div v-if="plane" class="space-y-6">
        
        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <h2 class="text-2xl font-bold">{{ plane.id }}</h2>
            <UBadge :color="getStatusColor(plane.status)" variant="subtle" size="lg">
              {{ plane.status }}
            </UBadge>
          </div>
          <p class="text-gray-500 text-lg">{{ plane.model }}</p>
        </div>

        <USeparator />

        <div class="grid grid-cols-2 gap-4">
          <div class="bg-gray-50 dark:bg-gray-800 p-3 rounded-md">
            <div class="text-xs text-gray-500 mb-1">Localisation actuelle</div>
            <div class="font-medium truncate">{{ plane.location }}</div>
          </div>
          <div class="bg-gray-50 dark:bg-gray-800 p-3 rounded-md">
             <div class="text-xs text-gray-500 mb-1">Coordonnées</div>
             <div class="font-mono text-sm">{{ plane.lat.toFixed(4) }}, {{ plane.lon.toFixed(4) }}</div>
          </div>
        </div>

        <div class="space-y-2">
            <h3 class="font-medium">Position temps réel</h3>
            <PlaneMiniMap :lat="plane.lat" :lon="plane.lon" />
        </div>

      </div>
      <div v-else class="h-full flex items-center justify-center text-gray-500">
        Aucun avion sélectionné
      </div>
    </template>

    <template #footer>
      <UButton label="Fermer" color="neutral" variant="ghost" @click="isOpen = false" />
    </template>
  </USlideover>
</template>