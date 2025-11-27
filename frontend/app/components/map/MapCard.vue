<script setup lang="ts">
const props = defineProps<{
  modelValue: string[]
  options: string[]
}>()

const emit = defineEmits(['update:modelValue'])

const selected = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})
</script>

<template>
    <UCard class="w-3/10 ml-5">
        <template #header>
            <div class="flex items-center gap-2">
                <UIcon name="i-lucide-map" class="w-5 h-5" />
                <h1 class="font-bold">Carte des îles Galapagos</h1>
            </div>
        </template>

        <div class="space-y-4">
            <h3 class="text-sm font-medium text-gray-500 mb-2">Couches à afficher</h3>
            <UCheckboxGroup 
                v-model="selected" 
                :items="options" 
                class="space-y-3"
            />
        </div>

        <template #footer>
            <div class="text-xs text-gray-400 flex items-center gap-1">
                Données temps réel
            </div>
        </template>
    </UCard>
</template>