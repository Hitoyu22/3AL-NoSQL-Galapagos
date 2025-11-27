package fr.esgi.galapagos.model.mongodb;

import fr.esgi.galapagos.model.enums.LockerStatus;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Locker {
    private ObjectId id;
    private int portId;
    private int number;
    private LockerStatus status;
    private ObjectId boxId;
    private ObjectId reservedForOrderId;
    private String maintenanceReason;
    private String lastUsed;

    public Locker(int portId, int number) {
        this.id = new ObjectId();
        this.portId = portId;
        this.number = number;
        this.status = LockerStatus.EMPTY;
    }

    public Document toDocument() {
        Document doc = new Document("_id", id)
                .append("port_id", portId)
                .append("number", number)
                .append("status", status.name().toLowerCase())
                .append("box_id", boxId)
                .append("last_used", lastUsed);

        if (reservedForOrderId != null) doc.append("reserved_for_order_id", reservedForOrderId);
        if (maintenanceReason != null) doc.append("maintenance_reason", maintenanceReason);

        return doc;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getPortId() {
        return portId;
    }

    public void setPortId(int portId) {
        this.portId = portId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public LockerStatus getStatus() {
        return status;
    }

    public void setStatus(LockerStatus status) {
        this.status = status;
    }

    public ObjectId getBoxId() {
        return boxId;
    }

    public void setBoxId(ObjectId boxId) {
        this.boxId = boxId;
    }

    public ObjectId getReservedForOrderId() {
        return reservedForOrderId;
    }

    public void setReservedForOrderId(ObjectId reservedForOrderId) {
        this.reservedForOrderId = reservedForOrderId;
    }

    public String getMaintenanceReason() {
        return maintenanceReason;
    }

    public void setMaintenanceReason(String maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }

    public String getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(String lastUsed) {
        this.lastUsed = lastUsed;
    }
}