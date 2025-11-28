package fr.esgi.galapagos.helper;

import fr.esgi.galapagos.model.enums.BoxStatus;
import graphql.schema.DataFetchingEnvironment;

public class BoxHelper {

    public record BoxInput(
            String id,
            String orderId,
            String clientId,
            Integer number,
            BoxStatus status,
            String content
    ) {}

    public static BoxInput extractBoxInput(DataFetchingEnvironment env) {
        String statusStr = env.getArgument("status");
        BoxStatus status = statusStr != null ? BoxStatus.valueOf(statusStr) : null;

        return new BoxInput(
                env.getArgument("id"),
                env.getArgument("orderId"),
                env.getArgument("clientId"),
                env.getArgument("number"),
                status,
                env.getArgument("content")
        );
    }
}