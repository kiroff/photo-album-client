# Photo Album (Microservices)

A multi-module, Spring-based microservices playground for a “photo album” style system. The repository is organized as several independent applications (each with its own build/run lifecycle) that work together via service discovery and an API gateway.

## Modules

- **`discovery-service`**  
  Service discovery (Eureka).

- **`api-gateway`**  
  Single entry point into the system (Spring Cloud Gateway — Server MVC). Routes requests to downstream services.

- **`resource-server`**  
  Protected backend APIs (OAuth2 Resource Server) that register with discovery.

- **`photo-album-client`**  
  Web client (Spring MVC + Thymeleaf) acting as the UI layer / OAuth2 client.

> Each module is a standalone Spring Boot application with its own `pom.xml` and Maven Wrapper scripts.

## Tech stack (high level)

- **Java:** 25
- **Build:** Maven (3.9+ recommended; Maven Wrapper included)
- **Framework:** Spring Boot 4.x + Spring Cloud 2025.x (varies by module)
- **Auth:** OAuth2/OIDC (resource server + client)
- **Service discovery:** Netflix Eureka

## Prerequisites

- JDK **25**
- Git
- (Optional) Docker / Docker Compose (if you use the provided container setups)

## Configuration notes

- Most configuration is module-specific and lives under each module’s `src/main/resources/`.
- If you’re using an external Identity Provider (e.g., Keycloak), ensure the issuer/client settings are aligned across:
  - `resource-server` (resource server config)
  - `photo-album-client` (oauth2 client config)
  - `api-gateway` (if/when auth is enforced at the edge)


## Troubleshooting

- **Version mismatch issues:** verify you are running **JDK 25** (`java -version`) and using the module’s Maven Wrapper (`./mvnw`).
- **Services not discovering each other:** start `discovery-service` first, then restart clients so they re-register.
- **Auth failures (401/403):** check issuer URL, client ID/secret, redirect URIs, and scopes in the relevant module config.
