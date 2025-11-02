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
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
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