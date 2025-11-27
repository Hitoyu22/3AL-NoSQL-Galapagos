<template>
  <header class="sticky top-0 z-50 w-full border-b border-gray-200 dark:border-gray-800 bg-white/75 dark:bg-gray-900/75 backdrop-blur">
    <UHeader 
        title="Galapagos" 
        to="/" 
        class="flex items-center justify-between h-16"
    >
        <template #right>
            <Nav />
            <UColorModeButton />
        </template>
    </UHeader>
  </header>
</template>