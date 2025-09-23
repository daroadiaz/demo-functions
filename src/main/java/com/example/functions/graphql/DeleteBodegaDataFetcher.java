package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import java.util.HashMap;
import java.util.Map;

public class DeleteBodegaDataFetcher implements DataFetcher<Map<String, Object>> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
        String idStr = environment.getArgument("id");
        Long id = Long.parseLong(idStr);

        dbService.deleteBodega(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Bodega deleted successfully");
        result.put("id", idStr);

        return result;
    }
}