# Pagination-Analyse: get_series_episodes (GraphQL: searchDocuments/episodes)

## Verwendeter GraphQL-Endpunkt
- **Query:** `searchDocuments` → `ISeriesSmartCollection` → `episodes` (ggf. verschachtelt über `seasons`)
- **Parameter:**
  - `first` (Int): Maximale Anzahl der Episoden pro Query (Paging)
  - `after` (String, optional): Cursor für die nächste Seite (klassisches GraphQL-Cursor-Paging)
  - `sortBy` (optional): Sortierung

## Response-Struktur
- Antwort enthält ein Feld `nodes` (Liste der Episoden)
- Feld `pageInfo` mit `hasNextPage` und `endCursor` (GraphQL-Standard für Cursor-Paging)

## Pagination-Mechanik
- Cursor-basiertes Paging nach GraphQL-Standard
- Client gibt `first` (Limit) und optional `after` (Cursor) an
- Response enthält `endCursor` für die nächste Seite und `hasNextPage`-Flag

## Mapping auf MCP-Cursor-Modell
- MCP-Cursor entspricht dem GraphQL-`endCursor`
- `nextCursor` wird gesetzt, wenn `hasNextPage=true`
- Bei letzter Seite bleibt `nextCursor` leer/fehlt

## Beispiel-Query
```graphql
query {
  searchDocuments(query: "heute-show", first: 1) {
    results {
      item {
        ... on ISeriesSmartCollection {
          episodes(first: 10, after: "YXJyYXljb25uZWN0aW9uOjEw") {
            nodes { ... }
            pageInfo {
              hasNextPage
              endCursor
            }
          }
        }
      }
    }
  }
}
```

## Beispiel-Response
```json
{
  "data": {
    "searchDocuments": {
      "results": [
        {
          "item": {
            "episodes": {
              "nodes": [ ... ],
              "pageInfo": {
                "hasNextPage": true,
                "endCursor": "YXJyYXljb25uZWN0aW9uOjEw"
              }
            }
          }
        }
      ]
    }
  }
}
```

## Besonderheiten & Edgecases
- Standardisiertes Cursor-Paging, sehr gut MCP-kompatibel
- Bei sehr großen Serien ggf. API-Limits beachten

