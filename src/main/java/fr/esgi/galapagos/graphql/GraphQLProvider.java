package fr.esgi.galapagos.graphql;

import fr.esgi.galapagos.helper.*;
import fr.esgi.galapagos.helper.BoxHelper.BoxInput;
import fr.esgi.galapagos.helper.ClientHelper.ClientInput;
import fr.esgi.galapagos.helper.SeaplaneHelper.SeaplaneInput;
import fr.esgi.galapagos.model.neo4j.Port;
import fr.esgi.galapagos.service.*;
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
    private static final ProductService productService = new ProductService();
    private static final OrderService orderService = new OrderService();
    private static final PortService portService = new PortService();
    private static final LockerService lockerService = new LockerService();

    public static GraphQL createGraphQL() {

        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        List<String> schemaFiles = List.of(
                "graphql/root.graphqls",
                "graphql/island.graphqls",
                "graphql/seaplane.graphqls",
                "graphql/box.graphqls",
                "graphql/client.graphqls",
                "graphql/product.graphqls",
                "graphql/order.graphqls",
                "graphql/port.graphqls",
                "graphql/locker.graphqls"
        );

        for (String schemaFile : schemaFiles) {
            typeRegistry.merge(schemaParser.parse(loadSchema(schemaFile)));
        }

        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("version", env -> "1.0.0")

                        .dataFetcher("islands", env -> {
                            Integer id = env.getArgument("id");
                            String name = env.getArgument("name");
                            return islandService.getIslands(id, name);
                        })

                        .dataFetcher("seaplanes", env -> {
                            String id = env.getArgument("id");
                            return seaplaneService.getSeaplanes(id);
                        })

                        .dataFetcher("products", env -> productService.getProducts(
                            env.getArgument("id"), 
                            env.getArgument("name")))
                        
                        .dataFetcher("orders", env -> {
                            String statusStr = env.getArgument("status");
                            return orderService.getOrders(
                                    env.getArgument("id"),
                                    env.getArgument("clientId"),
                                    statusStr != null ? fr.esgi.galapagos.model.enums.OrderStatus.valueOf(statusStr) : null
                            );
                        })

                        .dataFetcher("ports", env -> {
                            Integer id = env.getArgument("id");
                            String name = env.getArgument("name");
                            String islandName = env.getArgument("islandName");

                            List<Port> ports = portService.getPorts(id, name, islandName);

                            for (Port port : ports) {
                                if (port != null && port.getId() != null) {
                                    port.setLockers(lockerService.getLockers(port.getId(), null));
                                }
                            }

                            return ports;
                        })

                        .dataFetcher("lockers", env -> {
                            Integer portId = env.getArgument("portId");
                            String status = env.getArgument("status");
                            return lockerService.getLockers(portId, status);
                        })

                        .dataFetcher("boxes", env -> {
                            String id = env.getArgument("id");
                            String orderId = env.getArgument("orderId");
                            String clientId = env.getArgument("clientId");
                            String status = env.getArgument("status");
                            return boxService.getBoxes(id, orderId, clientId, status);
                        })

                        .dataFetcher("clients", env -> {
                            String id = env.getArgument("id");
                            String name = env.getArgument("name");
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
                                    input.fuelConsumptionKm(), input.cruiseSpeedKmh(), input.status(), input.portId()
                            );
                        })

                        .dataFetcher("updateSeaplane", environment ->
                                seaplaneService.updateSeaplane(
                                        environment.getArgument("id"),
                                        environment.getArgument("model"),
                                        environment.getArgument("boxCapacity"),
                                        environment.getArgument("fuelConsumptionKm"),
                                        environment.getArgument("cruiseSpeedKmh"),
                                        environment.getArgument("status")
                                )
                        )

                        .dataFetcher("deleteSeaplane", env -> {
                            String id = env.getArgument("id");
                            return seaplaneService.deleteSeaplane(id);
                        })

                        .dataFetcher("assignFlight", env -> {
                            String id = env.getArgument("seaplaneId");
                            String dep = env.getArgument("departurePort");
                            String arr = env.getArgument("arrivalPort");
                            return seaplaneService.assignFlight(id, dep, arr);
                        })

                        .dataFetcher("addLocker", env -> {
                            Integer portId = env.getArgument("portId");
                            if (portId == null) throw new IllegalArgumentException("Le paramètre portId est obligatoire.");
                            return lockerService.addLocker(portId);
                        })

                        .dataFetcher("updateLockerStatus", env ->
                                lockerService.updateLockerStatus(
                                        env.getArgument("id"),
                                        env.getArgument("status"),
                                        env.getArgument("maintenanceReason")
                                )
                        )

                        .dataFetcher("deleteLocker", env -> lockerService.deleteLocker(env.getArgument("id")))

                        .dataFetcher("createClient", env ->
                                clientService.createClient(
                                        env.getArgument("name"),
                                        env.getArgument("type"),
                                        env.getArgument("specialty"),
                                        env.getArgument("study"),
                                        env.getArgument("email")
                                )
                        )
                        
                        .dataFetcher("createProduct", env -> {
                            var input = ProductHelper.extractProductInput(env);
                            return productService.createProduct(input.name(), input.description(), input.stockAvailable(), input.weightKg(), input.unitPrice());
                        })
                        .dataFetcher("updateProduct", env -> {
                            var input = ProductHelper.extractProductInput(env);
                            return productService.updateProduct(input.id(), input.name(), input.description(), input.stockAvailable(), input.weightKg(), input.unitPrice());
                        })
                        .dataFetcher("deleteProduct", env -> productService.deleteProduct(env.getArgument("id")))

                        .dataFetcher("updateClient", env ->
                                clientService.updateClient(
                                        env.getArgument("id"),
                                        env.getArgument("name"),
                                        env.getArgument("type"),
                                        env.getArgument("specialty"),
                                        env.getArgument("study"),
                                        env.getArgument("email")
                                )
                        )

                        .dataFetcher("deleteClient", env ->
                                clientService.deleteClient(env.getArgument("id"))
                        )

                        .dataFetcher("createBox", env ->
                                boxService.createBox(
                                        env.getArgument("orderId"),
                                        env.getArgument("clientId"),
                                        env.getArgument("number"),
                                        env.getArgument("status"),
                                        env.getArgument("content")
                                )
                        )

                        .dataFetcher("updateBox", env ->
                                boxService.updateBox(
                                        env.getArgument("id"),
                                        env.getArgument("orderId"),
                                        env.getArgument("clientId"),
                                        env.getArgument("number"),
                                        env.getArgument("status"),
                                        env.getArgument("content")
                                )
                        )
                        .dataFetcher("createOrder", env -> {
                            var input = OrderHelper.extractOrderInput(env);
                            var products = input.products().stream()
                                .map(p -> new fr.esgi.galapagos.model.mongodb.Order.OrderedProduct(new org.bson.types.ObjectId(p.productId()), p.quantity()))
                                .collect(java.util.stream.Collectors.toList());

                        .dataFetcher("deleteBox", env ->
                                boxService.deleteBox(env.getArgument("id"))
                        )
                            return orderService.createOrder(
                                    input.clientId(), input.priority(), input.deliveryPort(),
                                    products, input.boxCount(), input.totalWeightKg()
                            );
                        })
                        .dataFetcher("updateOrderStatus", env -> {
                            String statusStr = env.getArgument("status");
                            return orderService.updateStatus(env.getArgument("id"), fr.esgi.galapagos.model.enums.OrderStatus.valueOf(statusStr));
                        })
                        .dataFetcher("deleteOrder", env -> orderService.deleteOrder(env.getArgument("id")))
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
