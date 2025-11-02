package fr.esgi.galapagos.graphql;

import graphql.GraphQL;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class GraphQLProvider {

    public static GraphQL createGraphQL() {
        String schema = """
                type Query {
                    hello: String
                }
                """;

        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("hello", new StaticDataFetcher("Bonjour depuis GraphQL Java")))
                .build();

        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schema);
        return GraphQL.newGraphQL(new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring)).build();
    }
}