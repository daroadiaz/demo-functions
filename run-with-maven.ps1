# Script completo para ejecutar Azure Functions con Maven

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AZURE FUNCTIONS CON MAVEN Y GRAPHQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Compilar y descargar dependencias
Write-Host "Paso 1: Compilando proyecto y descargando dependencias..." -ForegroundColor Yellow
./mvnw clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error durante la compilacion" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Compilacion exitosa" -ForegroundColor Green
Write-Host ""

# 2. Empaquetar funciones
Write-Host "Paso 2: Empaquetando funciones..." -ForegroundColor Yellow
./mvnw azure-functions:package

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error empaquetando funciones" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Funciones empaquetadas" -ForegroundColor Green
Write-Host ""

# 3. Ejecutar funciones
Write-Host "========================================" -ForegroundColor Green
Write-Host "INICIANDO AZURE FUNCTIONS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "Endpoints disponibles:" -ForegroundColor Cyan
Write-Host ""
Write-Host "REST Simple:" -ForegroundColor Yellow
Write-Host "  POST   http://localhost:7071/api/productos" -ForegroundColor White
Write-Host "  GET    http://localhost:7071/api/productos" -ForegroundColor White
Write-Host "  GET    http://localhost:7071/api/productos/{id}" -ForegroundColor White
Write-Host "  PUT    http://localhost:7071/api/productos/{id}" -ForegroundColor White
Write-Host "  DELETE http://localhost:7071/api/productos/{id}" -ForegroundColor White
Write-Host ""
Write-Host "  POST   http://localhost:7071/api/bodegas" -ForegroundColor White
Write-Host "  GET    http://localhost:7071/api/bodegas" -ForegroundColor White
Write-Host "  GET    http://localhost:7071/api/bodegas/{id}" -ForegroundColor White
Write-Host "  PUT    http://localhost:7071/api/bodegas/{id}" -ForegroundColor White
Write-Host "  DELETE http://localhost:7071/api/bodegas/{id}" -ForegroundColor White
Write-Host ""
Write-Host "REST Gateway:" -ForegroundColor Yellow
Write-Host "  http://localhost:7071/api/v1/productos" -ForegroundColor White
Write-Host "  http://localhost:7071/api/v1/bodegas" -ForegroundColor White
Write-Host ""
Write-Host "GraphQL:" -ForegroundColor Yellow
Write-Host "  http://localhost:7071/api/graphql" -ForegroundColor White
Write-Host "  http://localhost:7071/api/graphql/playground" -ForegroundColor White
Write-Host ""
Write-Host "Presiona Ctrl+C para detener" -ForegroundColor Gray
Write-Host ""

# Ejecutar con Maven
./mvnw azure-functions:run