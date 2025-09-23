package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import com.example.functions.dto.BodegaDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BodegasDataFetcher implements DataFetcher<Map<String, Object>> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
        Integer limit = environment.getArgument("limit");
        Integer offset = environment.getArgument("offset");
        Integer minCapacity = environment.getArgument("minCapacity");
        Boolean hasAvailableSpace = environment.getArgument("hasAvailableSpace");

        List<BodegaDTO> allBodegas = dbService.listBodegas();

        // Apply filters
        if (minCapacity != null) {
            allBodegas = allBodegas.stream()
                .filter(b -> b.getCapacidadMaxima() >= minCapacity)
                .collect(Collectors.toList());
        }

        if (Boolean.TRUE.equals(hasAvailableSpace)) {
            allBodegas = allBodegas.stream()
                .filter(b -> b.getEspacioUtilizado() < b.getCapacidadMaxima())
                .collect(Collectors.toList());
        }

        int totalCount = allBodegas.size();

        // Apply pagination
        if (offset != null && offset > 0) {
            allBodegas = allBodegas.stream()
                .skip(offset)
                .collect(Collectors.toList());
        }

        if (limit != null && limit > 0) {
            allBodegas = allBodegas.stream()
                .limit(limit)
                .collect(Collectors.toList());
        }

        // Transform bodegas to include calculated fields
        List<Map<String, Object>> transformedBodegas = allBodegas.stream()
            .map(b -> {
                Map<String, Object> bodegaMap = new HashMap<>();
                bodegaMap.put("id", b.getId());
                bodegaMap.put("codigo", b.getCodigo());
                bodegaMap.put("nombre", b.getNombre());
                bodegaMap.put("direccion", b.getDireccion());
                bodegaMap.put("telefono", b.getTelefono());
                bodegaMap.put("capacidadMaxima", b.getCapacidadMaxima());
                bodegaMap.put("espacioUtilizado", b.getEspacioUtilizado());
                bodegaMap.put("espacioDisponible", b.getCapacidadMaxima() - b.getEspacioUtilizado());
                bodegaMap.put("utilizacionPorcentaje",
                    (b.getEspacioUtilizado() * 100.0) / b.getCapacidadMaxima());
                bodegaMap.put("activo", b.getActivo());
                return bodegaMap;
            })
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("data", transformedBodegas);
        result.put("count", totalCount);
        result.put("hasMore", offset != null && limit != null &&
            (offset + limit) < totalCount);

        return result;
    }
}