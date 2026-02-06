# Pagination-Analyse: get_broadcast_schedule (Endpoint: /cmdm/epg/broadcasts)

## Verwendeter REST-Endpunkt
- **Pfad:** `/cmdm/epg/broadcasts`
- **Parameter:**
  - `from` (required): ISO 8601 datetime (Startzeit)
  - `to` (required): ISO 8601 datetime (Endzeit)
  - `tvServices` (optional): Kommagetrennte Liste von Kanälen
  - `limit` (optional, default: 50): Maximale Anzahl der Ergebnisse pro Seite
  - `page` (optional): Seitenzahl für Pagination
  - `profile`, `order` (optional)

## Response-Struktur
- Antwort enthält eine Liste von Broadcast-Objekten
- Es gibt keine expliziten Felder wie `next`, aber durch `limit` und `page` ist klassische Pagination möglich
- Die Response kann Felder wie `total`, `page`, `pages` enthalten (je nach API-Version)

## Pagination-Mechanik
- Klassische page/limit-Pagination
- Client kann mit `page` und `limit` beliebige Seiten abfragen
- Es gibt keine Cursor, sondern seitenbasierte Navigation
- Die API liefert pro Seite maximal `limit` Einträge

## Mapping auf MCP-Cursor-Modell
- MCP-Cursor kann die aktuelle page/limit-Kombination kodieren (z.B. als Base64-JSON)
- `nextCursor` wird gesetzt, solange weitere Seiten existieren (z.B. page < pages)
- Bei letzter Seite bleibt `nextCursor` leer/fehlt

## Beispiel-Request
```http
GET /cmdm/epg/broadcasts?from=2025-12-27T00:00:00%2B01:00&to=2025-12-27T23:59:59%2B01:00&limit=50&page=1
```

## Beispiel-Response
```json
{
  "broadcasts": [ ... ],
  "page": 1,
  "pages": 10,
  "total": 500
}
```

## Besonderheiten & Edgecases
- Sehr große Datenmengen möglich (z.B. alle Sender, ganzer Tag)
- Bei zu großem Zeitraum/zu vielen Ergebnissen: API-Limitierungen beachten
- page/limit müssen für MCP-Cursor-Logik kodiert werden

== API-Parameter
- from (optional, query): Zeitrahmen von
- to (optional, query): Zeitrahmen bis
- brands (optional, query): Sendungsmarken-IDs (kommasepariert)
- tvServices (optional, query): Sender (kommasepariert)
- series (optional, query): Serien-UUID
- season (optional, query): Season-UUID
- limit (optional, query): Ergebnisse pro Seite (Default: 3)
- page (optional, query): Seite (Default: 1)
- order (optional, query): Sortierung (asc/desc)
- onlyCompleteBroadcasts (optional, query): Nur Gesamtsendungen (true/false)
- profile (optional, query): Inhaltsprofil (Default: default)

== Paging-Mechanik
- page/limit Pagination
- nextCursor codiert page+limit
