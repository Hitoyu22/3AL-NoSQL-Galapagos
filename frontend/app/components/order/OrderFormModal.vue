<script setup lang="ts">
import type { Order, Product, Client, Port } from "~/graphql/types";
import { portService } from "~/graphql/ports";
import { clientService } from "~/graphql/clients";
import { productService } from "~/graphql/products";

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits(["update:modelValue", "create"]);

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

const { $graphql } = useNuxtApp();
const gqlClient = $graphql.default;

const { data, pending, error } = useAsyncData(
  "order-form-refs",
  async () => {
    const [ports, clients, products] = await Promise.all([
      portService.getAll(gqlClient),
      clientService.getAll(gqlClient),
      productService.getAll(gqlClient),
    ]);
    return { ports, clients, products };
  },
  {
    lazy: true,
    server: false,
  }
);

const clientOptions = computed(() =>
  (data.value?.clients || []).map((c: Client) => ({
    label: c.name,
    value: c.id,
  }))
);

const portOptions = computed(() =>
  (data.value?.ports || []).map((p: Port) => ({ label: p.name, value: p.name }))
);

const productOptions = computed(() =>
  (data.value?.products || []).map((p: Product) => ({
    label: `${p.name} (Stock: ${p.stockAvailable ?? 0})`,
    value: p.id,
    weight: p.weightKg,
    price: p.price,
    stock: p.stockAvailable ?? 0,
    description: p.description,
  }))
);

const getAvailableProductsForLine = (currentIndex: number) => {
  const selectedIds = state.items
    .map((item, index) => (index !== currentIndex ? item.productId : null))
    .filter(Boolean);

  return productOptions.value.filter((p) => !selectedIds.includes(p.value));
};

interface FormItem {
  productId: string;
  quantity: number;
}

const state = reactive({
  clientId: "",
  destinationPort: "",
  items: [{ productId: "", quantity: 1 }] as FormItem[],
});

const addProductLine = () => {
  state.items.push({ productId: "", quantity: 1 });
};

const removeProductLine = (index: number) => {
  if (state.items.length > 1) {
    state.items.splice(index, 1);
  }
};

const stats = computed(() => {
  let totalWeight = 0;
  let isValidStock = true;

  state.items.forEach((item) => {
    const prod = productOptions.value.find((p) => p.value === item.productId);
    if (prod) {
      totalWeight += prod.weight * item.quantity;
      if (item.quantity > prod.stock) isValidStock = false;
    }
  });

  const boxCount = Math.ceil(totalWeight / 15) || 1;
  return { totalWeight, boxCount, isValidStock };
});

const onSubmit = () => {
  if (!stats.value.isValidStock) return;

  const clients = data.value?.clients || [];
  const productsList = data.value?.products || [];

  const selectedClient = clients.find((c: Client) => c.id === state.clientId);

  const orderItems = state.items
    .filter((item) => item.productId)
    .map((item) => {
      const prodData = productsList.find(
        (p: Product) => p.id === item.productId
      )!;
      return {
        product: prodData,
        quantity: item.quantity,
      };
    });

  if (!selectedClient || orderItems.length === 0 || !state.destinationPort)
    return;

  const newOrder: any = {
    id: `TEMP-${Date.now()}`,
    clientId: state.clientId,
    clientName: selectedClient.name,
    date: new Date().toISOString(),
    status: "PENDING",
    destinationPort: state.destinationPort,
    items: orderItems,
    boxCount: stats.value.boxCount,
    totalWeight: stats.value.totalWeight,
  };

  emit("create", newOrder);

  state.clientId = "";
  state.destinationPort = "";
  state.items = [{ productId: "", quantity: 1 }];
};

const getStockForLine = (productId: string) => {
  const prod = productOptions.value.find((p) => p.value === productId);
  return prod ? prod.stock : 0;
};

const getProductDetails = (productId: string) => {
  return productOptions.value.find((p) => p.value === productId);
};
</script>

<template>
  <UModal v-model:open="isOpen" title="Nouvelle Commande" class="sm:max-w-xl">
    <template #body>
      <UAlert
        v-if="error"
        icon="i-lucide-alert-triangle"
        color="error"
        variant="subtle"
        title="Impossible de charger les données"
        :description="error.message"
        class="mb-4"
      />

      <form v-else class="space-y-4" @submit.prevent="onSubmit">
        <div class="grid grid-cols-2 gap-4">
          <UFormField label="Client" name="client">
            <USelect
              v-model="state.clientId"
              :items="clientOptions"
              :loading="pending"
              option-attribute="label"
              placeholder="Sélectionner..."
              class="w-full"
            />
          </UFormField>

          <UFormField label="Destination" name="destination">
            <USelect
              v-model="state.destinationPort"
              :items="portOptions"
              :loading="pending"
              option-attribute="label"
              placeholder="Port de livraison"
              class="w-full"
            />
          </UFormField>
        </div>

        <USeparator label="Produits" />

        <div
          v-if="pending && productOptions.length === 0"
          class="py-8 flex justify-center"
        >
          <UIcon
            name="i-lucide-loader-2"
            class="animate-spin text-primary w-8 h-8"
          />
        </div>

        <div v-else class="space-y-3 max-h-[300px] overflow-y-auto pr-2">
          <div
            v-for="(item, index) in state.items"
            :key="index"
            class="space-y-1"
          >
            <div class="flex items-start gap-2">
              <div class="grow">
                <USelect
                  v-model="item.productId"
                  :items="getAvailableProductsForLine(index)"
                  :loading="pending"
                  option-attribute="label"
                  placeholder="Sélectionner un produit..."
                  searchable
                  class="w-full"
                />
              </div>

              <div class="w-24">
                <UInput
                  v-model.number="item.quantity"
                  type="number"
                  min="1"
                  :max="getStockForLine(item.productId)"
                  placeholder="Qté"
                  :color="
                    item.quantity > getStockForLine(item.productId)
                      ? 'error'
                      : 'white'
                  "
                />
              </div>

              <UButton
                icon="i-lucide-trash"
                color="error"
                variant="ghost"
                :disabled="state.items.length === 1"
                @click="removeProductLine(index)"
              />
            </div>

            <div
              v-if="item.productId"
              class="text-xs flex justify-between px-1"
            >
              <span class="text-gray-400 truncate max-w-[250px]">
                {{ getProductDetails(item.productId)?.description }}
              </span>
              <span
                :class="
                  item.quantity > getStockForLine(item.productId)
                    ? 'text-red-500 font-bold'
                    : 'text-gray-500'
                "
              >
                Dispo: {{ getStockForLine(item.productId) }}
              </span>
            </div>
          </div>
        </div>

        <UButton
          label="Ajouter un produit"
          icon="i-lucide-plus"
          size="xs"
          variant="soft"
          block
          :disabled="pending"
          @click="addProductLine"
        />

        <div
          class="bg-gray-50 dark:bg-gray-800 p-3 rounded-lg flex justify-between items-center text-sm"
        >
          <div class="text-gray-500">
            Poids total estimé:
            <span class="font-bold text-gray-900 dark:text-white"
              >{{ stats.totalWeight.toFixed(1) }} kg</span
            >
          </div>
          <div class="flex items-center gap-2">
            <UIcon name="i-lucide-box" class="text-primary" />
            <span class="font-bold text-primary"
              >{{ stats.boxCount }} caisse(s)</span
            >
          </div>
        </div>

        <div class="flex justify-end gap-2 mt-6">
          <UButton
            label="Annuler"
            color="neutral"
            variant="ghost"
            @click="isOpen = false"
          />
          <UButton
            type="submit"
            label="Créer la commande"
            color="primary"
            :disabled="!stats.isValidStock || pending"
            :loading="pending"
          />
        </div>
      </form>
    </template>
  </UModal>
</template>
