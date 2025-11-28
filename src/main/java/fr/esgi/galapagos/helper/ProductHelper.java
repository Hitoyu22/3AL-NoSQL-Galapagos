package fr.esgi.galapagos.helper;

import graphql.schema.DataFetchingEnvironment;

public class ProductHelper {
    public record ProductInput(
            String id,
            String name,
            String description,
            Integer stockAvailable,
            Double weightKg,
            Double unitPrice
    ) {}

    public static ProductInput extractProductInput(DataFetchingEnvironment env) {
        return new ProductInput(
                env.getArgument("id"),
                env.getArgument("name"),
                env.getArgument("description"),
                env.getArgument("stockAvailable"),
                env.getArgument("weightKg"),
                env.getArgument("unitPrice")
        );
    }
}