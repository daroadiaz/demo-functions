package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import com.example.functions.dto.ProductoDTO;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class InventoryAnalyticsDataFetcher implements DataFetcher<Map<String, Object>> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
        List<ProductoDTO> productos = dbService.listProductos();

        Map<String, Object> analytics = new HashMap<>();

        // Total products
        analytics.put("totalProducts", productos.size());

        // Total value
        BigDecimal totalValue = productos.stream()
            .map(p -> p.getPrecio().multiply(BigDecimal.valueOf(p.getStock())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        analytics.put("totalValue", totalValue.doubleValue());

        // Low stock products
        List<ProductoDTO> lowStockProducts = productos.stream()
            .filter(p -> p.getStock() <= p.getStockMinimo())
            .collect(Collectors.toList());
        analytics.put("lowStockProducts", lowStockProducts);

        // Category summary
        Map<String, List<ProductoDTO>> byCategory = productos.stream()
            .filter(p -> p.getCategoria() != null)
            .collect(Collectors.groupingBy(ProductoDTO::getCategoria));

        List<Map<String, Object>> categorySummary = new ArrayList<>();
        for (Map.Entry<String, List<ProductoDTO>> entry : byCategory.entrySet()) {
            Map<String, Object> summary = new HashMap<>();
            List<ProductoDTO> categoryProducts = entry.getValue();

            summary.put("category", entry.getKey());
            summary.put("count", categoryProducts.size());

            BigDecimal categoryValue = categoryProducts.stream()
                .map(p -> p.getPrecio().multiply(BigDecimal.valueOf(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            summary.put("totalValue", categoryValue.doubleValue());

            BigDecimal avgPrice = categoryProducts.stream()
                .map(ProductoDTO::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(categoryProducts.size()), 2, BigDecimal.ROUND_HALF_UP);
            summary.put("averagePrice", avgPrice.doubleValue());

            categorySummary.add(summary);
        }
        analytics.put("categorySummary", categorySummary);

        return analytics;
    }
}