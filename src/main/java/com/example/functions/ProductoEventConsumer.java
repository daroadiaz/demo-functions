package com.example.functions;

import com.example.functions.dto.ProductoDTO;
import com.example.functions.service.DatabaseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductoEventConsumer {

    private static final Logger logger = Logger.getLogger(ProductoEventConsumer.class.getName());
    private final ObjectMapper objectMapper;
    private final DatabaseService databaseService;

    public ProductoEventConsumer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.databaseService = new DatabaseService();
    }

    @FunctionName("ProductoEventConsumer")
    public void run(
        @EventGridTrigger(name = "event") String content,
        final ExecutionContext context
    ) {
        context.getLogger().info("=== ProductoEventConsumer triggered ===");
        context.getLogger().info("Event content: " + content);

        try {
            JsonNode event = objectMapper.readTree(content);
            String eventType = event.get("eventType").asText();
            JsonNode dataNode = event.get("data");

            ProductoDTO producto = objectMapper.treeToValue(dataNode, ProductoDTO.class);

            context.getLogger().info("Processing event type: " + eventType);
            context.getLogger().info("Producto: " + producto.getCodigo());

            switch (eventType) {
                case "ProductoCreado":
                    handleProductoCreado(producto, context);
                    break;
                case "ProductoActualizado":
                    handleProductoActualizado(producto, context);
                    break;
                case "ProductoEliminado":
                    handleProductoEliminado(producto, context);
                    break;
                default:
                    context.getLogger().warning("Unknown event type: " + eventType);
            }

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error processing event: " + e.getMessage(), e);
            throw new RuntimeException("Failed to process event", e);
        }
    }

    private void handleProductoCreado(ProductoDTO producto, ExecutionContext context) {
        String sql = "INSERT INTO PRODUCTOS (ID, CODIGO, NOMBRE, DESCRIPCION, PRECIO, STOCK, STOCK_MINIMO, CATEGORIA, ACTIVO, FECHA_CREACION, FECHA_ACTUALIZACION) " +
                     "VALUES (PRODUCTO_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setBigDecimal(4, producto.getPrecio());
            stmt.setInt(5, producto.getStock());
            stmt.setInt(6, producto.getStockMinimo() != null ? producto.getStockMinimo() : 0);
            stmt.setString(7, producto.getCategoria());
            stmt.setBoolean(8, producto.getActivo() != null ? producto.getActivo() : true);
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));

            int rows = stmt.executeUpdate();
            conn.commit();

            context.getLogger().info("Producto creado exitosamente. Rows affected: " + rows);

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error creating producto: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create producto", e);
        }
    }

    private void handleProductoActualizado(ProductoDTO producto, ExecutionContext context) {
        String sql = "UPDATE PRODUCTOS SET CODIGO = ?, NOMBRE = ?, DESCRIPCION = ?, PRECIO = ?, STOCK = ?, STOCK_MINIMO = ?, CATEGORIA = ?, ACTIVO = ?, FECHA_ACTUALIZACION = ? WHERE ID = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setBigDecimal(4, producto.getPrecio());
            stmt.setInt(5, producto.getStock());
            stmt.setInt(6, producto.getStockMinimo() != null ? producto.getStockMinimo() : 0);
            stmt.setString(7, producto.getCategoria());
            stmt.setBoolean(8, producto.getActivo() != null ? producto.getActivo() : true);
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(10, producto.getId());

            int rows = stmt.executeUpdate();
            conn.commit();

            context.getLogger().info("Producto actualizado exitosamente. Rows affected: " + rows);

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error updating producto: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update producto", e);
        }
    }

    private void handleProductoEliminado(ProductoDTO producto, ExecutionContext context) {
        String sql = "DELETE FROM PRODUCTOS WHERE ID = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, producto.getId());

            int rows = stmt.executeUpdate();
            conn.commit();

            context.getLogger().info("Producto eliminado exitosamente. Rows affected: " + rows);

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error deleting producto: " + e.getMessage(), e);
            throw new RuntimeException("Failed to delete producto", e);
        }
    }
}
