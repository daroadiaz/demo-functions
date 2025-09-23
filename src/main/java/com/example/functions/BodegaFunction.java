package com.example.functions;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.example.functions.dto.BodegaDTO;
import com.example.functions.service.DatabaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

public class BodegaFunction {

    private DatabaseService dbService = new DatabaseService();
    private ObjectMapper mapper = new ObjectMapper();

    @FunctionName("BodegaCreate")
    public HttpResponseMessage create(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "bodega/create")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {
            String body = request.getBody().orElse("");
            BodegaDTO bodega = mapper.readValue(body, BodegaDTO.class);
            BodegaDTO created = dbService.createBodega(bodega);
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

    @FunctionName("BodegaList")
    public HttpResponseMessage list(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "bodega/list")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(dbService.listBodegas()))
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }

    @FunctionName("BodegaGet")
    public HttpResponseMessage get(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "bodega/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        try {
            BodegaDTO bodega = dbService.getBodega(id);
            if (bodega == null) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).build();
            }
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(bodega))
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }

    @FunctionName("BodegaUpdate")
    public HttpResponseMessage update(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "bodega/update/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        try {
            String body = request.getBody().orElse("");
            BodegaDTO bodega = mapper.readValue(body, BodegaDTO.class);
            BodegaDTO updated = dbService.updateBodega(id, bodega);
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

    @FunctionName("BodegaDelete")
    public HttpResponseMessage delete(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "bodega/delete/{id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") Long id,
            final ExecutionContext context) {

        try {
            dbService.deleteBodega(id);
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }
}