package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import com.example.functions.dto.ProductoDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductosDataFetcher implements DataFetcher<Map<String, Object>> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
        Integer limit = environment.getArgument("limit");
        Integer offset = environment.getArgument("offset");
        String categoria = environment.getArgument("categoria");
        Boolean activo = environment.getArgument("activo");

        List<ProductoDTO> allProductos = dbService.listProductos();

        // Apply filters
        if (categoria != null) {
            allProductos = allProductos.stream()
                .filter(p -> categoria.equals(p.getCategoria()))
                .collect(Collectors.toList());
        }

        if (activo != null) {
            allProductos = allProductos.stream()
                .filter(p -> activo.equals(p.getActivo()))
                .collect(Collectors.toList());
        }

        int totalCount = allProductos.size();

        // Apply pagination
        if (offset != null && offset > 0) {
            allProductos = allProductos.stream()
                .skip(offset)
                .collect(Collectors.toList());
        }

        if (limit != null && limit > 0) {
            allProductos = allProductos.stream()
                .limit(limit)
                .collect(Collectors.toList());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", allProductos);
        result.put("count", totalCount);
        result.put("hasMore", offset != null && limit != null &&
            (offset + limit) < totalCount);

        return result;
    }
}