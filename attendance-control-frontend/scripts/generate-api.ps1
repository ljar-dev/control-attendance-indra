param(
    [string]$Environment = "development"
)

Write-Host "🔧 Cargando configuración del entorno: $Environment" -ForegroundColor Yellow

# Cargar variables de entorno
$envFile = if ($Environment -eq "production") { ".env.production" } 
           elseif ($Environment -eq "development") { ".env.development" } 
           else { ".env" }

Get-Content $envFile | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        $name = $matches[1]
        $value = $matches[2]
        Set-Variable -Name $name -Value $value -Scope Script
    }
}

Write-Host "📡 API Docs URL: $API_DOCS_URL" -ForegroundColor Yellow
Write-Host "🌐 API Base Path: $API_BASE_PATH" -ForegroundColor Yellow

# Crear directorio temporal
New-Item -ItemType Directory -Force -Path "./temp" | Out-Null

# Descargar OpenAPI spec
Write-Host "⬇️  Descargando OpenAPI spec..." -ForegroundColor Yellow
Invoke-WebRequest -Uri $API_DOCS_URL -OutFile "./temp/openapi.json"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al descargar el OpenAPI spec" -ForegroundColor Red
    exit 1
}

Write-Host " OpenAPI spec descargado" -ForegroundColor Green

# Limpiar generación anterior
Write-Host "🧹 Limpiando archivos anteriores..." -ForegroundColor Yellow
Remove-Item -Path "./src/app/generated/api" -Recurse -Force -ErrorAction SilentlyContinue

# Generar servicios para Angular 16
Write-Host "🚀 Generando servicios..." -ForegroundColor Yellow
npx openapi-generator-cli generate `
  -g typescript-angular `
  -i ./temp/openapi.json `
  -o ./src/app/generated/api `
  --additional-properties=ngVersion=16.0.0,providedInRoot=true,withInterfaces=true,useSingleRequestParameter=true

# Crear archivo de configuración
$configContent = @"
// Este archivo es generado automáticamente
export const API_CONFIG = {
  basePath: '$API_BASE_PATH',
  environment: '$Environment'
};
"@

$configContent | Out-File -FilePath "./src/app/generated/api/api-config.ts" -Encoding UTF8

Write-Host " Servicios generados exitosamente" -ForegroundColor Green

# Limpiar temporales
Remove-Item -Path "./temp" -Recurse -Force