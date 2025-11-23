package fr.esgi.galapagos.model.mongodb;

import fr.esgi.galapagos.model.enums.DeliveryStatus;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.List;

public class Delivery {
    private ObjectId id;
    private ObjectId orderId;
    private String seaplaneId;
    private DeliveryStatus status;
    private String departureDate;
    private String arrivalDate;
    private String scheduledDeparture;
    private String delayReason;
    private List<String> plannedRoute;
    private String currentPort;
    private String destinationPort;
    private List<ObjectId> transportedBoxes;
    private double totalDistanceKm;
    private double estimatedFuelL;

    public Delivery(ObjectId orderId, String seaplaneId, DeliveryStatus status) {
        this.id = new ObjectId();
        this.orderId = orderId;
        this.seaplaneId = seaplaneId;
        this.status = status;
    }

    public Delivery departureDate(String d) { this.departureDate = d; return this; }
    public Delivery arrivalDate(String d) { this.arrivalDate = d; return this; }
    public Delivery scheduledDeparture(String d) { this.scheduledDeparture = d; return this; }
    public Delivery delayReason(String r) { this.delayReason = r; return this; }
    public Delivery route(List<String> r, String dest, double dist) {
        this.plannedRoute = r;
        this.destinationPort = dest;
        this.totalDistanceKm = dist;
        return this;
    }
    public Delivery currentStatus(String port, double fuel) {
        this.currentPort = port;
        this.estimatedFuelL = fuel;
        return this;
    }
    public Delivery boxes(List<ObjectId> b) { this.transportedBoxes = b; return this; }

    public Document toDocument() {
        Document doc = new Document("_id", id)
                .append("order_id", orderId)
                .append("seaplane_id", seaplaneId)
                .append("status", status.name().toLowerCase())
                .append("planned_route", plannedRoute)
                .append("destination_port", destinationPort);

        if (departureDate != null) doc.append("departure_date", departureDate);
        if (arrivalDate != null) doc.append("arrival_date", arrivalDate);
        if (scheduledDeparture != null) doc.append("scheduled_departure", scheduledDeparture);
        if (delayReason != null) doc.append("delay_reason", delayReason);
        if (currentPort != null) doc.append("current_port", currentPort);
        if (transportedBoxes != null) doc.append("transported_boxes", transportedBoxes);
        if (totalDistanceKm > 0) doc.append("total_distance_km", totalDistanceKm);
        if (estimatedFuelL > 0) doc.append("estimated_fuel_l", estimatedFuelL);

        return doc;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getOrderId() {
        return orderId;
    }

    public void setOrderId(ObjectId orderId) {
        this.orderId = orderId;
    }

    public String getSeaplaneId() {
        return seaplaneId;
    }

    public void setSeaplaneId(String seaplaneId) {
        this.seaplaneId = seaplaneId;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(String arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getScheduledDeparture() {
        return scheduledDeparture;
    }

    public void setScheduledDeparture(String scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }

    public String getDelayReason() {
        return delayReason;
    }

    public void setDelayReason(String delayReason) {
        this.delayReason = delayReason;
    }

    public List<String> getPlannedRoute() {
        return plannedRoute;
    }

    public void setPlannedRoute(List<String> plannedRoute) {
        this.plannedRoute = plannedRoute;
    }

    public String getCurrentPort() {
        return currentPort;
    }

    public void setCurrentPort(String currentPort) {
        this.currentPort = currentPort;
    }

    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    public List<ObjectId> getTransportedBoxes() {
        return transportedBoxes;
    }

    public void setTransportedBoxes(List<ObjectId> transportedBoxes) {
        this.transportedBoxes = transportedBoxes;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public double getEstimatedFuelL() {
        return estimatedFuelL;
    }

    public void setEstimatedFuelL(double estimatedFuelL) {
        this.estimatedFuelL = estimatedFuelL;
    }

}