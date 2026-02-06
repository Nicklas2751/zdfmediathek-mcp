package eu.wiegandt.zdfmediathekmcp.model

data class McpPagedResult<T>(
    val resources: List<T>,
    val nextCursor: String? = null
)
