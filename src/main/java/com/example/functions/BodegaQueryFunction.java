package com.example.functions;

import com.example.functions.dto.BodegaDTO;
import com.example.functions.service.DatabaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class BodegaQueryFunction {

    private final ObjectMapper objectMapper;
    private final DatabaseService databaseService;

    public BodegaQueryFunction() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.databaseService = new DatabaseService();
    }

    @FunctionName("GetBodegas")
    public HttpResponseMessage getBodegas(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET},
            route = "bodegas",
            authLevel = AuthorizationLevel.ANONYMOUS
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        context.getLogger().info("GetBodegas function triggered");

        try {
            List<BodegaDTO> bodegas = getAllBodegas();

            String json = objectMapper.writeValueAsString(bodegas);

            return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build();

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error getting bodegas: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage())
                .build();
        }
    }

    @FunctionName("GetBodegaById")
    public HttpResponseMessage getBodegaById(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET},
            route = "bodegas/{id}",
            authLevel = AuthorizationLevel.ANONYMOUS
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("id") String id,
        final ExecutionContext context
    ) {
        context.getLogger().info("GetBodegaById function triggered for ID: " + id);

        try {
            Long bodegaId = Long.parseLong(id);
            BodegaDTO bodega = getBodegaById(bodegaId);

            if (bodega == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("Bodega no encontrada")
                    .build();
            }

            String json = objectMapper.writeValueAsString(bodega);

            return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build();

        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("ID inválido")
                .build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error getting bodega: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage())
                .build();
        }
    }

    private List<BodegaDTO> getAllBodegas() throws Exception {
        List<BodegaDTO> bodegas = new ArrayList<>();
        String sql = "SELECT * FROM BODEGAS ORDER BY ID";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bodegas.add(mapResultSetToBodega(rs));
            }
        }

        return bodegas;
    }

    private BodegaDTO getBodegaById(Long id) throws Exception {
        String sql = "SELECT * FROM BODEGAS WHERE ID = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBodega(rs);
                }
            }
        }

        return null;
    }

    private BodegaDTO mapResultSetToBodega(ResultSet rs) throws Exception {
        BodegaDTO bodega = new BodegaDTO();
        bodega.setId(rs.getLong("ID"));
        bodega.setCodigo(rs.getString("CODIGO"));
        bodega.setNombre(rs.getString("NOMBRE"));
        bodega.setDireccion(rs.getString("DIRECCION"));
        bodega.setTelefono(rs.getString("TELEFONO"));
        bodega.setCapacidadMaxima(rs.getInt("CAPACIDAD_MAXIMA"));
        bodega.setEspacioUtilizado(rs.getInt("ESPACIO_UTILIZADO"));
        bodega.setActivo(rs.getBoolean("ACTIVO"));

        if (rs.getTimestamp("FECHA_CREACION") != null) {
            bodega.setFechaCreacion(rs.getTimestamp("FECHA_CREACION").toLocalDateTime());
        }
        if (rs.getTimestamp("FECHA_ACTUALIZACION") != null) {
            bodega.setFechaActualizacion(rs.getTimestamp("FECHA_ACTUALIZACION").toLocalDateTime());
        }

        return bodega;
    }
}
