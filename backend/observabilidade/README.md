# Observabilidade local

Stack oficial da Fase 4 para desenvolvimento local via Docker Desktop.

## Serviços

- Grafana: http://localhost:3000 (`admin` / `admin`)
- Prometheus: http://localhost:9090
- Jaeger: http://localhost:16686
- OTLP gRPC: `http://localhost:4317`
- OTLP HTTP: `http://localhost:4318`

## Subir

```powershell
docker compose -f .\observabilidade\docker-compose.yml up -d
```

## Reiniciar

```powershell
docker compose -f .\observabilidade\docker-compose.yml restart
```

## Derrubar

```powershell
docker compose -f .\observabilidade\docker-compose.yml down
```

## Ver status

```powershell
docker compose -f .\observabilidade\docker-compose.yml ps
```

## Observações

- O `Prometheus` faz scrape dos módulos Quarkus no host via `host.docker.internal`.
- Para traces em dev, os módulos usam `OTEL_ENDPOINT=http://localhost:4317`.
- O `dev-up.ps1` pode subir essa stack automaticamente; use `-SkipObservability` para não iniciar os containers.

