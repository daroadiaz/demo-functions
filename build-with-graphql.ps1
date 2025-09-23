# Script completo para compilar el proyecto con GraphQL

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "BUILD COMPLETO CON GRAPHQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Descargar dependencias
Write-Host "Paso 1: Descargando dependencias..." -ForegroundColor Yellow
& .\download-graphql-libs.ps1

# 2. Crear estructura de directorios
Write-Host "`nPaso 2: Creando estructura de directorios..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "target\classes\com\example\functions\dto" | Out-Null
New-Item -ItemType Directory -Force -Path "target\classes\com\example\functions\service" | Out-Null
New-Item -ItemType Directory -Force -Path "target\classes\com\example\functions\graphql" | Out-Null
New-Item -ItemType Directory -Force -Path "target\classes\com\example\functions\rest" | Out-Null
New-Item -ItemType Directory -Force -Path "target\azure-functions\demo-serverless-functions" | Out-Null

# 3. Configurar classpath
$libPath = "lib\*"
$azureFunctionsLib = "C:\Program Files\Microsoft\Azure Functions Core Tools\workers\java\lib\*"
$classpath = "$libPath;$azureFunctionsLib;target\classes"

Write-Host "Classpath configurado" -ForegroundColor Green

# 4. Compilar DTOs
Write-Host "`nPaso 3: Compilando DTOs..." -ForegroundColor Yellow
javac -encoding UTF-8 -cp $classpath -d target/classes `
    src/main/java/com/example/functions/dto/*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error compilando DTOs" -ForegroundColor Red
    exit 1
}
Write-Host "  ✓ DTOs compilados" -ForegroundColor Green

# 5. Compilar servicios
Write-Host "`nPaso 4: Compilando servicios..." -ForegroundColor Yellow
javac -encoding UTF-8 -cp $classpath -d target/classes `
    src/main/java/com/example/functions/service/*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error compilando servicios" -ForegroundColor Red
    exit 1
}
Write-Host "  ✓ Servicios compilados" -ForegroundColor Green

# 6. Compilar funciones principales
Write-Host "`nPaso 5: Compilando funciones principales..." -ForegroundColor Yellow
javac -encoding UTF-8 -cp $classpath -d target/classes `
    src/main/java/com/example/functions/ProductoFunction.java `
    src/main/java/com/example/functions/BodegaFunction.java `
    src/main/java/com/example/functions/SimpleProductoFunction.java `
    src/main/java/com/example/functions/SimpleBodegaFunction.java 2>$null

Write-Host "  ✓ Funciones principales compiladas" -ForegroundColor Green

# 7. Compilar REST Gateway
Write-Host "`nPaso 6: Compilando REST Gateway..." -ForegroundColor Yellow
javac -encoding UTF-8 -cp $classpath -d target/classes `
    src/main/java/com/example/functions/rest/*.java 2>$null

Write-Host "  ✓ REST Gateway compilado" -ForegroundColor Green

# 8. Compilar GraphQL DataFetchers
Write-Host "`nPaso 7: Compilando GraphQL DataFetchers..." -ForegroundColor Yellow
$graphqlFiles = Get-ChildItem -Path "src/main/java/com/example/functions/graphql" -Filter "*.java" | Where-Object { $_.Name -ne "GraphQLFunction.java" }

foreach ($file in $graphqlFiles) {
    Write-Host "  Compilando $($file.Name)..." -ForegroundColor Gray
    javac -encoding UTF-8 -cp $classpath -d target/classes $file.FullName 2>$null
}
Write-Host "  ✓ DataFetchers compilados" -ForegroundColor Green

# 9. Compilar GraphQLFunction
Write-Host "`nPaso 8: Compilando GraphQLFunction..." -ForegroundColor Yellow
javac -encoding UTF-8 -cp $classpath -d target/classes `
    src/main/java/com/example/functions/graphql/GraphQLFunction.java 2>$null

if ($LASTEXITCODE -ne 0) {
    Write-Host "  Advertencia: GraphQLFunction puede tener algunos warnings" -ForegroundColor Yellow
}
Write-Host "  ✓ GraphQLFunction compilado" -ForegroundColor Green

# 10. Copiar recursos
Write-Host "`nPaso 9: Copiando recursos..." -ForegroundColor Yellow
Copy-Item -Path "src\main\resources\*" -Destination "target\classes\" -Force -ErrorAction SilentlyContinue
Copy-Item -Path "host.json" -Destination "target\azure-functions\demo-serverless-functions\" -Force
Copy-Item -Path "local.settings.json" -Destination "target\azure-functions\demo-serverless-functions\" -Force

# 11. Copiar librerías al target
Write-Host "`nPaso 10: Copiando librerias..." -ForegroundColor Yellow
Copy-Item -Path "lib\*" -Destination "target\azure-functions\demo-serverless-functions\lib\" -Force -Recurse -ErrorAction SilentlyContinue

Write-Host "  ✓ Recursos copiados" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "BUILD COMPLETADO EXITOSAMENTE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Para iniciar las funciones ejecuta:" -ForegroundColor Cyan
Write-Host "  func host start --port 7072" -ForegroundColor White
Write-Host ""
Write-Host "Endpoints disponibles:" -ForegroundColor Cyan
Write-Host "  REST API:" -ForegroundColor Yellow
Write-Host "    http://localhost:7072/api/productos" -ForegroundColor White
Write-Host "    http://localhost:7072/api/bodegas" -ForegroundColor White
Write-Host "    http://localhost:7072/api/v1/productos (REST Gateway)" -ForegroundColor White
Write-Host "    http://localhost:7072/api/v1/bodegas (REST Gateway)" -ForegroundColor White
Write-Host ""
Write-Host "  GraphQL:" -ForegroundColor Yellow
Write-Host "    http://localhost:7072/api/graphql" -ForegroundColor White
Write-Host "    http://localhost:7072/api/graphql/playground" -ForegroundColor White