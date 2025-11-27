<script setup lang="ts">
import { h, resolveComponent, ref, watch } from 'vue'
import type { TableColumn } from '#ui/types'
import type { Client } from '~/graphql/types'
import { clientService } from '~/graphql/clients'
import ClientFormModal from '~/components/clients/ClientFormModal.vue'

const { $graphql } = useNuxtApp()
const gqlClient = $graphql.default

const UBadge = resolveComponent('UBadge')
const UButton = resolveComponent('UButton')
const toast = useToast()

const globalFilter = ref('')
const isModalOpen = ref(false)
const clientToEdit = ref<Client | null>(null)
const tableKey = ref(0)

const { data: clients, pending, refresh, error } = await useAsyncData('clients-list', () => 
  clientService.getAll(gqlClient)
)

const clientList = computed(() => clients.value || [])

const openCreate = () => {
    clientToEdit.value = null
    isModalOpen.value = true
}

const openEdit = (client: Client) => {
    clientToEdit.value = { ...client }
    isModalOpen.value = true
}

const handleDelete = async (client: Client) => {
    if(!confirm(`Voulez-vous vraiment supprimer ${client.name} ?`)) return

    try {
        await clientService.delete(gqlClient, client.id)
        toast.add({ title: 'Supprimé', description: `${client.name} a été retiré.`, color: 'neutral' })
        await refresh()
        tableKey.value++
    } catch (e) {
        toast.add({ title: 'Erreur', description: 'Impossible de supprimer le client.', color: 'error' })
        console.error(e)
    }
}

const handleCreate = async (formData: Omit<Client, 'id' | 'orderHistory'>) => {
    try {
        await clientService.create(gqlClient, formData)
        toast.add({ title: 'Succès', description: 'Client ajouté avec succès.', color: 'success' })
        isModalOpen.value = false
        await refresh()
        tableKey.value++
    } catch (e) {
        toast.add({ title: 'Erreur', description: 'Erreur lors de la création.', color: 'error' })
        console.error(e)
    }
}

const handleUpdate = async (updatedClient: Client) => {
    try {
        await clientService.update(gqlClient, updatedClient)
        toast.add({ title: 'Mis à jour', description: 'Informations modifiées.', color: 'success' })
        isModalOpen.value = false
        await refresh()
        tableKey.value++
    } catch (e) {
        toast.add({ title: 'Erreur', description: 'Erreur lors de la mise à jour.', color: 'error' })
        console.error(e)
    }
}

const columns: TableColumn<Client>[] = [
  {
    accessorKey: 'name',
    header: 'Nom',
    cell: ({ row }) => h('div', { class: 'font-medium text-gray-900 dark:text-white' }, row.getValue('name'))
  },
  {
    accessorKey: 'type',
    header: 'Type',
    cell: ({ row }) => {
      const type = row.getValue('type') as string
      const color = type.toLowerCase().includes('institution') ? 'purple' : 'primary'
      return h(UBadge, { color, variant: 'subtle', class: 'capitalize' }, () => type)
    }
  },
  {
    accessorKey: 'specialty',
    header: 'Spécialité',
    cell: ({ row }) => h('span', { class: 'text-gray-500 italic' }, row.getValue('specialty') || '-')
  },
  {
    accessorKey: 'study',
    header: 'Étude en cours',
    cell: ({ row }) => h('span', { class: 'truncate max-w-[200px] block' }, row.getValue('study') || '-')
  },
  {
    accessorKey: 'email',
    header: 'Contact',
    cell: ({ row }) => {
        const email = row.getValue('email') as string
        if (!email) return h('span', { class: 'text-gray-400' }, '-')
        return h('a', { 
            href: `mailto:${email}`,
            class: 'text-primary hover:underline font-mono text-xs' 
        }, email)
    }
  },
  {
    id: 'actions',
    header: '',
    cell: ({ row }) => {
        return h('div', { class: 'flex items-center gap-1 justify-end' }, [
            h(UButton, {
                icon: 'i-lucide-pencil',
                variant: 'ghost',
                color: 'primary',
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

watch(() => clients.value, (newVal) => {
  console.log('Clients mis à jour:', newVal)
}, { deep: true })
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
        <h1 class="text-2xl font-bold">Clients & Partenaires</h1>
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
                label="Nouveau client" 
                color="primary" 
                @click="openCreate"
            />
        </div>
    </div>

    <UAlert v-if="error" icon="i-lucide-alert-triangle" color="error" title="Erreur de chargement" :description="error.message" />

    <div class="flex px-4 py-3.5 bg-gray-50 dark:bg-gray-800/50 rounded-lg border border-gray-200 dark:border-gray-700">
      <UInput
        v-model="globalFilter"
        class="max-w-sm w-full"
        placeholder="Filtrer l'affichage..."
        icon="i-lucide-search"
      />
    </div>

    <UTable 
        :key="tableKey"
        :data="clientList" 
        :columns="columns" 
        :loading="pending"
        v-model:global-filter="globalFilter"
        class="bg-white dark:bg-gray-900"
    >
        <template #empty-state>
            <div class="flex flex-col items-center justify-center py-12 gap-3">
                <span class="italic text-gray-500">Aucun client trouvé dans la base de données.</span>
            </div>
        </template>
    </UTable>

    <ClientFormModal 
        v-model="isModalOpen" 
        :client-to-edit="clientToEdit"
        @create="handleCreate"
        @update="handleUpdate"
    />

  </div>
</template>