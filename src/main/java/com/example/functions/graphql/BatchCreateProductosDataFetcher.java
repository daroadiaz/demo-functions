package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import com.example.functions.dto.ProductoDTO;
import java.math.BigDecimal;
import java.util.*;

public class BatchCreateProductosDataFetcher implements DataFetcher<Map<String, Object>> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
        List<Map<String, Object>> productos = environment.getArgument("productos");

        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> input : productos) {
            try {
                ProductoDTO producto = new ProductoDTO();
                producto.setCodigo((String) input.get("codigo"));
                producto.setNombre((String) input.get("nombre"));
                producto.setDescripcion((String) input.get("descripcion"));

                Object precio = input.get("precio");
                if (precio instanceof Double) {
                    producto.setPrecio(BigDecimal.valueOf((Double) precio));
                } else if (precio instanceof Integer) {
                    producto.setPrecio(BigDecimal.valueOf((Integer) precio));
                }

                producto.setStock((Integer) input.get("stock"));
                producto.setStockMinimo((Integer) input.get("stockMinimo"));
                producto.setCategoria((String) input.get("categoria"));

                Boolean activo = (Boolean) input.get("activo");
                producto.setActivo(activo != null ? activo : true);

                dbService.createProducto(producto);
                success++;
            } catch (Exception e) {
                failed++;
                errors.add("Failed to create product: " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("total", productos.size());
        result.put("errors", errors.isEmpty() ? null : errors);

        return result;
    }
}