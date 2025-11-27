<script setup lang="ts">
import { z } from 'zod'
import type { FormSubmitEvent } from '#ui/types'
import type { Locker } from '~/graphql/types'

const props = defineProps<{
  modelValue: boolean
  lockerToEdit?: Locker | null
  portName: string
}>()

const emit = defineEmits(['update:modelValue', 'create', 'update'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const schema = z.object({
  number: z.number().min(1, 'Le numéro est requis'),
  status: z.enum(['EMPTY', 'OCCUPIED', 'RESERVED', 'MAINTENANCE']),
  maintenanceReason: z.string().optional()
})

type Schema = z.output<typeof schema>

const state = reactive<Partial<Schema>>({
  number: undefined,
  status: 'EMPTY',
  maintenanceReason: ''
})

const statusOptions = [
  { label: 'Disponible (Vide)', value: 'EMPTY' },
  { label: 'En Maintenance', value: 'MAINTENANCE' },
  { label: 'Occupé', value: 'OCCUPIED' },
  { label: 'Réservé', value: 'RESERVED' }
]

watch(() => props.modelValue, (isNowOpen) => {
  if (isNowOpen) {
    if (props.lockerToEdit) {
      state.number = props.lockerToEdit.number
      state.status = props.lockerToEdit.status
      state.maintenanceReason = props.lockerToEdit.maintenanceReason
    } else {
      state.number = undefined
      state.status = 'EMPTY'
      state.maintenanceReason = ''
    }
  }
})

async function onSubmit(event: FormSubmitEvent<Schema>) {
  if (props.lockerToEdit) {
    emit('update', { ...props.lockerToEdit, ...event.data })
  } else {
    emit('create', { ...event.data, portName: props.portName })
  }
  isOpen.value = false
}
</script>

<template>
  <UModal v-model:open="isOpen" :title="lockerToEdit ? `Modifier le casier #${lockerToEdit.number}` : 'Ajouter un casier'">
    <template #body>
      <UForm :schema="schema" :state="state" class="space-y-4" @submit="onSubmit">
        
        <UFormField label="Numéro du casier" name="number">
          <UInput v-model.number="state.number" type="number" :disabled="!!lockerToEdit" />
        </UFormField>

        <UFormField label="Statut" name="status">
          <USelect v-model="state.status" :items="statusOptions" option-attribute="label" class="w-full" />
        </UFormField>

        <UFormField v-if="state.status === 'MAINTENANCE'" label="Raison de la maintenance" name="maintenanceReason">
           <UTextarea v-model="state.maintenanceReason" placeholder="Ex: Serrure cassée..." />
        </UFormField>

        <div class="flex justify-end gap-2 mt-6">
            <UButton label="Annuler" color="neutral" variant="ghost" @click="isOpen = false" />
            <UButton type="submit" :label="lockerToEdit ? 'Enregistrer' : 'Créer'" color="primary" />
        </div>

      </UForm>
    </template>
  </UModal>
</template>