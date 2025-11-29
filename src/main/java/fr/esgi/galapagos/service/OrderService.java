package fr.esgi.galapagos.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import fr.esgi.galapagos.config.MongoConnection;
import fr.esgi.galapagos.model.enums.OrderStatus;
import fr.esgi.galapagos.model.mongodb.Order;
import fr.esgi.galapagos.model.mongodb.Product;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderService {

    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> productCollection;

    public OrderService() {
        this.collection = MongoConnection.getDatabase().getCollection("orders");
        this.productCollection = MongoConnection.getDatabase().getCollection("products");
    }

    public List<Order> getOrders(String id, String clientId, OrderStatus status) {
        List<Order> orders = new ArrayList<>();
        Bson filter = new Document();

        if (id != null) filter = Filters.eq("_id", new ObjectId(id));
        else {
            List<Bson> filters = new ArrayList<>();
            if (clientId != null) filters.add(Filters.eq("client_id", new ObjectId(clientId)));
            if (status != null) filters.add(Filters.eq("status", status.name().toLowerCase()));
            if (!filters.isEmpty()) filter = Filters.and(filters);
        }

        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            while (cursor.hasNext()) orders.add(mapToOrder(cursor.next()));
        }
        return orders;
    }

    public Order createOrder(String clientId, String priority, String port,
                             List<Order.OrderedProduct> products, int boxCount, double weight) {

        Order order = new Order(
                new ObjectId(clientId),
                LocalDateTime.now().toString(),
                OrderStatus.PENDING,
                priority,
                port,
                products,
                boxCount,
                weight
        );
        collection.insertOne(order.toDocument());
        return order;
    }

    public Order updateStatus(String id, OrderStatus status) {
        collection.updateOne(Filters.eq("_id", new ObjectId(id)),
                Updates.set("status", status.name().toLowerCase()));
        return getOrders(id, null, null).getFirst();
    }

    public boolean deleteOrder(String id) {
        return collection.findOneAndDelete(Filters.eq("_id", new ObjectId(id))) != null;
    }

    @SuppressWarnings("unchecked")
    private Order mapToOrder(Document doc) {
        List<Document> prodsDoc = (List<Document>) doc.get("products");
        List<Order.OrderedProduct> products = prodsDoc.stream()
                .map(d -> {
                    ObjectId prodId = d.getObjectId("product_id");
                    int qty = d.getInteger("quantity");
                    Document prodDoc = productCollection.find(Filters.eq("_id", prodId)).first();

                    if (prodDoc != null) {
                        Product p = new Product(
                                prodDoc.getString("name"),
                                prodDoc.getString("description"),
                                prodDoc.getInteger("stock_available"),
                                prodDoc.getDouble("weight_kg"),
                                prodDoc.getDouble("unit_price")
                        );
                        p.setId(prodDoc.getObjectId("_id"));
                        return new Order.OrderedProduct(p, qty);
                    }
                    return new Order.OrderedProduct(prodId, qty);
                })
                .collect(Collectors.toList());

        Order o = new Order(
                doc.getObjectId("client_id"),
                doc.getString("order_date"),
                OrderStatus.valueOf(doc.getString("status").toUpperCase()),
                doc.getString("priority"),
                doc.getString("delivery_port"),
                products,
                doc.getInteger("box_count"),
                doc.getDouble("total_weight_kg")
        );
        o.setId(doc.getObjectId("_id"));
        if (doc.containsKey("boxes_delivered")) {
            o.setBoxesDelivered(doc.getInteger("boxes_delivered"));
        }
        return o;
    }
}