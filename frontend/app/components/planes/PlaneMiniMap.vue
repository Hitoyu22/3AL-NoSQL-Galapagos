<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  lat: { type: Number, required: true },
  lon: { type: Number, required: true }
})

const map = ref(null)
const L = ref(null)
const isReady = ref(false)
const zoom = ref(10)

onMounted(async () => {
  if (process.client) {
    const leaflet = await import('leaflet')
    L.value = leaflet.default || leaflet
    isReady.value = true
  }
})

const getPlaneIcon = () => {
    if (!L.value) return undefined
    return L.value.divIcon({
        className: 'bg-transparent border-0',
        html: `<div style="font-size: 32px; text-shadow: 0px 0px 3px white;">✈️</div>`,
        iconSize: [32, 32],
        iconAnchor: [16, 16],
        popupAnchor: [0, -10]
    })
}
</script>

<template>
  <div class="h-64 w-full rounded-lg overflow-hidden border border-gray-200 dark:border-gray-700">
    <LMap
      v-if="isReady"
      ref="map"
      :zoom="zoom"
      :center="[lat, lon]" 
      :use-global-leaflet="false"
      :options="{ scrollWheelZoom: false, dragging: false, zoomControl: false }"
    >
      <LTileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution=""
        layer-type="base"
        name="OpenStreetMap"
      />

      <LMarker 
        :lat-lng="[lat, lon]"
        :icon="getPlaneIcon()"
      />
    </LMap>
    <div v-else class="h-full w-full flex items-center justify-center bg-gray-100 dark:bg-gray-800 text-gray-400">
      Chargement de la carte...
    </div>
  </div>
</template>