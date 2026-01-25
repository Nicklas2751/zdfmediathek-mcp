package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.mcp.pagination.McpPaginationPayloadHandler
import eu.wiegandt.zdfmediathekmcp.model.McpPagedResult
import eu.wiegandt.zdfmediathekmcp.model.SeasonSummary
import eu.wiegandt.zdfmediathekmcp.model.SeriesSummary
import eu.wiegandt.zdfmediathekmcp.model.ZdfSeasonResponse
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
     * @param cursor Optional MCP pagination cursor (Base64-encoded JSON {"page":X,"limit":Y})
     * @return McpPagedResult containing SeasonSummary resources and optional nextCursor
     */
    @McpTool(
        name = "list_seasons",
        description = """List all seasons available in the ZDF Mediathek. Returns title, season number, and full series details. 
                Parameter: limit (optional, default: 4), cursor (optional, MCP paging cursor).
                """
    )
    fun listSeasons(limit: Int? = 4, cursor: String? = null): McpPagedResult<SeasonSummary> {
        var actualLimit = limit ?: 4
        var page = 1

        if (!cursor.isNullOrBlank()) {
            try {
                val payload = McpPaginationPayloadHandler.decode(cursor)
                page = payload.page
                payload.limit?.let { actualLimit = it }
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid pagination cursor provided for list_seasons: {}", e.message)
                throw e
            }
        }

        logger.info("MCP Tool 'list_seasons' called with limit={} page={}", actualLimit, page)
        try {
            val response: ZdfSeasonResponse = zdfMediathekClient.listSeasons(actualLimit, page)
            logger.info("Successfully retrieved {} seasons", response.seasons.size)

            val resources = response.seasons.map { season ->
                SeasonSummary(
                    seasonUuid = season.seasonUuid,
                    seasonNumber = season.seasonNumber,
                    title = season.seasonTitle,
                    brandId = season.brand?.brandUuid ?: season.series?.brand?.brandUuid,
                    series = season.series?.let { SeriesSummary(it) }
                )
            }

            val nextCursor = if (response.seasons.size >= actualLimit) {
                McpPaginationPayloadHandler.encode(page + 1, actualLimit)
            } else {
                null
            }

            return McpPagedResult(resources = resources, nextCursor = nextCursor)
        } catch (e: Exception) {
            logger.error("Error executing list_seasons: {}", e.message, e)
            throw RuntimeException("Failed to list seasons: ${e.message}", e)
        }
    }
}
