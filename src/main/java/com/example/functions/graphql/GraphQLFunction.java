package com.example.functions.graphql;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import graphql.ExecutionResult;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.AsyncSerialExecutionStrategy;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class GraphQLFunction {

    private static GraphQL graphQL;
    private final ObjectMapper mapper = new ObjectMapper();

    static {
        initializeGraphQL();
    }

    private static void initializeGraphQL() {
        try {
            // Load schema
            InputStream schemaStream = GraphQLFunction.class
                .getClassLoader()
                .getResourceAsStream("schema.graphqls");

            if (schemaStream == null) {
                throw new RuntimeException("Could not load GraphQL schema");
            }

            SchemaParser schemaParser = new SchemaParser();
            TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(
                new InputStreamReader(schemaStream)
            );

            // Create runtime wiring
            RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                    .dataFetcher("producto", new ProductoDataFetcher())
                    .dataFetcher("productos", new ProductosDataFetcher())
                    .dataFetcher("bodega", new BodegaDataFetcher())
                    .dataFetcher("bodegas", new BodegasDataFetcher())
                    .dataFetcher("inventoryAnalytics", new InventoryAnalyticsDataFetcher())
                    .dataFetcher("warehouseCapacityAnalysis", new WarehouseCapacityAnalysisDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                    .dataFetcher("createProducto", new CreateProductoDataFetcher())
                    .dataFetcher("updateProducto", new UpdateProductoDataFetcher())
                    .dataFetcher("deleteProducto", new DeleteProductoDataFetcher())
                    .dataFetcher("createBodega", new CreateBodegaDataFetcher())
                    .dataFetcher("updateBodega", new UpdateBodegaDataFetcher())
                    .dataFetcher("deleteBodega", new DeleteBodegaDataFetcher())
                    .dataFetcher("batchCreateProductos", new BatchCreateProductosDataFetcher())
                    .dataFetcher("transferInventory", new TransferInventoryDataFetcher())
                )
                .build();

            // Generate schema
            SchemaGenerator schemaGenerator = new SchemaGenerator();
            GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(
                typeDefinitionRegistry,
                runtimeWiring
            );

            // Build GraphQL
            graphQL = GraphQL.newGraphQL(graphQLSchema)
                .queryExecutionStrategy(new AsyncExecutionStrategy())
                .mutationExecutionStrategy(new AsyncSerialExecutionStrategy())
                .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize GraphQL", e);
        }
    }

    @FunctionName("GraphQLEndpoint")
    public HttpResponseMessage handleGraphQL(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.POST, HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "graphql")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing GraphQL request");

        try {
            Map<String, Object> requestBody;

            if (request.getHttpMethod() == HttpMethod.GET) {
                // Handle GraphQL queries via GET
                Map<String, String> queryParams = request.getQueryParameters();
                requestBody = new HashMap<>();
                requestBody.put("query", queryParams.get("query"));

                String variables = queryParams.get("variables");
                if (variables != null) {
                    requestBody.put("variables", mapper.readValue(variables, Map.class));
                }
            } else {
                // Handle GraphQL queries via POST
                String body = request.getBody().orElse("{}");
                requestBody = mapper.readValue(body, Map.class);
            }

            String query = (String) requestBody.get("query");
            Map<String, Object> variables = (Map<String, Object>) requestBody.get("variables");

            if (query == null || query.isEmpty()) {
                return createErrorResponse(request, "Query is required");
            }

            // Execute GraphQL query
            ExecutionResult executionResult = graphQL.execute(
                graphql.ExecutionInput.newExecutionInput()
                    .query(query)
                    .variables(variables != null ? variables : new HashMap<>())
                    .build()
            );

            Map<String, Object> result = executionResult.toSpecification();

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type")
                    .body(mapper.writeValueAsString(result))
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error processing GraphQL request: " + e.getMessage());
            return createErrorResponse(request, "Internal server error: " + e.getMessage());
        }
    }

    @FunctionName("GraphQLPlayground")
    public HttpResponseMessage servePlayground(
            @HttpTrigger(name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "graphql/playground")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        String playgroundHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>GraphQL Playground</title>
                <link href="https://unpkg.com/graphiql@2.0.0/graphiql.min.css" rel="stylesheet" />
                <style>
                    body {
                        height: 100%;
                        margin: 0;
                        width: 100%;
                        overflow: hidden;
                    }
                    #graphiql {
                        height: 100vh;
                    }
                </style>
            </head>
            <body>
                <div id="graphiql">Loading...</div>
                <script crossorigin src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
                <script crossorigin src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
                <script crossorigin src="https://unpkg.com/graphiql@2.0.0/graphiql.min.js"></script>
                <script>
                    const fetcher = GraphiQL.createFetcher({
                        url: '/api/graphql',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    });

                    const root = ReactDOM.createRoot(document.getElementById('graphiql'));
                    root.render(
                        React.createElement(GraphiQL, {
                            fetcher: fetcher,
                            defaultQuery: `# Welcome to GraphQL Playground
# Try running some queries:

query GetAllProducts {
  productos {
    data {
      id
      nombre
      precio
      stock
      categoria
    }
    count
  }
}

query GetWarehouseAnalysis {
  warehouseCapacityAnalysis {
    totalWarehouses
    averageUtilization
    warehouses {
      name
      utilizationPercentage
    }
  }
}

mutation CreateProduct {
  createProducto(input: {
    codigo: "PROD001"
    nombre: "New Product"
    precio: 99.99
    stock: 100
    stockMinimo: 10
    categoria: "Electronics"
  }) {
    id
    nombre
    precio
  }
}`
                        })
                    );
                </script>
            </body>
            </html>
            """;

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "text/html")
                .body(playgroundHtml)
                .build();
    }

    private HttpResponseMessage createErrorResponse(HttpRequestMessage<Optional<String>> request, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("errors", List.of(Map.of("message", message)));

        try {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(error))
                    .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"errors\":[{\"message\":\"Failed to serialize error\"}]}")
                    .build();
        }
    }
}