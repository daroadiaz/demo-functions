# Script para descargar las dependencias necesarias
$libDir = "lib"
if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir
}

Write-Host "Descargando dependencias necesarias..." -ForegroundColor Yellow

# URLs de Maven Central
$dependencies = @(
    @{
        name = "azure-functions-java-library-3.0.0.jar"
        url = "https://repo1.maven.org/maven2/com/microsoft/azure/functions/azure-functions-java-library/3.0.0/azure-functions-java-library-3.0.0.jar"
    },
    @{
        name = "ojdbc11-21.9.0.0.jar"
        url = "https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/21.9.0.0/ojdbc11-21.9.0.0.jar"
    },
    @{
        name = "jackson-databind-2.15.2.jar"
        url = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar"
    },
    @{
        name = "jackson-core-2.15.2.jar"
        url = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar"
    },
    @{
        name = "jackson-annotations-2.15.2.jar"
        url = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar"
    }
)

foreach ($dep in $dependencies) {
    $filePath = Join-Path $libDir $dep.name
    if (-not (Test-Path $filePath)) {
        Write-Host "Descargando $($dep.name)..." -ForegroundColor Gray
        Invoke-WebRequest -Uri $dep.url -OutFile $filePath
    } else {
        Write-Host "$($dep.name) ya existe" -ForegroundColor Green
    }
}

Write-Host "Dependencias descargadas en $libDir" -ForegroundColor Green