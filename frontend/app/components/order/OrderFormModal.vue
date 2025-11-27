<script setup lang="ts">
import type { Order, Product } from '~/graphql/types'

import productsData from '~/graphql/data/products.json'

import { portService } from '~/graphql/ports'
import { clientService } from '~/graphql/clients'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits(['update:modelValue', 'create'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const { $graphql } = useNuxtApp()
const gqlClient = $graphql.default

const { data, pending } = await useAsyncData('order-form-data', async () => {
    const [ports, clients] = await Promise.all([
        portService.getAll(gqlClient),
        clientService.getAll(gqlClient)
    ])
    return { ports, clients }
})


const clientOptions = computed(() => 
    (data.value?.clients || []).map(c => ({ label: c.name, value: c.id }))
)

const portOptions = computed(() => 
    (data.value?.ports || []).map(p => ({ label: p.name, value: p.name }))
)

const productOptions = (productsData as Product[]).map(p => ({ 
    label: p.name, 
    value: p.id, 
    weight: p.weightKg 
}))


interface FormItem {
    productId: string
    quantity: number
}

const state = reactive({
    clientId: '',
    destinationPort: '',
    items: [{ productId: '', quantity: 1 }] as FormItem[]
})

const addProductLine = () => {
    state.items.push({ productId: '', quantity: 1 })
}

const removeProductLine = (index: number) => {
    if (state.items.length > 1) {
        state.items.splice(index, 1)
    }
}

const stats = computed(() => {
    let totalWeight = 0
    
    state.items.forEach(item => {
        const prod = productOptions.find(p => p.value === item.productId)
        if (prod) {
            totalWeight += prod.weight * item.quantity
        }
    })

    const boxCount = Math.ceil(totalWeight / 15) || 1

    return { totalWeight, boxCount }
})

const onSubmit = () => {
    const clients = data.value?.clients || []
    const selectedClient = clients.find(c => c.id === state.clientId)
    
    const orderItems = state.items
        .filter(item => item.productId)
        .map(item => {
            const prodData = (productsData as Product[]).find(p => p.id === item.productId)!
            return {
                product: prodData,
                quantity: item.quantity
            }
        })

    if (!selectedClient || orderItems.length === 0 || !state.destinationPort) return

    const newOrder: Order = {
        id: `ORD-${new Date().getFullYear()}-${Math.floor(Math.random() * 1000)}`,
        clientId: state.clientId,
        clientName: selectedClient.name,
        date: new Date().toISOString(),
        status: 'PENDING',
        destinationPort: state.destinationPort,
        items: orderItems,
        boxCount: stats.value.boxCount,
        totalWeight: stats.value.totalWeight
    }

    emit('create', newOrder)
    isOpen.value = false
    
    state.clientId = ''
    state.destinationPort = ''
    state.items = [{ productId: '', quantity: 1 }]
}
</script>

<template>
  <UModal v-model:open="isOpen" title="Nouvelle Commande" class="sm:max-w-xl">
    <template #body>
      <form class="space-y-4" @submit.prevent="onSubmit">
        
        <div class="grid grid-cols-2 gap-4">
            <UFormField label="Client" name="client">
                <USelect 
                    v-model="state.clientId" 
                    :items="clientOptions" 
                    :loading="pending"
                    option-attribute="label" 
                    placeholder="Sélectionner..." 
                    class="w-full"
                />
            </UFormField>

            <UFormField label="Destination" name="destination">
                <USelect 
                    v-model="state.destinationPort" 
                    :items="portOptions" 
                    :loading="pending"
                    option-attribute="label" 
                    placeholder="Port de livraison" 
                    class="w-full"
                />
            </UFormField>
        </div>

        <USeparator label="Produits" />

        <div class="space-y-3 max-h-[300px] overflow-y-auto pr-2">
            <div v-for="(item, index) in state.items" :key="index" class="flex items-end gap-2">
                <div class="grow">
                     <USelect 
                        v-model="item.productId" 
                        :items="productOptions" 
                        option-attribute="label"
                        placeholder="Produit..." 
                        class="w-full"
                    />
                </div>
                <div class="w-24">
                    <UInput v-model.number="item.quantity" type="number" min="1" placeholder="Qté" />
                </div>
                <UButton 
                    icon="i-lucide-trash" 
                    color="error" 
                    variant="ghost" 
                    :disabled="state.items.length === 1"
                    @click="removeProductLine(index)"
                />
            </div>
        </div>

        <UButton label="Ajouter un produit" icon="i-lucide-plus" size="xs" variant="soft" block @click="addProductLine" />

        <div class="bg-gray-50 dark:bg-gray-800 p-3 rounded-lg flex justify-between items-center text-sm">
            <div class="text-gray-500">
                Poids total estimé: <span class="font-bold text-gray-900 dark:text-white">{{ stats.totalWeight.toFixed(1) }} kg</span>
            </div>
            <div class="flex items-center gap-2">
                <UIcon name="i-lucide-box" class="text-primary" />
                <span class="font-bold text-primary">{{ stats.boxCount }} caisse(s) requise(s)</span>
            </div>
        </div>

        <div class="flex justify-end gap-2 mt-6">
            <UButton label="Annuler" color="neutral" variant="ghost" @click="isOpen = false" />
            <UButton type="submit" label="Créer la commande" color="primary" />
        </div>

      </form>
    </template>
  </UModal>
</template>