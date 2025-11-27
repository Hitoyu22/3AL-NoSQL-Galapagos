package fr.esgi.galapagos.graphql;

import fr.esgi.galapagos.helper.SeaplaneHelper;
import fr.esgi.galapagos.helper.SeaplaneHelper.SeaplaneInput;
import fr.esgi.galapagos.service.IslandService;
import fr.esgi.galapagos.service.SeaplaneService;
import fr.esgi.galapagos.service.BoxService;
import fr.esgi.galapagos.service.ClientService;
import graphql.GraphQL;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GraphQLProvider {

    private static final IslandService islandService = new IslandService();
    private static final SeaplaneService seaplaneService = new SeaplaneService();
    private static final BoxService boxService = new BoxService();
    private static final ClientService clientService = new ClientService();

    public static GraphQL createGraphQL() {

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        List<String> schemaFiles = List.of(
                "graphql/root.graphqls",
                "graphql/island.graphqls",
                "graphql/seaplane.graphqls",
                "graphql/box.graphqls",
                "graphql/client.graphqls"
        );

        for (String schemaFile : schemaFiles) {
            typeRegistry.merge(schemaParser.parse(loadSchema(schemaFile)));
        }

        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("version", env -> "1.0.0")
                        .dataFetcher("islands", environment -> {
                            Integer id = environment.getArgument("id");
                            String name = environment.getArgument("name");
                            return islandService.getIslands(id, name);
                        })
                        .dataFetcher("seaplanes", environment -> {
                            String id = environment.getArgument("id");
                            return seaplaneService.getSeaplanes(id);
                        })
                        .dataFetcher("boxes", environment -> {
                            String id = environment.getArgument("id");
                            String orderId = environment.getArgument("orderId");
                            String clientId = environment.getArgument("clientId");
                            String status = environment.getArgument("status");
                            return boxService.getBoxes(id, orderId, clientId, status);
                        })
                        .dataFetcher("clients", environment -> {
                            String id = environment.getArgument("id");
                            String name = environment.getArgument("name");
                            return clientService.getClients(id, name);
                        })
                )
                .type("Mutation", builder -> builder
                        .dataFetcher("createSeaplane", environment -> {
                            SeaplaneInput input = SeaplaneHelper.extractSeaplaneInput(environment);
                            if (input.boxCapacity() == null || input.fuelConsumptionKm() == null || input.cruiseSpeedKmh() == null) {
                                throw new IllegalArgumentException("Les arguments boxCapacity, fuelConsumptionKm et cruiseSpeedKmh sont obligatoires.");
                            }
                            return seaplaneService.createSeaplane(
                                    input.id(), input.model(), input.boxCapacity(),
                                    input.fuelConsumptionKm(), input.cruiseSpeedKmh(), input.status()
                            );
                        })
                        .dataFetcher("updateSeaplane", environment -> {
                            SeaplaneInput input = SeaplaneHelper.extractSeaplaneInput(environment);
                            return seaplaneService.updateSeaplane(
                                    input.id(), input.model(), input.boxCapacity(),
                                    input.fuelConsumptionKm(), input.cruiseSpeedKmh(), input.status()
                            );
                        })
                        .dataFetcher("deleteSeaplane", environment -> {
                            String id = environment.getArgument("id");
                            return seaplaneService.deleteSeaplane(id);
                        })
                        .dataFetcher("assignFlight", environment -> {
                            String id = environment.getArgument("seaplaneId");
                            String dep = environment.getArgument("departurePort");
                            String arr = environment.getArgument("arrivalPort");
                            return seaplaneService.assignFlight(id, dep, arr);
                        })
                        .dataFetcher("createClient", environment -> {
                            return clientService.createClient(
                                    environment.getArgument("name"),
                                    environment.getArgument("type"),
                                    environment.getArgument("specialty"),
                                    environment.getArgument("study"),
                                    environment.getArgument("email")
                            );
                        })
                        .dataFetcher("updateClient", environment -> {
                            return clientService.updateClient(
                                    environment.getArgument("id"),
                                    environment.getArgument("name"),
                                    environment.getArgument("type"),
                                    environment.getArgument("specialty"),
                                    environment.getArgument("study"),
                                    environment.getArgument("email")
                            );
                        })
                        .dataFetcher("deleteClient", environment -> {
                            String id = environment.getArgument("id");
                            return clientService.deleteClient(id);
                        })
                        .dataFetcher("createBox", environment -> {
                            String orderId = environment.getArgument("orderId");
                            String clientId = environment.getArgument("clientId");
                            Integer number = environment.getArgument("number");
                            String status = environment.getArgument("status");
                            String content = environment.getArgument("content");

                            return boxService.createBox(orderId, clientId, number, status, content);
                        })

                        .dataFetcher("updateBox", environment -> {
                            String id = environment.getArgument("id");
                            String orderId = environment.getArgument("orderId");
                            String clientId = environment.getArgument("clientId");
                            Integer number = environment.getArgument("number");
                            String status = environment.getArgument("status");
                            String content = environment.getArgument("content");

                            return boxService.updateBox(id, orderId, clientId, number, status, content);
                        })

                        .dataFetcher("deleteBox", environment -> {
                            String id = environment.getArgument("id");
                            return boxService.deleteBox(id);
                        })
                )
                .build();

        return GraphQL
                .newGraphQL(new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring))
                .build();
    }

    private static String loadSchema(String filename) {
        try (InputStream stream = GraphQLProvider.class.getClassLoader().getResourceAsStream(filename)) {
            if (stream == null) {
                throw new RuntimeException("Fichier de schéma introuvable : " + filename);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du schéma : " + filename, e);
        }
    }
}