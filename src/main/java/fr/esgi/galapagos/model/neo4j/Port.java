package fr.esgi.galapagos.model.neo4j;

import fr.esgi.galapagos.model.mongodb.Locker;
import java.util.List;
import java.util.Map;

public class Port {
    private Integer id;
    private String name;
    private String islandName;
    private double lat;
    private double lon;
    private int nbLockers;
    private List<Locker> lockers;

    public Port(Integer id, String name, String islandName, double lat, double lon, int nbLockers) {
        this.id = id;
        this.name = name;
        this.islandName = islandName;
        this.lat = lat;
        this.lon = lon;
        this.nbLockers = nbLockers;
    }

    public Port(int id, String name, String islandName, double lat, double lon) {
        this(id, name, islandName, lat, lon, 0);
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "id", id,
                "name", name,
                "lat", lat,
                "lon", lon
        );
    }

    public Integer getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIslandName() { return islandName; }
    public void setIslandName(String islandName) { this.islandName = islandName; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public int getNbLockers() {
        if (lockers != null) {
            return lockers.size();
        }
        return nbLockers;
    }

    public void setNbLockers(int nbLockers) { this.nbLockers = nbLockers; }

    public List<Locker> getLockers() { return lockers; }
    public void setLockers(List<Locker> lockers) { this.lockers = lockers; }
}