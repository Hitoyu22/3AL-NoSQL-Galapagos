<script setup lang="ts">
import { z } from 'zod'
import type { FormSubmitEvent } from '#ui/types'
import type { Seaplane } from '~/graphql/types'

const props = defineProps<{
  modelValue: boolean
  planeToEdit?: Seaplane | null
}>()

const emit = defineEmits(['update:modelValue', 'create', 'update'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const schema = z.object({
  id: z.string().min(3, 'L\'immatriculation doit faire au moins 3 caractères'),
  model: z.string().min(1, 'Le modèle est requis'),
  status: z.enum(['AVAILABLE', 'MAINTENANCE', 'IN_FLIGHT', 'AT_PORT']),
  location: z.string().min(1, 'La localisation est requise'),
  lat: z.number().min(-90).max(90),
  lon: z.number().min(-180).max(180)
})

type Schema = z.output<typeof schema>

const state = reactive<Partial<Schema>>({
  id: '',
  model: 'DHC-6 Twin Otter',
  status: 'AVAILABLE',
  location: 'Puerto Baquerizo Moreno',
  lat: -0.9025,
  lon: -89.6091
})

const statusOptions = [
  { label: 'Disponible', value: 'AVAILABLE' },
  { label: 'En maintenance', value: 'MAINTENANCE' },
  { label: 'Au port', value: 'AT_PORT' }
]

const toast = useToast()

watch(() => props.modelValue, (isNowOpen) => {
  if (isNowOpen) {
    if (props.planeToEdit) {
      state.id = props.planeToEdit.id
      state.model = props.planeToEdit.model
      state.status = props.planeToEdit.status
      state.location = props.planeToEdit.location
      state.lat = props.planeToEdit.lat
      state.lon = props.planeToEdit.lon
    } else {
      state.id = ''
      state.model = 'DHC-6 Twin Otter'
      state.status = 'AVAILABLE'
      state.location = 'Puerto Baquerizo Moreno'
      state.lat = -0.9025
      state.lon = -89.6091
    }
  }
})

async function onSubmit(event: FormSubmitEvent<Schema>) {
  if (props.planeToEdit) {
    emit('update', event.data)
    toast.add({ title: 'Succès', description: 'L\'avion a été mis à jour.', color: 'success' })
  } else {
    emit('create', event.data)
    toast.add({ title: 'Succès', description: 'L\'avion a été ajouté à la flotte.', color: 'success' })
  }
  isOpen.value = false
}
</script>

<template>
  <UModal v-model:open="isOpen" :title="planeToEdit ? 'Modifier l\'hydravion' : 'Ajouter un hydravion'">
    <template #body>
      <UForm :schema="schema" :state="state" class="space-y-4" @submit="onSubmit">
        
        <div class="grid grid-cols-2 gap-4">
            <UFormField label="Immatriculation (ID)" name="id">
            <UInput v-model="state.id" placeholder="HB-XXX" :disabled="!!planeToEdit" />
            </UFormField>

            <UFormField label="Modèle" name="model">
            <UInput v-model="state.model" />
            </UFormField>
        </div>
        <div class="grid grid-cols-2 gap-4">
            <UFormField label="Statut" name="status">
            <USelect v-model="state.status" :items="statusOptions" option-attribute="label" class="w-8/10" />
            </UFormField>
            <UFormField label="Localisation" name="location">
            <UInput v-model="state.location" />
            </UFormField>
        </div>

        <div class="grid grid-cols-2 gap-4">
            <UFormField label="Latitude" name="lat">
                <UInput v-model.number="state.lat" type="number" step="0.0001" />
            </UFormField>
            <UFormField label="Longitude" name="lon">
                <UInput v-model.number="state.lon" type="number" step="0.0001" />
            </UFormField>
        </div>

        <div class="flex justify-end gap-2 mt-6">
            <UButton label="Annuler" color="neutral" variant="ghost" @click="isOpen = false" />
            <UButton type="submit" :label="planeToEdit ? 'Enregistrer' : 'Créer l\'avion'" color="primary" />
        </div>

      </UForm>
    </template>
  </UModal>
</template>