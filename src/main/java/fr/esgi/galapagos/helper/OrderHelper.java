package fr.esgi.galapagos.helper;

import fr.esgi.galapagos.model.enums.OrderStatus;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderHelper {

    public record OrderInput(
            String id,
            String clientId,
            String priority,
            String deliveryPort,
            List<OrderedProductInput> products,
            Integer boxCount,
            Double totalWeightKg,
            OrderStatus status
    ) {}

    public record OrderedProductInput(String productId, int quantity) {}

    public static OrderInput extractOrderInput(DataFetchingEnvironment env) {
        List<Map<String, Object>> productsRaw = env.getArgument("products");
        List<OrderedProductInput> productsList = null;

        if (productsRaw != null) {
            productsList = productsRaw.stream()
                    .map(m -> new OrderedProductInput((String) m.get("productId"), (int) m.get("quantity")))
                    .collect(Collectors.toList());
        }

        String statusStr = env.getArgument("status");
        OrderStatus status = statusStr != null ? OrderStatus.valueOf(statusStr) : null;

        return new OrderInput(
                env.getArgument("id"),
                env.getArgument("clientId"),
                env.getArgument("priority"),
                env.getArgument("deliveryPort"),
                productsList,
                env.getArgument("boxCount"),
                env.getArgument("totalWeightKg"),
                status
        );
    }
}