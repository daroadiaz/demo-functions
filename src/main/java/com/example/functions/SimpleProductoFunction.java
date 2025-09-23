package com.example.functions;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class SimpleProductoFunction {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<Long, Map<String, Object>> productosDB = new HashMap<>();
    private static Long nextId = 1L;

    @FunctionName("CreateProducto")
    public HttpResponseMessage createProducto(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "productos")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new producto");

        try {
            String body = request.getBody().orElse("");
            Map<String, Object> producto = mapper.readValue(body, Map.class);

            // Agregar ID
            producto.put("id", nextId++);
            producto.put("createdAt", new Date().toString());

            // Guardar en memoria
            productosDB.put((Long) producto.get("id"), producto);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(producto))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error creating producto: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @FunctionName("ListProductos")
    public HttpResponseMessage listProductos(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "productos")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Listing productos");

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("data", new ArrayList<>(productosDB.values()));
            response.put("count", productosDB.size());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(response))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error listing productos: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @FunctionName("GetProducto")
    public HttpResponseMessage getProducto(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "productos/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Getting producto with id: " + id);

        try {
            Long productoId = Long.parseLong(id);
            Map<String, Object> producto = productosDB.get(productoId);

            if (producto == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Producto not found\"}")
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(producto))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting producto: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @FunctionName("UpdateProducto")
    public HttpResponseMessage updateProducto(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "productos/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Updating producto with id: " + id);

        try {
            Long productoId = Long.parseLong(id);
            Map<String, Object> existingProducto = productosDB.get(productoId);

            if (existingProducto == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Producto not found\"}")
                        .build();
            }

            String body = request.getBody().orElse("");
            Map<String, Object> updates = mapper.readValue(body, Map.class);

            // Actualizar campos
            existingProducto.putAll(updates);
            existingProducto.put("updatedAt", new Date().toString());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(existingProducto))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error updating producto: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @FunctionName("DeleteProducto")
    public HttpResponseMessage deleteProducto(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "productos/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting producto with id: " + id);

        try {
            Long productoId = Long.parseLong(id);
            Map<String, Object> removed = productosDB.remove(productoId);

            if (removed == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Producto not found\"}")
                        .build();
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Producto deleted successfully");
            response.put("id", id);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(response))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error deleting producto: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}