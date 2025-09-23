package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import com.example.functions.dto.BodegaDTO;
import java.util.*;
import java.util.stream.Collectors;

public class WarehouseCapacityAnalysisDataFetcher implements DataFetcher<Map<String, Object>> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
        List<BodegaDTO> bodegas = dbService.listBodegas();

        Map<String, Object> analysis = new HashMap<>();

        // Summary statistics
        int totalCapacity = bodegas.stream()
            .mapToInt(BodegaDTO::getCapacidadMaxima)
            .sum();
        int totalUsed = bodegas.stream()
            .mapToInt(BodegaDTO::getEspacioUtilizado)
            .sum();
        int totalAvailable = totalCapacity - totalUsed;

        analysis.put("totalWarehouses", bodegas.size());
        analysis.put("totalCapacity", totalCapacity);
        analysis.put("totalUsed", totalUsed);
        analysis.put("totalAvailable", totalAvailable);
        analysis.put("averageUtilization",
            totalCapacity > 0 ? (totalUsed * 100.0) / totalCapacity : 0);

        // Warehouse details
        List<Map<String, Object>> warehouseAnalysis = bodegas.stream()
            .map(b -> {
                Map<String, Object> warehouse = new HashMap<>();
                warehouse.put("id", b.getId().toString());
                warehouse.put("name", b.getNombre());
                warehouse.put("capacity", b.getCapacidadMaxima());
                warehouse.put("used", b.getEspacioUtilizado());
                warehouse.put("available", b.getCapacidadMaxima() - b.getEspacioUtilizado());
                warehouse.put("utilizationPercentage",
                    b.getCapacidadMaxima() > 0 ?
                    (b.getEspacioUtilizado() * 100.0) / b.getCapacidadMaxima() : 0);
                return warehouse;
            })
            .collect(Collectors.toList());

        analysis.put("warehouses", warehouseAnalysis);

        return analysis;
    }
}