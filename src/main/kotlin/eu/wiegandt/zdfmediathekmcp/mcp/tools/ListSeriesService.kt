package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.mcp.pagination.McpPaginationPayloadHandler
import eu.wiegandt.zdfmediathekmcp.model.McpPagedResult
import eu.wiegandt.zdfmediathekmcp.model.SeriesSummary
import eu.wiegandt.zdfmediathekmcp.model.ZdfSeriesResponse
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service

@Service
class ListSeriesService(private val zdfMediathekClient: ZdfMediathekClient) {
    private val logger = LoggerFactory.getLogger(ListSeriesService::class.java)

    /**
     * MCP Tool: List all series available in the ZDF Mediathek.
     * @param limit Maximum number of results to return (optional, default: 4).
     * @param cursor Optional MCP pagination cursor (Base64-encoded JSON {"page":X,"limit":Y})
     * @return McpPagedResult containing resources and optional nextCursor
     */
    @McpTool(
        name = "list_series",
        description = """List all series available in the ZDF Mediathek. Returns title, description, brand reference, and external links (ZDF, IMDb - if available). 
                Parameter: limit (optional, default: 4), cursor (optional, MCP paging cursor).
                """
    )
    fun listSeries(limit: Int? = 4, cursor: String? = null): McpPagedResult<SeriesSummary> {
        var actualLimit = limit ?: 4
        var page = 1

        if (!cursor.isNullOrBlank()) {
            try {
                val payload = McpPaginationPayloadHandler.decode(cursor)
                page = payload.page
                payload.limit?.let { actualLimit = it }
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid pagination cursor provided for list_series: {}", e.message)
                throw e
            }
        }

        logger.info("MCP Tool 'list_series' called with limit={} page={}", actualLimit, page)
        try {
            val response: ZdfSeriesResponse = zdfMediathekClient.listSeries(actualLimit, page)
            logger.info("Successfully retrieved {} series", response.series.size)

            val resources = response.series.map { SeriesSummary(it) }

            // Determine nextCursor: if we received as many items as requested, assume there may be a next page
            val nextCursor = if (response.series.size >= actualLimit) {
                McpPaginationPayloadHandler.encode(page + 1, actualLimit)
            } else {
                null
            }

            return McpPagedResult(resources = resources, nextCursor = nextCursor)
        } catch (e: Exception) {
            logger.error("Error executing list_series: {}", e.message, e)
            throw RuntimeException("Failed to list series: ${e.message}", e)
        }
    }
}
