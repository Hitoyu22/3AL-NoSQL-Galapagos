<script setup lang="ts">
import { h, resolveComponent, ref } from "vue";
import type { TableColumn } from "#ui/types";
import type { Order } from "~/graphql/types";
import { orderService } from "~/graphql/orders";
import { clientService } from "~/graphql/clients";
import OrderFormModal from "~/components/order/OrderFormModal.vue";
import OrderSlideover from "~/components/order/OrderSlideover.vue";

const { $graphql } = useNuxtApp();
const gqlClient = $graphql.default;

const UBadge = resolveComponent("UBadge");
const UButton = resolveComponent("UButton");
const UIcon = resolveComponent("UIcon");
const toast = useToast();

const isModalOpen = ref(false);
const globalFilter = ref("");
const isSlideoverOpen = ref(false);
const selectedOrder = ref<Order | null>(null);

const {
  data: orders,
  pending,
  refresh,
  error,
} = await useAsyncData("orders-list", async () => {
  const [fetchedOrders, fetchedClients] = await Promise.all([
    orderService.getAll(gqlClient),
    clientService.getAll(gqlClient),
  ]);

  return fetchedOrders.map((order) => ({
    ...order,
    clientName:
      fetchedClients.find((c) => c.id === order.clientId)?.name ||
      "Client Inconnu",
  }));
});

const openDetails = (order: Order) => {
  selectedOrder.value = order;
  isSlideoverOpen.value = true;
};

const handleCreate = async (newOrderData: Order) => {
  try {
    await orderService.create(gqlClient, newOrderData);
    toast.add({
      title: "Succès",
      description: `Commande créée avec succès.`,
      color: "success",
    });
    isModalOpen.value = false;

    await refresh();

    await refreshNuxtData("order-form-refs");
  } catch (e) {
    toast.add({
      title: "Erreur",
      description: "Impossible de créer la commande.",
      color: "error",
    });
    console.error(e);
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case "PENDING":
      return "neutral";
    case "IN_TRANSIT":
      return "primary";
    case "DELIVERED":
      return "success";
    case "PARTIALLY_DELIVERED":
      return "warning";
    default:
      return "neutral";
  }
};

const getStatusLabel = (status: string) => {
  switch (status) {
    case "PENDING":
      return "En préparation";
    case "IN_TRANSIT":
      return "En transit";
    case "DELIVERED":
      return "Livrée";
    case "PARTIALLY_DELIVERED":
      return "Partielle";
    default:
      return status;
  }
};

const columns: TableColumn<Order>[] = [
  {
    accessorKey: "id",
    header: "Référence",
    cell: ({ row }) =>
      h("span", { class: "font-mono font-bold text-xs" }, row.getValue("id")),
  },
  {
    accessorKey: "clientName",
    header: "Client",
    cell: ({ row }) =>
      h("div", { class: "font-medium" }, row.getValue("clientName")),
  },
  {
    accessorKey: "destinationPort",
    header: "Destination",
    cell: ({ row }) =>
      h(
        "div",
        { class: "flex items-center gap-1 text-gray-600 dark:text-gray-400" },
        [
          h(UIcon, { name: "i-lucide-map-pin", class: "w-3 h-3" }),
          row.getValue("destinationPort") as string,
        ]
      ),
  },
  {
    accessorKey: "status",
    header: "Statut",
    cell: ({ row }) =>
      h(
        UBadge,
        {
          color: getStatusColor(row.getValue("status")),
          variant: "subtle",
          class: "capitalize",
        },
        () => getStatusLabel(row.getValue("status"))
      ),
  },
  {
    header: "Logistique",
    cell: ({ row }) => {
      const boxes = row.original.boxCount;
      const weight = row.original.totalWeight.toFixed(1);
      return h(
        "div",
        { class: "text-xs text-gray-500" },
        `${boxes} caisse(s) • ${weight} kg`
      );
    },
  },
  {
    id: "actions",
    header: "",
    cell: ({ row }) =>
      h(UButton, {
        icon: "i-lucide-eye",
        variant: "ghost",
        color: "neutral",
        size: "xs",
        onClick: () => openDetails(row.original),
      }),
  },
];
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold">Commandes</h1>
      <div class="flex gap-2">
        <UButton
          icon="i-lucide-refresh-cw"
          color="gray"
          variant="ghost"
          :loading="pending"
          @click="refresh"
        />
        <UButton
          icon="i-lucide-plus"
          label="Nouvelle Commande"
          color="primary"
          @click="isModalOpen = true"
        />
      </div>
    </div>

    <UAlert
      v-if="error"
      icon="i-lucide-alert-triangle"
      color="error"
      title="Erreur API"
      :description="error.message"
    />

    <div
      class="flex px-4 py-3.5 bg-gray-50 dark:bg-gray-800/50 rounded-lg border border-gray-200 dark:border-gray-700"
    >
      <UInput
        v-model="globalFilter"
        class="max-w-sm w-full"
        placeholder="Rechercher une commande, un client..."
        icon="i-lucide-search"
      />
    </div>

    <UTable
      :data="orders || []"
      :columns="columns"
      :loading="pending"
      v-model:global-filter="globalFilter"
      class="bg-white dark:bg-gray-900"
    >
      <template #empty-state>
        <div class="text-center py-10 text-gray-500">
          Aucune commande trouvée.
        </div>
      </template>
    </UTable>

    <OrderFormModal v-model="isModalOpen" @create="handleCreate" />

    <OrderSlideover v-model="isSlideoverOpen" :order="selectedOrder" />
  </div>
</template>
