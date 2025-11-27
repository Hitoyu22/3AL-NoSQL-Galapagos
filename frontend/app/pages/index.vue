<script setup lang="ts">
import { gql } from 'nuxt-graphql-request/utils';
import { ref } from 'vue';
import { useNuxtApp } from '#app';

const version = ref<string | null>(null);

const { $graphql } = useNuxtApp();
const client = $graphql.default;

const fetchVersion = async () => {
  const query = gql`
    query {
      version
    }
  `;

  try {
    const data = await client.request<{ version: string }>(query);
    version.value = data.version;
  } catch (error) {
    console.error('GraphQL Error:', error);
  }
};

fetchVersion();
</script>

<template>
  <div>
    <h2>Version du serveur GraphQL :</h2>
    <p v-if="version">{{ version }}</p>
    <p v-else>Chargement...</p>
  </div>
</template>