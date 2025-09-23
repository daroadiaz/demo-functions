# Script para compilar con Maven y descargar dependencias automáticamente

Write-Host "========================================" -ForegroundColor Green
Write-Host "COMPILANDO CON MAVEN" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 1. Descargar dependencias y compilar
Write-Host "Descargando dependencias y compilando..." -ForegroundColor Yellow
Write-Host "(Maven descargara automaticamente las dependencias del pom.xml)" -ForegroundColor Gray
Write-Host ""

./mvnw clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "COMPILACION EXITOSA" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Las dependencias de GraphQL han sido descargadas en:" -ForegroundColor Cyan
    Write-Host "  ~/.m2/repository/" -ForegroundColor White
    Write-Host ""
    Write-Host "El proyecto esta compilado en:" -ForegroundColor Cyan
    Write-Host "  target/classes/" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "Error durante la compilacion" -ForegroundColor Red
    Write-Host "Verifica que tengas Java 11 o superior" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Para ejecutar las funciones:" -ForegroundColor Cyan
Write-Host "  ./mvnw azure-functions:run" -ForegroundColor White
Write-Host "o" -ForegroundColor Gray
Write-Host "  func host start --port 7072" -ForegroundColor White