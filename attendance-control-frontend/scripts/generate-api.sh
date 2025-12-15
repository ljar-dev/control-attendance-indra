#!/bin/bash

# Colores para la terminal
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Leer el entorno (por defecto development)
ENV=${1:-development}

echo -e "${YELLOW}🔧 Configuración del entorno: ${ENV}${NC}"

# Configurar URLs según el entorno (sin archivos .env)
if [ "$ENV" = "production" ]; then
    API_DOCS_URL="https://api.produccion.com/v3/api-docs"
    API_BASE_PATH="https://api.produccion.com"
elif [ "$ENV" = "development" ]; then
    API_DOCS_URL="http://localhost:8080/v3/api-docs"
    API_BASE_PATH="http://localhost:8080"
else
    # Por defecto usa development
    API_DOCS_URL="http://localhost:8080/v3/api-docs"
    API_BASE_PATH="http://localhost:8080"
fi

echo -e "${YELLOW}📡 API Docs URL: ${API_DOCS_URL}${NC}"
echo -e "${YELLOW}🌐 API Base Path: ${API_BASE_PATH}${NC}"

# Crear directorio temporal
mkdir -p ./temp

# Descargar el OpenAPI spec
echo -e "${YELLOW}⬇️  Descargando OpenAPI spec desde ${API_DOCS_URL}...${NC}"

curl -s -o ./temp/openapi.json "${API_DOCS_URL}"

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error al descargar el OpenAPI spec${NC}"
    echo -e "${YELLOW}Verifica que tu backend esté corriendo en ${API_DOCS_URL}${NC}"
    exit 1
fi

# Verificar que el archivo no esté vacío
if [ ! -s ./temp/openapi.json ]; then
    echo -e "${RED}❌ Error: El archivo OpenAPI descargado está vacío${NC}"
    echo -e "${YELLOW}Asegúrate de que tu backend esté respondiendo en ${API_DOCS_URL}${NC}"
    exit 1
fi

echo -e "${GREEN} OpenAPI spec descargado exitosamente${NC}"

# Limpiar generación anterior
echo -e "${YELLOW}🧹 Limpiando archivos anteriores...${NC}"
rm -rf ./src/app/generated/api

# Generar servicios para Angular 16
echo -e "${YELLOW}🚀 Generando servicios de API para Angular 16...${NC}"

npx openapi-generator-cli generate \
  -g typescript-angular \
  -i ./temp/openapi.json \
  -o ./src/app/generated/api \
  --additional-properties=ngVersion=16.0.0,providedInRoot=true,withInterfaces=true,useSingleRequestParameter=false

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error al generar servicios${NC}"
    exit 1
fi

# Crear archivo de configuración
echo -e "${YELLOW}📝 Creando archivo de configuración...${NC}"

cat > ./src/app/generated/api/api-config.ts << EOF
// Este archivo es generado automáticamente
export const API_CONFIG = {
  basePath: '${API_BASE_PATH}',
  environment: '${ENV}'
};
EOF

echo -e "${GREEN} Servicios generados exitosamente en ./src/app/generated/api${NC}"
echo -e "${GREEN} Configuración guardada para ambiente: ${ENV}${NC}"

# Limpiar temporales
rm -rf ./temp

echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✨ ¡Proceso completado exitosamente! ✨${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"