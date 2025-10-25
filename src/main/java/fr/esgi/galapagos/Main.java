package fr.esgi.galapagos;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
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
            GraphQL graphQL = GraphQL.newGraphQL(new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring)).build();

            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/graphql", exchange -> handleGraphQLRequest(exchange, graphQL));
            server.setExecutor(null);
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nArrêt du serveur...");
                server.stop(0);
            }));

            System.out.println("Serveur GraphQL prêt sur http://localhost:8080/graphql");
            System.out.println("Appuyez sur Ctrl+C pour arrêter le serveur");

            Thread.currentThread().join();

        } catch (InterruptedException e) {
            System.out.println("Serveur interrompu");
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
        }
    }

    private static void handleGraphQLRequest(HttpExchange exchange, GraphQL graphQL) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Gson gson = new Gson();
        TypeToken<Map<String, Object>> typeToken = new TypeToken<>() {};
        Map<String, Object> request = gson.fromJson(body, typeToken.getType());
        String query = (String) request.get("query");

        ExecutionResult result = graphQL.execute(ExecutionInput.newExecutionInput().query(query).build());
        String response = gson.toJson(result.toSpecification());

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}