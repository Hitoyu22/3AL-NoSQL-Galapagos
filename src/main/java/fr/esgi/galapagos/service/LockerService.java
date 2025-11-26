package fr.esgi.galapagos.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import fr.esgi.galapagos.config.MongoConnection;
import fr.esgi.galapagos.config.Neo4jConnection;
import fr.esgi.galapagos.model.enums.LockerStatus;
import fr.esgi.galapagos.model.mongodb.Locker;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LockerService {

    private final MongoDatabase mongoDatabase;
    private final Driver neo4jDriver;

    public LockerService() {
        this.mongoDatabase = MongoConnection.getDatabase();
        this.neo4jDriver = Neo4jConnection.getDriver();
    }

    public List<Locker> getLockers(Integer portId, String status) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("lockers");
        List<Locker> lockers = new ArrayList<>();

        List<Bson> filters = new ArrayList<>();
        if (portId != null) {
            filters.add(Filters.eq("port_id", portId));
        }
        if (status != null) {
            filters.add(Filters.eq("status", status.toLowerCase()));
        }

        Bson finalFilter = filters.isEmpty() ? new Document() : Filters.and(filters);

        for (Document doc : collection.find(finalFilter)) {
            lockers.add(mapDocumentToLocker(doc));
        }
        return lockers;
    }

    public int countLockersByPortId(int portId) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("lockers");
        return (int) collection.countDocuments(Filters.eq("port_id", portId));
    }

    private void updatePortLockerCount(int portId, int delta) {
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                String query = "MATCH (p:Port {id: $id}) SET p.nbLockers = coalesce(p.nbLockers, 0) + $delta";
                tx.run(query, Map.of("id", portId, "delta", delta));
                return null;
            });
        }
    }

    public Locker addLocker(int portId) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("lockers");

        Document lastLocker = collection.find(Filters.eq("port_id", portId))
                .sort(Sorts.descending("number"))
                .first();

        int nextNumber = (lastLocker != null) ? lastLocker.getInteger("number") + 1 : 1;

        Locker locker = new Locker(portId, nextNumber);
        collection.insertOne(locker.toDocument());

        updatePortLockerCount(portId, 1);

        return locker;
    }

    public Locker updateLockerStatus(String id, String newStatusStr, String maintenanceReason) {
        Document doc = findLockerDocumentById(id);

        String currentStatusStr = doc.getString("status").toUpperCase();
        LockerStatus currentStatus = LockerStatus.valueOf(currentStatusStr);

        if (currentStatus != LockerStatus.EMPTY && currentStatus != LockerStatus.MAINTENANCE) {
            throw new RuntimeException("Impossible de modifier le statut : le casier est actuellement utilisé ou réservé.");
        }

        LockerStatus newStatus;
        try {
            newStatus = LockerStatus.valueOf(newStatusStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide : " + newStatusStr);
        }

        if (newStatus == LockerStatus.MAINTENANCE && (maintenanceReason == null || maintenanceReason.isBlank())) {
            throw new RuntimeException("Une raison est obligatoire pour mettre un casier en maintenance.");
        }

        List<Bson> updates = new ArrayList<>();
        updates.add(Updates.set("status", newStatus.name().toLowerCase()));

        if (newStatus == LockerStatus.MAINTENANCE) {
            updates.add(Updates.set("maintenance_reason", maintenanceReason));
        } else {
            updates.add(Updates.unset("maintenance_reason"));
        }

        MongoCollection<Document> collection = mongoDatabase.getCollection("lockers");
        ObjectId objId = new ObjectId(id);
        collection.updateOne(Filters.eq("_id", objId), Updates.combine(updates));

        return mapDocumentToLocker(Objects.requireNonNull(collection.find(Filters.eq("_id", objId)).first()));
    }

    public boolean deleteLocker(String id) {
        Document doc = findLockerDocumentById(id);

        int portId = doc.getInteger("port_id");

        String currentStatusStr = doc.getString("status").toUpperCase();
        if (!LockerStatus.EMPTY.name().equals(currentStatusStr)) {
            throw new RuntimeException("Impossible de supprimer le casier : il n'est pas vide (Statut: " + currentStatusStr + ").");
        }

        MongoCollection<Document> collection = mongoDatabase.getCollection("lockers");
        ObjectId objId = new ObjectId(id);
        collection.deleteOne(Filters.eq("_id", objId));

        updatePortLockerCount(portId, -1);

        return true;
    }

    private Document findLockerDocumentById(String id) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("lockers");
        ObjectId objId = new ObjectId(id);

        Document doc = collection.find(Filters.eq("_id", objId)).first();
        if (doc == null) {
            throw new RuntimeException("Casier introuvable.");
        }

        return doc;
    }

    private Locker mapDocumentToLocker(Document doc) {
        Locker locker = new Locker(
                doc.getInteger("port_id"),
                doc.getInteger("number")
        );
        locker.setId(doc.getObjectId("_id"));

        String statusStr = doc.getString("status");
        if (statusStr != null) {
            locker.setStatus(LockerStatus.valueOf(statusStr.toUpperCase()));
        }

        if (doc.containsKey("box_id") && doc.get("box_id") != null) {
            locker.setBoxId(doc.getObjectId("box_id"));
        }
        if (doc.containsKey("reserved_for_order_id") && doc.get("reserved_for_order_id") != null) {
            locker.setReservedForOrderId(doc.getObjectId("reserved_for_order_id"));
        }

        locker.setMaintenanceReason(doc.getString("maintenance_reason"));
        locker.setLastUsed(doc.getString("last_used"));

        return locker;
    }
}