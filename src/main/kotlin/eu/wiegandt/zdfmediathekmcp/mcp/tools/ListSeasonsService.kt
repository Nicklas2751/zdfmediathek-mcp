package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.model.SeasonSummary
import eu.wiegandt.zdfmediathekmcp.model.SeriesSummary
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service

@Service
class ListSeasonsService(private val zdfMediathekClient: ZdfMediathekClient) {
    private val logger = LoggerFactory.getLogger(ListSeasonsService::class.java)

    /**
     * MCP Tool: List all seasons available in the ZDF Mediathek.
     *
     * @param limit Maximum number of results to return (optional, default: 4).
     * @return List of [SeasonSummary] objects.
     */
    @McpTool(
        name = "list_seasons",
        description = """List all seasons available in the ZDF Mediathek. Returns title, season number, and full series details. 
                Parameter: limit (optional, default: 4).
                """
    )
    fun listSeasons(limit: Int? = 4): List<SeasonSummary> {
        val actualLimit = limit ?: 4
        logger.info("MCP Tool 'list_seasons' called with limit={}", actualLimit)

        try {
            val result = zdfMediathekClient.listSeasons(actualLimit)
            logger.info("Successfully retrieved {} seasons", result.seasons.size)
            return result.seasons.map { season ->
                SeasonSummary(
                    seasonUuid = season.seasonUuid,
                    seasonNumber = season.seasonNumber,
                    title = season.seasonTitle,
                    brandId = season.brand?.brandUuid ?: season.series?.brand?.brandUuid,
                    series = season.series?.let { SeriesSummary(it) }
                )
            }
        } catch (e: Exception) {
            logger.error("Error executing list_seasons: {}", e.message, e)
            throw RuntimeException("Failed to list seasons: ${e.message}", e)
        }
    }
}
