package fr.esgi.galapagos.graphql;

import fr.esgi.galapagos.service.IslandService;
import graphql.GraphQL;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GraphQLProvider {

    private static final IslandService islandService = new IslandService();

    public static GraphQL createGraphQL() {
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();

        // On importe les différents fichiers graphqls afin quel le serveur puisse les exploiter
        List<String> schemaFiles = List.of(
                "graphql/root.graphqls",
                "graphql/island.graphqls"
        );

        for (String schemaFile : schemaFiles) {
            typeRegistry.merge(schemaParser.parse(loadSchema(schemaFile)));
        }

        // On définit un runtime dans le quel on revient définir chaque "nom" d'entité mise à disposition
        // version correspond au champ version dans Query (root.graphqls) et islands au tableau de Island (island.graphqls)
        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("version", env -> "1.0.0")
                        .dataFetcher("islands", environment -> {
                            String name = environment.getArgument("name");
                            return islandService.getIslands(name);
                        }))
                .build();

        return GraphQL.newGraphQL(new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring)).build();
    }

    private static String loadSchema(String filename) {
        try (InputStream stream = GraphQLProvider.class.getClassLoader().getResourceAsStream(filename)) {
            if (stream == null) {
                throw new RuntimeException("Fichier de schéma introuvable : " + filename);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du schéma : " + filename, e);
        }
    }
}