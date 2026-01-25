# Pagination-Analyse: get_current_broadcast (Endpoint: /cmdm/epg/broadcasts)

## Verwendeter REST-Endpunkt
- **Pfad:** `/cmdm/epg/broadcasts`
- **Parameter:**
  - `from` (berechnet: jetzt - 3h)
  - `to` (berechnet: jetzt + 15min)
  - `tvService` (required): Sender-Name
  - `limit` (optional, default: 10)

## Response-Struktur
- Antwort ist eine Liste von Broadcast-Objekten (wie bei get_broadcast_schedule)
- Keine expliziten Paging-Informationen, da typischerweise nur ein sehr kleines Zeitfenster abgefragt wird

## Pagination-Mechanik
- Paging ist für diesen Use Case nicht relevant, da immer nur ein sehr kleiner Ausschnitt (aktuelle Sendung) abgefragt wird
- `limit` kann gesetzt werden, aber es gibt keine Folgeseiten
- MCP-Cursor/Pagination ist nicht notwendig

## Mapping auf MCP-Cursor-Modell
- Kein Mapping erforderlich, da keine Pagination
- `nextCursor` bleibt immer leer/fehlt

## Beispiel-Request
```http
GET /cmdm/epg/broadcasts?from=2025-12-27T19:00:00%2B01:00&to=2025-12-27T19:15:00%2B01:00&tvService=ZDF&limit=10
```

## Beispiel-Response
```json
{
  "broadcasts": [ ... ]
}
```

## Besonderheiten & Edgecases
- Zeitfenster ist sehr klein, daher keine Notwendigkeit für Pagination
- Falls mehrere Sendungen im Zeitfenster liegen, wird die aktuell laufende ausgewählt

== API-Parameter
- tvService (required, query): Sender
- profile (optional, query): Inhaltsprofil (Default: default)

== Paging-Mechanik
- Kein Paging möglich, da nur aktueller und nächster Eintrag geliefert werden.
