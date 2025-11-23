package fr.esgi.galapagos.model.neo4j;

import java.util.Map;

public class Island {
    private String name;
    private double lat;
    private double lon;
    private double areaKm2;

    public Island(String name, double lat, double lon, double areaKm2) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.areaKm2 = areaKm2;
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "name", name,
                "lat", lat,
                "lon", lon,
                "area", areaKm2
        );
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