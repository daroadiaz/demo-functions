package com.example.functions;

import com.example.functions.dto.BodegaDTO;
import com.example.functions.service.DatabaseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BodegaEventConsumer {

    private static final Logger logger = Logger.getLogger(BodegaEventConsumer.class.getName());
    private final ObjectMapper objectMapper;
    private final DatabaseService databaseService;

    public BodegaEventConsumer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.databaseService = new DatabaseService();
    }

    @FunctionName("BodegaEventConsumer")
    public void run(
        @EventGridTrigger(name = "event") String content,
        final ExecutionContext context
    ) {
        context.getLogger().info("=== BodegaEventConsumer triggered ===");
        context.getLogger().info("Event content: " + content);

        try {
            JsonNode event = objectMapper.readTree(content);
            String eventType = event.get("eventType").asText();
            JsonNode dataNode = event.get("data");

            BodegaDTO bodega = objectMapper.treeToValue(dataNode, BodegaDTO.class);

            context.getLogger().info("Processing event type: " + eventType);
            context.getLogger().info("Bodega: " + bodega.getCodigo());

            switch (eventType) {
                case "BodegaCreada":
                    handleBodegaCreada(bodega, context);
                    break;
                case "BodegaActualizada":
                    handleBodegaActualizada(bodega, context);
                    break;
                case "BodegaEliminada":
                    handleBodegaEliminada(bodega, context);
                    break;
                default:
                    context.getLogger().warning("Unknown event type: " + eventType);
            }

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error processing event: " + e.getMessage(), e);
            throw new RuntimeException("Failed to process event", e);
        }
    }

    private void handleBodegaCreada(BodegaDTO bodega, ExecutionContext context) {
        String sql = "INSERT INTO BODEGAS (ID, CODIGO, NOMBRE, DIRECCION, TELEFONO, CAPACIDAD_MAXIMA, ESPACIO_UTILIZADO, ACTIVO, FECHA_CREACION, FECHA_ACTUALIZACION) " +
                     "VALUES (BODEGA_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bodega.getCodigo());
            stmt.setString(2, bodega.getNombre());
            stmt.setString(3, bodega.getDireccion());
            stmt.setString(4, bodega.getTelefono());
            stmt.setInt(5, bodega.getCapacidadMaxima() != null ? bodega.getCapacidadMaxima() : 0);
            stmt.setInt(6, bodega.getEspacioUtilizado() != null ? bodega.getEspacioUtilizado() : 0);
            stmt.setBoolean(7, bodega.getActivo() != null ? bodega.getActivo() : true);
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

            int rows = stmt.executeUpdate();
            conn.commit();

            context.getLogger().info("Bodega creada exitosamente. Rows affected: " + rows);

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error creating bodega: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create bodega", e);
        }
    }

    private void handleBodegaActualizada(BodegaDTO bodega, ExecutionContext context) {
        String sql = "UPDATE BODEGAS SET CODIGO = ?, NOMBRE = ?, DIRECCION = ?, TELEFONO = ?, CAPACIDAD_MAXIMA = ?, ESPACIO_UTILIZADO = ?, ACTIVO = ?, FECHA_ACTUALIZACION = ? WHERE ID = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bodega.getCodigo());
            stmt.setString(2, bodega.getNombre());
            stmt.setString(3, bodega.getDireccion());
            stmt.setString(4, bodega.getTelefono());
            stmt.setInt(5, bodega.getCapacidadMaxima() != null ? bodega.getCapacidadMaxima() : 0);
            stmt.setInt(6, bodega.getEspacioUtilizado() != null ? bodega.getEspacioUtilizado() : 0);
            stmt.setBoolean(7, bodega.getActivo() != null ? bodega.getActivo() : true);
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(9, bodega.getId());

            int rows = stmt.executeUpdate();
            conn.commit();

            context.getLogger().info("Bodega actualizada exitosamente. Rows affected: " + rows);

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error updating bodega: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update bodega", e);
        }
    }

    private void handleBodegaEliminada(BodegaDTO bodega, ExecutionContext context) {
        String sql = "DELETE FROM BODEGAS WHERE ID = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, bodega.getId());

            int rows = stmt.executeUpdate();
            conn.commit();

            context.getLogger().info("Bodega eliminada exitosamente. Rows affected: " + rows);

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error deleting bodega: " + e.getMessage(), e);
            throw new RuntimeException("Failed to delete bodega", e);
        }
    }
}
