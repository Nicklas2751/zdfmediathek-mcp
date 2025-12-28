# Plan: OAuth2 Implementierung für ZDF API

Dieser Plan beschreibt die Schritte zur Implementierung der OAuth2 Client Credentials Authentifizierung für den ZDF
Mediathek Service, basierend auf dem fehlgeschlagenen Integrationstest.

## Status Quo

- Integrationstest `searchDocuments_withOAuth2_sendsAuthorizationHeader` existiert und schlägt fehl (RED).
- `ZdfProperties` Klasse für Konfiguration (client.id, client.secret) existiert.
- `build.gradle.kts` enthält bereits `spring-boot-starter-oauth2-client`.

## Schritte zur Umsetzung (GREEN)

### 1. Konfiguration der OAuth2 Komponenten in `HttpServicesConfiguration`

Die Datei `src/main/kotlin/eu/wiegandt/zdfmediathekmcp/config/HttpServicesConfiguration.kt` muss angepasst werden.

**Zu erstellende Beans:**

1. **`clientRegistrationRepository`**:
    - Typ: `ReactiveClientRegistrationRepository`
    - Implementierung: `InMemoryReactiveClientRegistrationRepository`
    - Konfiguration: Erstellt eine `ClientRegistration` mit ID "zdf".
    - Parameter: `ZdfProperties` (für Credentials) und `baseUrl` (für Token-URI).
    - Grant Type: `client_credentials`.
    - Token URI: `${baseUrl}/oauth/token`.

2. **`authorizedClientService`**:
    - Typ: `ReactiveOAuth2AuthorizedClientService`
    - Implementierung: `InMemoryReactiveOAuth2AuthorizedClientService`.

3. **`authorizedClientManager`**:
    - Typ: `ReactiveOAuth2AuthorizedClientManager`
    - Implementierung: `AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager`.

### 2. Anpassung des WebClients

In der `proxyFactory` Methode in `HttpServicesConfiguration`:

1. **OAuth2 Filter erstellen**:
    - `ServerOAuth2AuthorizedClientExchangeFilterFunction` mit dem `authorizedClientManager` instanziieren.
    - Default Client Registration ID auf "zdf" setzen.

2. **WebClient Builder anpassen**:
    - Den OAuth2 Filter zum `clientBuilder` hinzufügen (`.filter(oauth)`).
    - Den statischen Header "Api-Auth" entfernen (falls er durch OAuth2 ersetzt wird, oder beibehalten falls zusätzlich
      nötig - laut Test erwarten wir einen Bearer Token via Authorization Header, was der OAuth2 Client automatisch
      macht).

## Verifikation

- Ausführen von `./gradlew test --tests eu.wiegandt.zdfmediathekmcp.ZdfMediathekServiceIT`.
- Beide Tests (`searchDocuments_validQuery_returnsResults` und `searchDocuments_withOAuth2_sendsAuthorizationHeader`)
  müssen bestehen.

