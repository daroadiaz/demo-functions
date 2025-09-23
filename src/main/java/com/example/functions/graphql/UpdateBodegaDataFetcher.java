package com.example.functions.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import com.example.functions.service.DatabaseService;
import com.example.functions.dto.BodegaDTO;
import java.util.Map;

public class UpdateBodegaDataFetcher implements DataFetcher<BodegaDTO> {
    private DatabaseService dbService = new DatabaseService();

    @Override
    public BodegaDTO get(DataFetchingEnvironment environment) throws Exception {
        String idStr = environment.getArgument("id");
        Long id = Long.parseLong(idStr);
        Map<String, Object> input = environment.getArgument("input");

        BodegaDTO bodega = new BodegaDTO();
        bodega.setCodigo((String) input.get("codigo"));
        bodega.setNombre((String) input.get("nombre"));
        bodega.setDireccion((String) input.get("direccion"));
        bodega.setTelefono((String) input.get("telefono"));
        bodega.setCapacidadMaxima((Integer) input.get("capacidadMaxima"));

        Integer espacioUtilizado = (Integer) input.get("espacioUtilizado");
        bodega.setEspacioUtilizado(espacioUtilizado != null ? espacioUtilizado : 0);

        Boolean activo = (Boolean) input.get("activo");
        bodega.setActivo(activo != null ? activo : true);

        return dbService.updateBodega(id, bodega);
    }
}