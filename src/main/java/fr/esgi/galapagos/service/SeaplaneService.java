package fr.esgi.galapagos.service;

import com.mongodb.client.MongoDatabase;
import fr.esgi.galapagos.config.MongoConnection;
import fr.esgi.galapagos.config.Neo4jConnection;
import fr.esgi.galapagos.model.enums.DeliveryStatus;
import fr.esgi.galapagos.model.enums.SeaplaneStatus;
import fr.esgi.galapagos.model.neo4j.Island;
import fr.esgi.galapagos.model.neo4j.Location;
import fr.esgi.galapagos.model.neo4j.Seaplane;
import org.bson.Document;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeaplaneService {

    private final Driver driver;
    private final MongoDatabase mongoDatabase;

    public SeaplaneService() {
        this.driver = Neo4jConnection.getDriver();
        this.mongoDatabase = MongoConnection.getDatabase();
    }

    public Seaplane createSeaplane(String id, String model, int boxCapacity, double fuelConsumptionKm, double cruiseSpeedKmh, String statusStr) {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                String query = "CREATE (s:Seaplane {id: $id, model: $model, box_capacity: $boxCapacity, fuel_consumption_km: $fuelConsumptionKm, cruise_speed_kmh: $cruiseSpeedKmh, status: $status}) RETURN s";
                SeaplaneStatus status = SeaplaneStatus.valueOf(statusStr);
                Map<String, Object> params = Map.of(
                        "id", id, "model", model, "boxCapacity", boxCapacity,
                        "fuelConsumptionKm", fuelConsumptionKm, "cruiseSpeedKmh", cruiseSpeedKmh, "status", status.name()
                );
                Result result = tx.run(query, params);
                if (result.hasNext()) {
                    return mapRecordToSeaplane(result.next().get("s").asNode(), null, null);
                }
                return null;
            });
        }
    }

    public List<Seaplane> getSeaplanes(String id) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                String query = "MATCH (s:Seaplane) " +
                        (id != null ? "WHERE s.id = $id " : "") +
                        "OPTIONAL MATCH (s)-[:STATIONED_AT]->(p:Port)<-[:HAS_PORT]-(i:Island) " +
                        "RETURN s, p, i";

                Map<String, Object> params = id != null ? Map.of("id", id) : Map.of();
                Result result = tx.run(query, params);

                return result.list(record -> {
                    Node sNode = record.get("s").asNode();
                    Node pNode = record.get("p").isNull() ? null : record.get("p").asNode();
                    Node iNode = record.get("i").isNull() ? null : record.get("i").asNode();
                    return mapRecordToSeaplane(sNode, pNode, iNode);
                });
            });
        }
    }

    public Seaplane updateSeaplane(String id, String model, Integer boxCapacity, Double fuelConsumptionKm, Double cruiseSpeedKmh, String statusStr) {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Result checkRes = tx.run("MATCH (s:Seaplane {id: $id}) RETURN s.status as status", Map.of("id", id));
                if (!checkRes.hasNext()) throw new RuntimeException("Hydravion introuvable");
                String currentStatus = checkRes.next().get("status").asString();

                if ("IN_FLIGHT".equals(currentStatus) && statusStr != null && !statusStr.equalsIgnoreCase("IN_FLIGHT")) {
                    throw new RuntimeException("Impossible de modifier le statut : l’hydravion est en vol");
                }

                StringBuilder query = new StringBuilder("MATCH (s:Seaplane {id: $id}) SET ");
                Map<String, Object> params = new HashMap<>();
                params.put("id", id);

                if (model != null) { query.append("s.model = $model, "); params.put("model", model); }
                if (boxCapacity != null) { query.append("s.box_capacity = $cap, "); params.put("cap", boxCapacity); }
                if (fuelConsumptionKm != null) { query.append("s.fuel_consumption_km = $fuel, "); params.put("fuel", fuelConsumptionKm); }
                if (cruiseSpeedKmh != null) { query.append("s.cruise_speed_kmh = $speed, "); params.put("speed", cruiseSpeedKmh); }
                if (statusStr != null) { query.append("s.status = $status, "); params.put("status", statusStr.toUpperCase()); }

                query.setLength(query.length() - 2);

                query.append(" WITH s OPTIONAL MATCH (s)-[:STATIONED_AT]->(p:Port)<-[:HAS_PORT]-(i:Island) ");
                query.append("RETURN s, p, i");

                Result result = tx.run(query.toString(), params);

                if (result.hasNext()) {
                    var record = result.next();
                    return mapRecordToSeaplane(
                            record.get("s").asNode(),
                            record.get("p").isNull() ? null : record.get("p").asNode(),
                            record.get("i").isNull() ? null : record.get("i").asNode()
                    );
                }
                return null;
            });
        }
    }

    public boolean deleteSeaplane(String id) {
        long activeDeliveries = mongoDatabase.getCollection("deliveries").countDocuments(
                new Document("seaplane_id", id)
                        .append("status", DeliveryStatus.IN_PROGRESS.name().toLowerCase())
        );

        if (activeDeliveries > 0) {
            throw new RuntimeException("Impossible de supprimer l’hydravion : des livraisons sont actuellement en cours.");
        }

        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Result res = tx.run("MATCH (s:Seaplane {id: $id})-[r:FLYING_TO|FLYING_FROM]-() RETURN count(r) as cnt", Map.of("id", id));
                if (res.hasNext() && res.next().get("cnt").asInt() > 0) {
                    throw new RuntimeException("Impossible de supprimer l’hydravion : il est associé à des vols.");
                }

                tx.run("MATCH (s:Seaplane {id: $id}) DETACH DELETE s", Map.of("id", id));
                return true;
            });
        }
    }

    public Seaplane assignFlight(String seaplaneId, String departurePort, String arrivalPort) {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                Result check = tx.run("MATCH (s:Seaplane {id: $id}) RETURN s.status as status", Map.of("id", seaplaneId));
                if (!check.hasNext()) throw new RuntimeException("Hydravion introuvable");

                String status = check.next().get("status").asString();
                if ("MAINTENANCE".equals(status) || "IN_FLIGHT".equals(status)) {
                    throw new RuntimeException("Impossible d’assigner un vol à l’hydravion");
                }

                String query =
                        "MATCH (s:Seaplane {id: $id}), (pDep:Port {name: $dep}), (pArr:Port {name: $arr}) " +
                                "SET s.status = 'IN_FLIGHT' " +
                                "OPTIONAL MATCH (s)-[r:STATIONED_AT]->() DELETE r " +
                                "CREATE (s)-[:FLYING_FROM]->(pDep) " +
                                "CREATE (s)-[:FLYING_TO]->(pArr) " +
                                "RETURN s";

                Result res = tx.run(query, Map.of("id", seaplaneId, "dep", departurePort, "arr", arrivalPort));

                if (res.hasNext()) {
                    return mapRecordToSeaplane(res.next().get("s").asNode(), null, null);
                }
                throw new RuntimeException("Impossible d’assigner un vol à l’hydravion");
            });
        }
    }

    private Seaplane mapRecordToSeaplane(Node sNode, Node pNode, Node iNode) {
        Seaplane seaplane = new Seaplane(
                sNode.get("id").asString(),
                sNode.get("model").asString(),
                sNode.get("box_capacity").asInt(),
                sNode.get("fuel_consumption_km").asDouble(),
                sNode.get("cruise_speed_kmh").asDouble(),
                SeaplaneStatus.valueOf(sNode.get("status").asString().toUpperCase())
        );

        if (pNode != null && iNode != null) {
            Island island = new Island(
                    iNode.get("id").asInt(),
                    iNode.get("name").asString(),
                    iNode.get("lat").asDouble(),
                    iNode.get("lon").asDouble(),
                    iNode.get("area_km2").asDouble()
            );
            Location loc = new Location(
                    pNode.get("name").asString(),
                    island,
                    pNode.get("lat").asDouble(),
                    pNode.get("lon").asDouble()
            );
            seaplane.setCurrentLocation(loc);
        }
        return seaplane;
    }
}