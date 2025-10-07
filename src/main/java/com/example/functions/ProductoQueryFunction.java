package com.example.functions;

import com.example.functions.dto.ProductoDTO;
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

public class ProductoQueryFunction {

    private final ObjectMapper objectMapper;
    private final DatabaseService databaseService;

    public ProductoQueryFunction() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.databaseService = new DatabaseService();
    }

    @FunctionName("GetProductos")
    public HttpResponseMessage getProductos(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET},
            route = "productos",
            authLevel = AuthorizationLevel.ANONYMOUS
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        context.getLogger().info("GetProductos function triggered");

        try {
            List<ProductoDTO> productos = getAllProductos();

            String json = objectMapper.writeValueAsString(productos);

            return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build();

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error getting productos: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage())
                .build();
        }
    }

    @FunctionName("GetProductoById")
    public HttpResponseMessage getProductoById(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET},
            route = "productos/{id}",
            authLevel = AuthorizationLevel.ANONYMOUS
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("id") String id,
        final ExecutionContext context
    ) {
        context.getLogger().info("GetProductoById function triggered for ID: " + id);

        try {
            Long productoId = Long.parseLong(id);
            ProductoDTO producto = getProductoById(productoId);

            if (producto == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("Producto no encontrado")
                    .build();
            }

            String json = objectMapper.writeValueAsString(producto);

            return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build();

        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("ID inválido")
                .build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error getting producto: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage())
                .build();
        }
    }

    private List<ProductoDTO> getAllProductos() throws Exception {
        List<ProductoDTO> productos = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTOS ORDER BY ID";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
        }

        return productos;
    }

    private ProductoDTO getProductoById(Long id) throws Exception {
        String sql = "SELECT * FROM PRODUCTOS WHERE ID = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProducto(rs);
                }
            }
        }

        return null;
    }

    private ProductoDTO mapResultSetToProducto(ResultSet rs) throws Exception {
        ProductoDTO producto = new ProductoDTO();
        producto.setId(rs.getLong("ID"));
        producto.setCodigo(rs.getString("CODIGO"));
        producto.setNombre(rs.getString("NOMBRE"));
        producto.setDescripcion(rs.getString("DESCRIPCION"));
        producto.setPrecio(rs.getBigDecimal("PRECIO"));
        producto.setStock(rs.getInt("STOCK"));
        producto.setStockMinimo(rs.getInt("STOCK_MINIMO"));
        producto.setCategoria(rs.getString("CATEGORIA"));
        producto.setActivo(rs.getBoolean("ACTIVO"));

        if (rs.getTimestamp("FECHA_CREACION") != null) {
            producto.setFechaCreacion(rs.getTimestamp("FECHA_CREACION").toLocalDateTime());
        }
        if (rs.getTimestamp("FECHA_ACTUALIZACION") != null) {
            producto.setFechaActualizacion(rs.getTimestamp("FECHA_ACTUALIZACION").toLocalDateTime());
        }

        return producto;
    }
}
