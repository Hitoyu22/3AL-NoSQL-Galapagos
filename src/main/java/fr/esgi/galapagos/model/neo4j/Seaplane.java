package fr.esgi.galapagos.model.neo4j;

import fr.esgi.galapagos.model.enums.SeaplaneStatus;

import java.util.Map;

public class Seaplane {
    private String id;
    private String model;
    private int boxCapacity;
    private double fuelConsumptionKm;
    private double cruiseSpeedKmh;
    private SeaplaneStatus status;

    public Seaplane(String id, String model, int boxCapacity, double fuelConsumptionKm, double cruiseSpeedKmh, SeaplaneStatus status) {
        this.id = id;
        this.model = model;
        this.boxCapacity = boxCapacity;
        this.fuelConsumptionKm = fuelConsumptionKm;
        this.cruiseSpeedKmh = cruiseSpeedKmh;
        this.status = status;
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "id", id,
                "model", model,
                "capacity", boxCapacity,
                "conso", fuelConsumptionKm,
                "speed", cruiseSpeedKmh,
                "status", status.name().toLowerCase()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getBoxCapacity() {
        return boxCapacity;
    }

    public void setBoxCapacity(int boxCapacity) {
        this.boxCapacity = boxCapacity;
    }

    public double getFuelConsumptionKm() {
        return fuelConsumptionKm;
    }

    public void setFuelConsumptionKm(double fuelConsumptionKm) {
        this.fuelConsumptionKm = fuelConsumptionKm;
    }

    public double getCruiseSpeedKmh() {
        return cruiseSpeedKmh;
    }

    public void setCruiseSpeedKmh(double cruiseSpeedKmh) {
        this.cruiseSpeedKmh = cruiseSpeedKmh;
    }

    public SeaplaneStatus getStatus() {
        return status;
    }

    public void setStatus(SeaplaneStatus status) {
        this.status = status;
    }

}