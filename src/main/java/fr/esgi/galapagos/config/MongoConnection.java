package fr.esgi.galapagos.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {

    private static MongoClient mongoClient;

    private MongoConnection() {
    }

    public static synchronized MongoClient getMongoClient() {
        if (mongoClient == null) {
            String uri = DatabaseConfig.getMongoUri();
            mongoClient = MongoClients.create(uri);
        }
        return mongoClient;
    }

    public static MongoDatabase getDatabase() {
        String uri = DatabaseConfig.getMongoUri();
        String databaseName = uri.substring(uri.lastIndexOf('/') + 1);
        if (databaseName.contains("?")) {
            databaseName = databaseName.substring(0, databaseName.indexOf('?'));
        }
        return getMongoClient().getDatabase(databaseName);
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }
}