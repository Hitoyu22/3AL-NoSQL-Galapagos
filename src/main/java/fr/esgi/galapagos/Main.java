package fr.esgi.galapagos;

import com.sun.net.httpserver.HttpServer;
import fr.esgi.galapagos.config.MongoConnection;
import fr.esgi.galapagos.config.Neo4jConnection;
import fr.esgi.galapagos.graphql.GraphQLHandler;
import fr.esgi.galapagos.graphql.GraphQLProvider;
import graphql.GraphQL;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        try {
            MongoConnection.getMongoClient();
            Neo4jConnection.getDriver();

            GraphQL graphQL = GraphQLProvider.createGraphQL();

            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/graphql", new GraphQLHandler(graphQL));
            server.setExecutor(null);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                MongoConnection.close();
                Neo4jConnection.close();
                server.stop(0);
                System.out.println("Serveur arrêté.");
            }));

            server.start();

            System.out.println("Serveur GraphQL prêt sur http://localhost:8080/graphql");
            System.out.println("Appuyez sur Ctrl+C pour arrêter le serveur");

            Thread.currentThread().join();

        } catch (InterruptedException e) {
            System.out.println("Serveur interrompu");
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur HTTP: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Échec de la connexion à une base de données. Le serveur ne démarrera pas.");
            System.err.println("Erreur: " + e.getMessage());
        }
    }
}