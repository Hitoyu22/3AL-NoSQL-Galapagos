package fr.esgi.galapagos.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import fr.esgi.galapagos.config.MongoConnection;
import fr.esgi.galapagos.config.Neo4jConnection;
import fr.esgi.galapagos.model.enums.*;
import fr.esgi.galapagos.model.mongodb.*;
import fr.esgi.galapagos.model.neo4j.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.time.LocalDateTime;
import java.util.*;

import static org.neo4j.driver.Values.parameters;

public class DataInitializer {

    private static final List<Island> ISLANDS = Arrays.asList(
            new Island("Baltra", -0.4511, -90.2653, 27),
            new Island("Bartolomé", -0.2865, -90.5521, 1.2),
            new Island("Darwin", 1.6781, -92.0033, 1.1),
            new Island("Española", -1.3833, -89.6167, 60),
            new Island("Fernandina", -0.3667, -91.5500, 642),
            new Island("Floreana", -1.2861, -90.4369, 173),
            new Island("Genovesa", 0.3205, -89.9558, 14),
            new Island("Isabela", -0.7167, -91.1000, 4640),
            new Island("Marchena", 0.3333, -90.4833, 130),
            new Island("Seymour Nord", -0.3950, -90.2881, 1.9),
            new Island("Pinta", 0.5833, -90.7500, 60),
            new Island("Pinzón", -0.6111, -90.6667, 18),
            new Island("Rábida", -0.4075, -90.7125, 4.9),
            new Island("San Cristóbal", -0.8333, -89.4333, 558),
            new Island("Santa Cruz", -0.6167, -90.3500, 986),
            new Island("Santa Fé", -0.8111, -90.0556, 24),
            new Island("Santiago", -0.2500, -90.7667, 585),
            new Island("Plaza Sud", -0.5861, -90.1650, 0.13),
            new Island("Wolf", 1.3806, -91.8000, 1.3)
    );

    private static final List<Port> PORTS = Arrays.asList(
            new Port("Puerto Baquerizo Moreno", "San Cristóbal", -0.9025, -89.609167, 10),
            new Port("Puerto Ayora", "Santa Cruz", -0.7400, -90.3117, 8),
            new Port("Puerto Villamil", "Isabela", -0.9569, -90.9672, 7),
            new Port("Aéroport de Baltra", "Baltra", -0.4511, -90.2653, 5),
            new Port("Puerto Velasco Ibarra", "Floreana", -1.2975, -90.4342, 5),
            new Port("Baie de Sullivan", "Santiago", -0.2916, -90.5708, 6),
            new Port("Baie Darwin", "Genovesa", 0.3205, -89.9558, 4),
            new Port("Punta Espinoza", "Fernandina", -0.2678, -91.4422, 5),
            new Port("Punta Suarez", "Española", -1.3725, -89.7308, 5),
            new Port("Caleta Tagus", "Isabela", -0.2514, -91.3694, 5),
            new Port("Bahía Gardner", "Española", -1.3450, -89.6289, 4),
            new Port("Punta Vicente Roca", "Isabela", 0.0417, -91.7333, 4),
            new Port("Canal de Itabaca", "Santa Cruz", -0.4583, -90.2708, 4),
            new Port("Punta Cormorant", "Floreana", -1.2333, -90.4167, 3),
            new Port("Baie de Bartolomé", "Bartolomé", -0.2833, -90.5500, 2),
            new Port("Anse Darwin", "Darwin", 1.6780, -92.0030, 2),
            new Port("Playa de las Bachas", "Seymour Nord", -0.3958, -90.2875, 3),
            new Port("Punta Mangle", "Marchena", 0.3330, -90.4830, 2),
            new Port("Cabo Douglas", "Pinta", 0.5830, -90.7500, 2),
            new Port("Rocas Pinzón", "Pinzón", -0.6110, -90.6660, 2),
            new Port("Playa Roja", "Rábida", -0.4070, -90.7120, 2),
            new Port("Baie de Santa Fé", "Santa Fé", -0.8110, -90.0550, 3),
            new Port("Quai Plaza Sud", "Plaza Sud", -0.5860, -90.1650, 2),
            new Port("Rocher Wolf", "Wolf", 1.3800, -91.8000, 2)
    );

    private static final List<Seaplane> SEAPLANES = Arrays.asList(
            new Seaplane("HB-LSR", "DHC-6 Twin Otter", 100, 25.0, 250.0, SeaplaneStatus.AVAILABLE),
            new Seaplane("HB-LSK", "Grumman G-21 Goose", 50, 18.0, 280.0, SeaplaneStatus.MAINTENANCE),
            new Seaplane("HB-LSC", "DHC-6 Twin Otter", 100, 25.0, 250.0, SeaplaneStatus.AVAILABLE),
            new Seaplane("HB-LSD", "Cessna 208 Caravan", 75, 20.0, 280.0, SeaplaneStatus.IN_FLIGHT),
            new Seaplane("HB-LSE", "DHC-3 Otter", 80, 22.0, 240.0, SeaplaneStatus.AVAILABLE),
            new Seaplane("HB-LSF", "Grumman G-73 Mallard", 60, 19.0, 290.0, SeaplaneStatus.AT_PORT),
            new Seaplane("HB-LSG", "DHC-6 Twin Otter", 100, 25.0, 250.0, SeaplaneStatus.AVAILABLE),
            new Seaplane("HB-LSH", "Cessna 208 Caravan", 75, 20.0, 280.0, SeaplaneStatus.AVAILABLE),
            new Seaplane("HB-LSI", "DHC-3 Otter", 80, 22.0, 240.0, SeaplaneStatus.IN_FLIGHT),
            new Seaplane("HB-LSJ", "Grumman G-21 Goose", 50, 18.0, 280.0, SeaplaneStatus.AVAILABLE),
            new Seaplane("HB-LSN", "Cessna 208 Caravan", 75, 20.0, 280.0, SeaplaneStatus.AVAILABLE),
            new Seaplane("HB-LSO", "Grumman G-73 Mallard", 60, 19.0, 290.0, SeaplaneStatus.AT_PORT)
    );

    public static void main(String[] args) {

        try {
            Driver neo4jDriver = Neo4jConnection.getDriver();
            MongoDatabase mongoDatabase = MongoConnection.getDatabase();

            clearDatabases(neo4jDriver, mongoDatabase);

            populateNeo4j(neo4jDriver);

            Map<String, ObjectId> ids = populateMongoDB(mongoDatabase);

            createTestData(mongoDatabase, ids);

            System.out.println("Mise à jour des données terminée");

        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
        } finally {
            Neo4jConnection.close();
            MongoConnection.close();
        }
    }

    private static void clearDatabases(Driver neo4jDriver, MongoDatabase database) {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> tx.run("MATCH (n) DETACH DELETE n").consume());
        }

        for (String collection : Arrays.asList("products", "clients", "lockers", "orders", "boxes", "deliveries")) {
            database.getCollection(collection).drop();
        }
    }

    private static void populateNeo4j(Driver neo4jDriver) {
        try (Session session = neo4jDriver.session()) {

            session.executeWrite(tx -> {
                for (Island island : ISLANDS) {
                    tx.run("CREATE (i:Island {name: $name, lat: $lat, lon: $lon, area_km2: $area})", island.toMap());
                }
                return null;
            });

            session.executeWrite(tx -> {
                for (Port port : PORTS) {
                    tx.run("CREATE (p:Port {name: $name, lat: $lat, lon: $lon})", port.toMap());
                    tx.run("MATCH (i:Island {name: $islandName}), (p:Port {name: $portName}) CREATE (i)-[:HAS_PORT]->(p)",
                            parameters("islandName", port.getIslandName(), "portName", port.getName()));
                }
                return null;
            });

            session.executeWrite(tx -> {
                tx.run("MATCH (p:Port {name: 'Puerto Baquerizo Moreno'}) SET p:Warehouse");
                return null;
            });

            session.executeWrite(tx -> {
                for (Seaplane seaplane : SEAPLANES) {
                    tx.run("CREATE (h:Seaplane {id: $id, model: $model, box_capacity: $capacity, " +
                                    "fuel_consumption_km: $conso, cruise_speed_kmh: $speed, status: $status})",
                            seaplane.toMap());
                }
                return null;
            });

            session.executeWrite(tx -> {
                tx.run("MATCH (h:Seaplane), (w:Warehouse) WHERE h.status = 'available' OR h.status = 'maintenance' CREATE (h)-[:STATIONED_AT]->(w)");
                tx.run("MATCH (h:Seaplane {id: 'HB-LSF'}), (p:Port {name: 'Puerto Ayora'}) CREATE (h)-[:STATIONED_AT]->(p)");
                tx.run("MATCH (h:Seaplane {id: 'HB-LSO'}), (p:Port {name: 'Punta Cormorant'}) CREATE (h)-[:STATIONED_AT]->(p)");

                tx.run("MATCH (h:Seaplane {id: 'HB-LSD'}) " +
                                "SET h.departure_time = datetime($dep), h.estimated_arrival = datetime($arr)",
                        parameters("dep", LocalDateTime.now().minusMinutes(45).toString(), "arr", LocalDateTime.now().plusMinutes(30).toString()));
                tx.run("MATCH (h:Seaplane {id: 'HB-LSD'}), (p:Port {name: 'Puerto Villamil'}) CREATE (h)-[:FLYING_TO]->(p)");
                tx.run("MATCH (h:Seaplane {id: 'HB-LSD'}), (p:Port {name: 'Puerto Baquerizo Moreno'}) CREATE (h)-[:FLYING_FROM]->(p)");

                tx.run("MATCH (h:Seaplane {id: 'HB-LSI'}) " +
                                "SET h.departure_time = datetime($dep), h.estimated_arrival = datetime($arr)",
                        parameters("dep", LocalDateTime.now().minusMinutes(120).toString(), "arr", LocalDateTime.now().minusMinutes(10).toString()));
                tx.run("MATCH (h:Seaplane {id: 'HB-LSI'}), (p:Port {name: 'Baie Darwin'}) CREATE (h)-[:FLYING_TO]->(p)");
                tx.run("MATCH (h:Seaplane {id: 'HB-LSI'}), (p:Port {name: 'Puerto Baquerizo Moreno'}) CREATE (h)-[:FLYING_FROM]->(p)");

                return null;
            });
        }
    }

    private static Map<String, ObjectId> populateMongoDB(MongoDatabase database) {
        Map<String, ObjectId> ids = new HashMap<>();

        MongoCollection<Document> productCol = database.getCollection("products");
        List<Product> products = Arrays.asList(
                new Product("Sonde volcanique haute température", "Capteur résistant jusqu'à 1200°C pour l'étude de la lave", 20, 5.5, 2500.0),
                new Product("Kit de prélèvement ADN faune", "Kit complet pour analyse génétique", 150, 0.8, 180.0),
                new Product("Station météo portable", "Station autonome avec capteurs multiples", 30, 12.0, 3200.0),
                new Product("Analyseur de qualité de l'eau", "Mesure pH, salinité, température", 50, 3.2, 1800.0),
                new Product("Drone d'observation longue portée", "Drone autonome pour cartographie aérienne", 5, 8.5, 8500.0),
                new Product("Caméra thermique infrarouge", "Imagerie thermique pour étude volcanique", 12, 2.1, 4200.0),
                new Product("Balises GPS haute précision", "Pour le suivi des mouvements de la faune", 200, 0.05, 250.0),
                new Product("Kit de carottage de roche", "Carottage et conservation d'échantillons", 35, 15.0, 1500.0),
                new Product("Système de filtration d'eau mobile", "Purification et analyse sur site", 18, 22.0, 2800.0),
                new Product("Microscope de terrain", "Microscope portable avec éclairage LED", 25, 1.5, 980.0),
                new Product("Sismographe portable", "Détection et enregistrement des secousses", 15, 7.2, 5300.0),
                new Product("Colliers de suivi de faune", "Colliers légers pour suivi GPS petits mammifères", 80, 0.2, 450.0),
                new Product("Filets de capture marine", "Filets haute résistance pour échantillons plancton", 40, 2.5, 320.0),
                new Product("Kit d'analyse de sol", "Analyse pH, NPK et composition sol volcanique", 60, 4.0, 750.0),
                new Product("Centrifugeuse de terrain", "Mini-centrifugeuse portable pour séparation échantillons sanguins", 10, 1.8, 1100.0)
        );
        for (Product p : products) productCol.insertOne(p.toDocument());

        ids.put("product1", products.get(0).getId());
        ids.put("product2", products.get(1).getId());
        ids.put("product3", products.get(2).getId());
        ids.put("product_sismo", products.get(10).getId());
        ids.put("product_collar", products.get(11).getId());
        ids.put("product_net", products.get(12).getId());
        ids.put("product_soil", products.get(13).getId());
        ids.put("product_centri", products.get(14).getId());

        MongoCollection<Document> clientCol = database.getCollection("clients");
        List<Client> clients = Arrays.asList(
                new Client("Dr. Elena Rodriguez", "chercheur", "Volcanologie", "Étude du volcan Sierra Negra", "e.rodriguez@galapagos-science.org"),
                new Client("Prof. Ben Carter", "chercheur", "Biologie marine", "Conservation des tortues géantes", "b.carter@darwin-research.ec"),
                new Client("Fondation Charles Darwin", "institution", "Pluridisciplinaire", "Recherches diverses", "contact@darwinfoundation.org"),
                new Client("Dr. Maria Gutierrez", "chercheur", "Ornithologie", "Comportement des fous à pieds bleus", "m.gutierrez@galapagos-birds.org"),
                new Client("Institut Océanographique Équatorien", "institution", "Océanographie", "Qualité de l'eau", "research@inocar.mil.ec"),
                new Client("Dr. Kenji Tanaka", "chercheur", "Sismologie", "Activité sismique sur Fernandina", "k.tanaka@geo-research.jp"),
                new Client("Université San Francisco de Quito", "institution", "Sciences de la vie", "Écologie insulaire", "gaias@usfq.edu.ec"),
                new Client("Dr. Sophie Dubois", "chercheur", "Botanique", "Flore endémique de Floreana", "s.dubois@paris-botanique.fr")
        );
        for (Client c : clients) clientCol.insertOne(c.toDocument());

        ids.put("client1", clients.get(0).getId());
        ids.put("client2", clients.get(1).getId());
        ids.put("client3", clients.get(2).getId());
        ids.put("client_tanaka", clients.get(5).getId());
        ids.put("client_usfq", clients.get(6).getId());
        ids.put("client_dubois", clients.get(7).getId());

        MongoCollection<Document> lockerCol = database.getCollection("lockers");
        for (Port port : PORTS) {
            for (int i = 1; i <= port.getNbLockers(); i++) {
                lockerCol.insertOne(new Locker(port.getName(), i).toDocument());
            }
        }

        return ids;
    }

    private static void createTestData(MongoDatabase database, Map<String, ObjectId> ids) {
        MongoCollection<Document> ordersCol = database.getCollection("orders");
        MongoCollection<Document> boxesCol = database.getCollection("boxes");
        MongoCollection<Document> deliveriesCol = database.getCollection("deliveries");
        MongoCollection<Document> lockersCol = database.getCollection("lockers");
        MongoCollection<Document> productsCol = database.getCollection("products");
        MongoCollection<Document> clientsCol = database.getCollection("clients");

        Order order1 = new Order(ids.get("client1"), LocalDateTime.now().minusDays(5).toString(), OrderStatus.DELIVERED, "normal", "Puerto Villamil",
                Arrays.asList(new Order.OrderedProduct(ids.get("product1"), 3), new Order.OrderedProduct(ids.get("product3"), 1)), 2, 28.5);
        ordersCol.insertOne(order1.toDocument());
        updateClientHistory(clientsCol, ids.get("client1"), order1.getId());
        decrementStock(productsCol, ids.get("product1"), 3);
        decrementStock(productsCol, ids.get("product3"), 1);

        Box box1 = new Box(order1.getId(), ids.get("client1"), 1, BoxStatus.DELIVERED, "3x Sonde volcanique");
        Box box2 = new Box(order1.getId(), ids.get("client1"), 2, BoxStatus.DELIVERED, "1x Station météo");
        boxesCol.insertMany(Arrays.asList(box1.toDocument(), box2.toDocument()));

        updateLocker(lockersCol, "Puerto Villamil", 1, LockerStatus.OCCUPIED.name().toLowerCase(), box1.getId());
        updateLocker(lockersCol, "Puerto Villamil", 2, LockerStatus.OCCUPIED.name().toLowerCase(), box2.getId());

        Order order2 = new Order(ids.get("client2"), LocalDateTime.now().minusDays(1).toString(), OrderStatus.PENDING, "normal", "Puerto Ayora",
                Collections.singletonList(new Order.OrderedProduct(ids.get("product2"), 50)), 1, 40.0);
        ordersCol.insertOne(order2.toDocument());
        updateClientHistory(clientsCol, ids.get("client2"), order2.getId());

        Order order3 = new Order(ids.get("client3"), LocalDateTime.now().minusHours(8).toString(), OrderStatus.IN_TRANSIT, "normal", "Baie Darwin",
                Arrays.asList(new Order.OrderedProduct(ids.get("product1"), 2), new Order.OrderedProduct(ids.get("product2"), 20)), 2, 27.0);
        ordersCol.insertOne(order3.toDocument());
        updateClientHistory(clientsCol, ids.get("client3"), order3.getId());
        decrementStock(productsCol, ids.get("product1"), 2);
        decrementStock(productsCol, ids.get("product2"), 20);

        Box box3_1 = new Box(order3.getId(), ids.get("client3"), 1, BoxStatus.IN_TRANSIT, "2x Sonde volcanique");
        Box box3_2 = new Box(order3.getId(), ids.get("client3"), 2, BoxStatus.IN_TRANSIT, "20x Kit ADN");
        boxesCol.insertMany(Arrays.asList(box3_1.toDocument(), box3_2.toDocument()));

        Delivery delivery1 = new Delivery(order3.getId(), "HB-LSI", DeliveryStatus.IN_PROGRESS)
                .departureDate(LocalDateTime.now().minusHours(2).toString())
                .route(Arrays.asList("Puerto Baquerizo Moreno", "Baie Darwin"), "Baie Darwin", 145.2)
                .currentStatus("in_flight", 3194.4)
                .boxes(Arrays.asList(box3_1.getId(), box3_2.getId()));
        deliveriesCol.insertOne(delivery1.toDocument());

        Order order4 = new Order(ids.get("client_dubois"), LocalDateTime.now().minusDays(3).toString(), OrderStatus.DELIVERED, "normal", "Punta Cormorant",
                Arrays.asList(new Order.OrderedProduct(ids.get("product_soil"), 5), new Order.OrderedProduct(ids.get("product2"), 10)), 1, 28.0);
        ordersCol.insertOne(order4.toDocument());
        updateClientHistory(clientsCol, ids.get("client_dubois"), order4.getId());
        decrementStock(productsCol, ids.get("product_soil"), 5);
        decrementStock(productsCol, ids.get("product2"), 10);

        Box box3 = new Box(order4.getId(), ids.get("client_dubois"), 1, BoxStatus.DELIVERED, "5x Kit sol, 10x Kit ADN");
        boxesCol.insertOne(box3.toDocument());
        updateLocker(lockersCol, "Punta Cormorant", 1, LockerStatus.OCCUPIED.name().toLowerCase(), box3.getId());

        Delivery delivery2 = new Delivery(order4.getId(), "HB-LSO", DeliveryStatus.COMPLETED)
                .departureDate(LocalDateTime.now().minusDays(3).plusHours(2).toString())
                .arrivalDate(LocalDateTime.now().minusDays(3).plusHours(4).toString())
                .route(Arrays.asList("Puerto Baquerizo Moreno", "Punta Cormorant"), "Punta Cormorant", 60.5)
                .currentStatus("Punta Cormorant", 1149.5)
                .boxes(List.of(box3.getId()));
        deliveriesCol.insertOne(delivery2.toDocument());

        Order order5 = new Order(ids.get("client_usfq"), LocalDateTime.now().minusDays(1).toString(), OrderStatus.PENDING, "urgent", "Canal de Itabaca",
                Arrays.asList(new Order.OrderedProduct(ids.get("product_net"), 20), new Order.OrderedProduct(ids.get("product_centri"), 2)), 2, 53.6);
        ordersCol.insertOne(order5.toDocument());
        updateClientHistory(clientsCol, ids.get("client_usfq"), order5.getId());

        Delivery delivery3 = new Delivery(order5.getId(), "HB-LSC", DeliveryStatus.SCHEDULED)
                .scheduledDeparture(LocalDateTime.now().plusHours(3).toString())
                .route(Arrays.asList("Puerto Baquerizo Moreno", "Canal de Itabaca"), "Canal de Itabaca", 80.0);
        deliveriesCol.insertOne(delivery3.toDocument());

        Order order6 = new Order(ids.get("client_tanaka"), LocalDateTime.now().minusHours(4).toString(), OrderStatus.PENDING, "urgent", "Punta Espinoza",
                Collections.singletonList(new Order.OrderedProduct(ids.get("product_sismo"), 3)), 1, 21.6);
        ordersCol.insertOne(order6.toDocument());
        updateClientHistory(clientsCol, ids.get("client_tanaka"), order6.getId());

        Delivery delivery4 = new Delivery(order6.getId(), "HB-LSN", DeliveryStatus.DELAYED)
                .scheduledDeparture(LocalDateTime.now().minusHours(1).toString())
                .delayReason("Maintenance technique requise avant le décollage")
                .route(Arrays.asList("Puerto Baquerizo Moreno", "Punta Espinoza"), "Punta Espinoza", 0);
        deliveriesCol.insertOne(delivery4.toDocument());

        Order order7 = new Order(ids.get("client_usfq"), LocalDateTime.now().minusDays(4).toString(), OrderStatus.PARTIALLY_DELIVERED, "normal", "Puerto Villamil",
                Collections.singletonList(new Order.OrderedProduct(ids.get("product_net"), 30)), 3, 75.0);
        order7.setBoxesDelivered(2);
        ordersCol.insertOne(order7.toDocument());
        updateClientHistory(clientsCol, ids.get("client_usfq"), order7.getId());
        decrementStock(productsCol, ids.get("product_net"), 30);

        Box box7_1 = new Box(order7.getId(), ids.get("client_usfq"), 1, BoxStatus.DELIVERED, "10x Filets marins");
        Box box7_2 = new Box(order7.getId(), ids.get("client_usfq"), 2, BoxStatus.DELIVERED, "10x Filets marins");
        Box box7_3 = new Box(order7.getId(), ids.get("client_usfq"), 3, BoxStatus.PENDING, "10x Filets marins");
        boxesCol.insertMany(Arrays.asList(box7_1.toDocument(), box7_2.toDocument(), box7_3.toDocument()));

        updateLocker(lockersCol, "Puerto Villamil", 3, LockerStatus.OCCUPIED.name().toLowerCase(), box7_1.getId());
        updateLocker(lockersCol, "Puerto Villamil", 4, LockerStatus.OCCUPIED.name().toLowerCase(), box7_2.getId());

        lockersCol.updateOne(new Document("port_name", "Puerto Ayora").append("number", 1),
                new Document("$set", new Document("status", LockerStatus.MAINTENANCE.name().toLowerCase()).append("maintenance_reason", "Défaillance de la serrure électronique")));
        lockersCol.updateOne(new Document("port_name", "Puerto Ayora").append("number", 2),
                new Document("$set", new Document("status", LockerStatus.MAINTENANCE.name().toLowerCase()).append("maintenance_reason", "Nettoyage requis")));

        lockersCol.updateOne(new Document("port_name", "Baie Darwin").append("number", 1),
                new Document("$set", new Document("status", LockerStatus.RESERVED.name().toLowerCase()).append("reserved_for_order_id", order3.getId())));
        lockersCol.updateOne(new Document("port_name", "Canal de Itabaca").append("number", 1),
                new Document("$set", new Document("status", LockerStatus.RESERVED.name().toLowerCase()).append("reserved_for_order_id", order5.getId())));

    }

    private static void decrementStock(MongoCollection<Document> col, ObjectId prodId, int qty) {
        col.updateOne(new Document("_id", prodId), Updates.inc("stock_available", -qty));
    }

    private static void updateClientHistory(MongoCollection<Document> col, ObjectId clientId, ObjectId orderId) {
        col.updateOne(Filters.eq("_id", clientId), Updates.push("order_history", orderId));
    }

    private static void updateLocker(MongoCollection<Document> col, String port, int num, String status, ObjectId boxId) {
        col.updateOne(new Document("port_name", port).append("number", num),
                new Document("$set", new Document("status", status).append("box_id", boxId)));
    }
}