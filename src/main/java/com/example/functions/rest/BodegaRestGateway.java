package com.example.functions.rest;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.example.functions.dto.BodegaDTO;
import com.example.functions.service.DatabaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class BodegaRestGateway {

    private DatabaseService dbService = new DatabaseService();
    private ObjectMapper mapper = new ObjectMapper();

    @FunctionName("BodegaRESTApi")
    public HttpResponseMessage handleBodegaREST(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "api/v1/bodegas/{id?}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String idStr,
            final ExecutionContext context) {

        context.getLogger().info("REST API - Processing " + request.getHttpMethod() + " request for bodegas");

        try {
            HttpMethod method = request.getHttpMethod();
            Map<String, String> queryParams = request.getQueryParameters();

            if (method == HttpMethod.GET) {
                if (idStr != null && !idStr.isEmpty()) {
                    Long id = Long.parseLong(idStr);
                    BodegaDTO bodega = dbService.getBodega(id);
                    if (bodega == null) {
                        return createJsonResponse(request, HttpStatus.NOT_FOUND,
                            createErrorResponse("Bodega not found"));
                    }
                    return createJsonResponse(request, HttpStatus.OK, bodega);
                } else {
                    List<BodegaDTO> bodegas = dbService.listBodegas();

                    // Apply filters if provided
                    String filterCapacity = queryParams.get("minCapacity");
                    if (filterCapacity != null) {
                        int minCapacity = Integer.parseInt(filterCapacity);
                        bodegas = bodegas.stream()
                            .filter(b -> b.getCapacidadMaxima() >= minCapacity)
                            .collect(Collectors.toList());
                    }

                    String filterAvailable = queryParams.get("hasAvailableSpace");
                    if ("true".equalsIgnoreCase(filterAvailable)) {
                        bodegas = bodegas.stream()
                            .filter(b -> b.getEspacioUtilizado() < b.getCapacidadMaxima())
                            .collect(Collectors.toList());
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("data", bodegas);
                    response.put("count", bodegas.size());
                    response.put("filters", queryParams);
                    return createJsonResponse(request, HttpStatus.OK, response);
                }
            } else if (method == HttpMethod.POST) {
                String body = request.getBody().orElse("");
                BodegaDTO bodega = mapper.readValue(body, BodegaDTO.class);

                // Validation
                if (bodega.getNombre() == null || bodega.getNombre().isEmpty()) {
                    return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                        createErrorResponse("Warehouse name is required"));
                }
                if (bodega.getCapacidadMaxima() <= 0) {
                    return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                        createErrorResponse("Maximum capacity must be greater than 0"));
                }

                BodegaDTO created = dbService.createBodega(bodega);
                return createJsonResponse(request, HttpStatus.CREATED, created);

            } else if (method == HttpMethod.PUT) {
                if (idStr == null || idStr.isEmpty()) {
                    return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                        createErrorResponse("Warehouse ID is required for update"));
                }

                Long id = Long.parseLong(idStr);
                String body = request.getBody().orElse("");
                BodegaDTO bodega = mapper.readValue(body, BodegaDTO.class);

                BodegaDTO existing = dbService.getBodega(id);
                if (existing == null) {
                    return createJsonResponse(request, HttpStatus.NOT_FOUND,
                        createErrorResponse("Warehouse not found"));
                }

                // Validate capacity
                if (bodega.getEspacioUtilizado() > bodega.getCapacidadMaxima()) {
                    return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                        createErrorResponse("Used space cannot exceed maximum capacity"));
                }

                BodegaDTO updated = dbService.updateBodega(id, bodega);
                return createJsonResponse(request, HttpStatus.OK, updated);

            } else if (method == HttpMethod.DELETE) {
                if (idStr == null || idStr.isEmpty()) {
                    return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                        createErrorResponse("Warehouse ID is required for deletion"));
                }

                Long id = Long.parseLong(idStr);
                BodegaDTO existing = dbService.getBodega(id);
                if (existing == null) {
                    return createJsonResponse(request, HttpStatus.NOT_FOUND,
                        createErrorResponse("Warehouse not found"));
                }

                dbService.deleteBodega(id);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Warehouse deleted successfully");
                response.put("id", id.toString());
                return createJsonResponse(request, HttpStatus.OK, response);
            }

            return createJsonResponse(request, HttpStatus.METHOD_NOT_ALLOWED,
                createErrorResponse("Method not allowed"));

        } catch (NumberFormatException e) {
            return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                createErrorResponse("Invalid parameter format"));
        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
            return createJsonResponse(request, HttpStatus.INTERNAL_SERVER_ERROR,
                createErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @FunctionName("BodegaCapacityAnalysis")
    public HttpResponseMessage analyzeCapacity(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "api/v1/bodegas/analysis/capacity")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("REST API - Analyzing warehouse capacity");

        try {
            List<BodegaDTO> bodegas = dbService.listBodegas();

            int totalCapacity = bodegas.stream()
                .mapToInt(BodegaDTO::getCapacidadMaxima)
                .sum();
            int totalUsed = bodegas.stream()
                .mapToInt(BodegaDTO::getEspacioUtilizado)
                .sum();
            int totalAvailable = totalCapacity - totalUsed;

            List<Map<String, Object>> warehouseAnalysis = bodegas.stream()
                .map(b -> {
                    Map<String, Object> analysis = new HashMap<>();
                    analysis.put("id", b.getId());
                    analysis.put("name", b.getNombre());
                    analysis.put("capacity", b.getCapacidadMaxima());
                    analysis.put("used", b.getEspacioUtilizado());
                    analysis.put("available", b.getCapacidadMaxima() - b.getEspacioUtilizado());
                    analysis.put("utilizationPercentage",
                        (b.getEspacioUtilizado() * 100.0) / b.getCapacidadMaxima());
                    return analysis;
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("summary", Map.of(
                "totalWarehouses", bodegas.size(),
                "totalCapacity", totalCapacity,
                "totalUsed", totalUsed,
                "totalAvailable", totalAvailable,
                "averageUtilization", totalCapacity > 0 ? (totalUsed * 100.0) / totalCapacity : 0
            ));
            response.put("warehouses", warehouseAnalysis);

            return createJsonResponse(request, HttpStatus.OK, response);

        } catch (Exception e) {
            context.getLogger().severe("Error analyzing capacity: " + e.getMessage());
            return createJsonResponse(request, HttpStatus.INTERNAL_SERVER_ERROR,
                createErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    private HttpResponseMessage createJsonResponse(HttpRequestMessage<Optional<String>> request,
                                                  HttpStatus status, Object body) {
        try {
            return request.createResponseBuilder(status)
                    .header("Content-Type", "application/json")
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type")
                    .body(mapper.writeValueAsString(body))
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to serialize response\"}")
                    .build();
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", java.time.Instant.now().toString());
        return error;
    }
}