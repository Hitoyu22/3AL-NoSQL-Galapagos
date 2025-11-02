package fr.esgi.galapagos.config;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConfig {

    private static final Dotenv dotenv = Dotenv.load();

    public static String getMongoUri() {
        return dotenv.get("MONGO_URI");
    }

    public static String getNeo4jUri() {
        return dotenv.get("NEO4J_URI");
    }

    public static String getNeo4jUser() {
        return dotenv.get("NEO4J_USER");
    }

    public static String getNeo4jPassword() {
        return dotenv.get("NEO4J_PASSWORD");
    }
}