package fr.esgi.galapagos.model.mongodb;

import fr.esgi.galapagos.model.enums.OrderStatus;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.List;
import java.util.stream.Collectors;

public class Order {
    private ObjectId id;
    private ObjectId clientId;
    private String orderDate;
    private OrderStatus status;
    private String priority;
    private String deliveryPort;
    private List<OrderedProduct> products;
    private int boxCount;
    private int boxesDelivered;
    private double totalWeightKg;

    public Order(ObjectId clientId, String orderDate, OrderStatus status, String priority, String deliveryPort, List<OrderedProduct> products, int boxCount, double totalWeightKg) {
        this.id = new ObjectId();
        this.clientId = clientId;
        this.orderDate = orderDate;
        this.status = status;
        this.priority = priority;
        this.deliveryPort = deliveryPort;
        this.products = products;
        this.boxCount = boxCount;
        this.totalWeightKg = totalWeightKg;
        this.boxesDelivered = 0;
    }

    public Document toDocument() {
        List<Document> productsDocs = products.stream()
                .map(OrderedProduct::toDocument)
                .collect(Collectors.toList());

        Document doc = new Document("_id", id)
                .append("client_id", clientId)
                .append("order_date", orderDate)
                .append("status", status.name().toLowerCase())
                .append("priority", priority)
                .append("delivery_port", deliveryPort)
                .append("products", productsDocs)
                .append("box_count", boxCount)
                .append("total_weight_kg", totalWeightKg);

        if (boxesDelivered > 0) {
            doc.append("boxes_delivered", boxesDelivered);
        }
        return doc;
    }

    public static class OrderedProduct {
        private ObjectId productId;
        private int quantity;
        private Product product;

        public OrderedProduct(ObjectId productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public OrderedProduct(Product product, int quantity) {
            this.product = product;
            this.productId = product.getId();
            this.quantity = quantity;
        }

        public Document toDocument() {
            return new Document("product_id", productId).append("quantity", quantity);
        }

        public ObjectId getProductId() {
            return productId;
        }

        public void setProductId(ObjectId productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getClientId() {
        return clientId;
    }

    public void setClientId(ObjectId clientId) {
        this.clientId = clientId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDeliveryPort() {
        return deliveryPort;
    }

    public void setDeliveryPort(String deliveryPort) {
        this.deliveryPort = deliveryPort;
    }

    public List<OrderedProduct> getProducts() {
        return products;
    }

    public void setProducts(List<OrderedProduct> products) {
        this.products = products;
    }

    public int getBoxCount() {
        return boxCount;
    }

    public void setBoxCount(int boxCount) {
        this.boxCount = boxCount;
    }

    public int getBoxesDelivered() {
        return boxesDelivered;
    }

    public void setBoxesDelivered(int boxesDelivered) {
        this.boxesDelivered = boxesDelivered;
    }

    public double getTotalWeightKg() {
        return totalWeightKg;
    }

    public void setTotalWeightKg(double totalWeightKg) {
        this.totalWeightKg = totalWeightKg;
    }
}