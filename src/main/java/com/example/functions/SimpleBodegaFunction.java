package com.example.functions;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class SimpleBodegaFunction {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<Long, Map<String, Object>> bodegasDB = new HashMap<>();
    private static Long nextId = 1L;

    @FunctionName("CreateBodega")
    public HttpResponseMessage createBodega(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "bodegas")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Creating new bodega");

        try {
            String body = request.getBody().orElse("");
            Map<String, Object> bodega = mapper.readValue(body, Map.class);

            // Agregar ID y timestamps
            bodega.put("id", nextId++);
            bodega.put("createdAt", new Date().toString());

            // Calcular espacio disponible
            Integer capacidad = (Integer) bodega.get("capacidadMaxima");
            Integer usado = (Integer) bodega.getOrDefault("espacioUtilizado", 0);
            bodega.put("espacioDisponible", capacidad - usado);

            // Guardar en memoria
            bodegasDB.put((Long) bodega.get("id"), bodega);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(bodega))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error creating bodega: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @FunctionName("ListBodegas")
    public HttpResponseMessage listBodegas(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "bodegas")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Listing bodegas");

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("data", new ArrayList<>(bodegasDB.values()));
            response.put("count", bodegasDB.size());

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(response))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error listing bodegas: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @FunctionName("GetBodega")
    public HttpResponseMessage getBodega(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "bodegas/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Getting bodega with id: " + id);

        try {
            Long bodegaId = Long.parseLong(id);
            Map<String, Object> bodega = bodegasDB.get(bodegaId);

            if (bodega == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Bodega not found\"}")
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(bodega))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting bodega: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @FunctionName("UpdateBodega")
    public HttpResponseMessage updateBodega(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "bodegas/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Updating bodega with id: " + id);

        try {
            Long bodegaId = Long.parseLong(id);
            Map<String, Object> existingBodega = bodegasDB.get(bodegaId);

            if (existingBodega == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Bodega not found\"}")
                        .build();
            }

            String body = request.getBody().orElse("");
            Map<String, Object> updates = mapper.readValue(body, Map.class);

            // Actualizar campos
            existingBodega.putAll(updates);
            existingBodega.put("updatedAt", new Date().toString());

            // Recalcular espacio disponible
            Integer capacidad = (Integer) existingBodega.get("capacidadMaxima");
            Integer usado = (Integer) existingBodega.getOrDefault("espacioUtilizado", 0);
            existingBodega.put("espacioDisponible", capacidad - usado);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(existingBodega))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error updating bodega: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @FunctionName("DeleteBodega")
    public HttpResponseMessage deleteBodega(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "bodegas/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Deleting bodega with id: " + id);

        try {
            Long bodegaId = Long.parseLong(id);
            Map<String, Object> removed = bodegasDB.remove(bodegaId);

            if (removed == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Bodega not found\"}")
                        .build();
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Bodega deleted successfully");
            response.put("id", id);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(response))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error deleting bodega: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}