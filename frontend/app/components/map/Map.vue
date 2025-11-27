<template>
  <div class="h-full w-full"> 
    <LMap
      ref="map"
      :zoom="zoom"
      :center="[-0.7, -90.5]" 
      :use-global-leaflet="false"
    >
      <LTileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution=""
        layer-type="base"
        name="OpenStreetMap"
      />

      <template v-if="isReady">
        <template v-if="activeFilters.includes('Ports & Entrepôts')">
          <LMarker 
            v-for="port in ports" 
            :key="port.name"
            :lat-lng="[port.lat, port.lon]"
            :icon="getPortIcon(port)"
          >
             <LPopup>
                <div class="flex flex-col gap-2 min-w-[150px]">
                    <div>
                        <div class="font-bold text-sm text-gray-900">{{ port.name }}</div>
                        <div class="text-xs text-gray-500">{{ port.island }}</div>
                    </div>
                    
                    <div class="space-y-1">
                        <div class="flex justify-between text-xs">
                            <span class="text-gray-500">Type:</span>
                            <span class="font-medium">{{ port.isWarehouse ? 'Entrepôt' : 'Port' }}</span>
                        </div>
                        <div class="flex justify-between text-xs">
                            <span class="text-gray-500">Casiers:</span>
                            <span class="font-medium">{{ port.lockers }}</span>
                        </div>
                    </div>

                    <UButton 
                        size="2xs" 
                        color="gray" 
                        variant="solid" 
                        block
                        icon="i-lucide-arrow-right"
                        :to="`/ports/${port.id}`"
                    >
                        Voir le stock
                    </UButton>
                </div>
             </LPopup>
          </LMarker>
        </template>

        <template v-if="activeFilters.includes('Hydravions')">
          <LMarker 
            v-for="plane in processedPlanes" 
            :key="plane.id"
            :lat-lng="[plane.displayLat, plane.displayLon]"
            :icon="getPlaneIcon(plane)"
          >
             <LPopup>
                <div class="flex flex-col gap-2 min-w-[150px]">
                    <div>
                        <div class="font-bold text-sm text-gray-900">{{ plane.id }}</div>
                        <div class="text-xs text-gray-500">{{ plane.model }}</div>
                    </div>

                    <div class="space-y-1">
                        <div class="flex justify-between text-xs items-center">
                            <span class="text-gray-500">Statut:</span>
                            <span class="font-bold" :class="getStatusTextColor(plane.status)">
                                {{ plane.status }}
                            </span>
                        </div>
                        <div class="text-xs text-gray-500 mt-1">
                            {{ plane.location }}
                        </div>
                    </div>
                </div>
             </LPopup>
          </LMarker>
        </template>

        <template v-if="activeFilters.includes('Itinéraires')">
            <LPolyline
                v-for="(route, index) in planeRoutes"
                :key="index"
                :lat-lngs="route.path"
                color="#3b82f6"
                :weight="2"
                :opacity="0.6"
                dash-array="5, 10"
            />
        </template>
      </template>

    </LMap>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
    ports: { type: Array, default: () => [] },
    planes: { type: Array, default: () => [] },
    activeFilters: { type: Array, default: () => [] }
})

const zoom = ref(9)
const map = ref(null)
const L = ref(null)
const isReady = ref(false)

const processedPlanes = computed(() => {
    const locationCounts = {}
    return props.planes.map(plane => {
        let lat = plane.lat
        let lon = plane.lon
        const locKey = `${lat},${lon}`

        if (locationCounts[locKey]) {
            locationCounts[locKey]++
            const count = locationCounts[locKey]
            lat = lat + (Math.cos(count) * 0.0005 * count) + (Math.random() * 0.01 - 0.005)
            lon = lon + (Math.sin(count) * 0.0005 * count) + (Math.random() * 0.01 - 0.005)
        } else {
            locationCounts[locKey] = 1
        }
        return { ...plane, displayLat: lat, displayLon: lon }
    })
})

const planeRoutes = computed(() => {
    return props.planes.filter(p => p.route).map(plane => {
        let destLat, destLon
        if(plane.location.includes("Villamil")) { destLat = -0.9569; destLon = -90.9672 } 
        else if (plane.location.includes("Darwin")) { destLat = 0.3205; destLon = -89.9558 }

        if (destLat && destLon) {
            return { path: [[plane.lat, plane.lon], [destLat, destLon]] }
        }
        return null
    }).filter(Boolean)
})

onMounted(async () => {
  if (process.client) {
    const leaflet = await import('leaflet')
    L.value = leaflet.default || leaflet
    isReady.value = true
  }
})


const getPortIcon = (port) => {
    if (!L.value) return undefined
    
    const iconContent = port.isWarehouse ? '🏭' : '⚓'
    
    return L.value.divIcon({
        className: 'bg-transparent border-0',
        html: `<div style="font-size: 28px; line-height: 1; text-shadow: 2px 2px 0px white, -1px -1px 0 white; cursor: pointer;">${iconContent}</div>`,
        iconSize: [30, 30],
        iconAnchor: [15, 15],
        popupAnchor: [0, -10]
    })
}

const getPlaneIcon = (plane) => {
    if (!L.value) return undefined

    const rotation = plane.status === 'IN_FLIGHT' ? 'transform: rotate(-45deg); display:inline-block;' : ''
    
    return L.value.divIcon({
        className: 'bg-transparent border-0',
        html: `<div style="font-size: 24px; line-height: 1; text-shadow: 0px 0px 3px white; ${rotation} cursor: pointer;">✈️</div>`,
        iconSize: [30, 30],
        iconAnchor: [15, 15],
        popupAnchor: [0, -10]
    })
}

const getStatusTextColor = (status) => {
    switch(status) {
        case 'AVAILABLE': return 'text-green-600';
        case 'MAINTENANCE': return 'text-orange-600';
        case 'IN_FLIGHT': return 'text-blue-600';
        case 'AT_PORT': return 'text-indigo-600';
        default: return 'text-gray-600';
    }
}
</script>