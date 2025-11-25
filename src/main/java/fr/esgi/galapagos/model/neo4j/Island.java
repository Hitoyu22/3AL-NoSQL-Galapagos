package fr.esgi.galapagos.model.neo4j;

import java.util.Map;

public class Island {
    private int id;
    private String name;
    private double lat;
    private double lon;
    private double areaKm2;

    public Island(int id, String name, double lat, double lon, double areaKm2) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.areaKm2 = areaKm2;
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "id", id,
                "name", name,
                "lat", lat,
                "lon", lon,
                "area", areaKm2
        );
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setAreaKm2(double areaKm2) {
        this.areaKm2 = areaKm2;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getAreaKm2() {
        return areaKm2;
    }

}