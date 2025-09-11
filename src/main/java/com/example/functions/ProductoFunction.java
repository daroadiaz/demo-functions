package com.example.functions;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.example.functions.dto.ProductoDTO;
import com.example.functions.service.DatabaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

public class ProductoFunction {
    
    private DatabaseService dbService = new DatabaseService();
    private ObjectMapper mapper = new ObjectMapper();
    
    @FunctionName("ProductoCreate")
    public HttpResponseMessage create(
            @HttpTrigger(name = "req", 
                methods = {HttpMethod.POST}, 
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "producto/create") 
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            String body = request.getBody().orElse("");
            ProductoDTO producto = mapper.readValue(body, ProductoDTO.class);
            ProductoDTO created = dbService.createProducto(producto);
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(created))
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }
    
    @FunctionName("ProductoList")
    public HttpResponseMessage list(
            @HttpTrigger(name = "req", 
                methods = {HttpMethod.GET}, 
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "producto/list") 
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(dbService.listProductos()))
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }
    
    @FunctionName("ProductoGet")
    public HttpResponseMessage get(
            @HttpTrigger(name = "req", 
                methods = {HttpMethod.GET}, 
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "producto/{id}") 
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {
        
        try {
            ProductoDTO producto = dbService.getProducto(id);
            if (producto == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).build();
            }
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(producto))
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }
    
    @FunctionName("ProductoUpdate")
    public HttpResponseMessage update(
            @HttpTrigger(name = "req", 
                methods = {HttpMethod.PUT}, 
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "producto/update/{id}") 
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {
        
        try {
            String body = request.getBody().orElse("");
            ProductoDTO producto = mapper.readValue(body, ProductoDTO.class);
            ProductoDTO updated = dbService.updateProducto(id, producto);
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(updated))
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }
    
    @FunctionName("ProductoDelete")
    public HttpResponseMessage delete(
            @HttpTrigger(name = "req", 
                methods = {HttpMethod.DELETE}, 
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "producto/delete/{id}") 
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {
        
        try {
            dbService.deleteProducto(id);
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }
}