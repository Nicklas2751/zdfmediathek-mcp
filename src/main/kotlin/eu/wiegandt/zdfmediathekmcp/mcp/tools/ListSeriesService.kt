package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.model.SeriesSummary
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service

@Service
class ListSeriesService(private val zdfMediathekClient: ZdfMediathekClient) {
    private val logger = LoggerFactory.getLogger(ListSeriesService::class.java)

    /**
     * MCP Tool: List all series available in the ZDF Mediathek.
     *
     * @param limit Maximum number of results to return (optional, default: 4).
     * @return List of [SeriesSummary] objects containing series details and links.
     */
    @McpTool(
        name = "list_series",
        description = """List all series available in the ZDF Mediathek. Returns title, description, brand reference, and external links (ZDF, IMDb - if available). 
                Parameter: limit (optional, default: 4).
                """
    )
    fun listSeries(limit: Int? = 4): List<SeriesSummary> {
        // If the MCP framework passes null (no parameter supplied), fall back to default
        val actualLimit = limit ?: 4
        logger.info("MCP Tool 'list_series' called with limit={}", actualLimit)

        try {
            val response = zdfMediathekClient.listSeries(actualLimit)
            logger.info("Successfully retrieved {} series", response.series.size)

            return response.series.map { SeriesSummary(it) }
        } catch (e: Exception) {
            logger.error("Error executing list_series: {}", e.message, e)
            throw RuntimeException("Failed to list series: ${e.message}", e)
        }
    }
}

