import { gql } from "nuxt-graphql-request/utils";
import type { Order } from "./types";

const GET_ORDERS = gql`
  query GetOrders($id: ID, $clientId: ID, $status: OrderStatus) {
    orders(id: $id, clientId: $clientId, status: $status) {
      id
      clientId
      orderDate
      status
      priority
      deliveryPort
      boxCount
      totalWeightKg
      products {
        quantity
        product {
          id
          name
          description
          unitPrice
          weightKg
        }
      }
    }
  }
`;

const CREATE_ORDER = gql`
  mutation CreateOrder(
    $clientId: ID!
    $priority: String!
    $deliveryPort: String!
    $products: [OrderedProductInput!]!
    $boxCount: Int!
    $totalWeightKg: Float!
  ) {
    createOrder(
      clientId: $clientId
      priority: $priority
      deliveryPort: $deliveryPort
      products: $products
      boxCount: $boxCount
      totalWeightKg: $totalWeightKg
    ) {
      id
    }
  }
`;

export const orderService = {
  async getAll(clientGql: any, filters: any = {}): Promise<Order[]> {
    const variables = {
      id: filters.id,
      clientId: filters.clientId,
      status: filters.status,
    };
    const data = await clientGql.request(GET_ORDERS, variables);

    return data.orders.map((o: any) => ({
      id: o.id,
      clientId: o.clientId,
      clientName: "",
      date: o.orderDate,
      status: o.status,
      destinationPort: o.deliveryPort,
      boxCount: o.boxCount,
      totalWeight: o.totalWeightKg,
      items: o.products.map((p: any) => ({
        quantity: p.quantity,
        product: {
          id: p.product.id,
          name: p.product.name,
          description: p.product.description,
          price: p.product.unitPrice,
          weightKg: p.product.weightKg,
        },
      })),
    }));
  },

  async create(clientGql: any, order: Order): Promise<any> {
    const variables = {
      clientId: order.clientId,
      priority: "NORMAL",
      deliveryPort: order.destinationPort,
      boxCount: order.boxCount,
      totalWeightKg: order.totalWeight,
      products: order.items.map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      })),
    };
    const data = await clientGql.request(CREATE_ORDER, variables);
    return data.createOrder;
  },
};
