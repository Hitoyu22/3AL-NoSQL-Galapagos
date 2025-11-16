package fr.esgi.galapagos.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jConnection {

    private static Driver driver;

    private Neo4jConnection() {
    }

    public static synchronized Driver getDriver() {
        if (driver == null) {
            String uri = DatabaseConfig.getNeo4jUri();
            String user = DatabaseConfig.getNeo4jUser();
            String password = DatabaseConfig.getNeo4jPassword();
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        }
        return driver;
    }

    public static void close() {
        if (driver != null) {
            driver.close();
            driver = null;
        }
    }
}