package fr.esgi.galapagos.model.mongodb;

import fr.esgi.galapagos.model.enums.BoxStatus;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Box {
    private ObjectId id;
    private ObjectId orderId;
    private ObjectId clientId;
    private int number;
    private BoxStatus status;
    private String content;

    public Box(ObjectId orderId, ObjectId clientId, int number, BoxStatus status, String content) {
        this.id = new ObjectId();
        this.orderId = orderId;
        this.clientId = clientId;
        this.number = number;
        this.status = status;
        this.content = content;
    }

    public ObjectId getId() { return id; }

    public Document toDocument() {
        return new Document("_id", id)
                .append("order_id", orderId)
                .append("client_id", clientId)
                .append("number", number)
                .append("status", status.name().toLowerCase())
                .append("content", content);
    }

    public ObjectId getOrderId() {
        return orderId;
    }

    public ObjectId getClientId() {
        return clientId;
    }

    public int getNumber() {
        return number;
    }

    public BoxStatus getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setOrderId(ObjectId orderId) {
        this.orderId = orderId;
    }

    public void setClientId(ObjectId clientId) {
        this.clientId = clientId;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setStatus(BoxStatus status) {
        this.status = status;
    }

    public void setContent(String content) {
        this.content = content;
    }

}