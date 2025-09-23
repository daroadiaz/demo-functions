# Script para descargar todas las dependencias de GraphQL necesarias

$libDir = "lib"
if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir
}

Write-Host "=====================================" -ForegroundColor Green
Write-Host "DESCARGANDO DEPENDENCIAS DE GRAPHQL" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""

# Lista completa de dependencias necesarias para GraphQL
$dependencies = @(
    # GraphQL Core
    @{
        name = "graphql-java-20.7.jar"
        url = "https://repo1.maven.org/maven2/com/graphql-java/graphql-java/20.7/graphql-java-20.7.jar"
    },
    @{
        name = "graphql-java-extended-scalars-20.2.jar"
        url = "https://repo1.maven.org/maven2/com/graphql-java/graphql-java-extended-scalars/20.2/graphql-java-extended-scalars-20.2.jar"
    },
    # Java DataLoader (requerido por GraphQL)
    @{
        name = "java-dataloader-3.2.0.jar"
        url = "https://repo1.maven.org/maven2/com/graphql-java/java-dataloader/3.2.0/java-dataloader-3.2.0.jar"
    },
    # SLF4J (logging)
    @{
        name = "slf4j-api-2.0.9.jar"
        url = "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar"
    },
    @{
        name = "slf4j-simple-2.0.9.jar"
        url = "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar"
    },
    # Reactive Streams (requerido por GraphQL)
    @{
        name = "reactive-streams-1.0.4.jar"
        url = "https://repo1.maven.org/maven2/org/reactivestreams/reactive-streams/1.0.4/reactive-streams-1.0.4.jar"
    },
    # ANTLR (parser usado por GraphQL)
    @{
        name = "antlr4-runtime-4.11.1.jar"
        url = "https://repo1.maven.org/maven2/org/antlr/antlr4-runtime/4.11.1/antlr4-runtime-4.11.1.jar"
    },
    # Azure Functions
    @{
        name = "azure-functions-java-library-3.0.0.jar"
        url = "https://repo1.maven.org/maven2/com/microsoft/azure/functions/azure-functions-java-library/3.0.0/azure-functions-java-library-3.0.0.jar"
    },
    # Jackson (JSON processing)
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
    },
    # Oracle JDBC (para base de datos)
    @{
        name = "ojdbc11-21.9.0.0.jar"
        url = "https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/21.9.0.0/ojdbc11-21.9.0.0.jar"
    }
)

$total = $dependencies.Count
$current = 0

foreach ($dep in $dependencies) {
    $current++
    $filePath = Join-Path $libDir $dep.name

    Write-Progress -Activity "Descargando dependencias" -Status "$current de $total" -PercentComplete (($current / $total) * 100)

    if (-not (Test-Path $filePath)) {
        Write-Host "[$current/$total] Descargando $($dep.name)..." -ForegroundColor Yellow
        try {
            Invoke-WebRequest -Uri $dep.url -OutFile $filePath -UseBasicParsing
            Write-Host "  ✓ Descargado exitosamente" -ForegroundColor Green
        } catch {
            Write-Host "  ✗ Error descargando: $_" -ForegroundColor Red
        }
    } else {
        Write-Host "[$current/$total] $($dep.name) ya existe" -ForegroundColor Cyan
    }
}

Write-Progress -Activity "Descargando dependencias" -Completed

Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "DESCARGA COMPLETADA" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
Write-Host "Total de dependencias: $total" -ForegroundColor Cyan
Write-Host "Ubicacion: $libDir" -ForegroundColor Cyan
Write-Host ""
Write-Host "Para compilar con estas dependencias usa:" -ForegroundColor Yellow
Write-Host '  javac -cp "lib/*;target/classes" -d target/classes src/main/java/com/example/functions/graphql/*.java' -ForegroundColor White