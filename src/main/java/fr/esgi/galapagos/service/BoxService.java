package fr.esgi.galapagos.service;

import fr.esgi.galapagos.config.MongoConnection;
import fr.esgi.galapagos.model.enums.BoxStatus;
import fr.esgi.galapagos.model.mongodb.Box;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class BoxService {

    private final MongoCollection<Document> boxCollection;

    public BoxService() {
        this.boxCollection = MongoConnection.getDatabase().getCollection("boxes");
    }

    public List<Box> getBoxes(String id, String orderId, String clientId, String status) {
        List<Box> boxes = new ArrayList<>();

        Document filter = new Document();

        if (id != null && !id.isEmpty()) {
            try {
                filter.append("_id", new ObjectId(id));
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        if (orderId != null && !orderId.isEmpty()) {
            try {
                filter.append("order_id", new ObjectId(orderId));
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        if (clientId != null && !clientId.isEmpty()) {
            try {
                filter.append("client_id", new ObjectId(clientId));
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        if (status != null && !status.isEmpty()) {
            filter.append("status", status.toLowerCase());
        }

        try (MongoCursor<Document> cursor = boxCollection.find(filter).iterator()) {
            while (cursor.hasNext()) {
                boxes.add(documentToBox(cursor.next()));
            }
        }

        return boxes;
    }

    private Box documentToBox(Document doc) {
        Box box = new Box(
                (ObjectId) doc.get("order_id"),
                (ObjectId) doc.get("client_id"),
                doc.getInteger("number"),
                BoxStatus.valueOf(doc.getString("status").toUpperCase()),
                doc.getString("content")
        );

        box.setId((ObjectId) doc.get("_id"));
        return box;
    }
}