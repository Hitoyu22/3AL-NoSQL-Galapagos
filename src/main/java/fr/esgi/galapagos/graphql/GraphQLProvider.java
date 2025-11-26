package fr.esgi.galapagos.graphql;

import fr.esgi.galapagos.helper.SeaplaneHelper;
import fr.esgi.galapagos.helper.SeaplaneHelper.SeaplaneInput;
import fr.esgi.galapagos.model.neo4j.Port;
import fr.esgi.galapagos.service.IslandService;
import fr.esgi.galapagos.service.LockerService;
import fr.esgi.galapagos.service.PortService;
import fr.esgi.galapagos.service.SeaplaneService;
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
    private static final PortService portService = new PortService();
    private static final LockerService lockerService = new LockerService();

    public static GraphQL createGraphQL() {

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();

        List<String> schemaFiles = List.of(
                "graphql/root.graphqls",
                "graphql/island.graphqls",
                "graphql/seaplane.graphqls",
                "graphql/port.graphqls",
                "graphql/locker.graphqls"
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
                        .dataFetcher("ports", environment -> {
                            Integer id = environment.getArgument("id");
                            String name = environment.getArgument("name");
                            String islandName = environment.getArgument("islandName");

                            List<Port> ports = portService.getPorts(id, name, islandName);

                            for (Port port : ports) {
                                if (port != null && port.getId() != null) {
                                    port.setLockers(lockerService.getLockers(port.getId(), null));
                                }
                            }

                            return ports;
                        })
                        .dataFetcher("lockers", environment -> {
                            Integer portId = environment.getArgument("portId");
                            String status = environment.getArgument("status");
                            return lockerService.getLockers(portId, status);
                        })
                )
                .type("Mutation", builder -> builder
                        .dataFetcher("createSeaplane", environment -> {
                            SeaplaneInput input = SeaplaneHelper.extractSeaplaneInput(environment);

                            if (input.boxCapacity() == null ||
                                    input.fuelConsumptionKm() == null ||
                                    input.cruiseSpeedKmh() == null) {

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
                        .dataFetcher("addLocker", environment -> {
                            Integer portId = environment.getArgument("portId");
                            if (portId == null) {
                                throw new IllegalArgumentException("Le paramètre portId est obligatoire.");
                            }
                            return lockerService.addLocker(portId);
                        })
                        .dataFetcher("updateLockerStatus", environment -> {
                            String id = environment.getArgument("id");
                            String status = environment.getArgument("status");
                            String reason = environment.getArgument("maintenanceReason");
                            return lockerService.updateLockerStatus(id, status, reason);
                        })
                        .dataFetcher("deleteLocker", environment -> {
                            String id = environment.getArgument("id");
                            return lockerService.deleteLocker(id);
                        })
                )
                .type("Port", builder -> builder
                        .dataFetcher("lockers", environment -> {
                            Port port = environment.getSource();
                            if (port == null || port.getId() == null) {
                                return List.of();
                            }
                            return lockerService.getLockers(port.getId(), null);
                        })
                        .dataFetcher("nbLockers", environment -> {
                            Port port = environment.getSource();
                            if (port == null || port.getId() == null) {
                                return 0;
                            }
                            return lockerService.countLockersByPortId(port.getId());
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