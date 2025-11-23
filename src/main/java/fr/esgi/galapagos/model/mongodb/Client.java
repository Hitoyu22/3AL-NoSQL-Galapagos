package fr.esgi.galapagos.model.mongodb;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private ObjectId id;
    private String name;
    private String type;
    private String specialty;
    private String study;
    private String email;
    private List<ObjectId> orderHistory;

    public Client(String name, String type, String specialty, String study, String email) {
        this.id = new ObjectId();
        this.name = name;
        this.type = type;
        this.specialty = specialty;
        this.study = study;
        this.email = email;
        this.orderHistory = new ArrayList<>();
    }

    public ObjectId getId() { return id; }

    public Document toDocument() {
        return new Document("_id", id)
                .append("name", name)
                .append("type", type)
                .append("specialty", specialty)
                .append("study", study)
                .append("email", email)
                .append("order_history", orderHistory);
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<ObjectId> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(List<ObjectId> orderHistory) {
        this.orderHistory = orderHistory;
    }
}