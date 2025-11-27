<script setup lang="ts">
import type { NuxtError } from '#app'

const props = defineProps<{
  error: NuxtError
}>()

useHead({
  title: 'Une erreur est survenue - Galapagos',
  htmlAttrs: {
    lang: 'fr'
  }
})
</script>

<template>
  <UApp>
    <div class="min-h-screen flex flex-col">
        <NavBar /> 
        <UContainer class="flex items-center justify-center">
        <UError 
          :error="error.statusCode === 404 ? {
            statusCode: 404,
            statusMessage: 'Page non trouvée',
            message: 'La page que vous recherchez n\'existe pas.'
          } : error"
          :clear="{
            label : 'Retour à l\'accueil'
          }"
        />
      </UContainer>
    </div>
  </UApp>
</template>