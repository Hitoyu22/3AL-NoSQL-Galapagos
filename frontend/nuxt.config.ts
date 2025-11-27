export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: [
    '@nuxt/ui',
    '@nuxtjs/leaflet',
    'nuxt-graphql-request'
  ],
  css: ['~/assets/css/main.css'],
  graphql: {
    clients: {
      default: {
        endpoint: 'http://localhost:8080/graphql'
      },
    },
    options: {
      method: 'POST',
    },
  },
})