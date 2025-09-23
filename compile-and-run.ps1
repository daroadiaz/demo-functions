# Script simplificado para compilar y ejecutar funciones

Write-Host "=====================================" -ForegroundColor Green
Write-Host "COMPILANDO FUNCIONES SERVERLESS" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green

# Crear directorios necesarios
New-Item -ItemType Directory -Force -Path "target\classes" | Out-Null
New-Item -ItemType Directory -Force -Path "target\azure-functions\demo-serverless-functions" | Out-Null

# Ruta de Azure Functions Core Tools
$azureFunctionsLib = "C:\Program Files\Microsoft\Azure Functions Core Tools\workers\java\lib\*"

# 1. Compilar funciones simplificadas
Write-Host "`nCompilando funciones simplificadas..." -ForegroundColor Yellow
javac -encoding UTF-8 -cp "$azureFunctionsLib" -d target/classes `
    src/main/java/com/example/functions/SimpleProductoFunction.java `
    src/main/java/com/example/functions/SimpleBodegaFunction.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilacion exitosa!" -ForegroundColor Green
} else {
    Write-Host "Error en la compilacion" -ForegroundColor Red
    exit 1
}

# 2. Copiar archivos de configuracion
Write-Host "`nCopiando archivos de configuracion..." -ForegroundColor Yellow
Copy-Item -Path "host.json" -Destination "target\azure-functions\demo-serverless-functions\" -Force
Copy-Item -Path "local.settings.json" -Destination "target\azure-functions\demo-serverless-functions\" -Force

# 3. Copiar clases compiladas
Write-Host "Copiando clases compiladas..." -ForegroundColor Yellow
Copy-Item -Path "target\classes\*" -Destination "target\azure-functions\demo-serverless-functions\" -Recurse -Force

# 4. Determinar puerto disponible
$port = 7071
if (Get-NetTCPConnection -LocalPort 7071 -ErrorAction SilentlyContinue) {
    $port = 7072
    Write-Host "Puerto 7071 ocupado, usando puerto $port" -ForegroundColor Yellow
}

# 5. Iniciar Azure Functions
Write-Host "`n=====================================" -ForegroundColor Green
Write-Host "INICIANDO AZURE FUNCTIONS" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host "Puerto: $port" -ForegroundColor Cyan
Write-Host "`nEndpoints disponibles:" -ForegroundColor Yellow
Write-Host "  POST   http://localhost:${port}/api/productos    - Crear producto" -ForegroundColor White
Write-Host "  GET    http://localhost:${port}/api/productos    - Listar productos" -ForegroundColor White
Write-Host "  GET    http://localhost:${port}/api/productos/{id} - Obtener producto" -ForegroundColor White
Write-Host "  PUT    http://localhost:${port}/api/productos/{id} - Actualizar producto" -ForegroundColor White
Write-Host "  DELETE http://localhost:${port}/api/productos/{id} - Eliminar producto" -ForegroundColor White
Write-Host ""
Write-Host "  POST   http://localhost:${port}/api/bodegas      - Crear bodega" -ForegroundColor White
Write-Host "  GET    http://localhost:${port}/api/bodegas      - Listar bodegas" -ForegroundColor White
Write-Host "  GET    http://localhost:${port}/api/bodegas/{id} - Obtener bodega" -ForegroundColor White
Write-Host "  PUT    http://localhost:${port}/api/bodegas/{id} - Actualizar bodega" -ForegroundColor White
Write-Host "  DELETE http://localhost:${port}/api/bodegas/{id} - Eliminar bodega" -ForegroundColor White
Write-Host "`nPresiona Ctrl+C para detener" -ForegroundColor Gray

func host start --port $port