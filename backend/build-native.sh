#!/bin/bash
# ============================================================
# build-native.sh — Build nativo GraalVM do ms-catalogo
# e deploy no Minikube local
#
# Pré-requisitos:
#   - GraalVM CE 21+ instalado (sdk install java 21-graalce via SDKMAN)
#   - Docker rodando
#   - Minikube rodando: minikube start
#
# Uso: ./build-native.sh
# ============================================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}=== Florinda Eats — Build Native ms-catalogo ===${NC}\n"

# ----------------------------------------------------------
# 1. Verifica pré-requisitos
# ----------------------------------------------------------
echo -e "${YELLOW}[1/5] Verificando pré-requisitos...${NC}"

if ! command -v native-image &> /dev/null; then
  echo "GraalVM native-image não encontrado."
  echo "Instale: sdk install java 21-graalce && gu install native-image"
  exit 1
fi

if ! command -v minikube &> /dev/null; then
  echo "Minikube não encontrado. Instale em: https://minikube.sigs.k8s.io/docs/start/"
  exit 1
fi

echo "GraalVM: $(native-image --version | head -1)"
echo "Minikube: $(minikube version --short)"

# ----------------------------------------------------------
# 2. Build nativo com container build (usa Docker)
# ----------------------------------------------------------
echo -e "\n${YELLOW}[2/5] Build native GraalVM (pode demorar 5-10 minutos)...${NC}"

cd ms-catalogo

mvn package -Pnative \
  -DskipTests \
  -Dquarkus.native.container-build=true \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.name=florinda/ms-catalogo \
  -Dquarkus.container-image.tag=native \
  --batch-mode --no-transfer-progress

echo -e "${GREEN}Build nativo concluído!${NC}"

# ----------------------------------------------------------
# 3. Mostra tamanho das imagens para comparação
# ----------------------------------------------------------
echo -e "\n${YELLOW}[3/5] Comparação de imagens:${NC}"
echo "Imagem nativa:"
docker image ls florinda/ms-catalogo:native --format "  Size: {{.Size}}"

echo ""
echo "Startup time esperado:"
echo "  JVM mode:    ~2000ms, heap ~200MB"
echo "  Native mode: ~30ms,   heap ~30MB"

# ----------------------------------------------------------
# 4. Carrega imagem no Minikube
# ----------------------------------------------------------
echo -e "\n${YELLOW}[4/5] Carregando imagem no Minikube...${NC}"
cd ..
minikube image load florinda/ms-catalogo:native
echo "Imagem carregada no Minikube."

# ----------------------------------------------------------
# 5. Deploy no Minikube
# ----------------------------------------------------------
echo -e "\n${YELLOW}[5/5] Aplicando manifests K8s...${NC}"

kubectl apply -f kubernetes/infra/00-namespaces.yml
kubectl apply -f kubernetes/infra/dependencies.yml

echo "Aguardando infraestrutura minima (Postgres/Redis) ficar Ready..."
kubectl rollout status deployment/postgres -n florinda-infra --timeout=180s
kubectl rollout status deployment/redis -n florinda-infra --timeout=180s

kubectl apply -f kubernetes/ms-catalogo/deployment.yml

echo "Aguardando pod ficar Ready..."
kubectl rollout status deployment/ms-catalogo -n florinda-app --timeout=60s

echo -e "\n${GREEN}=== Deploy concluído! ===${NC}"
echo ""
echo "Verificar startup time:"
echo "  kubectl logs -l app=ms-catalogo -n florinda-app | grep 'started in'"
echo ""
echo "Acessar Swagger:"
echo "  kubectl port-forward svc/ms-catalogo 8082:8082 -n florinda-app"
echo "  Acesse: http://localhost:8082/swagger-ui"
echo ""
echo "Ver métricas no Grafana:"
echo "  http://localhost:3000 → Dashboard 'ms-catalogo JVM vs Native'"
