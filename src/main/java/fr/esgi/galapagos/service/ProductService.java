package fr.esgi.galapagos.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import fr.esgi.galapagos.config.MongoConnection;
import fr.esgi.galapagos.model.mongodb.Product;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProductService {

    private final MongoCollection<Document> collection;

    public ProductService() {
        this.collection = MongoConnection.getDatabase().getCollection("products");
    }

    public List<Product> getProducts(String id, String name) {
        List<Product> products = new ArrayList<>();
        Bson filter = new Document();

        if (id != null) filter = Filters.eq("_id", new ObjectId(id));
        else if (name != null) filter = Filters.regex("name", Pattern.compile(name, Pattern.CASE_INSENSITIVE));

        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            while (cursor.hasNext()) products.add(mapToProduct(cursor.next()));
        }
        return products;
    }

    public Product createProduct(String name, String desc, int stock, double weight, double price) {
        Product product = new Product(name, desc, stock, weight, price);
        collection.insertOne(product.toDocument());
        return product;
    }

    public Product updateProduct(String id, String name, String desc, Integer stock, Double weight, Double price) {
        List<Bson> updates = new ArrayList<>();
        if (name != null) updates.add(Updates.set("name", name));
        if (desc != null) updates.add(Updates.set("description", desc));
        if (stock != null) updates.add(Updates.set("stock_available", stock));
        if (weight != null) updates.add(Updates.set("weight_kg", weight));
        if (price != null) updates.add(Updates.set("unit_price", price));

        if (updates.isEmpty()) throw new IllegalArgumentException("Rien à mettre à jour");

        collection.updateOne(Filters.eq("_id", new ObjectId(id)), Updates.combine(updates));
        return getProducts(id, null).getFirst();
    }

    public boolean deleteProduct(String id) {
        return collection.findOneAndDelete(Filters.eq("_id", new ObjectId(id))) != null;
    }

    private Product mapToProduct(Document doc) {
        Product p = new Product(
                doc.getString("name"), doc.getString("description"),
                doc.getInteger("stock_available"), doc.getDouble("weight_kg"),
                doc.getDouble("unit_price")
        );
        p.setId(doc.getObjectId("_id"));
        return p;
    }
}