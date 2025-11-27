<script setup lang="ts">
import DefaultLayout from './layouts/DefaultLayout.vue'

const colorMode = useColorMode()

const color = computed(() => colorMode.value === 'dark' ? '#1b1718' : 'white')

useHead({
  meta: [
    { charset: 'utf-8' },
    { name: 'viewport', content: 'width=device-width, initial-scale=1' },
    { key: 'theme-color', name: 'theme-color', content: color }
  ],
  link: [
    { rel: 'icon', href: '/favicon.ico' }
  ],
  htmlAttrs: {
    lang: 'fr'
  }
})

const title = 'Galapagos'

useSeoMeta({
  title,
  ogTitle: title
})
</script>

<template>
  <UApp>
    <DefaultLayout>
      <NuxtPage />
    </DefaultLayout>
  </UApp>
</template>