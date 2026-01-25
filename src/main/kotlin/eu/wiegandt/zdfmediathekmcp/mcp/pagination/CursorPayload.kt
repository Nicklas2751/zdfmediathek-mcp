package eu.wiegandt.zdfmediathekmcp.mcp.pagination

/**
 * DTO for MCP pagination cursor payload. Represents page and optional limit.
 */
data class CursorPayload(
    val page: Int = 1,
    val limit: Int? = null
)
