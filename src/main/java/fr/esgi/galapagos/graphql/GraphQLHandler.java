package fr.esgi.galapagos.graphql;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GraphQLHandler implements HttpHandler {

    private final GraphQL graphQL;
    private final Gson gson = new Gson();

    public GraphQLHandler(GraphQL graphQL) {
        this.graphQL = graphQL;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        TypeToken<Map<String, Object>> typeToken = new TypeToken<>() {};
        Map<String, Object> request = gson.fromJson(body, typeToken.getType());

        if (request == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        String query = (String) request.get("query");
        if (query == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.get("variables");

        ExecutionInput.Builder executionInput = ExecutionInput.newExecutionInput().query(query);
        if (variables != null) {
            executionInput.variables(variables);
        }

        ExecutionResult result = graphQL.execute(executionInput.build());
        String response = gson.toJson(result.toSpecification());

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}