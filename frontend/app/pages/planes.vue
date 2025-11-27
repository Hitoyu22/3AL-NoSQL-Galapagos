<script setup lang="ts">
import { h, resolveComponent, ref } from 'vue'
import type { Seaplane } from '~/graphql/types'
import { seaplaneService } from '~/graphql/seaplanes'
import type { TableColumn } from '#ui/types'
import PlaneSlideover from '~/components/planes/PlaneSlideover.vue'
import PlaneFormModal from '~/components/planes/PlaneFormModal.vue'

const { $graphql } = useNuxtApp()
const gqlClient = $graphql.default

const UBadge = resolveComponent('UBadge')
const UButton = resolveComponent('UButton')
const toast = useToast()

const { data: planes, pending, refresh, error } = await useAsyncData('planes-list', () => 
  seaplaneService.getAll(gqlClient)
)

const isSlideoverOpen = ref(false)
const isModalOpen = ref(false)
const selectedPlane = ref<Seaplane | null>(null)
const planeToEdit = ref<Seaplane | null>(null)
const globalFilter = ref('')

const statusToString = (status: string) => {
  switch(status) {
    case 'AVAILABLE': return 'Disponible';
    case 'MAINTENANCE': return 'En maintenance';
    case 'IN_FLIGHT': return 'En vol';
    case 'AT_PORT': return 'Au port';
    default: return status;
  }
}

const getStatusColor = (status: string) => {
  switch(status) {
    case 'AVAILABLE': return 'success';
    case 'MAINTENANCE': return 'warning';
    case 'IN_FLIGHT': return 'primary';
    case 'AT_PORT': return 'neutral';
    default: return 'neutral';
  }
}


const openDetails = (plane: Seaplane) => {
    selectedPlane.value = plane
    isSlideoverOpen.value = true
}

const openEdit = (plane: Seaplane) => {
    planeToEdit.value = plane
    isModalOpen.value = true
}

const openCreate = () => {
    planeToEdit.value = null
    isModalOpen.value = true
}

const handleCreate = async (newPlaneData: Partial<Seaplane>) => {
    try {
        await seaplaneService.create(gqlClient, newPlaneData)
        toast.add({ title: 'Succès', description: `Avion ${newPlaneData.id} ajouté.`, color: 'success' })
        isModalOpen.value = false
        refresh()
    } catch (e: any) {
        toast.add({ title: 'Erreur', description: 'Erreur lors de la création.', color: 'error' })
        console.error(e)
    }
}

const handleUpdate = async (updatedPlane: Seaplane) => {
    try {
        await seaplaneService.update(gqlClient, updatedPlane)
        toast.add({ title: 'Succès', description: 'Avion mis à jour.', color: 'success' })
        isModalOpen.value = false
        refresh()
    } catch (e: any) {
        toast.add({ title: 'Erreur', description: 'Impossible de modifier l\'avion.', color: 'error' })
        console.error(e)
    }
}

const columns: TableColumn<Seaplane>[] = [
  {
    accessorKey: 'id',
    header: 'Immatriculation',
    cell: ({ row }) => h('span', { class: 'font-mono font-bold' }, row.getValue('id'))
  },
  {
    accessorKey: 'model',
    header: 'Modèle'
  },
  {
    accessorKey: 'status',
    header: 'Statut',
    cell: ({ row }) => {
      const rawStatus = row.getValue('status') as string
      return h(UBadge, { color: getStatusColor(rawStatus), variant: 'subtle', class: 'capitalize' }, () => statusToString(rawStatus))
    }
  },
  {
    accessorKey: 'location',
    header: 'Localisation',
    cell: ({ row }) => h('div', { class: 'truncate max-w-[200px] text-sm text-gray-600' }, row.getValue('location'))
  },
  {
    id: 'actions',
    header: '',
    cell: ({ row }) => {
        return h('div', { class: 'flex items-center gap-1 justify-end' }, [
            h(UButton, {
                icon: 'i-lucide-eye',
                variant: 'ghost',
                color: 'neutral',
                size: 'xs',
                onClick: () => openDetails(row.original)
            }),
            h(UButton, {
                icon: 'i-lucide-pencil',
                variant: 'ghost',
                color: 'primary',
                size: 'xs',
                disabled: row.original.status === 'IN_FLIGHT',
                onClick: () => openEdit(row.original)
            })
        ])
    }
  }
]
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
        <h1 class="text-2xl font-bold">Flotte d'Hydravions</h1>
        <div class="flex gap-2">
            <UButton 
                icon="i-lucide-refresh-cw" 
                color="gray" 
                variant="ghost"
                :loading="pending"
                @click="refresh"
            />
            <UButton 
                icon="i-lucide-plus" 
                label="Nouveau" 
                color="primary" 
                @click="openCreate"
            />
        </div>
    </div>

    <UAlert v-if="error" icon="i-lucide-alert-triangle" color="error" title="Erreur API" :description="error.message" />

    <div class="flex px-4 py-3.5 bg-gray-50 dark:bg-gray-800/50 rounded-lg border border-gray-200 dark:border-gray-700">
      <UInput
        v-model="globalFilter"
        class="max-w-sm w-full"
        placeholder="Rechercher un avion..."
        icon="i-lucide-search"
      />
    </div>

    <UTable 
        :data="planes || []" 
        :columns="columns" 
        :loading="pending"
        v-model:global-filter="globalFilter"
        class="bg-white dark:bg-gray-900"
    >
        <template #empty-state>
            <div class="text-center py-10 text-gray-500">Aucun hydravion dans la flotte.</div>
        </template>
    </UTable>

    <PlaneSlideover 
        v-model="isSlideoverOpen" 
        :plane="selectedPlane" 
    />

    <PlaneFormModal 
        v-model="isModalOpen" 
        :plane-to-edit="planeToEdit"
        @create="handleCreate" 
        @update="handleUpdate"
    />

  </div>
</template>