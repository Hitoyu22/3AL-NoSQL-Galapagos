<script setup lang="ts">
import type { Order } from '~/graphql/types'

const props = defineProps<{
  modelValue: boolean
  order: Order | null
}>()

const emit = defineEmits(['update:modelValue'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})


const getStatusColor = (status: string) => {
    switch(status) {
        case 'PENDING': return 'neutral'
        case 'IN_TRANSIT': return 'primary'
        case 'DELIVERED': return 'success'
        case 'PARTIALLY_DELIVERED': return 'warning'
        default: return 'neutral'
    }
}

const getStatusLabel = (status: string) => {
    switch(status) {
        case 'PENDING': return 'En préparation'
        case 'IN_TRANSIT': return 'En transit'
        case 'DELIVERED': return 'Livrée / Disponible'
        case 'PARTIALLY_DELIVERED': return 'Livraison partielle'
        default: return status
    }
}

const currentLocationInfo = computed(() => {
    if (!props.order) return null

    switch(props.order.status) {
        case 'PENDING':
            return {
                label: 'Entrepôt Central',
                sub: 'Puerto Baquerizo Moreno',
                icon: 'i-lucide-warehouse',
                color: 'text-gray-500',
                bg: 'bg-gray-100 dark:bg-gray-800'
            }
        case 'IN_TRANSIT':
            return {
                label: 'En vol',
                sub: `Vers ${props.order.destinationPort}`,
                icon: 'i-lucide-plane',
                color: 'text-blue-500',
                bg: 'bg-blue-50 dark:bg-blue-900/20'
            }
        case 'DELIVERED':
            return {
                label: 'Consigne (Locker)',
                sub: `Port de ${props.order.destinationPort}`,
                icon: 'i-lucide-box-select',
                color: 'text-green-500',
                bg: 'bg-green-50 dark:bg-green-900/20'
            }
        default:
            return {
                label: 'Localisation inconnue',
                sub: '...',
                icon: 'i-lucide-help-circle',
                color: 'text-gray-500',
                bg: 'bg-gray-100'
            }
    }
})
</script>

<template>
  <USlideover v-model:open="isOpen" title="Détails de la commande">
    <template #body>
      <div v-if="order" class="space-y-6">
        
        <div class="flex items-center justify-between">
            <div>
                <h2 class="text-2xl font-bold font-mono">{{ order.id }}</h2>
                <div class="text-sm text-gray-500">{{ new Date(order.date).toLocaleDateString('fr-FR', { dateStyle: 'long' }) }}</div>
            </div>
            <UBadge :color="getStatusColor(order.status)" variant="subtle" size="lg">
                {{ getStatusLabel(order.status) }}
            </UBadge>
        </div>

        <USeparator />

        <div 
            v-if="currentLocationInfo"
            class="p-4 rounded-xl border border-gray-100 dark:border-gray-700 flex items-center gap-4"
            :class="currentLocationInfo.bg"
        >
            <div class="p-2 bg-white dark:bg-gray-900 rounded-full shadow-sm">
                <UIcon :name="currentLocationInfo.icon" class="w-6 h-6" :class="currentLocationInfo.color" />
            </div>
            <div>
                <div class="text-xs font-semibold uppercase tracking-wider text-gray-500">Localisation Actuelle</div>
                <div class="font-bold text-gray-900 dark:text-white">{{ currentLocationInfo.label }}</div>
                <div class="text-sm text-gray-500">{{ currentLocationInfo.sub }}</div>
            </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
            <div class="bg-gray-50 dark:bg-gray-800 p-3 rounded-md">
                <div class="text-xs text-gray-500 mb-1">Client</div>
                <div class="font-medium text-sm">{{ order.clientName }}</div>
                <div class="text-xs text-gray-400 font-mono mt-1">{{ order.clientId }}</div>
            </div>
            <div class="bg-gray-50 dark:bg-gray-800 p-3 rounded-md">
                <div class="text-xs text-gray-500 mb-1">Logistique</div>
                <div class="font-medium text-sm">{{ order.boxCount }} caisse(s)</div>
                <div class="text-xs text-gray-400 mt-1">Poids total: {{ order.totalWeight }} kg</div>
            </div>
        </div>

        <div>
            <h3 class="font-semibold mb-3 flex items-center gap-2">
                <UIcon name="i-lucide-list" /> Contenu de la commande
            </h3>
            <div class="border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden">
                <table class="w-full text-sm text-left">
                    <thead class="bg-gray-50 dark:bg-gray-800 text-gray-500 border-b border-gray-200 dark:border-gray-700">
                        <tr>
                            <th class="px-4 py-2 font-medium">Produit</th>
                            <th class="px-4 py-2 font-medium text-right">Qté</th>
                            <th class="px-4 py-2 font-medium text-right">Poids</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
                        <tr v-for="(item, idx) in order.items" :key="idx">
                            <td class="px-4 py-3">
                                <div class="font-medium">{{ item.product.name }}</div>
                                <div class="text-xs text-gray-400 truncate max-w-[180px]">{{ item.product.description }}</div>
                            </td>
                            <td class="px-4 py-3 text-right font-mono">x{{ item.quantity }}</td>
                            <td class="px-4 py-3 text-right text-gray-500">{{ (item.product.weightKg * item.quantity).toFixed(1) }} kg</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

      </div>
      <div v-else class="h-full flex items-center justify-center text-gray-500">
        Chargement...
      </div>
    </template>

    <template #footer>
        <div class="flex justify-between w-full">
            <UButton 
                v-if="order?.status === 'PENDING'"
                label="Lancer l'expédition" 
                color="primary" 
                variant="soft"
                icon="i-lucide-plane-takeoff"
                @click="isOpen = false"
            />
            <div v-else></div> <UButton label="Fermer" color="neutral" variant="ghost" @click="isOpen = false" />
        </div>
    </template>
  </USlideover>
</template>