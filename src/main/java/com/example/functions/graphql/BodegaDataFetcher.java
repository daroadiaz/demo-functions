package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import com.example.functions.dto.BodegaDTO;
import java.util.HashMap;
import java.util.Map;

public class BodegaDataFetcher implements DataFetcher<Map<String, Object>> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
        String idStr = environment.getArgument("id");
        Long id = Long.parseLong(idStr);
        BodegaDTO bodega = dbService.getBodega(id);

        if (bodega == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", bodega.getId());
        result.put("codigo", bodega.getCodigo());
        result.put("nombre", bodega.getNombre());
        result.put("direccion", bodega.getDireccion());
        result.put("telefono", bodega.getTelefono());
        result.put("capacidadMaxima", bodega.getCapacidadMaxima());
        result.put("espacioUtilizado", bodega.getEspacioUtilizado());
        result.put("espacioDisponible", bodega.getCapacidadMaxima() - bodega.getEspacioUtilizado());
        result.put("utilizacionPorcentaje",
            (bodega.getEspacioUtilizado() * 100.0) / bodega.getCapacidadMaxima());
        result.put("activo", bodega.getActivo());

        return result;
    }
}