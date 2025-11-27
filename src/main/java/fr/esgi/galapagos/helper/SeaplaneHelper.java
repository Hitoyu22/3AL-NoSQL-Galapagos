package fr.esgi.galapagos.helper;

import graphql.schema.DataFetchingEnvironment;

public class SeaplaneHelper {

    public record SeaplaneInput(
            String id,
            String model,
            Integer boxCapacity,
            Double fuelConsumptionKm,
            Double cruiseSpeedKmh,
            String status,
            Integer portId
    ) {}

    public static SeaplaneInput extractSeaplaneInput(DataFetchingEnvironment env) {
        return new SeaplaneInput(
                env.getArgument("id"),
                env.getArgument("model"),
                env.getArgument("boxCapacity"),
                env.getArgument("fuelConsumptionKm"),
                env.getArgument("cruiseSpeedKmh"),
                env.getArgument("status"),
                env.getArgument("portId")
        );
    }
}
