package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import com.example.functions.dto.BodegaDTO;
import java.util.HashMap;
import java.util.Map;

public class TransferInventoryDataFetcher implements DataFetcher<Map<String, Object>> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
        String fromIdStr = environment.getArgument("from");
        String toIdStr = environment.getArgument("to");
        Integer cantidad = environment.getArgument("cantidad");

        Long fromId = Long.parseLong(fromIdStr);
        Long toId = Long.parseLong(toIdStr);

        // Get bodegas
        BodegaDTO fromBodega = dbService.getBodega(fromId);
        BodegaDTO toBodega = dbService.getBodega(toId);

        if (fromBodega == null || toBodega == null) {
            throw new RuntimeException("One or both warehouses not found");
        }

        // Check capacity
        int availableSpace = toBodega.getCapacidadMaxima() - toBodega.getEspacioUtilizado();
        if (cantidad > availableSpace) {
            throw new RuntimeException("Insufficient capacity in destination warehouse");
        }

        if (cantidad > fromBodega.getEspacioUtilizado()) {
            throw new RuntimeException("Insufficient inventory in source warehouse");
        }

        // Update warehouses
        fromBodega.setEspacioUtilizado(fromBodega.getEspacioUtilizado() - cantidad);
        toBodega.setEspacioUtilizado(toBodega.getEspacioUtilizado() + cantidad);

        BodegaDTO updatedFrom = dbService.updateBodega(fromId, fromBodega);
        BodegaDTO updatedTo = dbService.updateBodega(toId, toBodega);

        // Prepare response
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", String.format("Successfully transferred %d units from %s to %s",
            cantidad, fromBodega.getNombre(), toBodega.getNombre()));
        result.put("fromBodega", createBodegaMap(updatedFrom));
        result.put("toBodega", createBodegaMap(updatedTo));
        result.put("quantityTransferred", cantidad);

        return result;
    }

    private Map<String, Object> createBodegaMap(BodegaDTO bodega) {
        Map<String, Object> bodegaMap = new HashMap<>();
        bodegaMap.put("id", bodega.getId().toString());
        bodegaMap.put("codigo", bodega.getCodigo());
        bodegaMap.put("nombre", bodega.getNombre());
        bodegaMap.put("direccion", bodega.getDireccion());
        bodegaMap.put("telefono", bodega.getTelefono());
        bodegaMap.put("capacidadMaxima", bodega.getCapacidadMaxima());
        bodegaMap.put("espacioUtilizado", bodega.getEspacioUtilizado());
        bodegaMap.put("espacioDisponible", bodega.getCapacidadMaxima() - bodega.getEspacioUtilizado());
        bodegaMap.put("utilizacionPorcentaje",
            (bodega.getEspacioUtilizado() * 100.0) / bodega.getCapacidadMaxima());
        bodegaMap.put("activo", bodega.getActivo());
        return bodegaMap;
    }
}