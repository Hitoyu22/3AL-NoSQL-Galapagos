package fr.esgi.galapagos.model.neo4j;

public class Location {
    private String portName;
    private Island island;
    private double lat;
    private double lon;

    public Location(String portName, Island island, double lat, double lon) {
        this.portName = portName;
        this.island = island;
        this.lat = lat;
        this.lon = lon;
    }

    public String getPortName() { return portName; }
    public Island getIsland() { return island; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public void setPortName(String portName) { this.portName = portName; }
    public void setIsland(Island island) { this.island = island; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLon(double lon) { this.lon = lon; }
}