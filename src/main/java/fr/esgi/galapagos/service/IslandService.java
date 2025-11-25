package fr.esgi.galapagos.service;

import fr.esgi.galapagos.config.Neo4jConnection;
import fr.esgi.galapagos.model.neo4j.Island;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class IslandService {

    private final Driver driver;

    public IslandService() {
        this.driver = Neo4jConnection.getDriver();
    }

    public List<Island> getIslands(Integer id, String name) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                StringBuilder query = new StringBuilder("MATCH (i:Island) ");
                Map<String, Object> params = new HashMap<>();

                if (id != null) {
                    query.append("WHERE i.id = $id ");
                    params.put("id", id);
                } else if (name != null) {
                    query.append("WHERE toLower(i.name) CONTAINS toLower($name) ");
                    params.put("name", name);
                }

                query.append("RETURN i");

                Result result = tx.run(query.toString(), params);

                return result.list(record -> {
                    Node node = record.get("i").asNode();
                    return new Island(
                            node.get("id").asInt(),
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