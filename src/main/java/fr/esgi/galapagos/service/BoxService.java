package fr.esgi.galapagos.service;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import fr.esgi.galapagos.config.MongoConnection;
import fr.esgi.galapagos.model.enums.BoxStatus;
import fr.esgi.galapagos.model.mongodb.Box;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;

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
    public Box createBox(String orderId, String clientId, int number,
                         String status, String content) {
        try {
            BoxStatus boxStatus = (status == null || status.isEmpty())
                    ? BoxStatus.PENDING
                    : BoxStatus.valueOf(status.toUpperCase());

            Box box = new Box(
                    new ObjectId(orderId),
                    new ObjectId(clientId),
                    number,
                    boxStatus,
                    content
            );

            Document doc = box.toDocument();
            boxCollection.insertOne(doc);

            return box;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de la boîte: " + e.getMessage());
        }
    }

    public Box updateBox(String id, String orderId, String clientId, Integer number,
                         String status, String content) {
        try {
            Document update = new Document();

            if (orderId != null && !orderId.isEmpty()) {
                update.append("order_id", new ObjectId(orderId));
            }

            if (clientId != null && !clientId.isEmpty()) {
                update.append("client_id", new ObjectId(clientId));
            }

            if (number != null) {
                update.append("number", number);
            }

            if (status != null && !status.isEmpty()) {
                BoxStatus boxStatus = BoxStatus.valueOf(status.toUpperCase());
                update.append("status", boxStatus.toString());
            }

            if (content != null && !content.isEmpty()) {
                update.append("content", content);
            }

            if (update.isEmpty()) {
                return getBoxById(id);
            }

            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                    .returnDocument(AFTER);

            Document result = boxCollection.findOneAndUpdate(
                    eq("_id", new ObjectId(id)),
                    new Document("$set", update),
                    options
            );

            if (result == null) {
                return null;
            }

            return documentToBox(result);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la boîte: " + e.getMessage());
        }
    }

    public boolean deleteBox(String id) {
        try {
            long deletedCount = boxCollection.deleteOne(
                    eq("_id", new ObjectId(id))
            ).getDeletedCount();

            return deletedCount > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public Box getBoxById(String id) {
        try {
            Document doc = boxCollection.find(
                    eq("_id", new ObjectId(id))
            ).first();

            if (doc == null) {
                return null;
            }

            return documentToBox(doc);
        } catch (Exception e) {
            return null;
        }
    }
}