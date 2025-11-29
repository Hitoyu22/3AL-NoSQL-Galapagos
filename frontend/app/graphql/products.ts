import { gql } from "nuxt-graphql-request/utils";
import type { Product } from "./types";

const GET_PRODUCTS = gql`
  query GetProducts {
    products {
      id
      name
      description
      stockAvailable
      weightKg
      unitPrice
    }
  }
`;

export const productService = {
  async getAll(clientGql: any): Promise<Product[]> {
    const data = await clientGql.request(GET_PRODUCTS);
    return data.products.map((p: any) => ({
      ...p,
      price: p.unitPrice,
    }));
  },
};
