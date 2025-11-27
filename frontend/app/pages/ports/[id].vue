<script setup lang="ts">
import { h, resolveComponent, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import type { TableColumn } from '#ui/types'
import type { Locker, Port } from '~/graphql/types'
import { portService } from '~/graphql/ports'
import { lockerService } from '~/graphql/locker'
import LockerFormModal from '~/components/ports/LockerFormModal.vue'

const route = useRoute()
const param = decodeURIComponent(route.params.id as string)

const { $graphql } = useNuxtApp()
const gqlClient = $graphql.default
const toast = useToast()

const UBadge = resolveComponent('UBadge')
const UButton = resolveComponent('UButton')
const UIcon = resolveComponent('UIcon')

const { data: portData, pending, refresh, error } = await useAsyncData(`port-${param}`, () => {
    const isId = !isNaN(Number(param))
    
    if (isId) {
        return portService.getById(gqlClient, Number(param))
    } else {
        return portService.getByName(gqlClient, param)
    }
})

const port = computed<Port>(() => {
    if (!portData.value) {
        return {
            name: isNaN(Number(param)) ? param : 'Port inconnu',
            island: '...',
            lat: 0,
            lon: 0,
            lockers: 0,
            isWarehouse: false
        }
    }
    return {
        id: portData.value.id,
        name: portData.value.name,
        island: portData.value.island,
        lat: portData.value.lat,
        lon: portData.value.lon,
        lockers: portData.value.lockers || 0,
        isWarehouse: false
    }
})

const lockers = computed<Locker[]>(() => {
    return (portData.value as any)?.lockersList || []
})

const activeLockers = computed(() => lockers.value.filter(l => l.status !== 'EMPTY'))
const availableLockers = computed(() => lockers.value.filter(l => l.status === 'EMPTY'))

const isModalOpen = ref(false)
const lockerToEdit = ref<Locker | null>(null)
const globalFilter = ref('')

const openCreate = () => {
    lockerToEdit.value = null
    isModalOpen.value = true
}

const openEdit = (locker: Locker) => {
    lockerToEdit.value = locker
    isModalOpen.value = true
}

const handleDelete = async (locker: Locker) => {
    if (!confirm(`Supprimer le casier #${locker.number} ?`)) return

    try {
        await lockerService.delete(gqlClient, locker.id)
        toast.add({ title: 'Supprimé', description: `Casier #${locker.number} supprimé.`, color: 'neutral' })
        refresh()
    } catch (e) {
        toast.add({ title: 'Erreur', description: 'Impossible de supprimer le casier.', color: 'error' })
    }
}

const handleCreate = async (data: any) => {
    const portId = portData.value?.id || ( !isNaN(Number(param)) ? Number(param) : null )
    
    if (!portId) {
        toast.add({ title: 'Erreur', description: 'Impossible d\'identifier le port.', color: 'error' })
        return
    }

    try {
        await lockerService.create(gqlClient, portId)
        toast.add({ title: 'Succès', description: 'Nouveau casier ajouté au port.', color: 'success' })
        isModalOpen.value = false
        refresh()
    } catch (e) {
        toast.add({ title: 'Erreur', description: 'Erreur lors de la création.', color: 'error' })
    }
}

const handleUpdate = async (updatedLocker: Locker) => {
    try {
        await lockerService.updateStatus(
            gqlClient, 
            updatedLocker.id, 
            updatedLocker.status, 
            updatedLocker.maintenanceReason
        )
        toast.add({ title: 'Succès', description: `Casier #${updatedLocker.number} mis à jour.`, color: 'success' })
        isModalOpen.value = false
        refresh()
    } catch (e) {
        toast.add({ title: 'Erreur', description: 'Impossible de mettre à jour le casier.', color: 'error' })
    }
}

const getStatusColor = (status: string) => {
    switch(status) {
        case 'OCCUPIED': return 'primary'
        case 'MAINTENANCE': return 'error'
        case 'RESERVED': return 'warning'
        default: return 'gray'
    }
}

const getStatusLabel = (status: string) => {
    switch(status) {
        case 'OCCUPIED': return 'Occupé'
        case 'MAINTENANCE': return 'Maintenance'
        case 'RESERVED': return 'Réservé'
        case 'EMPTY': return 'Disponible'
        default: return status
    }
}

const columns: TableColumn<Locker>[] = [
  {
    accessorKey: 'number',
    header: 'Numéro',
    cell: ({ row }) => h('span', { class: 'font-mono font-bold text-lg' }, `#${row.getValue('number')}`)
  },
  {
    accessorKey: 'status',
    header: 'Statut',
    cell: ({ row }) => h(UBadge, { color: 'success', variant: 'subtle' }, () => 'Disponible')
  },
  {
    id: 'info',
    header: 'Info',
    cell: () => h('span', { class: 'text-gray-400 text-xs italic' }, 'Prêt à être assigné')
  },
  {
    id: 'actions',
    header: '',
    cell: ({ row }) => {
        return h('div', { class: 'flex items-center gap-1 justify-end' }, [
            h(UButton, {
                icon: 'i-lucide-pencil',
                variant: 'ghost',
                color: 'gray',
                size: 'xs',
                onClick: () => openEdit(row.original)
            }),
            h(UButton, {
                icon: 'i-lucide-trash',
                variant: 'ghost',
                color: 'error',
                size: 'xs',
                onClick: () => handleDelete(row.original)
            })
        ])
    }
  }
]
</script>

<template>
  <div class="space-y-8">
    
    <div class="relative flex items-start justify-between bg-white dark:bg-gray-900 p-6 rounded-xl border border-gray-200 dark:border-gray-800 shadow-sm overflow-hidden">
        <div v-if="pending" class="absolute inset-0 bg-white/80 dark:bg-gray-900/80 z-10 flex items-center justify-center">
            <UIcon name="i-lucide-loader-2" class="w-8 h-8 animate-spin text-primary" />
        </div>

        <div class="flex items-center gap-4">
            <div class="p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                <UIcon :name="port.isWarehouse ? 'i-lucide-factory' : 'i-lucide-anchor'" class="w-8 h-8 text-blue-600 dark:text-blue-400" />
            </div>
            <div>
                <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ port.name }}</h1>
                <div class="flex items-center gap-2 text-gray-500">
                    <UIcon name="i-lucide-map-pin" class="w-4 h-4" />
                    <span>{{ port.island }}</span>
                    <span class="text-gray-300">|</span>
                    <span class="font-mono text-xs">{{ port.lat }}, {{ port.lon }}</span>
                </div>
            </div>
        </div>
        <div class="text-right">
            <div class="text-sm text-gray-500">Capacité Totale</div>
            <div class="text-3xl font-bold font-mono">{{ port.lockers }}</div>
        </div>
    </div>

    <div class="space-y-3">
        <h2 class="text-lg font-semibold flex items-center gap-2">
            <UIcon name="i-lucide-alert-circle" class="w-5 h-5 text-orange-500" />
            Casiers Actifs & Maintenance
        </h2>
        
        <div v-if="activeLockers.length > 0" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            <UCard v-for="locker in activeLockers" :key="locker.id" class="p-4">
                <div class="flex justify-between items-start mb-2">
                    <span class="text-xl font-bold font-mono">#{{ locker.number }}</span>
                    <UBadge :color="getStatusColor(locker.status)" variant="subtle">{{ getStatusLabel(locker.status) }}</UBadge>
                </div>
                
                <div class="text-sm text-gray-600 dark:text-gray-400 min-h-10">
                    <div v-if="locker.status === 'OCCUPIED'" class="flex items-center gap-1.5">
                        <UIcon name="i-lucide-box" class="w-4 h-4" />
                        <span class="truncate font-mono text-xs" :title="locker.boxId">Box: {{ locker.boxId }}</span>
                    </div>
                    <div v-else-if="locker.status === 'MAINTENANCE'" class="text-red-500 flex items-start gap-1.5">
                        <UIcon name="i-lucide-wrench" class="w-4 h-4 mt-0.5" />
                        <span class="leading-tight text-xs">{{ locker.maintenanceReason }}</span>
                    </div>
                </div>

                <div class="mt-3 flex justify-end">
                    <UButton size="xs" color="gray" variant="ghost" label="Gérer" icon="i-lucide-settings-2" @click="openEdit(locker)" />
                </div>
            </UCard>
        </div>
        <div v-else class="p-8 text-center text-gray-500 bg-gray-50 dark:bg-gray-800/50 rounded-lg border-dashed border border-gray-300">
            Aucun casier actif ou en maintenance. Tout est calme.
        </div>
    </div>

    <USeparator />

    <div class="space-y-4">
        <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold flex items-center gap-2">
                <UIcon name="i-lucide-check-circle" class="w-5 h-5 text-green-500" />
                Casiers Disponibles ({{ availableLockers.length }})
            </h2>
            <UButton icon="i-lucide-plus" label="Ajouter un casier" color="white" @click="openCreate" />
        </div>

        <div class="flex px-4 py-3.5 bg-gray-50 dark:bg-gray-800/50 rounded-lg border border-gray-200 dark:border-gray-700">
            <UInput v-model="globalFilter" class="max-w-sm w-full" placeholder="Rechercher un numéro..." icon="i-lucide-search" />
        </div>

        <UTable 
            :data="availableLockers" 
            :columns="columns" 
            :loading="pending"
            v-model:global-filter="globalFilter" 
            class="bg-white dark:bg-gray-900 max-h-[500px] overflow-y-auto"
        />
    </div>

    <LockerFormModal 
        v-model="isModalOpen" 
        :locker-to-edit="lockerToEdit" 
        :port-name="port.name"
        @create="handleCreate"
        @update="handleUpdate"
    />

  </div>
</template>