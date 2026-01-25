# Pagination-Analyse: list_series (Endpoint: /cmdm/series)

## Verwendeter REST-Endpunkt
- **Pfad:** `/cmdm/series`
- **Parameter:**
  - `limit` (optional, default: 4) – Maximale Anzahl der zurückgegebenen Serien

## Response-Struktur
- Antwort ist eine Liste von Series-Objekten mit Feldern wie `seriesUuid`, `title`, `description`, `brandId`, `imdbUrl`, `url`
- Keine expliziten Paging-Informationen wie `next`, `offset`, `total` in der Response dokumentiert

## API-Parameter
- `seriesTitle` (optional, query): Titel der Serie (Teilstring)
- `imdbId` (optional, query): IMDB-ID
- `limit` (optional, query): Anzahl der Ergebnisse pro Seite (Default: 3)
- `page` (optional, query): Seite (Default: 1)
- `profile` (optional, query): Inhaltsprofil (Default: default)

## Paging-Mechanik
- page/limit Pagination
- nextCursor codiert page+limit
- Keine Offset- oder Cursor-Parameter

## Mapping auf MCP-Cursor-Modell
- MCP-Pagination (Cursor-basiert) kann mit diesem Endpunkt nicht vollständig umgesetzt werden
- Es kann nur eine Seite mit bis zu `limit` Einträgen geliefert werden
- `nextCursor` kann nie gesetzt werden, da keine Folgeseiten unterstützt werden

## Beispiel-Request
```http
GET /cmdm/series?limit=4
```

## Beispiel-Response
```json
[
  {
    "seriesUuid": "...",
    "title": "...",
    "description": "...",
    "brandId": "...",
    "imdbUrl": "...",
    "url": "..."
  },
  ...
]
```

## Besonderheiten & Edgecases
- Kein Offset, keine Cursor, keine klassische Pagination
- Bei mehr als `limit` Serien ist keine vollständige Auflistung möglich
- Für vollständige MCP-Kompatibilität wäre ein API-Update nötig

== Hinweise
- page/limit Pagination laut OpenAPI vorhanden
- nextCursor codiert page+limit
- Keine Offset- oder Cursor-Parameter
