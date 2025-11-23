package fr.esgi.galapagos.model.mongodb;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Product {
    private ObjectId id;
    private String name;
    private String description;
    private Integer stockAvailable;
    private Double weightKg;
    private Double unitPrice;

    public Product(String name, String description, Integer stockAvailable, Double weightKg, Double unitPrice) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.stockAvailable = stockAvailable;
        this.weightKg = weightKg;
        this.unitPrice = unitPrice;
    }

    public ObjectId getId() {
        return id;
    }

    public Document toDocument() {
        return new Document("_id", id)
                .append("name", name)
                .append("description", description)
                .append("stock_available", stockAvailable)
                .append("weight_kg", weightKg)
                .append("unit_price", unitPrice);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(Integer stockAvailable) {
        this.stockAvailable = stockAvailable;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

}