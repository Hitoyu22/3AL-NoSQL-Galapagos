package fr.esgi.galapagos.helper;

import graphql.schema.DataFetchingEnvironment;

public class ClientHelper {

    public record ClientInput(
            String id,
            String name,
            String type,
            String specialty,
            String study,
            String email
    ) {}

    public static ClientInput extractClientInput(DataFetchingEnvironment env) {
        return new ClientInput(
                env.getArgument("id"),
                env.getArgument("name"),
                env.getArgument("type"),
                env.getArgument("specialty"),
                env.getArgument("study"),
                env.getArgument("email")
        );
    }
}