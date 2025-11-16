package fr.esgi.galapagos.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import fr.esgi.galapagos.config.MongoConnection;
import fr.esgi.galapagos.config.Neo4jConnection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.time.LocalDateTime;
import java.util.*;

import static org.neo4j.driver.Values.parameters;

public class DataInitializer {

    private static final List<Map<String, Object>> ISLANDS = Arrays.asList(
            Map.of("name", "Baltra", "lat", -0.4511, "lon", -90.2653, "area_km2", 27),
            Map.of("name", "Bartolomé", "lat", -0.2865, "lon", -90.5521, "area_km2", 1.2),
            Map.of("name", "Darwin", "lat", 1.6781, "lon", -92.0033, "area_km2", 1.1),
            Map.of("name", "Española", "lat", -1.3833, "lon", -89.6167, "area_km2", 60),
            Map.of("name", "Fernandina", "lat", -0.3667, "lon", -91.5500, "area_km2", 642),
            Map.of("name", "Floreana", "lat", -1.2861, "lon", -90.4369, "area_km2", 173),
            Map.of("name", "Genovesa", "lat", 0.3205, "lon", -89.9558, "area_km2", 14),
            Map.of("name", "Isabela", "lat", -0.7167, "lon", -91.1000, "area_km2", 4640),
            Map.of("name", "Marchena", "lat", 0.3333, "lon", -90.4833, "area_km2", 130),
            Map.of("name", "Seymour Nord", "lat", -0.3950, "lon", -90.2881, "area_km2", 1.9),
            Map.of("name", "Pinta", "lat", 0.5833, "lon", -90.7500, "area_km2", 60),
            Map.of("name", "Pinzón", "lat", -0.6111, "lon", -90.6667, "area_km2", 18),
            Map.of("name", "Rábida", "lat", -0.4075, "lon", -90.7125, "area_km2", 4.9),
            Map.of("name", "San Cristóbal", "lat", -0.8333, "lon", -89.4333, "area_km2", 558),
            Map.of("name", "Santa Cruz", "lat", -0.6167, "lon", -90.3500, "area_km2", 986),
            Map.of("name", "Santa Fé", "lat", -0.8111, "lon", -90.0556, "area_km2", 24),
            Map.of("name", "Santiago", "lat", -0.2500, "lon", -90.7667, "area_km2", 585),
            Map.of("name", "Plaza Sud", "lat", -0.5861, "lon", -90.1650, "area_km2", 0.13),
            Map.of("name", "Wolf", "lat", 1.3806, "lon", -91.8000, "area_km2", 1.3)
    );

    private static final List<Map<String, Object>> PORTS = Arrays.asList(
            Map.of("name", "Puerto Baquerizo Moreno", "island", "San Cristóbal", "lat", -0.9025, "lon", -89.609167, "nb_lockers", 10),
            Map.of("name", "Puerto Ayora", "island", "Santa Cruz", "lat", -0.7400, "lon", -90.3117, "nb_lockers", 8),
            Map.of("name", "Puerto Villamil", "island", "Isabela", "lat", -0.9569, "lon", -90.9672, "nb_lockers", 7),
            Map.of("name", "Aéroport de Baltra", "island", "Baltra", "lat", -0.4511, "lon", -90.2653, "nb_lockers", 5),
            Map.of("name", "Puerto Velasco Ibarra", "island", "Floreana", "lat", -1.2975, "lon", -90.4342, "nb_lockers", 5),
            Map.of("name", "Baie de Sullivan", "island", "Santiago", "lat", -0.2916, "lon", -90.5708, "nb_lockers", 6),
            Map.of("name", "Baie Darwin", "island", "Genovesa", "lat", 0.3205, "lon", -89.9558, "nb_lockers", 4),
            Map.of("name", "Punta Espinoza", "island", "Fernandina", "lat", -0.2678, "lon", -91.4422, "nb_lockers", 5),
            Map.of("name", "Punta Suarez", "island", "Española", "lat", -1.3725, "lon", -89.7308, "nb_lockers", 5),
            Map.of("name", "Caleta Tagus", "island", "Isabela", "lat", -0.2514, "lon", -91.3694, "nb_lockers", 5),
            Map.of("name", "Bahía Gardner", "island", "Española", "lat", -1.3450, "lon", -89.6289, "nb_lockers", 4),
            Map.of("name", "Punta Vicente Roca", "island", "Isabela", "lat", 0.0417, "lon", -91.7333, "nb_lockers", 4),
            Map.of("name", "Canal de Itabaca", "island", "Santa Cruz", "lat", -0.4583, "lon", -90.2708, "nb_lockers", 4),
            Map.of("name", "Punta Cormorant", "island", "Floreana", "lat", -1.2333, "lon", -90.4167, "nb_lockers", 3),
            Map.of("name", "Baie de Bartolomé", "island", "Bartolomé", "lat", -0.2833, "lon", -90.5500, "nb_lockers", 2),
            Map.of("name", "Anse Darwin", "island", "Darwin", "lat", 1.6780, "lon", -92.0030, "nb_lockers", 2),
            Map.of("name", "Playa de las Bachas", "island", "Seymour Nord", "lat", -0.3958, "lon", -90.2875, "nb_lockers", 3),
            Map.of("name", "Punta Mangle", "island", "Marchena", "lat", 0.3330, "lon", -90.4830, "nb_lockers", 2),
            Map.of("name", "Cabo Douglas", "island", "Pinta", "lat", 0.5830, "lon", -90.7500, "nb_lockers", 2),
            Map.of("name", "Rocas Pinzón", "island", "Pinzón", "lat", -0.6110, "lon", -90.6660, "nb_lockers", 2),
            Map.of("name", "Playa Roja", "island", "Rábida", "lat", -0.4070, "lon", -90.7120, "nb_lockers", 2),
            Map.of("name", "Baie de Santa Fé", "island", "Santa Fé", "lat", -0.8110, "lon", -90.0550, "nb_lockers", 3),
            Map.of("name", "Quai Plaza Sud", "island", "Plaza Sud", "lat", -0.5860, "lon", -90.1650, "nb_lockers", 2),
            Map.of("name", "Rocher Wolf", "island", "Wolf", "lat", 1.3800, "lon", -91.8000, "nb_lockers", 2)
    );

    private static final List<Map<String, Object>> SEAPLANES = Arrays.asList(
            Map.of("id", "HB-LSR", "model", "DHC-6 Twin Otter", "box_capacity", 100, "fuel_consumption_km", 25.0, "status", "available"),
            Map.of("id", "HB-LSK", "model", "Grumman G-21 Goose", "box_capacity", 50, "fuel_consumption_km", 18.0, "status", "maintenance"),
            Map.of("id", "HB-LSC", "model", "DHC-6 Twin Otter", "box_capacity", 100, "fuel_consumption_km", 25.0, "status", "available"),
            Map.of("id", "HB-LSD", "model", "Cessna 208 Caravan", "box_capacity", 75, "fuel_consumption_km", 20.0, "status", "in_flight"),
            Map.of("id", "HB-LSE", "model", "DHC-3 Otter", "box_capacity", 80, "fuel_consumption_km", 22.0, "status", "available"),
            Map.of("id", "HB-LSF", "model", "Grumman G-73 Mallard", "box_capacity", 60, "fuel_consumption_km", 19.0, "status", "at_port"),
            Map.of("id", "HB-LSG", "model", "DHC-6 Twin Otter", "box_capacity", 100, "fuel_consumption_km", 25.0, "status", "available"),
            Map.of("id", "HB-LSH", "model", "Cessna 208 Caravan", "box_capacity", 75, "fuel_consumption_km", 20.0, "status", "available"),
            Map.of("id", "HB-LSI", "model", "DHC-3 Otter", "box_capacity", 80, "fuel_consumption_km", 22.0, "status", "in_flight"),
            Map.of("id", "HB-LSJ", "model", "Grumman G-21 Goose", "box_capacity", 50, "fuel_consumption_km", 18.0, "status", "available"),
            Map.of("id", "HB-LSN", "model", "Cessna 208 Caravan", "box_capacity", 75, "fuel_consumption_km", 20.0, "status", "available"),
            Map.of("id", "HB-LSO", "model", "Grumman G-73 Mallard", "box_capacity", 60, "fuel_consumption_km", 19.0, "status", "at_port")
    );

    public static void main(String[] args) {
        System.out.println("=== Starting Galápagos data initialization ===");

        try {
            Driver neo4jDriver = Neo4jConnection.getDriver();
            MongoDatabase mongoDatabase = MongoConnection.getDatabase();

            System.out.println("\n[1/4] Clearing databases...");
            clearDatabases(neo4jDriver, mongoDatabase);

            System.out.println("\n[2/4] Populating Neo4j (Graph)...");
            populateNeo4j(neo4jDriver);

            System.out.println("\n[3/4] Populating MongoDB (Business Data)...");
            Map<String, ObjectId> ids = populateMongoDB(mongoDatabase, neo4jDriver);

            System.out.println("\n[4/4] Creating test data (orders and deliveries)...");
            createTestData(mongoDatabase, neo4jDriver, ids);

            System.out.println("\n✅ Initialization finished successfully!");
            System.out.println("\nSummary:");
            System.out.println("  - " + ISLANDS.size() + " islands");
            System.out.println("  - " + PORTS.size() + " ports");
            System.out.println("  - " + SEAPLANES.size() + " seaplanes");
            System.out.println("  - Products, clients, lockers, and orders created");

        } catch (Exception e) {
            System.err.println("❌ Error during initialization: " + e.getMessage());
        } finally {
            Neo4jConnection.close();
            MongoConnection.close();
        }
    }

    private static void clearDatabases(Driver neo4jDriver, MongoDatabase database) {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> tx.run("MATCH (n) DETACH DELETE n").consume());
            System.out.println("  ✓ Neo4j cleared");
        }

        for (String collection : Arrays.asList("products", "clients", "lockers", "orders", "boxes", "deliveries")) {
            database.getCollection(collection).drop();
        }
        System.out.println("  ✓ MongoDB cleared");
    }

    private static void populateNeo4j(Driver neo4jDriver) {
        try (Session session = neo4jDriver.session()) {

            session.executeWrite(tx -> {
                for (Map<String, Object> island : ISLANDS) {
                    tx.run("CREATE (i:Island {name: $name, lat: $lat, lon: $lon, area_km2: $area})",
                            parameters("name", island.get("name"), "lat", island.get("lat"),
                                    "lon", island.get("lon"), "area", island.get("area_km2")));
                }
                System.out.println("  ✓ " + ISLANDS.size() + " islands created");
                return null;
            });

            session.executeWrite(tx -> {
                for (Map<String, Object> port : PORTS) {
                    tx.run("CREATE (p:Port {name: $name, lat: $lat, lon: $lon})",
                            parameters("name", port.get("name"), "lat", port.get("lat"), "lon", port.get("lon")));

                    tx.run("MATCH (i:Island {name: $islandName}), (p:Port {name: $portName}) " +
                                    "CREATE (i)-[:HAS_PORT]->(p)",
                            parameters("islandName", port.get("island"), "portName", port.get("name")));
                }
                System.out.println("  ✓ " + PORTS.size() + " ports created and linked to islands");
                return null;
            });

            session.executeWrite(tx -> {
                tx.run("MATCH (p:Port {name: 'Puerto Baquerizo Moreno'}) SET p:Warehouse");
                System.out.println("  ✓ Main warehouse defined");
                return null;
            });

            session.executeWrite(tx -> {
                for (Map<String, Object> seaplane : SEAPLANES) {
                    tx.run("CREATE (h:Seaplane {id: $id, model: $model, box_capacity: $capacity, " +
                                    "fuel_consumption_km: $conso, status: $status})",
                            parameters("id", seaplane.get("id"), "model", seaplane.get("model"),
                                    "capacity", seaplane.get("box_capacity"),
                                    "conso", seaplane.get("fuel_consumption_km"),
                                    "status", seaplane.get("status")));
                }
                System.out.println("  ✓ " + SEAPLANES.size() + " seaplanes created");
                return null;
            });

            session.executeWrite(tx -> {
                tx.run("MATCH (h:Seaplane), (w:Warehouse) " +
                        "WHERE h.status = 'available' OR h.status = 'maintenance' " +
                        "CREATE (h)-[:STATIONED_AT]->(w)");

                tx.run("MATCH (h:Seaplane {id: 'HB-LSF'}), (p:Port {name: 'Puerto Ayora'}) " +
                        "CREATE (h)-[:STATIONED_AT]->(p)");
                tx.run("MATCH (h:Seaplane {id: 'HB-LSO'}), (p:Port {name: 'Punta Cormorant'}) " +
                        "CREATE (h)-[:STATIONED_AT]->(p)");

                tx.run("MATCH (h:Seaplane {id: 'HB-LSD'}), (p:Port {name: 'Puerto Villamil'}) " +
                        "CREATE (h)-[:FLYING_TO]->(p)");
                tx.run("MATCH (h:Seaplane {id: 'HB-LSI'}), (p:Port {name: 'Baie Darwin'}) " +
                        "CREATE (h)-[:FLYING_TO]->(p)");

                System.out.println("  ✓ Seaplanes positioned");
                return null;
            });

            System.out.println("  ✓ (Skipping TRAJET_VERS creation to keep graph clean)");
        }
    }

    private static Map<String, ObjectId> populateMongoDB(MongoDatabase database, Driver neo4jDriver) {
        Map<String, ObjectId> ids = new HashMap<>();

        MongoCollection<Document> products = database.getCollection("products");
        List<Document> productList = Arrays.asList(
                new Document("name", "High-Temp Volcanic Probe")
                        .append("description", "Sensor resistant up to 1200°C for lava study")
                        .append("stock_available", 20)
                        .append("weight_kg", 5.5)
                        .append("unit_price", 2500.0),
                new Document("name", "Fauna DNA Sampling Kit")
                        .append("description", "Complete kit for genetic analysis of turtles and iguanas")
                        .append("stock_available", 150)
                        .append("weight_kg", 0.8)
                        .append("unit_price", 180.0),
                new Document("name", "Portable Weather Station")
                        .append("description", "Autonomous station with multiple sensors")
                        .append("stock_available", 30)
                        .append("weight_kg", 12.0)
                        .append("unit_price", 3200.0),
                new Document("name", "Water Quality Analyzer")
                        .append("description", "Measures pH, salinity, temperature, turbidity")
                        .append("stock_available", 50)
                        .append("weight_kg", 3.2)
                        .append("unit_price", 1800.0),
                new Document("name", "Long-Range Observation Drone")
                        .append("description", "Autonomous drone for aerial mapping")
                        .append("stock_available", 5)
                        .append("weight_kg", 8.5)
                        .append("unit_price", 8500.0),
                new Document("name", "Infrared Thermal Camera")
                        .append("description", "Thermal imaging for volcanic study")
                        .append("stock_available", 12)
                        .append("weight_kg", 2.1)
                        .append("unit_price", 4200.0),
                new Document("name", "High-Precision GPS Tags")
                        .append("description", "For tracking fauna movements")
                        .append("stock_available", 200)
                        .append("weight_kg", 0.05)
                        .append("unit_price", 250.0),
                new Document("name", "Rock Sampler Kit")
                        .append("description", "Core drilling and sample preservation kit")
                        .append("stock_available", 35)
                        .append("weight_kg", 15.0)
                        .append("unit_price", 1500.0),
                new Document("name", "Mobile Water Filtration System")
                        .append("description", "On-site purification and analysis")
                        .append("stock_available", 18)
                        .append("weight_kg", 22.0)
                        .append("unit_price", 2800.0),
                new Document("name", "Field Microscope")
                        .append("description", "Portable microscope with LED lighting")
                        .append("stock_available", 25)
                        .append("weight_kg", 1.5)
                        .append("unit_price", 980.0),
                new Document("name", "Portable Seismograph")
                        .append("description", "Detection and recording of seismic tremors")
                        .append("stock_available", 15)
                        .append("weight_kg", 7.2)
                        .append("unit_price", 5300.0),
                new Document("name", "Fauna Tracking Collars")
                        .append("description", "Lightweight collars for small mammal GPS tracking")
                        .append("stock_available", 80)
                        .append("weight_kg", 0.2)
                        .append("unit_price", 450.0),
                new Document("name", "Marine Capture Nets")
                        .append("description", "High-resistance nets for plankton samples")
                        .append("stock_available", 40)
                        .append("weight_kg", 2.5)
                        .append("unit_price", 320.0),
                new Document("name", "Soil Analysis Kit")
                        .append("description", "Analyzes pH, NPK, and volcanic soil composition")
                        .append("stock_available", 60)
                        .append("weight_kg", 4.0)
                        .append("unit_price", 750.0),
                new Document("name", "Field Centrifuge")
                        .append("description", "Portable mini-centrifuge for blood sample separation")
                        .append("stock_available", 10)
                        .append("weight_kg", 1.8)
                        .append("unit_price", 1100.0)
        );
        products.insertMany(productList);
        ids.put("product1", productList.get(0).getObjectId("_id"));
        ids.put("product2", productList.get(1).getObjectId("_id"));
        ids.put("product3", productList.get(2).getObjectId("_id"));
        ids.put("product_sismo", productList.get(10).getObjectId("_id"));
        ids.put("product_collar", productList.get(11).getObjectId("_id"));
        ids.put("product_net", productList.get(12).getObjectId("_id"));
        ids.put("product_soil", productList.get(13).getObjectId("_id"));
        ids.put("product_centri", productList.get(14).getObjectId("_id"));
        System.out.println("  ✓ " + productList.size() + " products created");

        MongoCollection<Document> clients = database.getCollection("clients");
        List<Document> clientList = Arrays.asList(
                new Document("name", "Dr. Elena Rodriguez")
                        .append("type", "researcher")
                        .append("specialty", "Volcanology")
                        .append("study", "Study of Sierra Negra volcano (Isabela)")
                        .append("email", "e.rodriguez@galapagos-science.org")
                        .append("order_history", new ArrayList<>()),
                new Document("name", "Prof. Ben Carter")
                        .append("type", "researcher")
                        .append("specialty", "Marine Biology")
                        .append("study", "Conservation of giant tortoises")
                        .append("email", "b.carter@darwin-research.ec")
                        .append("order_history", new ArrayList<>()),
                new Document("name", "Charles Darwin Research Station")
                        .append("type", "institution")
                        .append("specialty", "Multidisciplinary")
                        .append("study", "Various research on the Galápagos ecosystem")
                        .append("email", "contact@darwinfoundation.org")
                        .append("order_history", new ArrayList<>()),
                new Document("name", "Dr. Maria Gutierrez")
                        .append("type", "researcher")
                        .append("specialty", "Ornithology")
                        .append("study", "Behavior of blue-footed boobies")
                        .append("email", "m.gutierrez@galapagos-birds.org")
                        .append("order_history", new ArrayList<>()),
                new Document("name", "Ecuadorian Oceanographic Institute")
                        .append("type", "institution")
                        .append("specialty", "Oceanography")
                        .append("study", "Water quality and marine currents")
                        .append("email", "research@inocar.mil.ec")
                        .append("order_history", new ArrayList<>()),
                new Document("name", "Dr. Kenji Tanaka")
                        .append("type", "researcher")
                        .append("specialty", "Seismology")
                        .append("study", "Seismic activity on Fernandina")
                        .append("email", "k.tanaka@geo-research.jp")
                        .append("order_history", new ArrayList<>()),
                new Document("name", "Universidad San Francisco de Quito")
                        .append("type", "institution")
                        .append("specialty", "Life Sciences")
                        .append("study", "Insular ecology research program")
                        .append("email", "gaias@usfq.edu.ec")
                        .append("order_history", new ArrayList<>()),
                new Document("name", "Dr. Sophie Dubois")
                        .append("type", "researcher")
                        .append("specialty", "Botany")
                        .append("study", "Endemic flora of Floreana")
                        .append("email", "s.dubois@paris-botanique.fr")
                        .append("order_history", new ArrayList<>())
        );
        clients.insertMany(clientList);
        ids.put("client1", clientList.get(0).getObjectId("_id"));
        ids.put("client2", clientList.get(1).getObjectId("_id"));
        ids.put("client3", clientList.get(2).getObjectId("_id"));
        ids.put("client_tanaka", clientList.get(5).getObjectId("_id"));
        ids.put("client_usfq", clientList.get(6).getObjectId("_id"));
        ids.put("client_dubois", clientList.get(7).getObjectId("_id"));
        System.out.println("  ✓ " + clientList.size() + " clients created");

        MongoCollection<Document> lockers = database.getCollection("lockers");
        List<Document> lockerList = new ArrayList<>();

        for (Map<String, Object> port : PORTS) {
            String portName = (String) port.get("name");
            int nbLockers = (int) port.get("nb_lockers");

            for (int i = 1; i <= nbLockers; i++) {
                lockerList.add(
                        new Document("port_name", portName)
                                .append("number", i)
                                .append("status", "empty")
                                .append("box_id", null)
                                .append("last_used", null)
                );
            }
        }
        if (!lockerList.isEmpty()) {
            lockers.insertMany(lockerList);
        }
        System.out.println("  ✓ " + lockerList.size() + " lockers created (total across " + PORTS.size() + " ports)");

        return ids;
    }

    private static void createTestData(MongoDatabase database, Driver neo4jDriver, Map<String, ObjectId> ids) {
        MongoCollection<Document> orders = database.getCollection("orders");
        MongoCollection<Document> boxes = database.getCollection("boxes");
        MongoCollection<Document> deliveries = database.getCollection("deliveries");
        MongoCollection<Document> lockers = database.getCollection("lockers");
        MongoCollection<Document> products = database.getCollection("products");


        List<Document> productsCmd1 = Arrays.asList(
                new Document("product_id", ids.get("product1")).append("quantity", 3),
                new Document("product_id", ids.get("product3")).append("quantity", 1)
        );
        Document order1 = new Document("client_id", ids.get("client1"))
                .append("order_date", LocalDateTime.now().minusDays(5).toString())
                .append("status", "delivered")
                .append("delivery_port", "Puerto Villamil")
                .append("products", productsCmd1)
                .append("box_count", 2)
                .append("total_weight_kg", 28.5);
        orders.insertOne(order1);
        products.updateOne(new Document("_id", ids.get("product1")), Updates.inc("stock_available", -3));
        products.updateOne(new Document("_id", ids.get("product3")), Updates.inc("stock_available", -1));

        ObjectId box1Id = new ObjectId();
        ObjectId box2Id = new ObjectId();
        boxes.insertMany(Arrays.asList(
                new Document("_id", box1Id).append("order_id", order1.getObjectId("_id")).append("client_id", ids.get("client1")).append("number", 1).append("status", "delivered").append("content", "3x Volcanic Probe"),
                new Document("_id", box2Id).append("order_id", order1.getObjectId("_id")).append("client_id", ids.get("client1")).append("number", 2).append("status", "delivered").append("content", "1x Weather Station")
        ));
        lockers.updateOne(new Document("port_name", "Puerto Villamil").append("number", 1), new Document("$set", new Document("status", "occupied").append("box_id", box1Id)));
        lockers.updateOne(new Document("port_name", "Puerto Villamil").append("number", 2), new Document("$set", new Document("status", "occupied").append("box_id", box2Id)));


        Document order2 = new Document("client_id", ids.get("client2"))
                .append("order_date", LocalDateTime.now().minusDays(1).toString())
                .append("status", "pending")
                .append("delivery_port", "Puerto Ayora")
                .append("products", Arrays.asList(new Document("product_id", ids.get("product2")).append("quantity", 50)))
                .append("box_count", 1)
                .append("total_weight_kg", 40.0);
        orders.insertOne(order2);



        List<Document> productsCmd3 = Arrays.asList(
                new Document("product_id", ids.get("product1")).append("quantity", 2),
                new Document("product_id", ids.get("product2")).append("quantity", 20)
        );
        Document order3 = new Document("client_id", ids.get("client3"))
                .append("order_date", LocalDateTime.now().minusHours(8).toString())
                .append("status", "in_transit")
                .append("delivery_port", "Baie Darwin")
                .append("products", productsCmd3)
                .append("box_count", 2)
                .append("total_weight_kg", 27.0);
        orders.insertOne(order3);
        products.updateOne(new Document("_id", ids.get("product1")), Updates.inc("stock_available", -2));
        products.updateOne(new Document("_id", ids.get("product2")), Updates.inc("stock_available", -20));

        Document delivery1 = new Document("order_id", order3.getObjectId("_id"))
                .append("seaplane_id", "HB-LSI")
                .append("departure_date", LocalDateTime.now().minusHours(2).toString())
                .append("status", "in_progress")
                .append("planned_route", Arrays.asList("Puerto Baquerizo Moreno", "Baie Darwin"))
                .append("current_port", "in_flight")
                .append("destination_port", "Baie Darwin")
                .append("transported_boxes", Arrays.asList(new ObjectId(), new ObjectId()))
                .append("total_distance_km", 145.2)
                .append("estimated_fuel_l", 3194.4);
        deliveries.insertOne(delivery1);


        List<Document> productsCmd4 = Arrays.asList(
                new Document("product_id", ids.get("product_soil")).append("quantity", 5),
                new Document("product_id", ids.get("product2")).append("quantity", 10)
        );
        Document order4 = new Document("client_id", ids.get("client_dubois"))
                .append("order_date", LocalDateTime.now().minusDays(3).toString())
                .append("status", "delivered")
                .append("delivery_port", "Punta Cormorant")
                .append("products", productsCmd4)
                .append("box_count", 1)
                .append("total_weight_kg", 28.0);
        orders.insertOne(order4);
        products.updateOne(new Document("_id", ids.get("product_soil")), Updates.inc("stock_available", -5));
        products.updateOne(new Document("_id", ids.get("product2")), Updates.inc("stock_available", -10));

        ObjectId box3Id = new ObjectId();
        boxes.insertOne(
                new Document("_id", box3Id).append("order_id", order4.getObjectId("_id")).append("client_id", ids.get("client_dubois")).append("number", 1).append("status", "delivered").append("content", "5x Soil Kit, 10x DNA Kit")
        );
        lockers.updateOne(new Document("port_name", "Punta Cormorant").append("number", 1), new Document("$set", new Document("status", "occupied").append("box_id", box3Id)));

        Document delivery2 = new Document("order_id", order4.getObjectId("_id"))
                .append("seaplane_id", "HB-LSO")
                .append("departure_date", LocalDateTime.now().minusDays(3).plusHours(2).toString())
                .append("arrival_date", LocalDateTime.now().minusDays(3).plusHours(4).toString())
                .append("status", "completed")
                .append("planned_route", Arrays.asList("Puerto Baquerizo Moreno", "Punta Cormorant"))
                .append("current_port", "Punta Cormorant")
                .append("destination_port", "Punta Cormorant")
                .append("transported_boxes", Arrays.asList(box3Id))
                .append("total_distance_km", 60.5)
                .append("estimated_fuel_l", 1149.5);
        deliveries.insertOne(delivery2);


        Document order5 = new Document("client_id", ids.get("client_usfq"))
                .append("order_date", LocalDateTime.now().minusDays(1).toString())
                .append("status", "pending")
                .append("delivery_port", "Canal de Itabaca")
                .append("products", Arrays.asList(
                        new Document("product_id", ids.get("product_net")).append("quantity", 20),
                        new Document("product_id", ids.get("product_centri")).append("quantity", 2)
                ))
                .append("box_count", 2)
                .append("total_weight_kg", 53.6);
        orders.insertOne(order5);


        System.out.println("  ✓ 5 orders created with stock management");
        System.out.println("  ✓ 3 boxes delivered (and stock decremented)");
        System.out.println("  ✓ 1 delivery in_progress (and stock decremented), 1 completed");
    }
}