package fr.esgi.galapagos.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import fr.esgi.galapagos.config.MongoConnection;
import fr.esgi.galapagos.model.mongodb.Client;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClientService {

    private final MongoCollection<Document> clientCollection;

    public ClientService() {
        this.clientCollection = MongoConnection.getDatabase().getCollection("clients");
    }

    public List<Client> getClients(String id, String name) {
        List<Client> clients = new ArrayList<>();
        Bson filter = new Document();

        try {
            if (id != null && !id.isEmpty()) {
                filter = Filters.eq("_id", new ObjectId(id));
            } else if (name != null && !name.isEmpty()) {
                filter = Filters.regex("name", Pattern.compile(name, Pattern.CASE_INSENSITIVE));
            }

            try (MongoCursor<Document> cursor = clientCollection.find(filter).iterator()) {
                while (cursor.hasNext()) {
                    clients.add(documentToClient(cursor.next()));
                }
            }
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }

        return clients;
    }

    public Client createClient(String name, String type, String specialty, String study, String email) {
        Client client = new Client(name, type, specialty, study, email);
        clientCollection.insertOne(client.toDocument());
        return client;
    }

    public Client updateClient(String id, String name, String type, String specialty, String study, String email) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Format d'ID invalide : " + id);
        }

        List<Bson> updates = new ArrayList<>();
        if (name != null) updates.add(Updates.set("name", name));
        if (type != null) updates.add(Updates.set("type", type));
        if (specialty != null) updates.add(Updates.set("specialty", specialty));
        if (study != null) updates.add(Updates.set("study", study));
        if (email != null) updates.add(Updates.set("email", email));

        if (updates.isEmpty()) {
            throw new IllegalArgumentException("Aucune donnée à mettre à jour fournie.");
        }

        Document updatedDoc = clientCollection.findOneAndUpdate(
                Filters.eq("_id", objectId),
                Updates.combine(updates)
        );

        if (updatedDoc == null) {
            throw new RuntimeException("Client introuvable avec l'ID : " + id);
        }

        Document freshDoc = clientCollection.find(Filters.eq("_id", objectId)).first();
        return documentToClient(freshDoc);
    }

    public boolean deleteClient(String id) {
        try {
            Document result = clientCollection.findOneAndDelete(Filters.eq("_id", new ObjectId(id)));
            return result != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Client documentToClient(Document doc) {
        if (doc == null) return null;

        Client client = new Client(
                doc.getString("name"),
                doc.getString("type"),
                doc.getString("specialty"),
                doc.getString("study"),
                doc.getString("email")
        );
        client.setId(doc.getObjectId("_id"));

        Object historyObj = doc.get("order_history");
        if (historyObj instanceof List) {
            client.setOrderHistory((List<ObjectId>) historyObj);
        }

        return client;
    }
}