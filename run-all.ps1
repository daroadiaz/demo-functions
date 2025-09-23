# Script completo para ejecutar el proyecto con GraphQL

param(
    [switch]$SkipDownload,
    [switch]$SkipBuild,
    [int]$Port = 7072
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AZURE FUNCTIONS CON GRAPHQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Descargar dependencias (si no existe el directorio lib o si no se omite)
if (-not $SkipDownload) {
    if (-not (Test-Path "lib")) {
        Write-Host "Descargando dependencias necesarias..." -ForegroundColor Yellow
        & .\download-graphql-libs.ps1
    } else {
        Write-Host "Directorio lib ya existe. Use -SkipDownload para omitir verificacion." -ForegroundColor Green
    }
}

# 2. Compilar proyecto (si no se omite)
if (-not $SkipBuild) {
    Write-Host "`nCompilando proyecto..." -ForegroundColor Yellow
    & .\build-with-graphql.ps1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error durante la compilacion" -ForegroundColor Red
        exit 1
    }
}

# 3. Verificar puerto disponible
if (Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue) {
    Write-Host "Puerto $Port esta ocupado" -ForegroundColor Yellow
    $Port = if ($Port -eq 7071) { 7072 } else { 7071 }
    Write-Host "Usando puerto alternativo: $Port" -ForegroundColor Cyan
}

# 4. Iniciar Azure Functions
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "INICIANDO AZURE FUNCTIONS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Puerto: $Port" -ForegroundColor Cyan
Write-Host ""
Write-Host "Endpoints REST simples:" -ForegroundColor Yellow
Write-Host "  POST   http://localhost:$Port/api/productos" -ForegroundColor White
Write-Host "  GET    http://localhost:$Port/api/productos" -ForegroundColor White
Write-Host "  GET    http://localhost:$Port/api/productos/{id}" -ForegroundColor White
Write-Host "  PUT    http://localhost:$Port/api/productos/{id}" -ForegroundColor White
Write-Host "  DELETE http://localhost:$Port/api/productos/{id}" -ForegroundColor White
Write-Host ""
Write-Host "  POST   http://localhost:$Port/api/bodegas" -ForegroundColor White
Write-Host "  GET    http://localhost:$Port/api/bodegas" -ForegroundColor White
Write-Host "  GET    http://localhost:$Port/api/bodegas/{id}" -ForegroundColor White
Write-Host "  PUT    http://localhost:$Port/api/bodegas/{id}" -ForegroundColor White
Write-Host "  DELETE http://localhost:$Port/api/bodegas/{id}" -ForegroundColor White
Write-Host ""
Write-Host "REST Gateway avanzado:" -ForegroundColor Yellow
Write-Host "  http://localhost:$Port/api/v1/productos (con batch y filtros)" -ForegroundColor White
Write-Host "  http://localhost:$Port/api/v1/bodegas (con analisis de capacidad)" -ForegroundColor White
Write-Host ""
Write-Host "GraphQL:" -ForegroundColor Yellow
Write-Host "  http://localhost:$Port/api/graphql" -ForegroundColor White
Write-Host "  http://localhost:$Port/api/graphql/playground" -ForegroundColor White
Write-Host ""
Write-Host "Presiona Ctrl+C para detener" -ForegroundColor Gray
Write-Host ""

# Configurar classpath para Azure Functions
$env:CLASSPATH = "lib\*;target\classes"

# Iniciar funciones
func host start --port $Port