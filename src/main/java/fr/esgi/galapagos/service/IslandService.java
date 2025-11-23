package fr.esgi.galapagos.service;

import fr.esgi.galapagos.config.Neo4jConnection;
import fr.esgi.galapagos.model.neo4j.Island;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;

import java.util.List;
import java.util.Map;

public class IslandService {

    private final Driver driver;

    public IslandService() {
        this.driver = Neo4jConnection.getDriver();
    }

    public List<Island> getIslands(String name) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                String query;
                Map<String, Object> params;

                if (name != null) {
                    query = "MATCH (i:Island {name: $name}) RETURN i";
                    params = Map.of("name", name);
                } else {
                    query = "MATCH (i:Island) RETURN i";
                    params = Map.of();
                }

                Result result = tx.run(query, params);

                return result.list(record -> {
                    Node node = record.get("i").asNode();
                    return new Island(
                            node.get("name").asString(),
                            node.get("lat").asDouble(),
                            node.get("lon").asDouble(),
                            node.get("area_km2").asDouble()
                    );
                });
            });
        }
    }
}