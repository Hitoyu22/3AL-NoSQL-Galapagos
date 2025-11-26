package fr.esgi.galapagos.service;

import fr.esgi.galapagos.config.Neo4jConnection;
import fr.esgi.galapagos.model.neo4j.Port;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortService {

    private final Driver driver;

    public PortService() {
        this.driver = Neo4jConnection.getDriver();
    }

    public List<Port> getPorts(Integer id, String name, String islandName) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                StringBuilder query = new StringBuilder("MATCH (p:Port)-[:HAS_PORT]-(i:Island) ");
                Map<String, Object> params = new HashMap<>();
                boolean whereAdded = false;

                if (id != null) {
                    query.append("WHERE p.id = $id ");
                    params.put("id", id);
                    whereAdded = true;
                }
                if (name != null) {
                    query.append(whereAdded ? "AND " : "WHERE ");
                    query.append("p.name = $name ");
                    params.put("name", name);
                    whereAdded = true;
                }
                if (islandName != null) {
                    query.append(whereAdded ? "AND " : "WHERE ");
                    query.append("toLower(i.name) CONTAINS toLower($islandName) ");
                    params.put("islandName", islandName);
                }

                query.append("RETURN p, i.name as islandName");

                Result result = tx.run(query.toString(), params);

                return result.list(record -> {
                    Node pNode = record.get("p").asNode();
                    String iName = record.get("islandName").asString();

                    return new Port(
                            pNode.get("id").asInt(),
                            pNode.get("name").asString(),
                            iName,
                            pNode.get("lat").asDouble(),
                            pNode.get("lon").asDouble()
                    );
                });
            });
        }
    }
}