# Pagination-Analyse: list_brands (Endpoint: /cmdm/brands)

## Verwendeter REST-Endpunkt
- **Pfad:** `/cmdm/brands`
- **Parameter:**
  - `limit` (optional, default: 10) – Maximale Anzahl der zurückgegebenen Brands
  - `page` (optional, default: 1) – Seite der Ergebnisse

## Response-Struktur
- Antwort ist eine Liste von Brand-Objekten mit Feldern wie `uuid`, `brandName`, `brandDescription`
- Keine expliziten Paging-Informationen wie `next`, `offset`, `total` in der Response dokumentiert

## Pagination-Mechanik
- Paging erfolgt über die Parameter `page` und `limit`
- `nextCursor` codiert die nächste Seite basierend auf `page` und `limit`
- Es gibt keine Offset- oder Cursor-Parameter
- Die API liefert die Ergebnisse der angeforderten Seite, maximal `limit` Einträge

## Mapping auf MCP-Cursor-Modell
- MCP-Pagination (Cursor-basiert) kann mit diesem Endpunkt nicht vollständig umgesetzt werden
- Es kann nur eine Seite mit bis zu `limit` Einträgen geliefert werden
- `nextCursor` kann nie gesetzt werden, da keine Folgeseiten unterstützt werden

## Beispiel-Request
```http
GET /cmdm/brands?limit=10&page=1
```

## Beispiel-Response
```json
[
  {
    "uuid": "...",
    "brandName": "Terra X",
    "brandDescription": "..."
  },
  ...
]
```

## Besonderheiten & Edgecases
- Kein Offset, keine Cursor, keine klassische Pagination
- Bei mehr als `limit` Brands ist keine vollständige Auflistung möglich
- Für vollständige MCP-Kompatibilität wäre ein API-Update nötig

== Hinweise
- page/limit Pagination laut OpenAPI vorhanden
- nextCursor codiert page+limit
