# TDD-Plan: get_current_broadcast Feature

## Überblick

Implementierung eines MCP-Tools `get_current_broadcast`, das die aktuell laufende Sendung eines bestimmten ZDF-Senders
zurückgibt.

## Motivation

- Nutzer/LLMs möchten schnell wissen: "Was läuft gerade auf ZDF?"
- Einfachere API als `get_broadcast_schedule` für den häufigen Use Case "jetzt"
- Nutzt intern den bestehenden `/cmdm/epg/broadcasts` Endpoint mit einem zeitlichen Fenster um "jetzt"

## API-Design

### MCP Tool Signatur

```kotlin
@McpTool(
    name = "get_current_broadcast",
    description = "Get the currently airing program on a specific ZDF channel. " +
            "Returns the broadcast that is currently on air at the time of the request. " +
            "Parameters: " +
            "- tvService: Channel name (required, e.g., ZDF, ZDFneo, 3sat, ZDFinfo, PHOENIX, KIKA). " +
            "- limit: Maximum number of broadcasts to return (default: 10). " +
            "Common channels: ZDF, ZDFneo, ZDFinfo, 3sat, PHOENIX, KIKA. " +
            "Returns the current program with title, time, description, and channel info."
)
fun getCurrentBroadcast(tvService: String, limit: Int = 10): CurrentBroadcastResponse
```

### Parameter

- `tvService` (String, required): Sender-Name (z.B. "ZDF", "ZDFneo", "3sat")
- `limit` (Int, optional, default: 10): Maximale Anzahl der Broadcasts, die vom API abgerufen werden (erhöht die
  Wahrscheinlichkeit, die aktuell laufende Sendung zu finden)

### Rückgabewert

Neue Response-Klasse `CurrentBroadcastResponse`:

```kotlin
data class CurrentBroadcastResponse(
    val tvService: String,
    val currentBroadcast: BroadcastItem?,
    val queriedAt: String  // ISO 8601 timestamp
)
```

### Implementierungs-Logik

1. Aktuellen Zeitpunkt ermitteln (`OffsetDateTime.now()`)
2. Zeitfenster berechnen:
    - `from`: jetzt - 3 Stunden (um sicherzugehen, dass wir die laufende Sendung erfassen)
    - `to`: jetzt + 15 Minuten (kleiner Buffer für API-Latenz)
3. `zdfMediathekService.getBroadcastSchedule()` mit dem übergebenen `limit` aufrufen
4. Erste Sendung aus Result nehmen, die:
    - `airtimeBegin <= jetzt` UND
    - `airtimeEnd > jetzt`
5. Falls keine Sendung gefunden: `currentBroadcast = null` zurückgeben

## TDD-Phasen

### Phase 1: Red - Unit Tests schreiben (schlagen fehl)

#### Test-Datei: `CurrentBroadcastServiceTest.kt`

**Test 1: `getCurrentBroadcast_validChannel_returnsCurrentBroadcast`**

- Setup: Mock `zdfMediathekService` gibt Broadcast-Liste zurück mit einer laufenden Sendung
- Aktion: `getCurrentBroadcast("ZDF")`
- Assertions:
    - Response hat `tvService = "ZDF"`
    - `currentBroadcast` ist nicht null
    - `currentBroadcast` hat erwarteten Titel
    - `queriedAt` ist valides ISO 8601 Format

**Test 2: `getCurrentBroadcast_emptyChannelName_throwsException`**

- Aktion: `getCurrentBroadcast("")`
- Assertion: `IllegalArgumentException` mit Message "Parameter 'tvService' is required"

**Test 3: `getCurrentBroadcast_blankChannelName_throwsException`**

- Aktion: `getCurrentBroadcast("   ")`
- Assertion: `IllegalArgumentException` mit Message "Parameter 'tvService' is required"

**Test 4: `getCurrentBroadcast_invalidLimit_throwsException`**

- Aktion: `getCurrentBroadcast("ZDF", limit = 0)`
- Assertion: `IllegalArgumentException` mit Message "Parameter 'limit' must be greater than 0"

**Test 5: `getCurrentBroadcast_noBroadcastFound_returnsNull`**

- Setup: Mock gibt leere Liste zurück
- Aktion: `getCurrentBroadcast("ZDF")`
- Assertions:
    - `currentBroadcast` ist null
    - `tvService` und `queriedAt` sind gesetzt

**Test 6: `getCurrentBroadcast_multipleBroadcasts_returnsCurrentOne`**

- Setup: Mock gibt 3 Broadcasts zurück (vergangen, aktuell, zukünftig)
- Aktion: `getCurrentBroadcast("ZDF")`
- Assertion: Nur die aktuell laufende Sendung wird zurückgegeben

**Test 7: `getCurrentBroadcast_apiThrowsException_wrapsException`**

- Setup: Mock wirft `RuntimeException`
- Aktion: `getCurrentBroadcast("ZDF")`
- Assertion: `RuntimeException` mit Message "Failed to get current broadcast"

**Test 8: `getCurrentBroadcast_callsApiWithCorrectTimeWindow`**

- Aktion: `getCurrentBroadcast("ZDF", limit = 15)`
- Assertion: Verify dass `getBroadcastSchedule` aufgerufen wurde mit:
    - `tvService = "ZDF"`
    - `limit = 15`
    - `from` ist ca. 3 Stunden vor jetzt (±1 Minute Toleranz)
    - `to` ist ca. 15 Minuten nach jetzt (±1 Minute Toleranz)

### Phase 2: Red - Integration Tests schreiben (schlagen fehl)

#### Test-Datei: `CurrentBroadcastServiceIT.kt`

**Test 1: `getCurrentBroadcast_realApi_returnsCurrentBroadcast`**

- Setup: WireMock mit realistischer ZDF API Response
- Aktion: `getCurrentBroadcast("ZDF")`
- Assertions:
    - Response enthält Daten
    - `tvService = "ZDF"`
    - `queriedAt` ist valides ISO 8601
    - OAuth2 Token wurde abgerufen (verify WireMock Token-Endpoint)

**Test 2: `getCurrentBroadcast_apiReturnsNoResults_handlesGracefully`**

- Setup: WireMock gibt leere Broadcast-Liste
- Aktion: `getCurrentBroadcast("UnknownChannel")`
- Assertion: `currentBroadcast` ist null, keine Exception

**Test 3: `getCurrentBroadcast_apiReturns401_throwsException`**

- Setup: WireMock gibt 401 zurück
- Aktion: `getCurrentBroadcast("ZDF")`
- Assertion: Exception wird geworfen

### Phase 3: Green - Produktivcode implementieren

#### Neue Dateien erstellen:

**1. `CurrentBroadcastResponse.kt`**

```kotlin
package eu.wiegandt.zdfmediathekmcp.model

data class CurrentBroadcastResponse(
    val tvService: String,
    val currentBroadcast: BroadcastItem?,
    val queriedAt: String
)
```

**2. `CurrentBroadcastService.kt`**

- Service-Klasse mit `@Service` und `@McpTool`-Annotation
- Konstruktor injiziert `ZdfMediathekService`
- Logger für Info/Debug/Error
- `getCurrentBroadcast()`-Methode implementieren:
    - Parameter-Validierung (tvService nicht blank, limit > 0)
    - Aktuellen Zeitpunkt ermitteln
    - Zeitfenster berechnen (from: -3h, to: +15min)
    - API aufrufen mit dem übergebenen `limit`
    - Broadcasts filtern nach: `airtimeBegin <= now < airtimeEnd`
    - Erste gefundene Sendung zurückgeben (oder null)
    - Exception-Handling mit logging
    - Try-catch um `RuntimeException` zu wrappen

**Implementierungs-Details:**

- Zeitzone: Europe/Berlin (`ZoneId.of("Europe/Berlin")`)
- Zeitfenster: from = now - 3h, to = now + 15min
- API-Limit: Nutzer-definiert (default: 10)
- Filter-Logik: Finde ersten Broadcast wo `airtimeBegin <= now && airtimeEnd > now`

### Phase 4: Refactoring

**Mögliche Verbesserungen:**

1. **Konstanten extrahieren**: Zeitfenster-Werte (3h, 15min) als Konstanten
2. **Hilfsmethode**: `findCurrentBroadcast(broadcasts, now)` für bessere Testbarkeit
3. **Logging-Verbesserungen**: Strukturiertes Logging mit MDC
4. **Error-Messages**: Konsistent mit anderen Services
5. **Timezone-Handling**: Prüfen, ob ZDF API besser mit UTC umgeht

**Code-Quality-Checks:**

- Detekt: 0 Violations
- SonarQube: >= A Rating
- Test Coverage: >= 90%
- Alle Tests grün (Unit + Integration)

## Offene Fragen & Entscheidungen

### Zeitfenster-Größe

- **From: -3 Stunden**: Ausreichend, um auch lange Sendungen (Filme) zu erfassen
- **To: +15 Minuten**: Kleiner Buffer, nicht zu weit in die Zukunft

### Was wenn mehrere Broadcasts gefunden werden?

- **Lösung**: Ersten nehmen, der die Bedingung erfüllt (sollte nur einer sein)
- API-Limit: Nutzer-definiert (default: 10) - erhöht die Wahrscheinlichkeit, die laufende Sendung zu finden

### Was wenn keine Sendung läuft?

- **Lösung**: `currentBroadcast = null` zurückgeben
- Kein Fehler, sondern valider Zustand (z.B. nachts, Sendepause)

### Timezone-Handling

- **Entscheidung**: `Europe/Berlin` nutzen, da ZDF deutsche Sender sind
- API erwartet ISO 8601 mit Timezone-Offset

### Soll `tvService` validiert werden?

- **Entscheidung**: NEIN - wie bei `get_broadcast_schedule`
- ZDF API validiert das bereits und gibt 404 oder leere Liste zurück
- Keeps the code simple and maintainable

## Testing-Strategie

### Unit Tests (mit Mockito)

- Alle Business-Logik isoliert testen
- Mock `ZdfMediathekService`
- Edge Cases: leer, null, Exceptions
- Zeitfenster-Berechnung testen (mit fixer Zeit via `Clock`)

### Integration Tests (mit WireMock)

- OAuth2 Token-Flow mocken (minimal)
- Realistische ZDF API Responses
- Error-Handling (401, 404, 500)
- End-to-End Flow validieren

### Test-Daten

- Realistische Broadcast-Objekte aus bestehenden `broadcast_schedule_response.json`
- Timestamps relativ zu "now" generieren für reproduzierbare Tests

## Best Practices

### Spring Best Practices

- `@Service` für Business-Logik
- Constructor Injection (nicht `@Autowired`)
- Immutable Data Classes
- Proper Exception Handling

### Kotlin Best Practices

- Data Classes für DTOs
- Null-Safety (`currentBroadcast: BroadcastItem?`)
- Named Parameters bei Funktionsaufrufen
- Extension Functions falls sinnvoll

### TDD Best Practices

- Red-Green-Refactor Zyklus strikt befolgen
- Tests zuerst schreiben
- Minimal nötige Implementierung für Green
- Refactoring erst wenn alle Tests grün

### Open Source Best Practices

- Klare, beschreibende Namen
- Ausführliche KDoc-Dokumentation
- Konsistenter Code-Stil (wie bestehende Services)
- Gute Fehlermeldungen für API-Nutzer

## Timeline

1. **Phase 1 (Red - Unit Tests)**: ~30 Minuten
2. **Phase 2 (Red - Integration Tests)**: ~20 Minuten
3. **Phase 3 (Green - Implementierung)**: ~40 Minuten
4. **Phase 4 (Refactoring)**: ~20 Minuten
5. **Dokumentation Update**: ~10 Minuten

**Gesamt**: ~2 Stunden

## Success Criteria

- ✅ **Alle Tests grün** (Unit + Integration) - 13/13 Tests grün
- ✅ **Code Coverage >= 90%** - CurrentBroadcastService: Line 100%, Branch 93%, Instruction 100%
- ⏭️ **Detekt: 0 Violations** - Nicht konfiguriert im Projekt
- ⏭️ **SonarQube: A Rating** - Wird im CI ausgeführt
- ✅ **README.md aktualisiert** - get_current_broadcast zu "Currently Available" verschoben
- ✅ **AGENTS.md aktualisiert** - Bereits referenziert
- ✅ **MCP Tool funktioniert** - Erfolgreich vom User getestet
- ✅ **Logging korrekt und konsistent** - Konsistent mit BroadcastScheduleService
- ✅ **Code ist lesbar und wartbar** - Klare Struktur, gute Dokumentation
- ✅ **Konsistent mit bestehendem Code-Stil** - Folgt allen Projektkonventionen

## Implementierungs-Zusammenfassung

### Phase 1: Red - Unit Tests ✅

- 8 Unit Tests erstellt
- Alle Tests schlugen erwartungsgemäß fehl (NotImplementedError)

### Phase 2: Red - Integration Tests ✅

- 5 Integration Tests mit WireMock erstellt
- Alle Tests schlugen erwartungsgemäß fehl

### Phase 3: Green - Produktivcode ✅

- `CurrentBroadcastResponse.kt` - Data Class für Response
- `CurrentBroadcastService.kt` - Vollständige Implementierung mit:
    - Parameter-Validierung
    - Zeitfenster-Berechnung (now -3h bis now +15min)
    - API-Integration
    - Filter-Logik für aktuell laufende Sendung
    - Umfassendes Error-Handling und Logging
- Alle 13 Tests grün

### Phase 4: Refactoring ✅

- Konstanten aus companion object in Klassenvariablen verschoben (UPPER_SNAKE_CASE)
- Integration Test mit UTC-Normalisierung für OffsetDateTime-Vergleich optimiert
- Dokumentation aktualisiert (README.md)
- Code-Qualität geprüft und bestätigt

### Zeitaufwand

- **Tatsächlich**: ~2.5 Stunden (inkl. Diskussion und Fehlerbehebung)
- **Geplant**: ~2 Stunden

### Besondere Herausforderungen

1. **Mockito nullable Parameter** - Lösung: `anyString()` statt `any()` oder `nullable()`
2. **Jackson UTC-Konvertierung** - Lösung: `withOffsetSameInstant(ZoneOffset.UTC)` im Test-Comparator
3. **Konstanten-Konvention** - Diskussion und Lösung: UPPER_SNAKE_CASE als `private val` direkt in Klasse

**Status: ERFOLGREICH ABGESCHLOSSEN** ✅

