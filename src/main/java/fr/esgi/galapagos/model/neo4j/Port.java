package fr.esgi.galapagos.model.neo4j;

import java.util.Map;

public class Port {
    private String name;
    private String islandName;
    private double lat;
    private double lon;
    private int nbLockers;

    public Port(String name, String islandName, double lat, double lon, int nbLockers) {
        this.name = name;
        this.islandName = islandName;
        this.lat = lat;
        this.lon = lon;
        this.nbLockers = nbLockers;
    }

    public String getName() { return name; }
    public String getIslandName() { return islandName; }
    public int getNbLockers() { return nbLockers; }

    public Map<String, Object> toMap() {
        return Map.of(
                "name", name,
                "lat", lat,
                "lon", lon
        );
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIslandName(String islandName) {
        this.islandName = islandName;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setNbLockers(int nbLockers) {
        this.nbLockers = nbLockers;
    }

}