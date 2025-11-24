package fr.esgi.galapagos.service;

import fr.esgi.galapagos.config.Neo4jConnection;
import fr.esgi.galapagos.model.enums.SeaplaneStatus;
import fr.esgi.galapagos.model.neo4j.Island;
import fr.esgi.galapagos.model.neo4j.Seaplane;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;

import java.util.List;
import java.util.Map;

public class SeaplaneService {

    private final Driver driver;

    public SeaplaneService() {
        this.driver = Neo4jConnection.getDriver();
    }

    public Seaplane createSeaplane(String id, String model, int boxCapacity, double fuelConsumptionKm, double cruiseSpeedKmh, String statusStr) {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                String query = "CREATE (s:Seaplane {id: $id, model: $model, box_capacity: $boxCapacity, fuel_consumption_km: $fuelConsumptionKm, cruise_speed_kmh: $cruiseSpeedKmh, status: $status}) RETURN s";

                SeaplaneStatus status = SeaplaneStatus.valueOf(statusStr);

                Map<String, Object> params = Map.of(
                        "id", id,
                        "model", model,
                        "boxCapacity", boxCapacity,
                        "fuelConsumptionKm", fuelConsumptionKm,
                        "cruiseSpeedKmh", cruiseSpeedKmh,
                        "status", status.name()
                );

                Result result = tx.run(query, params);

                if (result.hasNext()) {
                    Node node = result.next().get("s").asNode();
                    return new Seaplane(
                            node.get("id").asString(),
                            node.get("model").asString(),
                            node.get("box_capacity").asInt(),
                            node.get("fuel_consumption_km").asDouble(),
                            node.get("cruise_speed_kmh").asDouble(),
                            SeaplaneStatus.valueOf(node.get("status").asString())
                    );
                }
                return null;
            });
        }
    }

    public List<Seaplane> getSeaplanes(String id) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                String query;
                Map<String, Object> params;

                if (id != null) {
                    query = "MATCH (s:Seaplane {id: $id}) RETURN s";
                    params = Map.of("id", id);
                } else {
                    query = "MATCH (s:Seaplane) RETURN s";
                    params = Map.of();
                }

                Result result = tx.run(query, params);

                return result.list(record -> {
                    Node node = record.get("s").asNode();
                    return new Seaplane(
                            node.get("id").asString(),
                            node.get("model").asString(),
                            node.get("box_capacity").asInt(),
                            node.get("fuel_consumption_km").asDouble(),
                            node.get("cruise_speed_kmh").asDouble(),
                            SeaplaneStatus.valueOf(node.get("status").asString().toUpperCase())
                    );
                });
            });
        }
    }
}