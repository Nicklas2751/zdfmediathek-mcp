# Pagination-Analyse: list_seasons (Endpoint: /cmdm/seasons)

## Verwendeter REST-Endpunkt
- **Pfad:** `/cmdm/seasons`
- **Parameter:**
  - `limit` (optional, default: 10) – Maximale Anzahl der zurückgegebenen Staffeln

## Response-Struktur
- Antwort ist eine Liste von Season-Objekten mit Feldern wie `seasonUuid`, `seasonNumber`, `title`, `series`, `brandId`
- Keine expliziten Paging-Informationen wie `next`, `offset`, `total` in der Response dokumentiert

## API-Parameter
- `seasonNumber` (optional, query): Nummer der Season
- `seasonTitle` (optional, query): Titel der Season (Teilstring)
- `imdbId` (optional, query): IMDB-ID
- `limit` (optional, query): Anzahl der Ergebnisse pro Seite (Default: 3)
- `page` (optional, query): Seite (Default: 1)
- `profile` (optional, query): Inhaltsprofil (Default: default)

## Paging-Mechanik
== Hinweise
- page/limit Pagination laut OpenAPI vorhanden
- nextCursor codiert page+limit
- Keine Offset- oder Cursor-Parameter

## Beispiel-Request
```http
GET /cmdm/seasons?limit=10
```

## Beispiel-Response
```json
[
  {
    "seasonUuid": "...",
    "seasonNumber": 1,
    "title": "...",
    "series": { ... },
    "brandId": "..."
  },
  ...
]
```

## Besonderheiten & Edgecases
- Kein Offset, keine Cursor, keine klassische Pagination
- Bei mehr als `limit` Staffeln ist keine vollständige Auflistung möglich
- Für vollständige MCP-Kompatibilität wäre ein API-Update nötig
