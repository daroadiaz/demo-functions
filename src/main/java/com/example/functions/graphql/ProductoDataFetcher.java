package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import com.example.functions.dto.ProductoDTO;

public class ProductoDataFetcher implements DataFetcher<ProductoDTO> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public ProductoDTO get(DataFetchingEnvironment environment) throws Exception {
        String idStr = environment.getArgument("id");
        Long id = Long.parseLong(idStr);
        return dbService.getProducto(id);
    }
}