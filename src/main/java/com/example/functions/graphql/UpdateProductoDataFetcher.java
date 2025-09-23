package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import com.example.functions.dto.ProductoDTO;
import java.math.BigDecimal;
import java.util.Map;

public class UpdateProductoDataFetcher implements DataFetcher<ProductoDTO> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public ProductoDTO get(DataFetchingEnvironment environment) throws Exception {
        String idStr = environment.getArgument("id");
        Long id = Long.parseLong(idStr);
        Map<String, Object> input = environment.getArgument("input");

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

        return dbService.updateProducto(id, producto);
    }
}