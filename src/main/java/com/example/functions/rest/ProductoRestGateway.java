package com.example.functions.rest;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.example.functions.dto.ProductoDTO;
import com.example.functions.service.DatabaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ProductoRestGateway {

    private DatabaseService dbService = new DatabaseService();
    private ObjectMapper mapper = new ObjectMapper();

    @FunctionName("ProductoRESTApi")
    public HttpResponseMessage handleProductoREST(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "api/v1/productos/{id?}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String idStr,
            final ExecutionContext context) {

        context.getLogger().info("REST API - Processing " + request.getHttpMethod() + " request for productos");

        try {
            HttpMethod method = request.getHttpMethod();

            if (method == HttpMethod.GET) {
                if (idStr != null && !idStr.isEmpty()) {
                    Long id = Long.parseLong(idStr);
                    ProductoDTO producto = dbService.getProducto(id);
                    if (producto == null) {
                        return createJsonResponse(request, HttpStatus.NOT_FOUND,
                            createErrorResponse("Producto not found"));
                    }
                    return createJsonResponse(request, HttpStatus.OK, producto);
                } else {
                    List<ProductoDTO> productos = dbService.listProductos();
                    Map<String, Object> response = new HashMap<>();
                    response.put("data", productos);
                    response.put("count", productos.size());
                    return createJsonResponse(request, HttpStatus.OK, response);
                }
            } else if (method == HttpMethod.POST) {
                String body = request.getBody().orElse("");
                ProductoDTO producto = mapper.readValue(body, ProductoDTO.class);

                if (producto.getNombre() == null || producto.getNombre().isEmpty()) {
                    return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                        createErrorResponse("Product name is required"));
                }

                ProductoDTO created = dbService.createProducto(producto);
                return createJsonResponse(request, HttpStatus.CREATED, created);

            } else if (method == HttpMethod.PUT) {
                if (idStr == null || idStr.isEmpty()) {
                    return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                        createErrorResponse("Product ID is required for update"));
                }

                Long id = Long.parseLong(idStr);
                String body = request.getBody().orElse("");
                ProductoDTO producto = mapper.readValue(body, ProductoDTO.class);

                ProductoDTO existing = dbService.getProducto(id);
                if (existing == null) {
                    return createJsonResponse(request, HttpStatus.NOT_FOUND,
                        createErrorResponse("Product not found"));
                }

                ProductoDTO updated = dbService.updateProducto(id, producto);
                return createJsonResponse(request, HttpStatus.OK, updated);

            } else if (method == HttpMethod.DELETE) {
                if (idStr == null || idStr.isEmpty()) {
                    return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                        createErrorResponse("Product ID is required for deletion"));
                }

                Long id = Long.parseLong(idStr);
                ProductoDTO existing = dbService.getProducto(id);
                if (existing == null) {
                    return createJsonResponse(request, HttpStatus.NOT_FOUND,
                        createErrorResponse("Product not found"));
                }

                dbService.deleteProducto(id);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Product deleted successfully");
                response.put("id", id.toString());
                return createJsonResponse(request, HttpStatus.OK, response);
            }

            return createJsonResponse(request, HttpStatus.METHOD_NOT_ALLOWED,
                createErrorResponse("Method not allowed"));

        } catch (NumberFormatException e) {
            return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                createErrorResponse("Invalid ID format"));
        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
            return createJsonResponse(request, HttpStatus.INTERNAL_SERVER_ERROR,
                createErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @FunctionName("ProductoBatchOperations")
    public HttpResponseMessage handleBatchOperations(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "api/v1/productos/batch")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("REST API - Processing batch operations for productos");

        try {
            String body = request.getBody().orElse("");
            Map<String, Object> batchRequest = mapper.readValue(body, Map.class);
            String operation = (String) batchRequest.get("operation");
            List<Map<String, Object>> items = (List<Map<String, Object>>) batchRequest.get("items");

            if (operation == null || items == null || items.isEmpty()) {
                return createJsonResponse(request, HttpStatus.BAD_REQUEST,
                    createErrorResponse("Operation and items are required"));
            }

            Map<String, Object> response = new HashMap<>();
            List<Map<String, Object>> results = new java.util.ArrayList<>();
            int successCount = 0;
            int failureCount = 0;

            for (Map<String, Object> item : items) {
                Map<String, Object> result = new HashMap<>();
                try {
                    ProductoDTO producto = mapper.convertValue(item, ProductoDTO.class);

                    if ("CREATE".equalsIgnoreCase(operation)) {
                        ProductoDTO created = dbService.createProducto(producto);
                        result.put("status", "success");
                        result.put("data", created);
                        successCount++;
                    } else if ("UPDATE".equalsIgnoreCase(operation)) {
                        Long id = producto.getId();
                        if (id == null) {
                            result.put("status", "error");
                            result.put("error", "ID required for update");
                            failureCount++;
                        } else {
                            ProductoDTO updated = dbService.updateProducto(id, producto);
                            result.put("status", "success");
                            result.put("data", updated);
                            successCount++;
                        }
                    } else {
                        result.put("status", "error");
                        result.put("error", "Invalid operation: " + operation);
                        failureCount++;
                    }
                } catch (Exception e) {
                    result.put("status", "error");
                    result.put("error", e.getMessage());
                    failureCount++;
                }
                results.add(result);
            }

            response.put("results", results);
            response.put("summary", Map.of(
                "total", items.size(),
                "success", successCount,
                "failures", failureCount
            ));

            return createJsonResponse(request, HttpStatus.OK, response);

        } catch (Exception e) {
            context.getLogger().severe("Error processing batch request: " + e.getMessage());
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