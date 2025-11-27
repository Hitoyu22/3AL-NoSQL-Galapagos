<script setup lang="ts">
import { z } from 'zod'
import type { FormSubmitEvent } from '#ui/types'
import type { Client } from '~/graphql/types'

const props = defineProps<{
  modelValue: boolean
  clientToEdit?: Client | null
}>()

const emit = defineEmits(['update:modelValue', 'create', 'update'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const schema = z.object({
  name: z.string().min(2, 'Le nom doit faire au moins 2 caractères'),
  type: z.enum(['chercheur', 'institution']),
  specialty: z.string().min(1, 'La spécialité est requise'),
  study: z.string().optional(),
  email: z.string().email('Email invalide')
})

type Schema = z.output<typeof schema>

const state = reactive<Partial<Schema>>({
  name: '',
  type: 'chercheur',
  specialty: '',
  study: '',
  email: ''
})

const typeOptions = [
  { label: 'Chercheur', value: 'chercheur' },
  { label: 'Institution', value: 'institution' }
]

const toast = useToast()

watch(() => props.modelValue, (isNowOpen) => {
  if (isNowOpen) {
    if (props.clientToEdit) {
      state.name = props.clientToEdit.name
      state.type = props.clientToEdit.type
      state.specialty = props.clientToEdit.specialty
      state.study = props.clientToEdit.study
      state.email = props.clientToEdit.email
    } else {
      state.name = ''
      state.type = 'chercheur'
      state.specialty = ''
      state.study = ''
      state.email = ''
    }
  }
})

async function onSubmit(event: FormSubmitEvent<Schema>) {
  if (props.clientToEdit) {
    emit('update', { ...event.data, id: props.clientToEdit.id })
  } else {
    emit('create', event.data)
  }
  isOpen.value = false
}
</script>

<template>
  <UModal v-model:open="isOpen" :title="clientToEdit ? 'Modifier le client' : 'Ajouter un client'" :description="clientToEdit ? 'Modifiez les informations ci-dessous.' : 'Remplissez le formulaire pour créer un nouveau client.'">
    <template #body>
      <UForm :schema="schema" :state="state" class="space-y-4" @submit="onSubmit">
        
        <UFormField label="Nom complet" name="name">
          <UInput v-model="state.name" placeholder="ex: Dr. Elena Rodriguez" class="w-full" />
        </UFormField>

        <div class="grid grid-cols-2 gap-4">
            <UFormField label="Type" name="type">
                <USelect v-model="state.type" :items="typeOptions" option-attribute="label" class="w-full" />
            </UFormField>

            <UFormField label="Spécialité / Domaine" name="specialty">
                <UInput v-model="state.specialty" placeholder="ex: Volcanologie" class="w-full"/>
            </UFormField>
        </div>

        <UFormField label="Étude / Projet en cours" name="study">
           <UInput v-model="state.study" placeholder="ex: Analyse du Sierra Negra" class="w-full"/>
        </UFormField>

        <UFormField label="Email de contact" name="email">
           <UInput v-model="state.email" type="email" placeholder="contact@exemple.com" class="w-full"/>
        </UFormField>

        <div class="flex justify-end gap-2 mt-6">
            <UButton label="Annuler" color="neutral" variant="ghost" @click="isOpen = false" />
            <UButton type="submit" :label="clientToEdit ? 'Enregistrer' : 'Créer'" color="primary" />
        </div>

      </UForm>
    </template>
  </UModal>
</template>